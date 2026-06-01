package com.github.mochi7054;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Inspect {
    public static void run() {
        System.out.println("==================================================");
        System.out.println("STARTING MEKANISM JAR CLASS SCANNING");
        System.out.println("==================================================");

        try {
            Class<?> baseClass = Class.forName("mekanism.api.recipes.cache.CachedRecipe");
            URL location = baseClass.getProtectionDomain().getCodeSource().getLocation();
            System.out.println("MEKANISM JAR LOCATION: " + location);

            File jarFile = new File(location.toURI());
            if (jarFile.exists() && jarFile.isFile()) {
                try (JarFile jar = new JarFile(jarFile)) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (name.endsWith(".class")) {
                            String className = name.replace('/', '.').substring(0, name.length() - 6);
                            if (className.contains("CachedRecipe") || className.contains("OutputHandler") || className.contains("finishProcessing")) {
                                System.out.println("FOUND CLASS: " + className);
                            }
                        }
                    }
                }
            } else {
                System.out.println("Jar file does not exist or is not a file: " + jarFile);
            }
        } catch (Exception e) {
            System.out.println("Error scanning jar: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("==================================================");
        System.out.println("FINISHED MEKANISM JAR CLASS SCANNING");
        System.out.println("==================================================");

        // Standard inspection
        inspectClass("mekanism.api.Upgrade");
        inspectClass("mekanism.common.tile.component.TileComponentUpgrade");
    }

    private static void inspectClass(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            System.out.println("CLASS: " + clazz.getName());

            System.out.println("  Constructors:");
            for (Constructor<?> c : clazz.getDeclaredConstructors()) {
                System.out.println("    - " + c.toString());
            }

            System.out.println("  Fields:");
            for (Field f : clazz.getDeclaredFields()) {
                System.out.println("    - " + Modifier.toString(f.getModifiers()) + " " + f.getType().getName() + " " + f.getName());
            }

            System.out.println("  Methods:");
            for (Method m : clazz.getDeclaredMethods()) {
                System.out.println("    - " + Modifier.toString(m.getModifiers()) + " " + m.getReturnType().getName() + " " + m.getName() + "(" + getParamTypes(m) + ")");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("CLASS NOT FOUND: " + className);
        } catch (Throwable t) {
            System.out.println("ERROR INSPECTING " + className + ": " + t.getMessage());
            t.printStackTrace();
        }
        System.out.println("--------------------------------------------------");
    }

    private static String getParamTypes(Method m) {
        StringBuilder sb = new StringBuilder();
        Class<?>[] params = m.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(params[i].getName());
        }
        return sb.toString();
    }
}
