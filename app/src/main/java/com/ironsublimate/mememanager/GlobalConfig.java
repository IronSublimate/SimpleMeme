package com.ironsublimate.mememanager;

import android.os.Environment;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2018/07/02
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class GlobalConfig {

    public static final String assetsFolderName = "imagehuyi";//apk内置的所有表情包目录
    public static final String storageFolderName = "expressionBaby";//所有表情包都在此目录下建立子目录
    public static final String appDirPath = Environment.getExternalStorageDirectory() + "/" + GlobalConfig.storageFolderName + "/";
    public static final String appTempDirPath = Environment.getExternalStorageDirectory() + "/" + GlobalConfig.storageFolderName + "/" + "__TEMP__" + "/";
    public static final String serverUrl = "https://www.ihewro.com/exp/";
    public static final String getDirListUrl = serverUrl + "expFolderList.php";
    public static final String getDirDetailUrl = serverUrl + "expFolderDetail.php";
    public static final String appShareOrSavePath = appDirPath + "应用默认文件夹/";//文件夹名称为



    //错误常量
    public static final String ERROR_FILE_LIMIT = "ERROR_FILE_LIMIT";
}
