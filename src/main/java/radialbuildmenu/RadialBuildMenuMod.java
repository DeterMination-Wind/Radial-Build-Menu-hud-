package radialbuildmenu;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.input.KeyBind;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.Dialog;
import arc.scene.ui.Image;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.Scl;
import arc.util.Scaling;
import arc.util.Strings;
import arc.util.Time;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.game.EventType.WorldLoadEvent;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.SettingsMenuDialog;
import mindustry.type.Planet;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;

import java.util.Locale;

import static mindustry.Vars.player;
import static mindustry.Vars.state;
import static mindustry.Vars.ui;
import static mindustry.Vars.content;
import static mindustry.Vars.mobile;
import static mindustry.Vars.control;

public class RadialBuildMenuMod extends mindustry.mod.Mod{
    private static final String overlayName = "rbm-overlay";

    private static final String keyEnabled = "rbm-enabled";
    private static final String keyHudScale = "rbm-hudscale";
    private static final String keyHudAlpha = "rbm-hudalpha";
    private static final String keyTimeMinutes = "rbm-time-minutes";
    private static final String keyPlanetName = "rbm-planet";

    private static final String keySlotPrefix = "rbm-slot-";
    private static final String keyTimeSlotPrefix = "rbm-time-slot-";
    private static final String keyPlanetSlotPrefix = "rbm-planet-slot-";

    private static final String[] defaultSlotNames = {
        "conveyor",
        "router",
        "junction",
        "sorter",
        "overflow-gate",
        "underflow-gate",
        "bridge-conveyor",
        "power-node"
    };

    public static final KeyBind radialMenu = KeyBind.add("rbm_radial_menu", KeyCode.unset, "blocks");

    public RadialBuildMenuMod(){
        Events.on(ClientLoadEvent.class, e -> {
            ensureDefaults();
            registerSettings();
            Time.runTask(10f, this::ensureOverlayAttached);
        });

        Events.on(WorldLoadEvent.class, e -> Time.runTask(10f, this::ensureOverlayAttached));
    }

    private void ensureDefaults(){
        Core.settings.defaults(keyEnabled, true);
        Core.settings.defaults(keyHudScale, 100);
        Core.settings.defaults(keyHudAlpha, 100);
        Core.settings.defaults(keyTimeMinutes, 0);
        Core.settings.defaults(keyPlanetName, "");
        for(int i = 0; i < defaultSlotNames.length; i++){
            Core.settings.defaults(keySlotPrefix + i, defaultSlotNames[i]);
            Core.settings.defaults(keyTimeSlotPrefix + i, defaultSlotNames[i]);
            Core.settings.defaults(keyPlanetSlotPrefix + i, defaultSlotNames[i]);
        }
    }

    private void registerSettings(){
        if(ui == null || ui.settings == null) return;

        ui.settings.addCategory("@rbm.category", table -> {
            table.checkPref(keyEnabled, true);
            table.sliderPref(keyHudScale, 100, 50, 200, 5, v -> v + "%");
            table.sliderPref(keyHudAlpha, 100, 0, 100, 5, v -> v + "%");
            table.pref(new HotkeySetting());
            table.pref(new HeaderSetting(Core.bundle.get("rbm.section.base")));
            for(int i = 0; i < 8; i++) table.pref(new SlotSetting(i, keySlotPrefix, "rbm.setting.slot"));

            table.pref(new HeaderSetting(Core.bundle.get("rbm.section.time")));
            table.sliderPref(keyTimeMinutes, 0, 0, 240, 5, v -> v == 0 ? Core.bundle.get("rbm.setting.off") : Core.bundle.format("rbm.setting.minutes", v));
            for(int i = 0; i < 8; i++) table.pref(new SlotSetting(i, keyTimeSlotPrefix, "rbm.setting.timeslot"));

            table.pref(new HeaderSetting(Core.bundle.get("rbm.section.planet")));
            table.pref(new PlanetSetting());
            for(int i = 0; i < 8; i++) table.pref(new SlotSetting(i, keyPlanetSlotPrefix, "rbm.setting.planetslot"));
        });
    }

    private static class HeaderSetting extends SettingsMenuDialog.SettingsTable.Setting{
        public HeaderSetting(String title){
            super("rbm-header");
            this.title = title;
        }

