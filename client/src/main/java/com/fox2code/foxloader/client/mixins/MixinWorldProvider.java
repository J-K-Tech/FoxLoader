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

@Mixin(WorldProvider.class)
public class MixinWorldProvider {
    @Shadow
    public World worldObj;
    public static WorldProvider[] customWorldProviders={null};
    public String WPname;
    public void setName(String name){
        this.WPname=name;
    }

    public static WorldProvider getProviderForDimensioncustom(int arg0) throws IllegalAccessException, InstantiationException {
        return customWorldProviders[arg0].getClass().newInstance();
    }
    public static void addprovider(WorldProvider provider){
        WorldProvider[] tmp=new WorldProvider[customWorldProviders.length+1];
        tmp[customWorldProviders.length]=provider;
        customWorldProviders=tmp;
    }
    @Inject(method = "getChunkProvider",at=@At("HEAD"),cancellable = true)
    public void getChunkProvider(CallbackInfoReturnable ci) {
        if(!worldObj.multiplayerWorld) {
            System.out.println(this.worldObj.getWorldInfo().getGenType());
            if (this.worldObj.getWorldInfo().getGenType()!=0&&this.worldObj.getWorldInfo().getGenType()!=-1) {
                ci.setReturnValue(new ChunkProviderFlatworld(this.worldObj, this.worldObj.getRandomSeed(), this.worldObj.getWorldInfo().isMapFeaturesEnabled()));
                ci.cancel();
            }
        }
    }
}
