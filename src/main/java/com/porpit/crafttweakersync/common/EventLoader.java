package com.porpit.crafttweakersync.common;

import com.porpit.crafttweakersync.CraftTweakerSync;
import com.porpit.crafttweakersync.common.scriptdata.ScriptFileManager;
import com.porpit.crafttweakersync.network.MessageServerFileInfo;
import com.porpit.crafttweakersync.network.NetworkLoader;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class EventLoader {
    public static final EventBus EVENT_BUS = new EventBus();

    public EventLoader() {
        MinecraftForge.EVENT_BUS.register(this);
        EventLoader.EVENT_BUS.register(this);
    }


    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if(event.player.getEntityWorld().isRemote){

            return;
        }

        EntityPlayerMP player = (EntityPlayerMP)event.player;
        MessageServerFileInfo message=new MessageServerFileInfo(ScriptFileManager.getInstance().getAllScriptData());
        CraftTweakerSync.logger.debug("发送玩家服务器CraftTweaker文件数据 以进行比对");
        NetworkLoader.instance.sendTo(message,player);
    }
}