        @Override
        public void add(SettingsMenuDialog.SettingsTable table){
            table.row();
            table.add(title).color(Color.gray).padTop(12f).padBottom(4f).left().growX();
            table.row();
        }
    }

    private class HotkeySetting extends SettingsMenuDialog.SettingsTable.Setting{
        public HotkeySetting(){
            super("rbm-hotkey");
            title = Core.bundle.get("rbm.setting.hotkey");
        }

        @Override
        public void add(SettingsMenuDialog.SettingsTable table){
            float prefWidth = Math.min(Core.graphics.getWidth() / 1.2f, 560f);
            table.table(Tex.button, t -> {
                t.left().margin(10f);
                t.add(title).left().growX();
                t.label(() -> radialMenu.value.key.toString()).color(Pal.accent).padLeft(10f);
                t.button("@rbm.setting.opencontrols", Styles.flatt, () -> ui.controls.show())
                    .width(190f)
                    .height(40f)
                    .padLeft(10f);
            }).width(prefWidth).padTop(6f);
            table.row();
        }
    }

    private class SlotSetting extends SettingsMenuDialog.SettingsTable.Setting{
        private final int slot;
        private final String prefix;
        private final String titleKey;

        public SlotSetting(int slot, String prefix, String titleKey){
            super(prefix + slot);
            this.slot = slot;
            this.prefix = prefix;
            this.titleKey = titleKey;
            title = Core.bundle.format(titleKey, slot + 1);
        }

        @Override
        public void add(SettingsMenuDialog.SettingsTable table){
            float prefWidth = Math.min(Core.graphics.getWidth() / 1.2f, 560f);
            table.table(Tex.button, t -> {
                t.left().margin(10f);

                t.add(title).width(120f).left();
                t.table(info -> {
                    info.left();

                    Image icon = info.image(Tex.clear).size(32f).padRight(8f).get();
                    icon.setScaling(Scaling.fit);

                    info.labelWrap(() -> {
                        Block block = slotBlock(prefix, slot);
                        return block == null ? Core.bundle.get("rbm.setting.none") : block.localizedName;
                    }).left().growX().fillX().minWidth(0f);

                    final Block[] lastBlock = {null};
                    info.update(() -> {
                        Block block = slotBlock(prefix, slot);
                        if(block == lastBlock[0]) return;
                        lastBlock[0] = block;
                        icon.setDrawable(block == null ? Tex.clear : new TextureRegionDrawable(block.uiIcon));
                    });
                }).left().growX().fillX().minWidth(0f);

                t.button("@rbm.setting.set", Styles.flatt, () -> showBlockSelectDialog(block -> {
                    Core.settings.put(name, block == null ? "" : block.name);
                })).width(140f).height(40f).padLeft(8f);
            }).width(prefWidth).padTop(6f);
            table.row();
        }
    }

    private class PlanetSetting extends SettingsMenuDialog.SettingsTable.Setting{
        public PlanetSetting(){
            super(keyPlanetName);
            title = Core.bundle.get("rbm.setting.planet");
        }

        @Override
        public void add(SettingsMenuDialog.SettingsTable table){
            float prefWidth = Math.min(Core.graphics.getWidth() / 1.2f, 560f);
            table.table(Tex.button, t -> {
                t.left().margin(10f);

                t.add(title).width(120f).left();
                t.table(info -> {
                    info.left();

                    Image icon = info.image(Tex.clear).size(32f).padRight(8f).get();
                    icon.setScaling(Scaling.fit);

                    info.labelWrap(() -> {
                        Planet planet = selectedPlanet();
                        return planet == null ? Core.bundle.get("rbm.setting.planet.none") : planet.localizedName;
                    }).left().growX().fillX().minWidth(0f);

                    final Planet[] lastPlanet = {null};
                    info.update(() -> {
                        Planet planet = selectedPlanet();
                        if(planet == lastPlanet[0]) return;
                        lastPlanet[0] = planet;
                        icon.setDrawable(planet == null ? Tex.clear : new TextureRegionDrawable(planet.uiIcon));
                    });
                }).left().growX().fillX().minWidth(0f);

                t.button("@rbm.setting.set", Styles.flatt, () -> showPlanetSelectDialog(planet -> {
                    Core.settings.put(keyPlanetName, planet == null ? "" : planet.name);
                })).width(140f).height(40f).padLeft(8f);

                t.button("@rbm.setting.clear", Styles.flatt, () -> Core.settings.put(keyPlanetName, ""))
                    .width(100f)
                    .height(40f)
                    .padLeft(6f);
            }).width(prefWidth).padTop(6f);
            table.row();
        }
    }

