package com.platform.assets;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.platform.sqlite.PlatformSqliteHelper;
import com.platform.sqlite.PlatformSqliteHelper.OwnedAsset;
import com.ravenwallet.core.BRCoreTransactionAsset;
import com.ravenwallet.presenter.AssetChangeListener;
import com.ravenwallet.tools.threads.executor.BRExecutor;
import com.ravenwallet.tools.util.BRConstants;

import java.util.ArrayList;
import java.util.List;

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

public class AssetsRepository {

    private final PlatformSqliteHelper dbHelper;
    private SQLiteDatabase mDatabase;
    private List<AssetChangeListener> listeners = new ArrayList<>();
    private static AssetsRepository instance;

    public static AssetsRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AssetsRepository(context);
        }
        return instance;
    }

    public void addListener(AssetChangeListener assetChangeListener) {
        if (listeners == null) listeners = new ArrayList<>();
        if (assetChangeListener != null)
            listeners.add(assetChangeListener);
    }

    private AssetsRepository(Context context) {
        dbHelper = PlatformSqliteHelper.getInstance(context);
    }

    private SQLiteDatabase getWritable() {
        if (mDatabase == null || !mDatabase.isOpen()) {
            mDatabase = dbHelper.getWritableDatabase();
        }
        dbHelper.setWriteAheadLoggingEnabled(BRConstants.WAL);

        return mDatabase;
    }

    private SQLiteDatabase getReadable() {
        return getWritable();
    }

    public List<Asset> getAllAssets() {
        List<Asset> assets = new ArrayList<>();
        String selectQuery = String.format("SELECT * FROM %s ORDER BY %s DESC",
                OwnedAsset.TABLE_NAME,
                OwnedAsset.COLUMN_SORT_PRIORITY
        );
        SQLiteDatabase db = getReadable();
        try (Cursor cursor = db.rawQuery(selectQuery, null)) {

            while (cursor.moveToNext()) {
                Asset asset = new Asset(cursor);
                assets.add(asset);
                Log.d("AssetRepository", asset.getName() + " priority " + asset.getSortPriority());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return assets;
    }

    public List<Asset> getVisibleAssets() {
        List<Asset> assets = new ArrayList<>();
        String selectQuery = String.format("SELECT * FROM %s WHERE %s = %s ORDER BY %s DESC",
                OwnedAsset.TABLE_NAME,
                OwnedAsset.COLUMN_VISIBLE,
                "1",
                OwnedAsset.COLUMN_SORT_PRIORITY
        );
        SQLiteDatabase db = getReadable();
        try (Cursor cursor = db.rawQuery(selectQuery, null)) {

            while (cursor.moveToNext()) {
                Asset asset = new Asset(cursor);
                assets.add(asset);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return assets;
    }

    public boolean checkNameAssetExist(String name) {
        boolean nameExist = false;
        String selectQuery = String.format("SELECT * FROM %s WHERE %s = %s ORDER BY %s ASC",
                OwnedAsset.TABLE_NAME,
                OwnedAsset.COLUMN_NAME,
                "'" + name + "'",
                OwnedAsset.COLUMN_SORT_PRIORITY
        );
        SQLiteDatabase db = getReadable();
        try (Cursor cursor = db.rawQuery(selectQuery, null)) {
            nameExist = cursor != null && cursor.moveToFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nameExist;
    }

    public boolean updateAssetVisibility(Asset asset) {
        ContentValues values = new ContentValues();
        values.put(OwnedAsset.COLUMN_VISIBLE, asset.getIsVisible());

        try (SQLiteDatabase db = getWritable()) {
            int n = db.update(OwnedAsset.TABLE_NAME, values, OwnedAsset._ID + " = ?", new String[]{String.valueOf(asset.getID())});
            notifyListeners();
            return n != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void notifyListeners() {
        if (listeners != null) {
            for (final AssetChangeListener listener : listeners)
                if (listener != null) {
                    BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            listener.onChange();
                        }
                    });
                }
        }
    }

    public boolean updateAssetPriority(Asset asset) {
        ContentValues values = new ContentValues();
        values.put(OwnedAsset.COLUMN_SORT_PRIORITY, asset.getSortPriority());
//        values.put(OwnedAsset._ID, asset.getID());

        try (SQLiteDatabase db = getWritable()) {

            int n = db.update(OwnedAsset.TABLE_NAME, values, OwnedAsset._ID + " = ?", new String[]{String.valueOf(asset.getID())});
            notifyListeners();
            return n != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertAsset(Asset asset) {
        ContentValues values = getContentValues(asset);
        Log.d("AssetRepository", asset.getName() + " priority " + asset.getSortPriority());
        try (SQLiteDatabase db = getWritable()) {

            long n = db.insert(OwnedAsset.TABLE_NAME, null, values);
            notifyListeners();
            return n != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @NonNull
    private ContentValues getContentValues(Asset asset) {
        ContentValues values = new ContentValues();
        values.put(OwnedAsset.COLUMN_NAME, asset.getName());
        values.put(OwnedAsset.COLUMN_TYPE, asset.getType());
        values.put(OwnedAsset.COLUMN_TX_HASH, asset.getTxHash());
        values.put(OwnedAsset.COLUMN_AMOUNT, asset.getAmount());
        values.put(OwnedAsset.COLUMN_UNITS, asset.getUnits());
        values.put(OwnedAsset.COLUMN_REISSUABLE, asset.getReissuable());
        values.put(OwnedAsset.COLUMN_HAS_IPFS, asset.getHasIpfs());
        values.put(OwnedAsset.COLUMN_IPFS_HASH, asset.getIpfsHash());
        values.put(OwnedAsset.COLUMN_OWNERSHIP, asset.getOwnership());
        values.put(OwnedAsset.COLUMN_SORT_PRIORITY, asset.getSortPriority());
        values.put(OwnedAsset.COLUMN_VISIBLE, asset.getIsVisible());
        return values;
    }

    public Asset getAssetByTxHash(String txHash) {
        Asset asset = null;
        String selectQuery = String.format("SELECT * FROM %s WHERE %s = %s",
                OwnedAsset.TABLE_NAME,
                OwnedAsset.COLUMN_TX_HASH,
                "'" + txHash + "'"
        );
        SQLiteDatabase db = getReadable();
        try (Cursor cursor = db.rawQuery(selectQuery, null)) {
            if (cursor != null && cursor.moveToFirst())
                asset = new Asset(cursor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return asset;
    }

    public Asset getAsset(String name) {
        Asset asset = null;
        String selectQuery = String.format("SELECT * FROM %s WHERE %s = %s",
                OwnedAsset.TABLE_NAME,
                OwnedAsset.COLUMN_NAME,
                "'" + name + "'"
        );
        SQLiteDatabase db = getReadable();
        try (Cursor cursor = db.rawQuery(selectQuery, null)) {
            if (cursor != null && cursor.moveToFirst())
                asset = new Asset(cursor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return asset;
    }

    public boolean updateAsset(Asset asset) {
        Log.d(AssetsRepository.class.getName(),"updateAsset called");
        ContentValues values = getContentValues(asset);
        String assetName = asset.getName();
        return updateAssetByName(values, assetName);
    }

    private boolean updateAssetByName(ContentValues values, String assetName) {
        try (SQLiteDatabase db = getWritable()) {
            long n = db.update(OwnedAsset.TABLE_NAME, values,
                    OwnedAsset.COLUMN_NAME + " = '" + assetName + "'", null);
            notifyListeners();
            return n != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateOwnerShip(String assetName, int i) {
        ContentValues values = new ContentValues();
        values.put(OwnedAsset.COLUMN_OWNERSHIP, i);
        updateAssetByName(values, assetName);
    }

    public void updateAmount(String assetName, double newAmount) {
        ContentValues values = new ContentValues();
        values.put(OwnedAsset.COLUMN_AMOUNT, newAmount);
        updateAssetByName(values, assetName);
    }

    public void deleteAsset(String assetName) {
        if (TextUtils.isEmpty(assetName)) return;
        try (SQLiteDatabase db = getWritable()) {
            db.delete(OwnedAsset.TABLE_NAME, OwnedAsset.COLUMN_NAME + " = '" + assetName + "'", null);
            notifyListeners();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getAssetCount() {
        long count = DatabaseUtils.queryNumEntries(getReadable(), OwnedAsset.TABLE_NAME);

       /* String selectQuery = String.format("SELECT COUNT (*) FROM %s",
                OwnedAsset.TABLE_NAME);
        SQLiteDatabase db = getReadable();
        try (Cursor cursor = db.rawQuery(selectQuery, null)) {
            cursor.moveToNext();
            count = cursor.getCount();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return (int) count;
    }

    public void deleteAllAssets() {
        try (SQLiteDatabase db = getWritable()) {
            int count = db.delete(OwnedAsset.TABLE_NAME, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeListener(AssetChangeListener listener) {
        if (listeners != null && listener != null)
            listeners.remove(listener);
    }

    public void updateAssetData(BRCoreTransactionAsset asset) {
        ContentValues values = new ContentValues();
        values.put(OwnedAsset.COLUMN_HAS_IPFS, asset.getHasIPFS());
        values.put(OwnedAsset.COLUMN_IPFS_HASH, asset.getIPFSHash());
        values.put(OwnedAsset.COLUMN_REISSUABLE, asset.getReissuable());
        values.put(OwnedAsset.COLUMN_UNITS, asset.getUnit());
        updateAssetByName(values, asset.getName());
    }
}
