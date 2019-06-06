package com.platform.sqlite;

/**
 * RavenWallet
 * <p/>
 * Created by Mihail Gutan on <mihail@breadwallet.com> 11/8/16.
 * Copyright (c) 2016 breadwallet LLC
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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.ravenwallet.tools.util.BRConstants;

public class PlatformSqliteHelper extends SQLiteOpenHelper {
    private static final String TAG = PlatformSqliteHelper.class.getName();

    private static PlatformSqliteHelper instance;

    public static final String DATABASE_NAME = "platform.db";
    private static final int DATABASE_VERSION = 5;

    public static synchronized PlatformSqliteHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (instance == null) {
            instance = new PlatformSqliteHelper(context.getApplicationContext());
        }
        return instance;
    }

//    /**
//     * KV Store table
//     */
//    public static final String KV_STORE_TABLE_NAME = "kvStoreTable";
//    public static final String KV_VERSION = "version";
//    public static final String KV_REMOTE_VERSION = "remote_version";
//    public static final String KV_KEY = "key";
//    public static final String KV_VALUE = "value";
//    public static final String KV_TIME = "thetime";
//    public static final String KV_DELETED = "deleted";


    /**
     * Address Book Table
     */
    public static class AddressBook implements BaseColumns {
        public static final String TABLE_NAME = "AddressBook";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ADDRESS = "address";
    }

    /**
     * Owned Assets Table
     */
    public static class OwnedAsset implements BaseColumns {
        public static final String TABLE_NAME = "OwnedAsset";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_TX_HASH = "tx_hash";
        public static final String COLUMN_AMOUNT = "amount";
        public static final String COLUMN_UNITS = "units";
        public static final String COLUMN_REISSUABLE = "reissuable";
        public static final String COLUMN_HAS_IPFS = "has_ipfs";
        public static final String COLUMN_IPFS_HASH = "ipfs_hash";
        public static final String COLUMN_OWNERSHIP = "ownership";
        public static final String COLUMN_SORT_PRIORITY = "sort_priority";
        public static final String COLUMN_VISIBLE = "visible";
    }


//    private static final String KV_DATABASE_CREATE = String.format("CREATE TABLE IF NOT EXISTS %s(" +
//            "   %s         INTEGER  NOT NULL, " +
//            "   %s  INTEGER  NOT NULL DEFAULT 0, " +
//            "   %s             TEXT    NOT NULL, " +
//            "   %s           BLOB    NOT NULL, " +
//            "   %s         INTEGER  NOT NULL, " + // server unix timestamp in MS
//            "   %s         INTEGER    NOT NULL, " +
//            "   PRIMARY KEY (%s, %s) " +
//            ");", KV_STORE_TABLE_NAME, KV_VERSION, KV_REMOTE_VERSION, KV_KEY, KV_VALUE, KV_TIME, KV_DELETED, KV_KEY, KV_VERSION);

    private static final String CREATE_TABLE_ADDRESS_BOOK = String.format("CREATE TABLE IF NOT EXISTS %s(" +
            "   %s         INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "   %s             TEXT    NOT NULL, " +
            "   %s             TEXT    NOT NULL " +
            ");", AddressBook.TABLE_NAME, AddressBook._ID, AddressBook.COLUMN_NAME, AddressBook.COLUMN_ADDRESS);

    private static final String CREATE_TABLE_OWNED_ASSET = String.format("CREATE TABLE IF NOT EXISTS %s(" +
            "   %s         INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "   %s             TEXT    NOT NULL, "    +
            "   %s             TEXT    NOT NULL, "    +
            "   %s             TEXT    NOT NULL, "    +
            "   %s             DOUBLE     NOT NULL, " +
            "   %s             INTEGER    NOT NULL, " +
            "   %s             INTEGER    NOT NULL, " +
            "   %s             INTEGER    NOT NULL, " +
            "   %s             TEXT               , " +
            "   %s             INTEGER    NOT NULL, " +
            "   %s             INTEGER    NOT NULL, " +
            "   %s             INTEGER    NOT NULL  " +
            ");",
            OwnedAsset.TABLE_NAME,
            OwnedAsset._ID,
            OwnedAsset.COLUMN_NAME,
            OwnedAsset.COLUMN_TYPE,
            OwnedAsset.COLUMN_TX_HASH,
            OwnedAsset.COLUMN_AMOUNT,
            OwnedAsset.COLUMN_UNITS,
            OwnedAsset.COLUMN_REISSUABLE,
            OwnedAsset.COLUMN_HAS_IPFS,
            OwnedAsset.COLUMN_IPFS_HASH,
            OwnedAsset.COLUMN_OWNERSHIP,
            OwnedAsset.COLUMN_SORT_PRIORITY,
            OwnedAsset.COLUMN_VISIBLE
    );

    private PlatformSqliteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setWriteAheadLoggingEnabled(BRConstants.WAL);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
//        Log.d(TAG, "onCreate: " + KV_DATABASE_CREATE);
//        // Creating the KV Store table
//        database.execSQL(KV_DATABASE_CREATE);

        // Creating the Address Book table
        database.execSQL(CREATE_TABLE_ADDRESS_BOOK);

        // Creating the Assets table
        database.execSQL(CREATE_TABLE_OWNED_ASSET);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + OwnedAsset.TABLE_NAME);
        //recreate the dbs
        onCreate(db);
    }
}
