package mdtxcompat;

import mindustry.game.Schematic;

import java.lang.reflect.Method;

public class MindustryXSchematicShareBridge implements SchematicShareBridge {
    @Override
    public boolean isSupported() {
        return shareMethod("shareSchematic") != null && shareMethod("shareSchematicClipboard") != null;
    }

    @Override
    public void shareToChat(Schematic schematic) {
        invokeShare("shareSchematic", schematic);
    }

    @Override
    public void shareToClipboard(Schematic schematic) {
        invokeShare("shareSchematicClipboard", schematic);
    }

    private static void invokeShare(String methodName, Schematic schematic) {
        try {
            Method method = shareMethod(methodName);
            if (method != null) {
                method.invoke(null, schematic);
            }
        } catch (Throwable ignored) {
        }
    }

    private static Method shareMethod(String methodName) {
        try {
            Class<?> shareFeature = Class.forName("mindustryX.features.ShareFeature");
            return shareFeature.getMethod(methodName, Schematic.class);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