    private void showBlockSelectDialog(arc.func.Cons<Block> consumer){
        BaseDialog dialog = new BaseDialog("@rbm.selectblock.title");
        dialog.addCloseButton();

        String[] searchText = {""};

        Table list = new Table();
        list.top().left();
        list.defaults().growX().height(54f).pad(2f);

        ScrollPane pane = new ScrollPane(list);
        pane.setFadeScrollBars(false);

        Runnable rebuild = () -> {
            list.clearChildren();

            list.button("@rbm.selectblock.none", Styles.flatt, () -> {
                dialog.hide();
                consumer.get(null);
            }).row();

            String query = searchText[0] == null ? "" : searchText[0].trim().toLowerCase(Locale.ROOT);

            for(Block block : content.blocks()){
                if(block == null) continue;
                if(block.category == null) continue;
                if(!block.isVisible()) continue;
                if(block.buildVisibility == BuildVisibility.hidden) continue;
                if(!block.placeablePlayer) continue;

                if(!query.isEmpty()){
                    String name = block.name.toLowerCase(Locale.ROOT);
                    String localized = Strings.stripColors(block.localizedName).toLowerCase(Locale.ROOT);
                    if(!name.contains(query) && !localized.contains(query)){
                        continue;
                    }
                }

                list.button(b -> {
                    b.left();
                    b.image(block.uiIcon).size(32f).padRight(8f);
                    b.add(block.localizedName).left().growX().wrap();
                    b.add(block.name).color(Color.gray).padLeft(8f).right();
                }, Styles.flatt, () -> {
                    dialog.hide();
                    consumer.get(block);
                }).row();
            }
        };

        dialog.cont.table(t -> {
            t.left();
            t.image(mindustry.gen.Icon.zoom).padRight(8f);
            t.field("", text -> {
                searchText[0] = text;
                rebuild.run();
            }).growX().get().setMessageText("@players.search");
        }).growX().padBottom(6f);

        dialog.cont.row();
        dialog.cont.add(pane).grow().minHeight(320f);

        dialog.shown(rebuild);
        dialog.show();
    }

    private void showPlanetSelectDialog(arc.func.Cons<Planet> consumer){
        BaseDialog dialog = new BaseDialog("@rbm.selectplanet.title");
        dialog.addCloseButton();

        String[] searchText = {""};

        Table list = new Table();
        list.top().left();
        list.defaults().growX().height(54f).pad(2f);

        ScrollPane pane = new ScrollPane(list);
        pane.setFadeScrollBars(false);

        Runnable rebuild = () -> {
            list.clearChildren();

            list.button("@rbm.selectplanet.none", Styles.flatt, () -> {
                dialog.hide();
                consumer.get(null);
            }).row();

            String query = searchText[0] == null ? "" : searchText[0].trim().toLowerCase(Locale.ROOT);

            for(Planet planet : content.planets()){
                if(planet == null) continue;
                if(!planet.visible) continue;
                if(!planet.accessible) continue;

                if(!query.isEmpty()){
                    String name = planet.name.toLowerCase(Locale.ROOT);
                    String localized = Strings.stripColors(planet.localizedName).toLowerCase(Locale.ROOT);
                    if(!name.contains(query) && !localized.contains(query)){
                        continue;
                    }
                }

                list.button(b -> {
                    b.left();
                    b.image(planet.uiIcon).size(32f).padRight(8f);
                    b.add(planet.localizedName).left().growX().wrap();
                    b.add(planet.name).color(Color.gray).padLeft(8f).right();
                }, Styles.flatt, () -> {
                    dialog.hide();
                    consumer.get(planet);
                }).row();
            }
        };

        dialog.cont.table(t -> {
            t.left();
            t.image(mindustry.gen.Icon.zoom).padRight(8f);
            t.field("", text -> {
                searchText[0] = text;
                rebuild.run();
            }).growX().get().setMessageText("@players.search");
        }).growX().padBottom(6f);

        dialog.cont.row();
        dialog.cont.add(pane).grow().minHeight(320f);

        dialog.shown(rebuild);
        dialog.show();
    }

