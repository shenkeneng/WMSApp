package com.frxs.WMS.rest.model;

import android.text.TextUtils;

/**
 * Created by ewu on 2016/3/23.
 */
public class ApiResponse<T> {
    /**
     * Flag : string
     * Info : string
     * Data : {"EmpID":0,"OpAreaID":0,"WID":0,"EmpName":"string","UserAccount":"string","UserMobile":"string","Password":"string","PasswordSalt":"string","IsLocked":0,"IsDeleted":0}
     * DataCount : 0
     */

    private String Flag;
    private String Info;
    private T Data;
    private int DataCount;

    public String getFlag() {
        return Flag;
    }

    public void setFlag(String Flag) {
        this.Flag = Flag;
    }

    public String getInfo() {
        return Info;
    }

    public void setInfo(String Info) {
        this.Info = Info;
    }

    public T getData() {
        return Data;
    }

    public void setData(T Data) {
        this.Data = Data;
    }

    public int getDataCount() {
        return DataCount;
    }

    public void setDataCount(int DataCount) {
        this.DataCount = DataCount;
    }

    public static class DataBean {
    }
    public boolean isSuccessful() {
        if (Flag.equals("SUCCESS")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isAuthenticationFailed() {
        if (!TextUtils.isEmpty(Info) && Info.equals("999999")) {
            return true;
        } else {
            return false;
        }
    }

   /* *//**
         * Flag : 0
         * FlagDescription : SUCCESS
         * CachedTime : 2018-06-05 16:49:20.635711
         * Data : {"EmpID":10488,"EmpName":"00011301","WID":202,"UserMobile":"","IsMaster":1,"IsFrozen":0,"IsLocked":0,"IsDeleted":0,"PasswordSalt":"47c2f53b205c42cf88194389b0a944d5","UserGuid":"50f34489-6ec3-4709-be1f-1f1d485e19ae","UserAccount":"00011301","UserPwd":"123456","WareHouseWID":200,"Multiple":15}
         * Info : OK
         *//*

    private int Flag;
    private String FlagDescription;
    private String CachedTime;
    private String Info;

    public int getFlag() {
        return Flag;
    }

    public void setFlag(int Flag) {
        this.Flag = Flag;
    }

    public String getFlagDescription() {
        return FlagDescription;
    }

    public void setFlagDescription(String FlagDescription) {
        this.FlagDescription = FlagDescription;
    }

    public String getCachedTime() {
        return CachedTime;
    }

    public void setCachedTime(String CachedTime) {
        this.CachedTime = CachedTime;
    }

     public String getInfo() {
        return Info;
    }

    public void setInfo(String Info) {
        this.Info = Info;
    }

    private T Data;

    public T getData() {
        return Data;
    }

    public void setData(T data) {
        Data = Data;
    }

    public boolean isSuccessful() {
        if (Flag == 0) {
            return true;
        } else {
            return false;
        }
    }*/


}
