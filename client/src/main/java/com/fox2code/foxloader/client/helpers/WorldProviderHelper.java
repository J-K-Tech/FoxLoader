package com.fox2code.foxloader.client.helpers;

import net.minecraft.src.game.level.WorldProvider;

import java.lang.reflect.InvocationTargetException;

public class WorldProviderHelper {
    public static void addWorldProvider(String name,WorldProvider worldProvider) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        worldProvider.getClass().getMethod("setName").invoke(worldProvider,name);
        WorldProvider.class.getMethod("addprovider").invoke(null,name,worldProvider);
    }
}