package com.ait.lienzo.shared.java.util;

public class ClassUtils {

    /**
     * GWT < 2.6 cannot emulate Class#getSimpleName().
     * This helper method is used to achieve GWT compilation for version 2.5.x
     * * 
     * @param clazz The class instance.
     * @return A simple name for the class.
     */
    public static String getSimpleName(Class clazz) {
        return clazz.getName();
    }
}
