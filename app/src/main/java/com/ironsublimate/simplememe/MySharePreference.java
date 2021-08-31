package com.ironsublimate.simplememe;

import android.content.ContentValues;

import com.ironsublimate.simplememe.bean.UserPreference;

import org.litepal.LitePal;

import java.util.List;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2018/07/09
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class MySharePreference {


    /**
     * 返回用户使用某种新功能的情况，1表示已经用过了，0表示没用过
     * @param key
     * @return
     */
    public static int getUserUsedStatus(String key){
        List<UserPreference> userPreferenceList = LitePal.findAll(UserPreference.class);
        UserPreference currentUserPreference = null;
        UserPreference newUserPreference = null;

        int status;
        boolean flag = false;


        if (userPreferenceList.size() == 1){
            currentUserPreference = userPreferenceList.get(0);
            flag = true;
        }else {
            flag = false;
            newUserPreference = new UserPreference();
        }


        switch (key){
            case "isAddNew" :
                if (flag){
                    status =  currentUserPreference.getIsAddNew();
                    ContentValues values = new ContentValues();
                    values.put("isAddNew", "1");
                    LitePal.update(UserPreference.class, values, currentUserPreference.getId());
                }else {
                    status = 0;
                    newUserPreference.setIsAddNew(1);
                    newUserPreference.save();
                }
                break;

            case "isFirstEnter":
                if (flag){
                    status =  currentUserPreference.getIsFirstEnter();
                    ContentValues values = new ContentValues();
                    values.put("isFirstEnter", "1");
                    LitePal.update(UserPreference.class, values, currentUserPreference.getId());
                }else {
                    status = 0;
                    newUserPreference.setIsFirstEnter(1);
                    newUserPreference.save();
                }
                break;

            case "isOpenTheImageDialog":
                if (flag){
                    status =  currentUserPreference.getIsOpenTheImageDialog();
                    ContentValues values = new ContentValues();
                    values.put("isOpenTheImageDialog", "1");
                    LitePal.update(UserPreference.class, values, currentUserPreference.getId());
                }else {
                    status = 0;
                    newUserPreference.setIsOpenTheImageDialog(1);
                    newUserPreference.save();
                }
                break;

            case "isSaveImage":
                if (flag){
                    status =  currentUserPreference.getIsSaveImage();
                    ContentValues values = new ContentValues();
                    values.put("isSaveImage", "1");
                    LitePal.update(UserPreference.class, values, currentUserPreference.getId());
                }else {
                    status = 0;
                    newUserPreference.setIsSaveImage(1);
                    newUserPreference.save();
                }
                break;

            case "isDeleteImage":
                if (flag){
                    status =  currentUserPreference.getIsDeleteImage();
                    ContentValues values = new ContentValues();
                    values.put("isDeleteImage", "1");
                    LitePal.update(UserPreference.class, values, currentUserPreference.getId());
                }else {
                    status = 0;
                    newUserPreference.setIsDeleteImage(1);
                    newUserPreference.save();
                }
                break;

            case "isImageDes":
                if (flag){
                    status =  currentUserPreference.getIsImageDes();
                    ContentValues values = new ContentValues();
                    values.put("isImageDes", "1");
                    LitePal.update(UserPreference.class, values, currentUserPreference.getId());
                }else {
                    status = 0;
                    newUserPreference.setIsImageDes(1);
                    newUserPreference.save();
                }
                break;

            default:
                status =  0;
                break;
        }




        return status;
    }

    /**
     * 设置用户使用某种新功能的情况
     */
    private static void setUserUsedStatus(){

    }
}
