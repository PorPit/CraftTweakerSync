package com.porpit.crafttweakersync.network;

import com.porpit.crafttweakersync.CraftTweakerSync;
import com.porpit.crafttweakersync.common.scriptdata.ScriptFileManager;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MessageFileData implements IMessage {
    private String path;
    private byte[] data;
    private boolean eofFlag;

    public MessageFileData() {
    }

    public MessageFileData(String path, byte[] data, boolean eofFlag) {
        this.path = path;
        this.data = data;
        this.eofFlag = eofFlag;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int pathlength = buf.readInt();
        byte pathbyte[] = new byte[pathlength];
        buf.readBytes(pathbyte);
        path = new String(pathbyte);
        int datalength=buf.readInt();
        data=new byte[datalength];
        buf.readBytes(data);
        eofFlag=buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(path.getBytes().length);
        buf.writeBytes(path.getBytes());
        buf.writeInt(data.length);
        buf.writeBytes(data);
        buf.writeBoolean(eofFlag);
    }
    public static class Handler implements IMessageHandler<MessageFileData, IMessage> {
        @Override
        public IMessage onMessage(MessageFileData message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                ScriptFileManager.getInstance().addTask(new Runnable() {
                    @Override
                    public void run() {
                        CraftTweakerSync.logger.debug("保存从服务器获取的文件"+message.path+"中");
                        ScriptFileManager.getInstance().saveFile(message.path,message.data,message.eofFlag);
                    }
                });
            }

            return null;
        }
    }
}
