package com.frxs.WMS.model;

import java.io.Serializable;
import java.util.List;

/**
 * <pre>
 *     author : ewu
 *     e-mail : xxx@xx
 *     time   : 2017/08/30
 *     desc   : xxxx描述
 *     version: 1.0
 * </pre>
 */
public class OrderBean implements Serializable {

    /**
     * OpAreaID : 0
     * WID : 0
     * StoreID : 0
     * StoreNo : string
     * StoreName : string
     * SendCardNo : string
     * DeliveryTime : 2018-06-27T02:15:24.049Z
     * PickQty : 0
     * ShelfAreaCode : string
     * ShelfAreaName : string
     * PreAssembleCode : string
     * PreAssembleName : string
     * PickStatus : string
     * DeliveryID : string
     * ShelfAreaNameList : ["string"]
     */

    private int OpAreaID;
    private int WID;
    private String StoreID;
    private String StoreNo;
    private String StoreName;
    private String SendCardNo;
    private String DeliveryTime;
    private int PickQty;
    private String ShelfAreaCode;
    private String ShelfAreaName;
    private String PreAssembleCode;
    private String PreAssembleName;
    private String PickStatus;
    private String DeliveryID;
    private List<String> ShelfAreaNameList;
    private boolean IsPrintedDelivery;
    private List<PickingOrderPackingListBean> PickingOrderPackingList;
    private String PGroupName;


    public int getOpAreaID() {
        return OpAreaID;
    }

    public void setOpAreaID(int OpAreaID) {
        this.OpAreaID = OpAreaID;
    }

    public int getWID() {
        return WID;
    }

    public void setWID(int WID) {
        this.WID = WID;
    }

    public String getStoreID() {
        return StoreID;
    }

    public void setStoreID(String StoreID) {
        this.StoreID = StoreID;
    }

    public String getStoreNo() {
        return StoreNo;
    }

    public void setStoreNo(String StoreNo) {
        this.StoreNo = StoreNo;
    }

    public String getStoreName() {
        return StoreName;
    }

    public void setStoreName(String StoreName) {
        this.StoreName = StoreName;
    }

    public String getSendCardNo() {
        return SendCardNo;
    }

    public void setSendCardNo(String SendCardNo) {
        this.SendCardNo = SendCardNo;
    }

    public String getDeliveryTime() {
        return DeliveryTime;
    }

    public void setDeliveryTime(String DeliveryTime) {
        this.DeliveryTime = DeliveryTime;
    }

    public int getPickQty() {
        return PickQty;
    }

    public void setPickQty(int PickQty) {
        this.PickQty = PickQty;
    }

    public String getShelfAreaCode() {
        return ShelfAreaCode;
    }

    public void setShelfAreaCode(String ShelfAreaCode) {
        this.ShelfAreaCode = ShelfAreaCode;
    }

    public String getShelfAreaName() {
        return ShelfAreaName;
    }

    public void setShelfAreaName(String ShelfAreaName) {
        this.ShelfAreaName = ShelfAreaName;
    }

    public String getPreAssembleCode() {
        return PreAssembleCode;
    }

    public void setPreAssembleCode(String PreAssembleCode) {
        this.PreAssembleCode = PreAssembleCode;
    }

    public String getPreAssembleName() {
        return PreAssembleName;
    }

    public void setPreAssembleName(String PreAssembleName) {
        this.PreAssembleName = PreAssembleName;
    }

    public String getPickStatus() {
        return PickStatus;
    }

    public void setPickStatus(String PickStatus) {
        this.PickStatus = PickStatus;
    }

    public String getDeliveryID() {
        return DeliveryID;
    }

    public void setDeliveryID(String DeliveryID) {
        this.DeliveryID = DeliveryID;
    }

    public String getPGroupName() {
        return PGroupName;
    }

    public void setPGroupName(String PGroupName) {
        this.PGroupName = PGroupName;
    }

    public List<String> getShelfAreaNameList() {
        return ShelfAreaNameList;
    }

    public void setShelfAreaNameList(List<String> ShelfAreaNameList) {
        this.ShelfAreaNameList = ShelfAreaNameList;
    }

    public List<PickingOrderPackingListBean> getPickingOrderPackingList() {
        return PickingOrderPackingList;
    }

    public void setPickingOrderPackingList(List<PickingOrderPackingListBean> PickingOrderPackingList) {
        this.PickingOrderPackingList = PickingOrderPackingList;
    }

    public class PickingOrderPackingListBean extends PackingBoxBean {
        /**
         * PackingID : 1
         * PickID : PK101100019553
         * PackingBoxCode : 00001
         * PackingBoxName : 周转箱
         * Volume : 100
         * IsTackBack : 0
         * Qty : 0
         */

        private int PackingID;
        private String PickID;

        public int getPackingID() {
            return PackingID;
        }

        public void setPackingID(int PackingID) {
            this.PackingID = PackingID;
        }

        public String getPickID() {
            return PickID;
        }

        public void setPickID(String PickID) {
            this.PickID = PickID;
        }

    }

    public boolean isPrintedDelivery() {
        return IsPrintedDelivery;
    }

    public void setPrintedDelivery(boolean printedDelivery) {
        IsPrintedDelivery = printedDelivery;
    }
}
