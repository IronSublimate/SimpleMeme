package com.ironsublimate.mememanager.bean;

import org.litepal.crud.LitePalSupport;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2018/08/27
 *     desc   : 用户偏好信息的表
 *     version: 1.0
 * </pre>
 */
public class UserPreference  extends LitePalSupport {
    private int id;//主键
    private int isFirstEnter;//首次进入该应用，0表示是
    private int isAddNew;//是否已经点击过新建表情包
    private int isOpenTheImageDialog;//是否打开过表情弹窗
    private int isSaveImage;//显示图片按钮
    private int isDeleteImage;//删除图片按钮
    private int isImageDes;//图片描述区域

    public UserPreference() {
    }

    public UserPreference(int isFirstEnter, int isAddNew) {
        this.isFirstEnter = isFirstEnter;
        this.isAddNew = isAddNew;
    }

    public UserPreference(int isAddNew) {
        this.isAddNew = isAddNew;
    }

    public int getIsFirstEnter() {
        return isFirstEnter;
    }

    public void setIsFirstEnter(int isFirstEnter) {
        this.isFirstEnter = isFirstEnter;
    }

    public int getIsAddNew() {
        return isAddNew;
    }

    public void setIsAddNew(int isAddNew) {
        this.isAddNew = isAddNew;
    }

    public int getIsOpenTheImageDialog() {
        return isOpenTheImageDialog;
    }

    public void setIsOpenTheImageDialog(int isOpenTheImageDialog) {
        this.isOpenTheImageDialog = isOpenTheImageDialog;
    }

    public int getIsSaveImage() {
        return isSaveImage;
    }

    public void setIsSaveImage(int isSaveImage) {
        this.isSaveImage = isSaveImage;
    }

    public int getIsDeleteImage() {
        return isDeleteImage;
    }

    public void setIsDeleteImage(int isDeleteImage) {
        this.isDeleteImage = isDeleteImage;
    }

    public int getIsImageDes() {
        return isImageDes;
    }

    public void setIsImageDes(int isImageDes) {
        this.isImageDes = isImageDes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
