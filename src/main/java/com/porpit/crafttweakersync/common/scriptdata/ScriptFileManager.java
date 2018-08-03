package com.porpit.crafttweakersync.common.scriptdata;

import com.porpit.crafttweakersync.CraftTweakerSync;
import com.porpit.crafttweakersync.network.MessageFileData;
import com.porpit.crafttweakersync.network.MessageFileRequest;
import com.porpit.crafttweakersync.network.NetworkLoader;
import com.porpit.crafttweakersync.util.FileHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;

import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptFileManager {

    private static ScriptFileManager instance;

    public static ScriptFileManager getInstance() {
        if (instance == null) {
            instance = new ScriptFileManager();
        }
        return instance;
    }

    public List<Runnable> tasks = new ArrayList<>();


    protected Map<ScriptFileInfo, byte[]> allScriptFiles;

    public boolean needToRestart = false;

    private ScriptFileManager() {
        FileManagerThread thread = new FileManagerThread();
        thread.start();
    }

    private class FileManagerThread extends Thread {
        @Override
        public void run() {
            while (true) {
                List<Runnable> tempTasks = new ArrayList<>(tasks);
                for (int i = 0; i < tempTasks.size(); i++) {
                    tasks.get(i).run();
                }
                tasks.removeAll(tempTasks);
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addTask(Runnable runnable) {
        tasks.add(runnable);
    }


    //Client side  target: clientside files  ref: serverside files
    public List<ScriptFileInfo> getOutdatedFile(List<ScriptFileInfo> target, List<ScriptFileInfo> ref) {
        List<ScriptFileInfo> outdatedFile = new ArrayList<>();
        for (ScriptFileInfo targetSFI : target) {
            for (int i = 0; i < ref.size(); i++) {
                if (targetSFI.equals(ref.get(i))) {
                    break;
                }
                if (i == ref.size() - 1) {
                    outdatedFile.add(targetSFI);
                }
            }
        }
        return outdatedFile;
    }

    //Client side   target: clientside files  ref: serverside files
    public List<ScriptFileInfo> getNeededFile(List<ScriptFileInfo> target, List<ScriptFileInfo> ref) {
        List<ScriptFileInfo> neededFile = new ArrayList<>(ref);
        for (ScriptFileInfo refSFI : ref) {
            for (int i = 0; i < target.size(); i++) {
                if (refSFI.equals(target.get(i))) {
                    neededFile.remove(refSFI);
                    break;
                }
            }
        }
        return neededFile;
    }

    public List<ScriptFileInfo> getAllScriptData() {
        if (allScriptFiles == null) {
            updateAllScriptData();
        }
        return new ArrayList<>(allScriptFiles.keySet());
    }

    public List<ScriptFileInfo> updateAllScriptData() {
        if (allScriptFiles == null) {
            allScriptFiles = new HashMap<>();
        }
        allScriptFiles.clear();
        ;
        File scriptFile = FileHelper.getScriptDirectory();
        return FileHelper.getAllScriptData(scriptFile);
    }

    public byte[] getScriptFileData(ScriptFileInfo fileInfo) {
        if (allScriptFiles.containsKey(fileInfo)) {
            return allScriptFiles.get(fileInfo);
        } else {
            return null;
        }
    }

    //server side
    public void sendScriptFileData(List<ScriptFileInfo> scriptFileInfoList, EntityPlayerMP playerMP) {
        for (int i = 0; i < scriptFileInfoList.size(); i++) {
            //for (ScriptFileInfo fileInfo : scriptFileInfoList) {
            byte[] bytes = getScriptFileData(scriptFileInfoList.get(i));
            if (bytes == null) {
                continue;
            }
            if (playerMP == null) {
                continue;
            }
            MessageFileData message = new MessageFileData(scriptFileInfoList.get(i).getFilePath(), bytes, i == scriptFileInfoList.size() - 1);
            NetworkLoader.instance.sendTo(message, playerMP);
        }
    }

    //client side
    public void deleteFileData(List<ScriptFileInfo> neededToDelete) {
        for (ScriptFileInfo fileInfo : neededToDelete) {
            FileHelper.deleteFile(fileInfo.getFilePath());
        }
        updateAllScriptData();
    }

    public void sendScriptFileDataRequest(List<ScriptFileInfo> scriptFileInfoList) {
        MessageFileRequest message = new MessageFileRequest(scriptFileInfoList);
        NetworkLoader.instance.sendToServer(message);
    }

    //client side
    public boolean saveFile(String path, byte[] data, boolean totalEOF) {
        boolean tempEOFFlag = false;
        try {
            FileHelper.writeBytesToFile(new File(path), data);
            needToRestart = true;
            if (totalEOF == true && FMLCommonHandler.instance().getSide().isClient()) {
                ScriptFileManager.getInstance().addTask(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            Minecraft.getMinecraft().ingameGUI.setOverlayMessage(new TextComponentString("§c已从服务器更新CraftTweaker最新文件,请重启游戏以免影响正常游戏!"), false);
                            Minecraft.getMinecraft().player.sendMessage((new TextComponentString("§c已从服务器更新CraftTweaker最新文件,请重启游戏以免影响正常游戏!")));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });

                updateAllScriptData();
            }
            tempEOFFlag = true;
        } catch (Exception e) {
            e.printStackTrace();
            CraftTweakerSync.logger.error("保存文件" + path + "时发生异常!");
        }
        return tempEOFFlag;
    }
}
