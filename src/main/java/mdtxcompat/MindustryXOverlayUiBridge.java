package mdtxcompat;

import arc.func.Prov;
import arc.scene.Element;
import arc.scene.ui.layout.Table;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MindustryXOverlayUiBridge implements OverlayUiBridge {
    @Override
    public boolean isSupported() {
        return overlayInstance() != null;
    }

    @Override
    public OverlayWindowHandle registerWindow(String name, Table table, Prov<Boolean> availability) {
        Object overlay = overlayInstance();
        if (overlay == null) return OverlayUiBridge.UNSUPPORTED.registerWindow(name, table, availability);

        try {
            Method registerWindow = overlay.getClass().getMethod("registerWindow", String.class, Table.class);
            Object window = registerWindow.invoke(overlay, name, table);
            if (window == null) return OverlayUiBridge.UNSUPPORTED.registerWindow(name, table, availability);

            if (availability != null) {
                tryInvoke(window, "setAvailability", new Class[]{Prov.class}, availability);
            }
            return new WindowHandle(window);
        } catch (Throwable ignored) {
            return OverlayUiBridge.UNSUPPORTED.registerWindow(name, table, availability);
        }
    }

    @Override
    public void closeEditorIfOpen() {
        Object overlay = overlayInstance();
        if (overlay == null) return;

        try {
            Method getOpen = overlay.getClass().getMethod("getOpen");
            Object open = getOpen.invoke(overlay);
            if (open instanceof Boolean && (Boolean) open) {
                Method toggle = overlay.getClass().getMethod("toggle");
                toggle.invoke(overlay);
            }
        } catch (Throwable ignored) {
        }
    }

    private static Object overlayInstance() {
        try {
            Class<?> overlayClass = Class.forName("mindustryX.features.ui.OverlayUI");
            Field instanceField = overlayClass.getField("INSTANCE");
            return instanceField.get(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static final class WindowHandle implements OverlayWindowHandle {
        private final Object window;

        private WindowHandle(Object window) {
            this.window = window;
        }

        @Override
        public void configure(boolean autoHeight, boolean resizable) {
            tryInvoke(window, "setAutoHeight", new Class[]{boolean.class}, autoHeight);
            tryInvoke(window, "setResizable", new Class[]{boolean.class}, resizable);
        }

        @Override
        public void setEnabledAndPinned(boolean enabled, boolean pinned) {
            Object data = invoke(window, "getData");
            if (data == null) return;
            tryInvoke(data, "setEnabled", new Class[]{boolean.class}, enabled);
            tryInvoke(data, "setPinned", new Class[]{boolean.class}, pinned);
        }

        @Override
        public Boolean getEnabled() {
            Object data = invoke(window, "getData");
            if (data == null) return null;
            Object enabled = invoke(data, "getEnabled");
            return enabled instanceof Boolean ? (Boolean) enabled : null;
        }

        @Override
        public Element asElement() {
            return window instanceof Element ? (Element) window : null;
        }
    }

    private static Object invoke(Object target, String methodName) {
        if (target == null) return null;
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void tryInvoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        if (target == null) return;
        try {
            Method method = target.getClass().getMethod(methodName, parameterTypes);
            method.invoke(target, args);
        } catch (Throwable ignored) {
        }
    }
}
