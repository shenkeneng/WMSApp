package com.frxs.WMS.model;

import java.util.List;

/**
 * Created by Chentie on 2018/6/27.
 */

public class PackQty {


    /**
     * PickID : string
     * DeliveryTime : 2018-06-27T09:42:25.558Z
     * WPackingBoxList : [{"ID":0,"PackingBoxCode":"string","PackingBoxName":"string","VLength":0,"VWidth":0,"VHeight":0,"Volume":0,"IsTackBack":0,"Qty":0}]
     * UserID : 0
     * UserName : string
     * WID : 0
     * OpAreaID : 0
     */

    private String PickID;
    private String DeliveryTime;
    private int UserID;
    private String UserName;
    private int WID;
    private int OpAreaID;
    private List<WPackingBoxListBean> WPackingBoxList;

    //接口鉴权必传字段
    private String Token;
    private String UserAccount;
    private String Version;

    public String getToken() {
        return Token;
    }

    public void setToken(String token) {
        Token = token;
    }

    public String getUserAccount() {
        return UserAccount;
    }

    public void setUserAccount(String userAccount) {
        UserAccount = userAccount;
    }

    public String getVersion() {
        return Version;
    }

    public void setVersion(String version) {
        Version = version;
    }

    public String getPickID() {
        return PickID;
    }

    public void setPickID(String PickID) {
        this.PickID = PickID;
    }

    public String getDeliveryTime() {
        return DeliveryTime;
    }

    public void setDeliveryTime(String DeliveryTime) {
        this.DeliveryTime = DeliveryTime;
    }

    public int getUserID() {
        return UserID;
    }

    public void setUserID(int UserID) {
        this.UserID = UserID;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String UserName) {
        this.UserName = UserName;
    }

    public int getWID() {
        return WID;
    }

    public void setWID(int WID) {
        this.WID = WID;
    }

    public int getOpAreaID() {
        return OpAreaID;
    }

    public void setOpAreaID(int OpAreaID) {
        this.OpAreaID = OpAreaID;
    }

    public List<WPackingBoxListBean> getWPackingBoxList() {
        return WPackingBoxList;
    }

    public void setWPackingBoxList(List<WPackingBoxListBean> WPackingBoxList) {
        this.WPackingBoxList = WPackingBoxList;
    }

    public static class WPackingBoxListBean {
        /**
         * ID : 0
         * PackingBoxCode : string
         * PackingBoxName : string
         * VLength : 0
         * VWidth : 0
         * VHeight : 0
         * Volume : 0
         * IsTackBack : 0
         * Qty : 0
         */

        private int ID;
        private String PackingBoxCode;
        private String PackingBoxName;
        private float VLength;
        private float VWidth;
        private float VHeight;
        private float Volume;
        private int IsTackBack;
        private int Qty;

        public int getID() {
            return ID;
        }

        public void setID(int ID) {
            this.ID = ID;
        }

        public String getPackingBoxCode() {
            return PackingBoxCode;
        }

        public void setPackingBoxCode(String PackingBoxCode) {
            this.PackingBoxCode = PackingBoxCode;
        }

        public String getPackingBoxName() {
            return PackingBoxName;
        }

        public void setPackingBoxName(String PackingBoxName) {
            this.PackingBoxName = PackingBoxName;
        }

        public float getVLength() {
            return VLength;
        }

        public void setVLength(float VLength) {
            this.VLength = VLength;
        }

        public float getVWidth() {
            return VWidth;
        }

        public void setVWidth(float VWidth) {
            this.VWidth = VWidth;
        }

        public float getVHeight() {
            return VHeight;
        }

        public void setVHeight(float VHeight) {
            this.VHeight = VHeight;
        }

        public float getVolume() {
            return Volume;
        }

        public void setVolume(float Volume) {
            this.Volume = Volume;
        }

        public int getIsTackBack() {
            return IsTackBack;
        }

        public void setIsTackBack(int IsTackBack) {
            this.IsTackBack = IsTackBack;
        }

        public int getQty() {
            return Qty;
        }

        public void setQty(int Qty) {
            this.Qty = Qty;
        }
    }
}
