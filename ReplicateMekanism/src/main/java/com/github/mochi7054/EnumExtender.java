package com.github.mochi7054;

import java.lang.reflect.Field;
import java.util.Arrays;
import mekanism.api.Upgrade;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import sun.misc.Unsafe;

public class EnumExtender {
    public static Upgrade extendUpgrade() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Unsafe unsafe = (Unsafe) theUnsafe.get(null);

            // Allocate Upgrade instance
            Upgrade replicaUpgrade = (Upgrade) unsafe.allocateInstance(Upgrade.class);

            // Set Enum fields
            setField(unsafe, replicaUpgrade, Enum.class, "name", "REPLICA");
            setFieldInt(unsafe, replicaUpgrade, Enum.class, "ordinal", 7);

            // Set Upgrade fields
            ILangEntry langKey = new ReplicaLangEntry("upgrade.replicatemekanism.replica");
            ILangEntry descLangKey = new ReplicaLangEntry("upgrade.replicatemekanism.replica.desc");

            setField(unsafe, replicaUpgrade, Upgrade.class, "name", "replica");
            setField(unsafe, replicaUpgrade, Upgrade.class, "langKey", langKey);
            setField(unsafe, replicaUpgrade, Upgrade.class, "descLangKey", descLangKey);
            setFieldInt(unsafe, replicaUpgrade, Upgrade.class, "maxStack", 1);
            setField(unsafe, replicaUpgrade, Upgrade.class, "color", EnumColor.DARK_BLUE);

            // Add to $VALUES
            Field valuesField = Upgrade.class.getDeclaredField("$VALUES");
            valuesField.setAccessible(true);
            Upgrade[] oldValues = (Upgrade[]) valuesField.get(null);
            Upgrade[] newValues = Arrays.copyOf(oldValues, oldValues.length + 1);
            newValues[oldValues.length] = replicaUpgrade;

            // Update $VALUES using Unsafe
            long valuesOffset = unsafe.staticFieldOffset(valuesField);
            Object valuesBase = unsafe.staticFieldBase(valuesField);
            unsafe.putObject(valuesBase, valuesOffset, newValues);

            // Clear Class caches
            clearClassCaches(unsafe, Upgrade.class);

            // Override BY_ID field using Unsafe
            try {
                Field byIdField = Upgrade.class.getDeclaredField("BY_ID");
                long byIdOffset = unsafe.staticFieldOffset(byIdField);
                Object byIdBase = unsafe.staticFieldBase(byIdField);
                @SuppressWarnings("unchecked")
                java.util.function.IntFunction<Upgrade> originalById = (java.util.function.IntFunction<Upgrade>) unsafe.getObject(byIdBase, byIdOffset);
                java.util.function.IntFunction<Upgrade> customById = id -> {
                    if (id == 7) {
                        return replicaUpgrade;
                    }
                    return originalById != null ? originalById.apply(id) : null;
                };
                unsafe.putObject(byIdBase, byIdOffset, customById);
                System.out.println("SUCCESSFULLY OVERRODE Upgrade.BY_ID!");
            } catch (Exception e) {
                System.err.println("FAILED TO OVERRIDE Upgrade.BY_ID: " + e.getMessage());
            }

            System.out.println("SUCCESSFULLY INJECTED REPLICA UPGRADE ENUM CONSTANT!");
            return replicaUpgrade;
        } catch (Exception e) {
            System.err.println("FAILED TO INJECT REPLICA UPGRADE ENUM CONSTANT: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static void setField(Unsafe unsafe, Object obj, Class<?> declaringClass, String fieldName, Object value) throws Exception {
        Field field = declaringClass.getDeclaredField(fieldName);
        long offset = unsafe.objectFieldOffset(field);
        unsafe.putObject(obj, offset, value);
    }

    private static void setFieldInt(Unsafe unsafe, Object obj, Class<?> declaringClass, String fieldName, int value) throws Exception {
        Field field = declaringClass.getDeclaredField(fieldName);
        long offset = unsafe.objectFieldOffset(field);
        unsafe.putInt(obj, offset, value);
    }

    private static void clearClassCaches(Unsafe unsafe, Class<?> clazz) throws Exception {
        try {
            Field enumConstantsField = Class.class.getDeclaredField("enumConstants");
            long enumConstantsOffset = unsafe.objectFieldOffset(enumConstantsField);
            unsafe.putObject(clazz, enumConstantsOffset, null);
        } catch (NoSuchFieldException e) {
            // Field might not exist or have different name in some VMs
        }

        try {
            Field enumConstantDirectoryField = Class.class.getDeclaredField("enumConstantDirectory");
            long enumConstantDirectoryOffset = unsafe.objectFieldOffset(enumConstantDirectoryField);
            unsafe.putObject(clazz, enumConstantDirectoryOffset, null);
        } catch (NoSuchFieldException e) {
            // Field might not exist or have different name in some VMs
        }
    }

    private static class ReplicaLangEntry implements ILangEntry {
        private final String key;

        public ReplicaLangEntry(String key) {
            this.key = key;
        }

        @Override
        public String getTranslationKey() {
            return key;
        }

        @Override
        public MutableComponent translate(Object... args) {
            return Component.translatable(key, args);
        }

        @Override
        public MutableComponent translate() {
            return Component.translatable(key);
        }

        @Override
        public MutableComponent translateColored(TextColor color, Object... args) {
            return translate(args).withStyle(style -> style.withColor(color));
        }

        @Override
        public MutableComponent translateColored(TextColor color) {
            return translate().withStyle(style -> style.withColor(color));
        }

        @Override
        public MutableComponent translateColored(EnumColor color) {
            return translate().withStyle(style -> style.withColor(color.getColor()));
        }

        @Override
        public MutableComponent translateColored(EnumColor color, Object... args) {
            return translate(args).withStyle(style -> style.withColor(color.getColor()));
        }
    }
}
