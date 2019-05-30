package com.platform.assets;


import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.ravenwallet.core.BRCoreTransactionAsset;
import com.ravenwallet.core.MyTransactionAsset;

/**
 * RavenWallet
 * <p/>
 * Created by Mihail Gutan on <mihail@breadwallet.com> 6/22/17.
 * Copyright (c) 2017 breadwallet LLC
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class Asset implements Parcelable {

    private int id;
    private String name;
    private String type;
    private String txHash;
    private double amount;
    private int units;
    private int reissuable;
    private int hasIpfs;
    private String ipfsHash;
    private int ownership;
    private int sortPriority;
    private int isVisible;
//    private static int sortScore = 0;

    public Asset(String name, String type, String txHash, double amount, int units, int reissuable, int hasIpfs, String ipfsHash, int ownership, int sortPriority, int isVisible) {
        this.name = name;
        this.type = type;
        this.txHash = txHash;
        this.amount = amount;
        this.units = units;
        this.reissuable = reissuable;
        this.hasIpfs = hasIpfs;
        this.ipfsHash = ipfsHash;
        this.ownership = ownership;
        this.sortPriority = sortPriority;
        this.isVisible = isVisible;
    }

    public Asset(MyTransactionAsset transactionAsset, String txHash, int ownership,int sortPriority) {
        this.name = transactionAsset.getName();
        if (transactionAsset.getType() != null)
            this.type = transactionAsset.getType();
        this.txHash = txHash;
        this.amount = transactionAsset.getAmount();
        this.units = transactionAsset.getUnit();
        this.reissuable = transactionAsset.getReissuable();
        this.hasIpfs = transactionAsset.getHasIPFS();
        this.ipfsHash = transactionAsset.getIPFSHash();
        this.ownership = ownership;
        this.isVisible = 1;
        this.sortPriority = sortPriority;
//        sortScore++;
    }

    public Asset(BRCoreTransactionAsset transactionAsset) {
        this.name = transactionAsset.getName();
        this.type = transactionAsset.getType();
        this.amount = transactionAsset.getAmount();
        this.units = (int) transactionAsset.getUnit();
        this.reissuable = (int) transactionAsset.getReissuable();
        this.hasIpfs = (int) transactionAsset.getHasIPFS();
        this.ipfsHash = transactionAsset.getIPFSHash();
        this.ownership = 0;
        this.isVisible = 1;
    }

    public Asset(Cursor cursor) {
        this.id = cursor.getInt(0);
        this.name = cursor.getString(1);
        this.type = cursor.getString(2);
        this.txHash = cursor.getString(3);
        this.amount = cursor.getDouble(4);
        this.units = cursor.getInt(5);
        this.reissuable = cursor.getInt(6);
        this.hasIpfs = cursor.getInt(7);
        this.ipfsHash = cursor.getString(8);
        this.ownership = cursor.getInt(9);
        this.sortPriority = cursor.getInt(10);
        this.isVisible = cursor.getInt(11);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }

    public int getReissuable() {
        return reissuable;
    }

    public void setReissuable(int reissuable) {
        this.reissuable = reissuable;
    }

    public int getHasIpfs() {
        return hasIpfs;
    }

    public void setHasIpfs(int hasIpfs) {
        this.hasIpfs = hasIpfs;
    }

    public String getIpfsHash() {
        return ipfsHash;
    }

    public void setIpfsHash(String ipfsHash) {
        this.ipfsHash = ipfsHash;
    }

    public int getOwnership() {
        return ownership;
    }

    public void setOwnership(int ownership) {
        this.ownership = ownership;
    }

    public int getSortPriority() {
        return sortPriority;
    }

    public void setSortPriority(int sortPriority) {
        this.sortPriority = sortPriority;
    }

    public int getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(int isVisible) {
        this.isVisible = isVisible;
    }

//    public static int getSortScore() {
//        return sortScore;
//    }

    public BRCoreTransactionAsset getCoreAsset() {
        final BRCoreTransactionAsset coreTransactionAsset = new BRCoreTransactionAsset();
        coreTransactionAsset.setName(this.name);
        coreTransactionAsset.setNamelen(this.name.length());
        coreTransactionAsset.setUnit(this.units);
        coreTransactionAsset.setIPFSHash(this.ipfsHash);
        coreTransactionAsset.setHasIPFS(this.hasIpfs);
        coreTransactionAsset.setReissuable(this.reissuable);
        coreTransactionAsset.setAmount((long) this.amount);
        int typeIndex = 0;
        try {
            AssetType assetType = AssetType.valueOf(type);
            typeIndex = assetType.getIndex();
        } catch (Exception e) {
            e.printStackTrace();
        }
        coreTransactionAsset.setType(typeIndex);
        return coreTransactionAsset;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.type);
        dest.writeString(this.txHash);
        dest.writeDouble(this.amount);
        dest.writeInt(this.units);
        dest.writeInt(this.reissuable);
        dest.writeInt(this.hasIpfs);
        dest.writeString(this.ipfsHash);
        dest.writeInt(this.ownership);
        dest.writeInt(this.sortPriority);
        dest.writeInt(this.isVisible);
    }

    protected Asset(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.type = in.readString();
        this.txHash = in.readString();
        this.amount = in.readDouble();
        this.units = in.readInt();
        this.reissuable = in.readInt();
        this.hasIpfs = in.readInt();
        this.ipfsHash = in.readString();
        this.ownership = in.readInt();
        this.sortPriority = in.readInt();
        this.isVisible = in.readInt();
    }

    public static final Creator<Asset> CREATOR = new Creator<Asset>() {
        @Override
        public Asset createFromParcel(Parcel source) {
            return new Asset(source);
        }

        @Override
        public Asset[] newArray(int size) {
            return new Asset[size];
        }
    };
}