    private Planet selectedPlanet(){
        String name = Core.settings.getString(keyPlanetName, "");
        if(name == null || name.trim().isEmpty()) return null;
        return content.planet(name.trim());
    }

    private Block slotBlock(String prefix, int slot){
        if(slot < 0 || slot >= 8) return null;
        String name = Core.settings.getString(prefix + slot, defaultSlotNames[slot]);
        if(name == null || name.trim().isEmpty()) return null;
        return content.block(name);
    }

    private String resolveSlotPrefix(){
        if(!state.isGame() || state.rules.editor) return keySlotPrefix;

        Planet current = state.getPlanet();
        String wanted = Core.settings.getString(keyPlanetName, "");
        if(current != null && wanted != null){
            wanted = wanted.trim();
            if(!wanted.isEmpty() && wanted.equals(current.name)){
                return keyPlanetSlotPrefix;
            }
        }

        int minutes = Core.settings.getInt(keyTimeMinutes, 0);
        if(minutes > 0){
            double currentMinutes = state.tick / 60.0 / 60.0;
            if(currentMinutes >= minutes){
                return keyTimeSlotPrefix;
            }
        }

        return keySlotPrefix;
    }

    private void ensureOverlayAttached(){
        if(mobile) return;
        if(ui == null || ui.hudGroup == null) return;

        if(ui.hudGroup.find(overlayName) != null) return;

        RadialHud hud = new RadialHud(this);
        hud.name = overlayName;
        hud.touchable = Touchable.disabled;
        ui.hudGroup.addChild(hud);
    }

    private static class RadialHud extends Element{
        private final RadialBuildMenuMod mod;

        private boolean active;
        private float centerX, centerY;
        private int hovered = -1;
        private final Block[] slots = new Block[8];
        private String slotsPrefix = keySlotPrefix;

        public RadialHud(RadialBuildMenuMod mod){
            this.mod = mod;
        }

        @Override
        public void act(float delta){
            super.act(delta);

            if(parent != null){
                setBounds(0f, 0f, parent.getWidth(), parent.getHeight());
            }else{
                setSize(Core.graphics.getWidth(), Core.graphics.getHeight());
            }

            if(active){
                if(!canStayActive()){
                    active = false;
                    hovered = -1;
                    return;
                }

                hovered = findHovered();

                if(Core.input.keyRelease(radialMenu)){
                    commitSelection();
                    active = false;
                    hovered = -1;
                }else if(!Core.input.keyDown(radialMenu)){
                    // failsafe: if focus changed and no release is received
                    active = false;
                    hovered = -1;
                }
            }else{
                if(canActivate() && Core.input.keyTap(radialMenu)){
                    begin();
                }
            }
        }

        @Override
        public void draw(){
            if(!active) return;

            float alpha = parentAlpha * Mathf.clamp(Core.settings.getInt(keyHudAlpha, 100) / 100f);
            float scale = Mathf.clamp(Core.settings.getInt(keyHudScale, 100) / 100f, 0.1f, 5f);

            float iconSize = Scl.scl(46f) * scale;
            float radius = Scl.scl(80f) * scale;
            float slotBack = iconSize / 2f + Scl.scl(10f) * scale;

            Draw.z(1000f);

            // soft background disc around the cursor
            Draw.color(0f, 0f, 0f, 0.18f * alpha);
            Fill.circle(centerX, centerY, radius + iconSize * 0.75f);

            // ring
            Draw.color(Pal.accent, 0.65f * alpha);
            Lines.stroke(Scl.scl(2f) * scale);
            Lines.circle(centerX, centerY, radius);

            for(int i = 0; i < 8; i++){
                float angle = 90f - i * 45f;
                float px = centerX + Mathf.cosDeg(angle) * radius;
                float py = centerY + Mathf.sinDeg(angle) * radius;

                boolean isHovered = i == hovered;

                // slot background
                Draw.color(0f, 0f, 0f, (isHovered ? 0.70f : 0.45f) * alpha);
                Fill.circle(px, py, slotBack);

                // slot border
                Draw.color(isHovered ? Pal.accent : Color.gray, (isHovered ? 1f : 0.35f) * alpha);
                Lines.stroke(Scl.scl(isHovered ? 2.4f : 1.6f) * scale);
                Lines.circle(px, py, slotBack);

                Block block = slots[i];
                if(block != null){
                    Draw.color(Color.white, alpha);
                    Draw.rect(block.uiIcon, px, py, iconSize, iconSize);
                }else{
                    // empty slot indicator (X)
                    Draw.color(Color.lightGray, 0.9f * alpha);
                    Lines.stroke(Scl.scl(2f) * scale);
                    float s = iconSize / 3f;
                    Lines.line(px - s, py - s, px + s, py + s);
                    Lines.line(px - s, py + s, px + s, py - s);
                }
            }

            Draw.reset();
        }

