package com.fox2code.foxloader.client.mixins;

import com.fox2code.foxloader.client.WorldProviderCustom;
import net.minecraft.src.game.level.WorldProvider;
import net.minecraft.src.game.level.WorldProviderHell;
import net.minecraft.src.game.level.chunk.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

@Mixin(SaveOldDir.class)
public abstract class MixinSaveOldDir  {


@Inject(method = "getChunkLoader",at = @At("HEAD"),cancellable = true)
public void getChunkLoader(WorldProvider worldProvider, CallbackInfoReturnable ci) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        System.out.println(worldProvider.worldType);
        if (worldProvider instanceof WorldProviderCustom){
            File save = ((AcessorSaveHandlers)this).getSaveDirectory();
            File file = new File(save, "dimensions/"+((WorldProviderCustom) worldProvider).wpName);
            file.mkdirs();
            System.out.println("saving "+file.toString());
            ci.setReturnValue(new ChunkLoader(file, true));
            ci.cancel();
        }
    }
}
