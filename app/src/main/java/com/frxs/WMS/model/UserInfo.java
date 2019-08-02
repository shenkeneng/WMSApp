package com.frxs.WMS.model;

import java.io.Serializable;

/**
 * <pre>
 *     author : ewu
 *     e-mail : xxx@xx
 *     time   : 2017/06/07
 *     desc   : xxxx描述
 *     version: 1.0
 * </pre>
 */
public class UserInfo implements Serializable {


    /**
     * EmpID : 0
     * OpAreaID : 0
     * WID : 0
     * EmpName : string
     * UserAccount : string
     * UserMobile : string
     * Password : string
     * PasswordSalt : string
     * IsLocked : 0
     * IsDeleted : 0
     */

    private int EmpID;
    private int OpAreaID;
    private int WID;
    private String EmpName;
    private String UserAccount;
    private String UserMobile;
    private String Password;
    private String PasswordSalt;
    private int IsLocked;
    private int IsDeleted;
    private String OpAreaName;
    private String WName;
    private String Token;

    public int getEmpID() {
        return EmpID;
    }

    public void setEmpID(int EmpID) {
        this.EmpID = EmpID;
    }

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

    public String getEmpName() {
        return EmpName;
    }

    public void setEmpName(String EmpName) {
        this.EmpName = EmpName;
    }

    public String getUserAccount() {
        return UserAccount;
    }

    public void setUserAccount(String UserAccount) {
        this.UserAccount = UserAccount;
    }

    public String getUserMobile() {
        return UserMobile;
    }

    public void setUserMobile(String UserMobile) {
        this.UserMobile = UserMobile;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String Password) {
        this.Password = Password;
    }

    public String getPasswordSalt() {
        return PasswordSalt;
    }

    public void setPasswordSalt(String PasswordSalt) {
        this.PasswordSalt = PasswordSalt;
    }

    public int getIsLocked() {
        return IsLocked;
    }

    public void setIsLocked(int IsLocked) {
        this.IsLocked = IsLocked;
    }

    public int getIsDeleted() {
        return IsDeleted;
    }

    public void setIsDeleted(int IsDeleted) {
        this.IsDeleted = IsDeleted;
    }

    public String getOpAreaName() {
        return OpAreaName;
    }

    public void setOpAreaName(String opAreaName) {
        OpAreaName = opAreaName;
    }

    public String getWName() {
        return WName;
    }

    public void setWName(String WName) {
        this.WName = WName;
    }

    public String getToken() {
        return Token;
    }

    public void setToken(String token) {
        Token = token;
    }
}