        private boolean canActivate(){
            if(!Core.settings.getBool(keyEnabled, true)) return false;
            if(ui == null || ui.hudfrag == null || !ui.hudfrag.shown) return false;
            if(Core.scene.hasDialog()) return false;
            if(Core.scene.hasKeyboard()) return false;
            if(ui.chatfrag != null && ui.chatfrag.shown()) return false;
            if(ui.consolefrag != null && ui.consolefrag.shown()) return false;
            if(player == null || player.dead()) return false;
            if(!state.rules.editor && !player.isBuilder()) return false;
            return true;
        }

        private boolean canStayActive(){
            // allow staying active even if the keybind changes mid-hold
            if(!Core.settings.getBool(keyEnabled, true)) return false;
            if(ui == null || ui.hudfrag == null || !ui.hudfrag.shown) return false;
            if(Core.scene.hasDialog()) return false;
            if(Core.scene.hasKeyboard()) return false;
            if(ui.chatfrag != null && ui.chatfrag.shown()) return false;
            if(ui.consolefrag != null && ui.consolefrag.shown()) return false;
            if(player == null || player.dead()) return false;
            return state.rules.editor || player.isBuilder();
        }

        private void begin(){
            active = true;
            centerX = Core.input.mouseX();
            centerY = Core.input.mouseY();

            slotsPrefix = mod.resolveSlotPrefix();
            for(int i = 0; i < slots.length; i++){
                slots[i] = mod.slotBlock(slotsPrefix, i);
            }

            hovered = findHovered();
        }

        private int findHovered(){
            float scale = Mathf.clamp(Core.settings.getInt(keyHudScale, 100) / 100f, 0.1f, 5f);
            float iconSize = Scl.scl(46f) * scale;
            float radius = Scl.scl(80f) * scale;
            float hit = iconSize / 2f + Scl.scl(12f) * scale;
            float hit2 = hit * hit;

            float mx = Core.input.mouseX();
            float my = Core.input.mouseY();

            int best = -1;
            float bestDst2 = hit2;

            for(int i = 0; i < 8; i++){
                float angle = 90f - i * 45f;
                float px = centerX + Mathf.cosDeg(angle) * radius;
                float py = centerY + Mathf.sinDeg(angle) * radius;
                float dx = mx - px;
                float dy = my - py;
                float dst2 = dx * dx + dy * dy;
                if(dst2 <= bestDst2){
                    bestDst2 = dst2;
                    best = i;
                }
            }

            return best;
        }

        private void commitSelection(){
            if(hovered < 0 || hovered >= slots.length) return;
            Block block = slots[hovered];
            if(block == null) return;

            if(!state.rules.editor && !unlocked(block)){
                ui.showInfoFade("@rbm.block.unavailable");
                return;
            }

            control.input.block = block;
            if(ui != null && ui.hudfrag != null && block.isVisible() && block.category != null){
                ui.hudfrag.blockfrag.currentCategory = block.category;
            }
        }

        private static boolean unlocked(Block block){
            return block.unlockedNowHost()
                && block.placeablePlayer
                && block.environmentBuildable()
                && block.supportsEnv(state.rules.env);
        }
    }
}
