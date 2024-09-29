package com.fox2code.foxloader.client.mixins;

import com.fox2code.foxloader.launcher.FoxLauncher;
import com.fox2code.foxloader.loader.ClientModLoader;
import com.fox2code.foxloader.loader.ModLoader;
import com.fox2code.foxloader.network.NetworkPlayer;
import com.fox2code.foxloader.network.SidedMetadataAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.src.client.GameSettings;
import net.minecraft.src.client.gui.StringTranslate;
import net.minecraft.src.client.player.EntityPlayerSP;
import net.minecraft.src.game.entity.player.EntityPlayer;
import net.minecraft.src.game.level.World;
import net.minecraft.src.game.level.WorldProvider;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Shadow public volatile boolean running;
    @Shadow public GameSettings gameSettings;
    @Shadow private static File minecraftDir;
    @Unique private NetworkPlayer.ConnectionType loadedWorldType;
    @Unique private boolean closeGameDelayed;
    @Unique private boolean showDebugInfoPrevious;

    @Inject(method = "startGame", at = @At("HEAD"))
    public void onStartGame(CallbackInfo ci) {
        ClientModLoader.Internal.notifyRun();
    }

    @Inject(method = "runTick", at = @At("HEAD"))
    public void onRunTick(CallbackInfo ci) {
        ModLoader.Internal.notifyOnTick();
    }

    @Inject(method = "changeWorld", at = @At("RETURN"))
    public void onChangeWorld(World world, String var2, EntityPlayer player, CallbackInfo ci) {
        if (world == null) {
            if (loadedWorldType != null) {
                NetworkPlayer.ConnectionType
                        tmp = this.loadedWorldType;
                this.loadedWorldType = null;
                ModLoader.Internal.notifyOnServerStop(tmp);
            }
        } else if (loadedWorldType == null) {
            ModLoader.Internal.notifyOnServerStart(
                    this.loadedWorldType = world.multiplayerWorld ?
                            NetworkPlayer.ConnectionType.CLIENT_ONLY :
                            NetworkPlayer.ConnectionType.SINGLE_PLAYER);
            if (!world.multiplayerWorld) {
                SidedMetadataAPI.Internal.setActiveMetaData(null);
            }
        }
    }

    @Inject(method = "startMainThread", at = @At("RETURN"))
    private static void onGameStarted(String var0, String var1, String var2, CallbackInfo ci) {
        try {
            Frame[] frames = Frame.getFrames();
            final List<Image> icons = Collections.singletonList(
                    ImageIO.read(Objects.requireNonNull(Minecraft.class.getResource("/icon.png"))));
            for (Frame frame : frames) {
                try {
                    frame.setIconImages(icons);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        } catch (Exception ignored) {}
    }

    @Inject(method = "getMinecraftDir", at = @At("HEAD"))
    private static void onGetMinecraftDir(CallbackInfoReturnable<File> cir) {
        if (minecraftDir == null) {
            minecraftDir = FoxLauncher.getGameDir();
        }
    }

    // Linux fix:
    @Inject(method = "shutdown", at = @At(value = "HEAD"), cancellable = true)
    public void shutdownRedirect(CallbackInfo ci) {
        if (this.running && ClientModLoader.linuxFix) {
            this.closeGameDelayed = true;
            ci.cancel();
        }
    }

    @Inject(method = "runTick", at = @At("RETURN"))
    public void onRunTickEnd(CallbackInfo ci) {
        if (this.showDebugInfoPrevious != this.gameSettings.showDebugInfo) {
            boolean debugEnabled = this.showDebugInfoPrevious = this.gameSettings.showDebugInfo;
            if (debugEnabled) { // F3 + Maj show time
                ClientModLoader.showFrameTimes = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
            } else ClientModLoader.showFrameTimes = false;
        }
        if (this.closeGameDelayed) {
            this.closeGameDelayed = false;
            this.running = false;
        }
    }@Shadow
    public World theWorld;
    @Shadow
    public EntityPlayerSP thePlayer;

    @Shadow

    public void changeWorld(World world, String arg2, EntityPlayer player) {}

    @Inject(method = "usePortal",at=@At("HEAD"),cancellable = true)
    public void usePortal(CallbackInfo ci) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (thePlayer.getClass().getField("incustomportal").getBoolean(thePlayer)){
            if(thePlayer.dimension==0){
            String dim=(String) thePlayer.getClass().getField("goingtodim").get(thePlayer);
            thePlayer.dimension=3;

            WorldProvider wp=(WorldProvider)WorldProvider.class.getMethod("getProviderForDimensioncustom").invoke(null,dim);

            if (this.thePlayer.isEntityAlive()) {
                this.theWorld.updateEntityWithOptionalForce(this.thePlayer, false);
            }
            short hc = this.theWorld.worldInfo.getHighestChunkOW();
            short lc = this.theWorld.worldInfo.getLowestChunkOW();
            World world = new World(this.theWorld, wp);
            world.highestChunk = hc;
            world.highestY = hc << 4;
            world.lowestChunk = lc;
            world.lowestY = lc << 4;

            this.changeWorld(world, StringTranslate.getInstance().translateKey("gui.world.enterNether"), this.thePlayer);
            ci.cancel();


        }
        else {
            this.thePlayer.setLocationAndAngles(this.thePlayer.posX, this.thePlayer.posY, this.thePlayer.posZ, this.thePlayer.rotationYaw, this.thePlayer.rotationPitch);
            if (this.thePlayer.isEntityAlive()) {
                this.theWorld.updateEntityWithOptionalForce(this.thePlayer, false);
            }

            short hc = this.theWorld.worldInfo.getHighestChunkOW();
            short lc = this.theWorld.worldInfo.getLowestChunkOW();
            World world = new World(this.theWorld, WorldProvider.getProviderForDimension(0));
            world.highestChunk = hc;
            world.highestY = hc << 4;
            world.lowestChunk = lc;
            world.lowestY = lc << 4;
            this.changeWorld(world, StringTranslate.getInstance().translateKey("gui.world.leaveNether"), this.thePlayer);
        }
    }
    }
}
