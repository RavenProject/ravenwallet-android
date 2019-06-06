package com.ravenwallet.tools.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.ravenwallet.tools.util.TypesConverter;

public class ApiUTxo {

//    byte[] txid = TypesConverter.hexToBytesReverse(obj.getString("txid"));
//    int vout = obj.getInt("vout");
//    byte[] scriptPubKey = TypesConverter.hexToBytes(obj.getString("scriptPubKey"));
//    long amount = obj.getLong("satoshis");

    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("txid")
    @Expose
    private String txid;
    @SerializedName("vout")
    @Expose
    private int vout;
    @SerializedName("scriptPubKey")
    @Expose
    private String scriptPubKey;
    @SerializedName("assetName")
    @Expose
    private String assetName;
    //    @SerializedName("amount")
//    @Expose
//    private double amount;
    @SerializedName("satoshis")
    @Expose
    private long satoshis;
    //    @SerializedName("height")
//    @Expose
//    private Integer height;
//    @SerializedName("confirmations")
//    @Expose
//    private Integer confirmations;
    @SerializedName("secretKey")
    @Expose
    private byte[] secretKey;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public int getVout() {
        return vout;
    }

    public void setVout(int vout) {
        this.vout = vout;
    }

    public String getScriptPubKey() {
        return scriptPubKey;
    }

    public void setScriptPubKey(String scriptPubKey) {
        this.scriptPubKey = scriptPubKey;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

//    public double getAmount() {
//        return amount;
//    }
//
//    public void setAmount(double amount) {
//        this.amount = amount;
//    }

    public long getSatoshis() {
        return satoshis;
    }

    public void setSatoshis(long satoshis) {
        this.satoshis = satoshis;
    }

//    public Integer getHeight() {
//        return height;
//    }
//
//    public void setHeight(Integer height) {
//        this.height = height;
//    }
//
//    public Integer getConfirmations() {
//        return confirmations;
//    }
//
//    public void setConfirmations(Integer confirmations) {
//        this.confirmations = confirmations;
//    }


    public byte[] getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(byte[] secretKey) {
        this.secretKey = secretKey;
    }
}