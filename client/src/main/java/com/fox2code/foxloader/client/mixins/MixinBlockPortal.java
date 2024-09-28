package com.fox2code.foxloader.client.mixins;

import net.minecraft.src.game.block.Block;
import net.minecraft.src.game.block.BlockPortal;
import net.minecraft.src.game.block.Material;
import net.minecraft.src.game.level.WorldProvider;
import org.spongepowered.asm.mixin.Mixin;

import java.lang.reflect.InvocationTargetException;

@Mixin(BlockPortal.class)

public class MixinBlockPortal extends Block {
    public int dimension=-1;
    public WorldProvider wp;

    public Block withProvider(WorldProvider worldProvider) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        this.wp=worldProvider;

        WorldProvider.class.getMethod("addprovider").invoke(null,this.wp);
        return this;
    }

    protected MixinBlockPortal(int id, Material material) {
        super(id, material);
    }
}
