package com.porpit.crafttweakersync.network;

import com.porpit.crafttweakersync.CraftTweakerSync;
import com.porpit.crafttweakersync.common.scriptdata.ScriptFileInfo;
import com.porpit.crafttweakersync.common.scriptdata.ScriptFileManager;
import com.porpit.crafttweakersync.util.FileHelper;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Arrays;
import java.util.List;

public class MessageFileRequest implements IMessage {

    private List<ScriptFileInfo> neededFiles;

    public MessageFileRequest() {
    }

    public MessageFileRequest(List<ScriptFileInfo> neededFiles) {
        this.neededFiles = neededFiles;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            int byteLength = buf.readInt();
            byte bytes[] = new byte[byteLength];
            buf.readBytes(bytes);
            ScriptFileInfo[] scriptFileInfos = (ScriptFileInfo[]) FileHelper.getObjectFromBytes(bytes);
            neededFiles = Arrays.asList(scriptFileInfos);
        } catch (Exception e) {
            neededFiles = null;
            e.printStackTrace();
            CraftTweakerSync.logger.warn("客户端獲取信息失敗");
        }


    }

    @Override
    public void toBytes(ByteBuf buf) {
        ScriptFileInfo[] scriptFileInfos = new ScriptFileInfo[neededFiles.size()];
        neededFiles.toArray(scriptFileInfos);
        try {
            byte bytes[] = FileHelper.getBytesFromObject(scriptFileInfos);
            System.out.println(neededFiles + "," + bytes);
            buf.writeInt(bytes.length);
            buf.writeBytes(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            CraftTweakerSync.logger.warn("發送數據失敗");
        }
    }

    public static class Handler implements IMessageHandler<MessageFileRequest, IMessage> {
        @Override
        public IMessage onMessage(MessageFileRequest message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                if (message.neededFiles == null) {
                    return null;

                }
                ScriptFileManager.getInstance().addTask(new Runnable() {
                    @Override
                    public void run() {
                        ScriptFileManager.getInstance().sendScriptFileData(message.neededFiles,ctx.getServerHandler().player);
                    }
                });
            }

            return null;
        }
    }
}
