package com.fox2code.foxloader.client.mixins;

import com.fox2code.foxloader.client.helpers.WorldProviderHelper;
import net.minecraft.src.game.block.Block;
import net.minecraft.src.game.block.BlockPortal;
import net.minecraft.src.game.block.Material;
import net.minecraft.src.game.entity.Entity;
import net.minecraft.src.game.level.World;
import net.minecraft.src.game.level.WorldProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.InvocationTargetException;

@Mixin(BlockPortal.class)

public class MixinBlockPortal extends Block {
    public int dimension=-1;
    public String wp=null;

    public Block withProvider(WorldProvider worldProvider,String name) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        this.wp=name;
        WorldProviderHelper.addWorldProvider(name,worldProvider);
        return this;
    }
    @Inject(method = "onEntityCollidedWithBlock",at=@At("HEAD"))
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity, CallbackInfo ci) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (entity.ridingEntity == null && entity.riddenByEntity == null) {
            entity.getClass().getMethod("setInPortalcustom").invoke(entity);
        }
    }

    public MixinBlockPortal(int id, Material material) {
        super(id, material);
    }
}
