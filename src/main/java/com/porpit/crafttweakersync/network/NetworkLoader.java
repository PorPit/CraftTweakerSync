package com.porpit.crafttweakersync.network;

import com.porpit.crafttweakersync.CraftTweakerSync;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkLoader {
    public static SimpleNetworkWrapper instance = NetworkRegistry.INSTANCE.newSimpleChannel(CraftTweakerSync.MODID);

    private static int nextID = 0;

    public NetworkLoader(FMLPreInitializationEvent event) {
        registerMessage(MessageServerFileInfo.Handler.class,MessageServerFileInfo.class,Side.CLIENT);
        registerMessage(MessageFileRequest.Handler.class,MessageFileRequest.class,Side.SERVER);
        registerMessage(MessageFileData.Handler.class,MessageFileData.class,Side.CLIENT);
    }

    private static <REQ extends IMessage, REPLY extends IMessage> void registerMessage(
            Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType, Side side) {
        instance.registerMessage(messageHandler, requestMessageType, nextID++, side);
    }
}
