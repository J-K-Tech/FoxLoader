package com.fox2code.foxloader.client.helpers;

import net.minecraft.src.game.level.WorldProvider;

import java.lang.reflect.InvocationTargetException;

public class WorldProviderHelper {
    public static void setName(String name, WorldProvider wp) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        wp.getClass().getMethod("setName").invoke(wp,name);
    }
}