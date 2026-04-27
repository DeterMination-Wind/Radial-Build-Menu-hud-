package mdtxcompat;

import arc.math.geom.Vec2;
import arc.util.Log;

import java.lang.reflect.Method;

public class MindustryXMarkerBridge implements MarkerBridge {
    private boolean available = true;

    @Override
    public boolean isSupported() {
        return available && markerMethod() != null;
    }

    @Override
    public void mark(String text, int tileX, int tileY) {
        if (!available) return;
        try {
            Method method = markerMethod();
            if (method == null) {
                available = false;
                return;
            }
            method.invoke(null, text, new Vec2(tileX, tileY));
        } catch (Throwable t) {
            available = false;
            Log.err("MindustryX marker call failed; disabling integration.", t);
        }
    }

    private static Method markerMethod() {
        try {
            Class<?> markerType = Class.forName("mindustryX.features.MarkerType");
            return markerType.getMethod("newMarkFromChat", String.class, Vec2.class);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
