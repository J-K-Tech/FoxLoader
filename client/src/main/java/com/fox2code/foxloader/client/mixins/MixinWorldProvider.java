package com.fox2code.foxloader.client.mixins;

import net.minecraft.src.game.level.World;
import net.minecraft.src.game.level.WorldProvider;
import net.minecraft.src.game.level.chunk.ChunkProvider;
import net.minecraft.src.game.level.chunk.ChunkProviderFlatworld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(WorldProvider.class)
public class MixinWorldProvider {
    @Shadow
    public World worldObj;
    public static Map<String,WorldProvider> customWorldProviders=new HashMap<>();
    public static WorldProvider getProviderForDimensioncustom(String arg0) throws IllegalAccessException, InstantiationException {
        return customWorldProviders.get(arg0);
    }
    public static void addprovider(String name,WorldProvider provider){
        customWorldProviders.put(name, provider);
    }
    @Inject(method = "getChunkProvider",at=@At("HEAD"),cancellable = true)
    public void getChunkProvider(CallbackInfoReturnable ci) {
        if(!worldObj.multiplayerWorld) {
            if (this.worldObj.getWorldInfo().getGenType()!=0&&this.worldObj.getWorldInfo().getGenType()!=-1) {
                ci.setReturnValue(new ChunkProviderFlatworld(this.worldObj, this.worldObj.getRandomSeed(), this.worldObj.getWorldInfo().isMapFeaturesEnabled()));
                ci.cancel();
            }
        }
    }
}
