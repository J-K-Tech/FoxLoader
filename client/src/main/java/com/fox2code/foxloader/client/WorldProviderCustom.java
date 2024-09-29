package com.fox2code.foxloader.client;

import net.minecraft.src.game.level.World;
import net.minecraft.src.game.level.WorldProvider;
import net.minecraft.src.game.level.chunk.ChunkProviderFlatworld;
import net.minecraft.src.game.level.chunk.IChunkProvider;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashMap;
import java.util.Map;

public class WorldProviderCustom extends WorldProvider {
    @Shadow
    public World worldObj;
    public  static Map<String,WorldProviderCustom> customWorldProviders=new HashMap<>();
    public static WorldProviderCustom getProviderForDimensioncustom(String arg0) {
        return customWorldProviders.get(arg0);
    }


    public static void addprovider(String name, WorldProviderCustom provider){
        customWorldProviders.put(name, provider);
    }
    @Override
    public IChunkProvider getChunkProvider() {
        if(!worldObj.multiplayerWorld) {
            if (this.worldObj.getWorldInfo().getGenType()!=0&&this.worldObj.getWorldInfo().getGenType()!=-1) {
                return new ChunkProviderFlatworld(this.worldObj, this.worldObj.getRandomSeed(), this.worldObj.getWorldInfo().isMapFeaturesEnabled());
            }
        }return super.getChunkProvider();
    }
}
