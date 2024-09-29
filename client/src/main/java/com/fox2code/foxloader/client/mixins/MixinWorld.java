package com.fox2code.foxloader.client.mixins;

import com.fox2code.foxloader.client.WorldProviderCustom;
import com.fox2code.foxloader.loader.ClientMod;
import com.fox2code.foxloader.network.NetworkPlayer;
import com.fox2code.foxloader.registry.*;
import jdk.internal.org.objectweb.asm.Opcodes;
import net.minecraft.client.Minecraft;
import net.minecraft.src.client.gui.StringTranslate;
import net.minecraft.src.client.player.EntityPlayerSP;
import net.minecraft.src.game.block.tileentity.TileEntity;
import net.minecraft.src.game.entity.Entity;
import net.minecraft.src.game.entity.other.EntityItem;
import net.minecraft.src.game.entity.player.EntityPlayer;
import net.minecraft.src.game.level.World;
import net.minecraft.src.game.level.WorldInfo;
import net.minecraft.src.game.level.WorldProvider;
import net.minecraft.src.game.level.WorldProviderSurface;
import net.minecraft.src.game.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Mixin(World.class)
public abstract class MixinWorld implements RegisteredWorld {
    @Shadow public List<EntityPlayer> playerEntities;
    @Shadow public List<TileEntity> loadedTileEntityList;
    @Shadow public List<Entity> loadedEntityList;
    @Shadow public boolean multiplayerWorld;
    @Shadow public WorldInfo worldInfo;

    @Shadow public abstract int getBlockId(int x, int y, int z);
    @Shadow public abstract int getBlockMetadata(int xCoord, int yCoord, int zCoord);
    @Shadow public abstract boolean setBlockAndMetadataWithNotify(int xCoord, int yCoord, int zCoord, int block, int metadata);
    @Shadow public abstract boolean entityJoinedWorld(Entity entity);

    @Shadow @Final public WorldProvider worldProvider;
    @Redirect(method = "init()V",at = @At(value = "FIELD",target = "Lnet/minecraft/src/game/level/World;worldProvider:Lnet/minecraft/src/game/level/WorldProvider", opcode = Opcodes.PUTFIELD,ordinal =2))
    public void worldtype(World self, WorldProvider worldProvider) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (worldProvider instanceof WorldProviderCustom &&!multiplayerWorld){
            NBTTagCompound pnbt = this.worldInfo.getPlayerNBTTagCompound();
            WorldProviderCustom wp= WorldProviderCustom.getProviderForDimensioncustom(
                    pnbt.getString("customDimension")
                    );
            if (wp==null) {
                World w= new World(Minecraft.theMinecraft.theWorld, WorldProvider.getProviderForDimension(0));


                short hc = Minecraft.theMinecraft.theWorld.worldInfo.getHighestChunkOW();
                short lc = Minecraft.theMinecraft.theWorld.worldInfo.getLowestChunkOW();
                w.highestChunk = hc;
                w.highestY = hc << 4;
                w.lowestChunk = lc;
                w.lowestY = lc << 4;
                Minecraft.theMinecraft.changeWorld(w, "dimension "+pnbt.getString("customDimension")+" not found", Minecraft.theMinecraft.thePlayer);

                this.worldProvider=new WorldProviderSurface();
            }
        else this.worldProvider=wp;
        }
    }

    @Override
    public boolean hasRegisteredControl() {
        return !this.multiplayerWorld;
    }

    @Override
    public int getRegisteredBlockId(int xCoord, int yCoord, int zCoord) {
        return this.getBlockId(xCoord, yCoord, zCoord);
    }

    @Override
    public int getRegisteredBlockMetadata(int xCoord, int yCoord, int zCoord) {
        return this.getBlockMetadata(xCoord, yCoord, zCoord);
    }

    @Override
    public void setRegisteredBlockAndMetadataWithNotify(int xCoord, int yCoord, int zCoord, int block, int metadata) {
        if (this.multiplayerWorld) {
            throw new IllegalStateException("Can't set blocks on a multiplayer world");
        }
        this.setBlockAndMetadataWithNotify(xCoord, yCoord, zCoord, block, metadata);
    }

    @Override
    public void forceSetRegisteredBlockAndMetadataWithNotify(int xCoord, int yCoord, int zCoord, int block, int metadata) {
        this.setBlockAndMetadataWithNotify(xCoord, yCoord, zCoord, block, metadata);
    }

    @Override
    public RegisteredEntityItem spawnRegisteredEntityItem(double x, double y, double z, RegisteredItemStack registeredItemStack) {
        EntityItem entityItem = new EntityItem((World) (Object) this,
                x, y, z, ClientMod.toItemStack(registeredItemStack));
        if (!this.entityJoinedWorld(entityItem)) {
            return null;
        }
        return (RegisteredEntityItem) entityItem;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<? extends RegisteredEntity> getRegisteredEntities() {
        return (List<? extends RegisteredEntity>) (Object) this.loadedEntityList;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<? extends RegisteredTileEntity> getRegisteredTileEntities() {
        return (List<? extends RegisteredTileEntity>) (Object) this.loadedTileEntityList;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<? extends NetworkPlayer> getRegisteredNetworkPlayers() {
        return (List<? extends NetworkPlayer>) (Object) this.playerEntities;
    }

    @Override
    public int getRegisteredDimensionID() {
        return this.worldProvider.worldType;
    }
}
