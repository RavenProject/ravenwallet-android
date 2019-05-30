package com.ravenwallet.core;

public class MyTransactionAsset {

    private String type;
    private String name;
    private double amount;
    private int unit;
    private int reissuable;
    private int hasIPFS;
    private String IPFSHash;

    public MyTransactionAsset(String type, String name, double amount, int unit, int reissuable, int hasIPFS, String IPFSHash) {
        this.type = type;
        this.name = name;
        this.amount = amount;
        this.unit = unit;
        this.reissuable = reissuable;
        this.hasIPFS = hasIPFS;
        this.IPFSHash = IPFSHash;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getUnit() {
        return unit;
    }

    public void setUnit(int unit) {
        this.unit = unit;
    }

    public int getReissuable() {
        return reissuable;
    }

    public void setReissuable(int reissuable) {
        this.reissuable = reissuable;
    }

    public int getHasIPFS() {
        return hasIPFS;
    }

    public void setHasIPFS(int hasIPFS) {
        this.hasIPFS = hasIPFS;
    }

    public String getIPFSHash() {
        return IPFSHash;
    }

    public void setIPFSHash(String IPFSHash) {
        this.IPFSHash = IPFSHash;
    }
}
