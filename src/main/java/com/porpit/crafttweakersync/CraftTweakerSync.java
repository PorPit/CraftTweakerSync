package com.porpit.crafttweakersync;

import com.porpit.crafttweakersync.common.CommonProxy;
import com.porpit.crafttweakersync.common.scriptdata.ScriptFileInfo;
import com.porpit.crafttweakersync.common.scriptdata.ScriptFileManager;
import com.porpit.crafttweakersync.util.FileHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod(modid = CraftTweakerSync.MODID, name = CraftTweakerSync.NAME, version = CraftTweakerSync.VERSION, dependencies="after:crafttweaker;", acceptedMinecraftVersions = "1.12.2")
public class CraftTweakerSync {
    public static final String MODID = "crafttweakersync";
    public static final String NAME = "CraftTweakerSync";
    public static final String VERSION = "1.0";

    @SidedProxy(clientSide = "com.porpit.crafttweakersync.client.ClientProxy", serverSide = "com.porpit.crafttweakersync.common.CommonProxy")
    public static CommonProxy proxy;
    @Mod.Instance(CraftTweakerSync.MODID)
    public static CraftTweakerSync instance;

    public static Logger logger;

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
        logger=event.getModLog();
        List<ScriptFileInfo> scriptFileInfoList = ScriptFileManager.getInstance().getAllScriptData();
        for(ScriptFileInfo scriptFileInfo : scriptFileInfoList){
            logger.info("读取文件:"+scriptFileInfo.getFilePath()+",MD5值:"+ scriptFileInfo.getFileMD5());
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        logger.info("CraftTweakerSync (CraftTweaker 脚本自动同步MOD) 成功加载! 作者:PorPit(泼皮)");
        logger.info("如有问题反馈泼皮QQ:692066768");
    }
}
