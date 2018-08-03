package com.porpit.crafttweakersync.common.scriptdata;

import com.porpit.crafttweakersync.util.FileHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

public class ScriptFileInfo implements Serializable {
    private String filePath;
    private String fileMD5;


    public ScriptFileInfo(){}
    public ScriptFileInfo(String filePath, String fileMD5) {
        this.filePath = filePath;
        this.fileMD5 = fileMD5;
    }
    public ScriptFileInfo(File file) throws Exception {

        this.filePath = file.getPath();
        this.fileMD5 = FileHelper.getMd5ByFile(file);
        //客户端只需存入路径信息
        //服务端需要文件数据以进行传输
        if(FMLCommonHandler.instance().getSide().isServer()){
            ScriptFileManager.getInstance().allScriptFiles.put(this,FileHelper.getFileBytes(file));
        }else {
            ScriptFileManager.getInstance().allScriptFiles.put(this,null);
        }

    }




    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScriptFileInfo that = (ScriptFileInfo) o;
        return Objects.equals(filePath, that.filePath) &&
                Objects.equals(fileMD5, that.fileMD5);
    }

    @Override
    public int hashCode() {

        return Objects.hash(filePath, fileMD5);
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setFileMD5(String fileMD5) {
        this.fileMD5 = fileMD5;
    }

    public String getFileMD5() {
        return this.fileMD5;
    }
}
