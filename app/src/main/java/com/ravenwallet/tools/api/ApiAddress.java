package com.ravenwallet.tools.api;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ApiAddress {

    @SerializedName("addrStr")
    @Expose
    private String addrStr;
//    @SerializedName("balance")
//    @Expose
//    private Integer balance;
//    @SerializedName("balanceSat")
//    @Expose
//    private Integer balanceSat;
//    @SerializedName("totalReceived")
//    @Expose
//    private Integer totalReceived;
//    @SerializedName("totalReceivedSat")
//    @Expose
//    private Integer totalReceivedSat;
//    @SerializedName("totalSent")
//    @Expose
//    private Integer totalSent;
//    @SerializedName("totalSentSat")
//    @Expose
//    private Integer totalSentSat;
//    @SerializedName("unconfirmedBalance")
//    @Expose
//    private Integer unconfirmedBalance;
//    @SerializedName("unconfirmedBalanceSat")
//    @Expose
//    private Integer unconfirmedBalanceSat;
//    @SerializedName("unconfirmedTxApperances")
//    @Expose
//    private Integer unconfirmedTxApperances;
//    @SerializedName("txApperances")
//    @Expose
//    private Integer txApperances;
    @SerializedName("transactions")
    @Expose
    private List<String> transactions = null;

//    public String getAddrStr() {
//        return addrStr;
//    }
//
//    public void setAddrStr(String addrStr) {
//        this.addrStr = addrStr;
//    }
//
//    public Integer getBalance() {
//        return balance;
//    }
//
//    public void setBalance(Integer balance) {
//        this.balance = balance;
//    }
//
//    public Integer getBalanceSat() {
//        return balanceSat;
//    }
//
//    public void setBalanceSat(Integer balanceSat) {
//        this.balanceSat = balanceSat;
//    }
//
//    public Integer getTotalReceived() {
//        return totalReceived;
//    }
//
//    public void setTotalReceived(Integer totalReceived) {
//        this.totalReceived = totalReceived;
//    }
//
//    public Integer getTotalReceivedSat() {
//        return totalReceivedSat;
//    }
//
//    public void setTotalReceivedSat(Integer totalReceivedSat) {
//        this.totalReceivedSat = totalReceivedSat;
//    }
//
//    public Integer getTotalSent() {
//        return totalSent;
//    }
//
//    public void setTotalSent(Integer totalSent) {
//        this.totalSent = totalSent;
//    }
//
//    public Integer getTotalSentSat() {
//        return totalSentSat;
//    }
//
//    public void setTotalSentSat(Integer totalSentSat) {
//        this.totalSentSat = totalSentSat;
//    }
//
//    public Integer getUnconfirmedBalance() {
//        return unconfirmedBalance;
//    }
//
//    public void setUnconfirmedBalance(Integer unconfirmedBalance) {
//        this.unconfirmedBalance = unconfirmedBalance;
//    }
//
//    public Integer getUnconfirmedBalanceSat() {
//        return unconfirmedBalanceSat;
//    }
//
//    public void setUnconfirmedBalanceSat(Integer unconfirmedBalanceSat) {
//        this.unconfirmedBalanceSat = unconfirmedBalanceSat;
//    }
//
//    public Integer getUnconfirmedTxApperances() {
//        return unconfirmedTxApperances;
//    }
//
//    public void setUnconfirmedTxApperances(Integer unconfirmedTxApperances) {
//        this.unconfirmedTxApperances = unconfirmedTxApperances;
//    }
//
//    public Integer getTxApperances() {
//        return txApperances;
//    }
//
//    public void setTxApperances(Integer txApperances) {
//        this.txApperances = txApperances;
//    }

    public List<String> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<String> transactions) {
        this.transactions = transactions;
    }

}