package com.porpit.crafttweakersync.network;

import com.porpit.crafttweakersync.CraftTweakerSync;
import com.porpit.crafttweakersync.common.scriptdata.ScriptFileInfo;
import com.porpit.crafttweakersync.common.scriptdata.ScriptFileManager;
import com.porpit.crafttweakersync.util.FileHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.*;
import java.util.logging.Logger;

public class MessageServerFileInfo implements IMessage {

    private List<ScriptFileInfo> scriptFileInfoList;

    public MessageServerFileInfo() {
    }

    public MessageServerFileInfo(List<ScriptFileInfo> scriptFileInfoList) {
        this.scriptFileInfoList = scriptFileInfoList;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            int byteLength = buf.readInt();
            byte bytes[] = new byte[byteLength];
            buf.readBytes(bytes);
            ScriptFileInfo[] scriptFileInfos = (ScriptFileInfo[]) FileHelper.getObjectFromBytes(bytes);
            scriptFileInfoList = Arrays.asList(scriptFileInfos);
        } catch (Exception e) {
            scriptFileInfoList = null;
            e.printStackTrace();
            CraftTweakerSync.logger.warn("服務器獲取信息失敗");
        }


    }

    @Override
    public void toBytes(ByteBuf buf) {
        ScriptFileInfo[] scriptFileInfos = new ScriptFileInfo[scriptFileInfoList.size()];
        scriptFileInfoList.toArray(scriptFileInfos);
        try {
            byte bytes[] = FileHelper.getBytesFromObject(scriptFileInfos);
            buf.writeInt(bytes.length);
            buf.writeBytes(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            CraftTweakerSync.logger.warn("發送數據失敗");
        }
    }

    public static class Handler implements IMessageHandler<MessageServerFileInfo, IMessage> {
        @Override
        public IMessage onMessage(MessageServerFileInfo message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                CraftTweakerSync.logger.debug("获取到服务器发送的CraftTweaker 文件信息");
                if (message.scriptFileInfoList == null) {
                    return null;
                }
                ScriptFileManager sfm = ScriptFileManager.getInstance();
                List<ScriptFileInfo> sList1 = sfm.getOutdatedFile(sfm.getAllScriptData(), message.scriptFileInfoList);
                List<ScriptFileInfo> sList2 = sfm.getNeededFile(sfm.getAllScriptData(), message.scriptFileInfoList);
                String str1 = "过时的文件:";
                String str2 = "需要更新的文件:";

                for (ScriptFileInfo s : sList1) {
                    str1 += s.getFilePath() + ",";
                }
                for (ScriptFileInfo s : sList2) {
                    str2 += s.getFilePath() + ",";
                }




                CraftTweakerSync.logger.debug(str1);
                CraftTweakerSync.logger.debug(str2);
                ScriptFileManager.getInstance().addTask(new Runnable() {
                    @Override
                    public void run() {
                        CraftTweakerSync.logger.debug("删除过时的文件中...");
                        ScriptFileManager.getInstance().deleteFileData(sList1);
                        CraftTweakerSync.logger.debug("发送文件请求中...");
                        sfm.sendScriptFileDataRequest(sList2);
                        try {
                            Thread.sleep(1000);
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        if (ScriptFileManager.getInstance().needToRestart) {
                            Minecraft.getMinecraft().ingameGUI.setOverlayMessage(new TextComponentString("§c检测到你已从服务器同步CraftTweaker配置文件,并且未重启游戏,请重启游戏以免影响正常游戏!"), false);
                            if (Minecraft.getMinecraft().player != null) {
                                Minecraft.getMinecraft().player.sendMessage((new TextComponentString("§c检测到你已从服务器同步CraftTweaker配置文件,并且未重启游戏,请重启游戏以免影响正常游戏!")));

                            }
                        } else {
                            if (sList1.size() == 0 && sList2.size() == 0) {
                                Minecraft.getMinecraft().ingameGUI.setOverlayMessage(new TextComponentString("§a检测到CraftTweaker配置文件与服务器一致!您可以正常进行游戏!"), false);
                                if (Minecraft.getMinecraft().player != null) {
                                    Minecraft.getMinecraft().player.sendMessage((new TextComponentString("§a检测到CraftTweaker配置文件与服务器一致!您可以正常进行游戏!")));
                                }
                            }
                        }
                    }
                });
            }
            return null;
        }
    }
}
