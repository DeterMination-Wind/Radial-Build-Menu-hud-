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
import arc.scene.ui.TextArea;
import arc.scene.ui.TextField;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.Scl;
import arc.util.Scaling;
import arc.util.Strings;
import arc.util.Time;
import arc.util.serialization.Jval;
import arc.util.serialization.Jval.Jformat;
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

    private static final int slotsPerRing = 8;
    private static final int maxSlots = 16;

    private static final String keyEnabled = "rbm-enabled";
    private static final String keyHudScale = "rbm-hudscale";
    private static final String keyHudAlpha = "rbm-hudalpha";
    private static final String keyInnerRadius = "rbm-inner-radius";
    private static final String keyOuterRadius = "rbm-outer-radius";
    private static final String keyHudColor = "rbm-hudcolor";
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
        Core.settings.defaults(keyInnerRadius, 80);
        Core.settings.defaults(keyOuterRadius, 140);
        Core.settings.defaults(keyHudColor, defaultHudColorHex());
        Core.settings.defaults(keyTimeMinutes, 0);
        Core.settings.defaults(keyPlanetName, "");
        for(int i = 0; i < maxSlots; i++){
            String def = defaultSlotName(i);
            Core.settings.defaults(keySlotPrefix + i, def);
            Core.settings.defaults(keyTimeSlotPrefix + i, def);
            Core.settings.defaults(keyPlanetSlotPrefix + i, def);
        }
    }

    private void registerSettings(){
        if(ui == null || ui.settings == null) return;

        ui.settings.addCategory("@rbm.category", table -> {
            table.checkPref(keyEnabled, true);
            table.pref(new HotkeySetting());

            table.pref(new HeaderSetting(Core.bundle.get("rbm.section.appearance")));
            table.sliderPref(keyHudScale, 100, 50, 200, 5, v -> v + "%");
            table.sliderPref(keyHudAlpha, 100, 0, 100, 5, v -> v + "%");
            table.sliderPref(keyInnerRadius, 80, 40, 200, 5, v -> v + "px");
            table.sliderPref(keyOuterRadius, 140, 60, 360, 5, v -> v + "px");
            table.pref(new HudColorSetting());

            table.pref(new HeaderSetting(Core.bundle.get("rbm.section.base")));
            for(int i = 0; i < maxSlots; i++) table.pref(new SlotSetting(i, keySlotPrefix, "rbm.setting.slot"));

            table.pref(new HeaderSetting(Core.bundle.get("rbm.section.time")));
            table.pref(new TimeMinutesSetting());
            for(int i = 0; i < maxSlots; i++) table.pref(new SlotSetting(i, keyTimeSlotPrefix, "rbm.setting.timeslot"));

            table.pref(new HeaderSetting(Core.bundle.get("rbm.section.planet")));
            table.pref(new PlanetSetting());
            for(int i = 0; i < maxSlots; i++) table.pref(new SlotSetting(i, keyPlanetSlotPrefix, "rbm.setting.planetslot"));

            table.pref(new HeaderSetting(Core.bundle.get("rbm.section.io")));
            table.pref(new IoSetting());
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

    private class HudColorSetting extends SettingsMenuDialog.SettingsTable.Setting{
        public HudColorSetting(){
            super(keyHudColor);
            title = Core.bundle.get("setting.rbm-hudcolor.name");
        }

        @Override
        public void add(SettingsMenuDialog.SettingsTable table){
            float prefWidth = Math.min(Core.graphics.getWidth() / 1.2f, 560f);
            table.table(Tex.button, t -> {
                t.left().margin(10f);
                t.add(title).width(120f).left();

                Image preview = new Image(Tex.whiteui);
                preview.setScaling(Scaling.stretch);
                preview.setColor(readHudColor());
                t.add(preview).size(22f).padRight(8f);

                TextField field = new TextField();
                field.setMessageText(defaultHudColorHex());
                field.setText(Core.settings.getString(keyHudColor, defaultHudColorHex()));
                field.setFilter((text, c) -> isHexChar(c));

                field.changed(() -> {
                    String hex = normalizeHex(field.getText());
                    Core.settings.put(keyHudColor, hex);
                    preview.setColor(readHudColor());
                });

                field.update(() -> {
                    if(Core.scene.getKeyboardFocus() == field) return;
                    String value = Core.settings.getString(keyHudColor, defaultHudColorHex());
                    if(value == null) value = defaultHudColorHex();
                    if(!value.equals(field.getText())){
                        field.setText(value);
                    }
                    preview.setColor(readHudColor());
                });

                t.add(field).width(160f);
            }).width(prefWidth).padTop(6f);
            table.row();
        }

        private boolean isHexChar(char c){
            return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
        }
    }

    private class TimeMinutesSetting extends SettingsMenuDialog.SettingsTable.Setting{
        public TimeMinutesSetting(){
            super(keyTimeMinutes);
        }

        @Override
        public void add(SettingsMenuDialog.SettingsTable table){
            TextField field = new TextField();
            field.setMessageText("0");
            field.setFilter((text, c) -> Character.isDigit(c));

            field.changed(() -> {
                int minutes = Strings.parseInt(field.getText(), 0);
                if(minutes < 0) minutes = 0;
                Core.settings.put(name, minutes);
            });

            field.update(() -> {
                if(Core.scene.getKeyboardFocus() == field) return;
                String value = Integer.toString(Core.settings.getInt(name, 0));
                if(!value.equals(field.getText())){
                    field.setText(value);
                }
            });

            Table prefTable = table.table().left().padTop(3f).get();
            prefTable.add(field).width(140f);
            prefTable.label(() -> title);
            addDesc(prefTable);
            table.row();
        }
    }

    private class IoSetting extends SettingsMenuDialog.SettingsTable.Setting{
        public IoSetting(){
            super("rbm-io");
            title = Core.bundle.get("rbm.io.title");
        }

        @Override
        public void add(SettingsMenuDialog.SettingsTable table){
            float prefWidth = Math.min(Core.graphics.getWidth() / 1.2f, 560f);
            table.table(Tex.button, t -> {
                t.left().margin(10f);

                t.add(title).width(120f).left();
                t.button("@rbm.io.export", Styles.flatt, RadialBuildMenuMod.this::showExportDialog)
                    .width(160f).height(40f).padLeft(8f);
                t.button("@rbm.io.import", Styles.flatt, RadialBuildMenuMod.this::showImportDialog)
                    .width(160f).height(40f).padLeft(8f);
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

    private static String defaultSlotName(int slot){
        if(slot >= 0 && slot < defaultSlotNames.length) return defaultSlotNames[slot];
        return "";
    }

    private String slotName(String prefix, int slot){
        if(slot < 0 || slot >= maxSlots) return "";
        String value = Core.settings.getString(prefix + slot, defaultSlotName(slot));
        if(value == null) return "";
        return value.trim();
    }

    private Block slotBlock(String prefix, int slot){
        String name = slotName(prefix, slot);
        if(name.isEmpty()) return null;
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

    private static String defaultHudColorHex(){
        int r = Math.min(255, Math.max(0, (int)(Pal.accent.r * 255f)));
        int g = Math.min(255, Math.max(0, (int)(Pal.accent.g * 255f)));
        int b = Math.min(255, Math.max(0, (int)(Pal.accent.b * 255f)));
        return String.format(Locale.ROOT, "%02x%02x%02x", r, g, b);
    }

    private static String normalizeHex(String text){
        if(text == null) return defaultHudColorHex();
        String hex = text.trim();
        if(hex.startsWith("#")) hex = hex.substring(1);
        hex = hex.toLowerCase(Locale.ROOT);
        if(hex.length() > 6) hex = hex.substring(0, 6);
        while(hex.length() < 6) hex += "0";
        for(int i = 0; i < hex.length(); i++){
            char c = hex.charAt(i);
            boolean ok = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f');
            if(!ok) return defaultHudColorHex();
        }
        return hex;
    }

    private Color readHudColor(){
        String hex = normalizeHex(Core.settings.getString(keyHudColor, defaultHudColorHex()));
        try{
            return Color.valueOf(hex);
        }catch(Throwable t){
            return Pal.accent;
        }
    }

    private void showExportDialog(){
        String json = exportConfig();

        BaseDialog dialog = new BaseDialog("@rbm.io.export");
        dialog.addCloseButton();

        TextArea area = new TextArea(json);
        area.setDisabled(true);
        area.setPrefRows(12);

        ScrollPane pane = new ScrollPane(area);
        pane.setFadeScrollBars(false);

        dialog.cont.add(pane).grow().minHeight(220f);
        dialog.cont.row();
        dialog.cont.button("@rbm.io.copy", Styles.flatt, () -> {
            Core.app.setClipboardText(json);
            ui.showInfoFade("@rbm.io.copied");
        }).height(44f).growX().padTop(8f);

        dialog.show();
    }

    private void showImportDialog(){
        BaseDialog dialog = new BaseDialog("@rbm.io.import");
        dialog.addCloseButton();

        TextArea area = new TextArea("");
        area.setMessageText(Core.bundle.get("rbm.io.pastehere"));
        area.setPrefRows(12);

        ScrollPane pane = new ScrollPane(area);
        pane.setFadeScrollBars(false);

        dialog.cont.add(pane).grow().minHeight(220f);
        dialog.cont.row();
        dialog.cont.button("@rbm.io.import.apply", Styles.flatt, () -> {
            if(importConfig(area.getText())){
                ui.showInfoFade("@rbm.io.import.success");
                dialog.hide();
            }else{
                ui.showInfoFade("@rbm.io.import.invalid");
            }
        }).height(44f).growX().padTop(8f);

        dialog.show();
    }

    private String exportConfig(){
        Jval root = Jval.newObject();
        root.put("schema", 1);

        root.put("hudScale", Core.settings.getInt(keyHudScale, 100));
        root.put("hudAlpha", Core.settings.getInt(keyHudAlpha, 100));
        root.put("innerRadius", Core.settings.getInt(keyInnerRadius, 80));
        root.put("outerRadius", Core.settings.getInt(keyOuterRadius, 140));
        root.put("hudColor", normalizeHex(Core.settings.getString(keyHudColor, defaultHudColorHex())));

        root.put("timeMinutes", Core.settings.getInt(keyTimeMinutes, 0));
        root.put("planet", Core.settings.getString(keyPlanetName, ""));

        root.put("slots", exportSlots(keySlotPrefix));
        root.put("timeSlots", exportSlots(keyTimeSlotPrefix));
        root.put("planetSlots", exportSlots(keyPlanetSlotPrefix));

        return root.toString(Jformat.plain);
    }

    private Jval exportSlots(String prefix){
        Jval arr = Jval.newArray();
        for(int i = 0; i < maxSlots; i++){
            arr.add(slotName(prefix, i));
        }
        return arr;
    }

    private boolean importConfig(String text){
        if(text == null) return false;
        try{
            Jval root = Jval.read(text);
            if(root == null || !root.isObject()) return false;

            if(root.has("hudScale")) Core.settings.put(keyHudScale, root.getInt("hudScale", 100));
            if(root.has("hudAlpha")) Core.settings.put(keyHudAlpha, root.getInt("hudAlpha", 100));
            if(root.has("innerRadius")) Core.settings.put(keyInnerRadius, root.getInt("innerRadius", 80));
            if(root.has("outerRadius")) Core.settings.put(keyOuterRadius, root.getInt("outerRadius", 140));
            if(root.has("hudColor")) Core.settings.put(keyHudColor, normalizeHex(root.getString("hudColor", defaultHudColorHex())));

            if(root.has("timeMinutes")) Core.settings.put(keyTimeMinutes, Math.max(0, root.getInt("timeMinutes", 0)));
            if(root.has("planet")){
                String planet = root.getString("planet", "");
                Core.settings.put(keyPlanetName, planet == null ? "" : planet);
            }

            if(root.has("slots")) importSlots(root.get("slots"), keySlotPrefix);
            if(root.has("timeSlots")) importSlots(root.get("timeSlots"), keyTimeSlotPrefix);
            if(root.has("planetSlots")) importSlots(root.get("planetSlots"), keyPlanetSlotPrefix);

            return true;
        }catch(Throwable t){
            return false;
        }
    }

    private void importSlots(Jval arr, String prefix){
        if(arr == null || !arr.isArray()) return;
        int size = Math.min(arr.asArray().size, maxSlots);
        for(int i = 0; i < size; i++){
            String value = arr.asArray().get(i).asString();
            Core.settings.put(prefix + i, (value == null ? "" : value).trim());
        }
        for(int i = size; i < maxSlots; i++){
            Core.settings.put(prefix + i, "");
        }
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
        private final Block[] slots = new Block[maxSlots];
        private String slotsPrefix = keySlotPrefix;
        private boolean outerActive;
        private final Color hudColor = new Color();

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
            float innerRadius = Scl.scl(Core.settings.getInt(keyInnerRadius, 80)) * scale;
            float outerRadius = Scl.scl(Core.settings.getInt(keyOuterRadius, 140)) * scale;
            outerRadius = Math.max(outerRadius, innerRadius + iconSize * 1.15f);
            float slotBack = iconSize / 2f + Scl.scl(10f) * scale;

            Draw.z(1000f);

            hudColor.set(mod.readHudColor());

            // soft background disc around the cursor
            Draw.color(0f, 0f, 0f, 0.18f * alpha);
            float backRadius = (outerActive ? outerRadius : innerRadius) + iconSize * 0.75f;
            Fill.circle(centerX, centerY, backRadius);

            // ring
            Draw.color(hudColor, 0.65f * alpha);
            Lines.stroke(Scl.scl(2f) * scale);
            Lines.circle(centerX, centerY, innerRadius);
            if(outerActive){
                Lines.circle(centerX, centerY, outerRadius);
            }

            int slotCount = outerActive ? maxSlots : slotsPerRing;
            for(int i = 0; i < slotCount; i++){
                float ringRadius = (i < slotsPerRing ? innerRadius : outerRadius);
                int ringIndex = i % slotsPerRing;
                float angle = 90f - ringIndex * 45f;
                float px = centerX + Mathf.cosDeg(angle) * ringRadius;
                float py = centerY + Mathf.sinDeg(angle) * ringRadius;

                boolean isHovered = i == hovered;

                // slot background
                Draw.color(0f, 0f, 0f, (isHovered ? 0.70f : 0.45f) * alpha);
                Fill.circle(px, py, slotBack);

                // slot border
                Draw.color(isHovered ? hudColor : Color.gray, (isHovered ? 1f : 0.35f) * alpha);
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

            outerActive = false;
            for(int i = slotsPerRing; i < maxSlots; i++){
                if(!mod.slotName(slotsPrefix, i).isEmpty()){
                    outerActive = true;
                    break;
                }
            }

            hovered = findHovered();
        }

        private int findHovered(){
            float scale = Mathf.clamp(Core.settings.getInt(keyHudScale, 100) / 100f, 0.1f, 5f);
            float iconSize = Scl.scl(46f) * scale;
            float innerRadius = Scl.scl(Core.settings.getInt(keyInnerRadius, 80)) * scale;
            float outerRadius = Scl.scl(Core.settings.getInt(keyOuterRadius, 140)) * scale;
            outerRadius = Math.max(outerRadius, innerRadius + iconSize * 1.15f);
            float hit = iconSize / 2f + Scl.scl(12f) * scale;
            float hit2 = hit * hit;

            float mx = Core.input.mouseX();
            float my = Core.input.mouseY();

            int best = -1;
            float bestDst2 = hit2;

            int slotCount = outerActive ? maxSlots : slotsPerRing;
            for(int i = 0; i < slotCount; i++){
                float ringRadius = (i < slotsPerRing ? innerRadius : outerRadius);
                int ringIndex = i % slotsPerRing;
                float angle = 90f - ringIndex * 45f;
                float px = centerX + Mathf.cosDeg(angle) * ringRadius;
                float py = centerY + Mathf.sinDeg(angle) * ringRadius;
                float dx = mx - px;
                float dy = my - py;
                float dst2 = dx * dx + dy * dy;
                if(dst2 <= bestDst2){
                    bestDst2 = dst2;
                    best = i;
                }
            }

            if(best != -1) return best;

            float dx = mx - centerX;
            float dy = my - centerY;
            float deadzone = iconSize * 0.35f;
            if(dx * dx + dy * dy < deadzone * deadzone) return -1;

            int sector = sectorIndex(dx, dy);
            int candidate = (outerActive ? slotsPerRing + sector : sector);
            if(candidate < 0 || candidate >= slotCount) return -1;
            return slots[candidate] == null ? -1 : candidate;
        }

        private int sectorIndex(float dx, float dy){
            // NOTE: use angleExact(x, y). Mathf.atan2() has unusual parameter order.
            float angle = Mathf.angleExact(dx, dy);

            float rotated = 90f - angle;
            rotated = ((rotated % 360f) + 360f) % 360f;
            int idx = (int)Math.floor((rotated + 22.5f) / 45f) % 8;
            if(idx < 0) idx += 8;
            return idx;
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
