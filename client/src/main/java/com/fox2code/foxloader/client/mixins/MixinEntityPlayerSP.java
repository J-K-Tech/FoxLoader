package com.fox2code.foxloader.client.mixins;

import com.fox2code.foxloader.loader.ClientMod;
import com.fox2code.foxloader.loader.ModContainer;
import com.fox2code.foxloader.network.NetworkConnection;
import com.fox2code.foxloader.network.NetworkPlayer;
import com.fox2code.foxloader.registry.RegisteredItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.src.client.gui.GuiScreen;
import net.minecraft.src.client.gui.StringTranslate;
import net.minecraft.src.client.player.EntityPlayerSP;
import net.minecraft.src.game.entity.Entity;
import net.minecraft.src.game.entity.player.EntityPlayer;
import net.minecraft.src.game.level.World;
import net.minecraft.src.game.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends EntityPlayer implements NetworkPlayer {

    @Shadow public Minecraft mc;
    public String customDimension=null;
    @Inject(method = "writeEntityToNBT",at = @At("TAIL"))
    public void writeEntityToNBT(NBTTagCompound var1,CallbackInfo ci) {
        var1.setString("customDimension", this.customDimension);
    }

    @Inject(method = "writeEntityToNBT",at = @At("TAIL"))
    public void readEntityFromNBT(NBTTagCompound var1, CallbackInfo ci) {
        this.customDimension = var1.getString("customDimension");
    }
    public MixinEntityPlayerSP(World var1) {
        super(var1);
    }

    @Override
    public NetworkConnection getNetworkConnection() {
        return null;
    }

    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.SINGLE_PLAYER;
    }

    @Override
    public void sendNetworkData(ModContainer modContainer, byte[] data) {}

    @Override
    public void displayChatMessage(String chatMessage) {
        StringTranslate st = StringTranslate.getInstance();
        if (chatMessage.indexOf('\n') == -1) {
            Minecraft.getInstance().ingameGUI.addChatMessage(st.translateKey(chatMessage));
        } else {
            String[] splits = chatMessage.split("\\n");
            for (String split : splits) {
                Minecraft.getInstance().ingameGUI.addChatMessage(st.translateKey(split));
            }
        }
    }

    @Override
    public boolean hasFoxLoader() {
        return true;
    }

    @Override
    public String getPlayerName() {
        return ((EntityPlayer) (Object) this).username;
    }

    @Override
    public boolean isOperator() {
        return ((Entity) (Object) this).worldObj.worldInfo.isCheatsEnabled();
    }

    @Override
    public void kick(String message) {
        throw new IllegalStateException("kick cannot be used client-side");
    }

    @Override
    public NetworkPlayerController getNetworkPlayerController() {
        return (NetworkPlayerController) Minecraft.getInstance().playerController;
    }

    @Override
    public boolean isConnected() {
        final Minecraft mc = Minecraft.getInstance();
        return mc.theWorld != null && !mc.isMultiplayerWorld();
    }

    @Override
    public RegisteredItemStack getRegisteredHeldItem() {
        EntityPlayerSP networkPlayerSP = (EntityPlayerSP) (Object) this;
        return ClientMod.toRegisteredItemStack(networkPlayerSP.inventory.getCurrentItem());
    }

    @Override
    public void sendPlayerThroughPortalRegistered() {
        this.mc.usePortal();
        this.inPortal = false;
    }
    public String goingtodim=null;
    @Shadow
    public World worldObj;
    @Shadow
    protected Random rand;
    @Shadow
    public Entity ridingEntity;
    @Shadow
    public abstract void mountEntity(Entity entity);
    public float timeInPortalcustom = 0.f;
    public boolean incustomportal=false;

    public int timeUntilPortalcustom=0;

    public void setInPortalcustom(String name) {
        if (this.timeUntilPortalcustom > 0) {
            this.timeUntilPortalcustom = 10;
        } else {
            this.incustomportal = true;
            this.goingtodim=name;
        }
    }
    @Inject(method = "onLivingUpdate",at=@At("HEAD"))
    public void onLivingUpdate(CallbackInfo ci) {
        if (this.incustomportal){
            if (!this.worldObj.multiplayerWorld && this.ridingEntity != null) {
                this.mountEntity((Entity)null);
            }

            if (this.mc.currentScreen != null) {
                this.mc.displayGuiScreen((GuiScreen)null);
            }

            if (this.timeInPortalcustom == 0.0F) {
                this.mc.sndManager.playSoundFX("portal.trigger", 1.0F, this.rand.nextFloat() * 0.4F + 0.8F);
            }

            this.timeInPortalcustom += 0.0125F;
            if (this.timeInPortalcustom >= 1.0F) {
                this.timeInPortalcustom = 1.0F;
                if (!this.worldObj.multiplayerWorld) {
                    this.timeUntilPortalcustom = 10;
                    this.mc.sndManager.playSoundFX("portal.travel", 1.0F, this.rand.nextFloat() * 0.4F + 0.8F);
                    this.mc.usePortal();
                    this.customDimension=goingtodim;
                    this.goingtodim=null;
                    this.incustomportal = false;
                }
            }

        }
        else {
            this.goingtodim=null;
            this.incustomportal = false;
            if (this.timeInPortalcustom > 0.0F) {
                this.timeInPortalcustom -= 0.05F;
            }

            if (this.timeInPortalcustom < 0.0F) {
                this.timeInPortalcustom = 0.0F;
            }
        }

        if (this.timeUntilPortalcustom > 0) {
            this.timeUntilPortalcustom--;
        }
    }
}
