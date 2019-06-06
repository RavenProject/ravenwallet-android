package com.platform.addressBook;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.platform.sqlite.PlatformSqliteHelper;
import com.ravenwallet.tools.util.BRConstants;

import java.util.ArrayList;
import java.util.List;

import com.platform.sqlite.PlatformSqliteHelper.AddressBook;

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

public class AddressBookRepository {

    private final PlatformSqliteHelper dbHelper;
    private SQLiteDatabase mDatabase;

    private static AddressBookRepository instance;

    public static AddressBookRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AddressBookRepository(context);
        }
        return instance;
    }

    private AddressBookRepository(Context context) {
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

    public List<AddressBookItem> getAllAddresses() {
        List<AddressBookItem> addresses = new ArrayList<>();
        Cursor cursor = null;
        try {
            SQLiteDatabase db = getReadable();
            String selectQuery = "SELECT  * FROM " + AddressBook.TABLE_NAME;

            cursor = db.rawQuery(selectQuery, null);
            while (cursor.moveToNext()) {
                AddressBookItem address = new AddressBookItem(cursor);
                addresses.add(address);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            //closeDB();
        }
        return addresses;
    }

    public boolean insertAddress(AddressBookItem address) {
        try {
            SQLiteDatabase db = getWritable();

            ContentValues values = new ContentValues();
            values.put(AddressBook.COLUMN_NAME, address.getName());
            values.put(AddressBook.COLUMN_ADDRESS, address.getAddress());

            long n = db.insert(AddressBook.TABLE_NAME, null, values);
            return n != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deleteAll() {
        try (SQLiteDatabase db = getWritable()) {
            int count = db.delete(PlatformSqliteHelper.AddressBook.TABLE_NAME, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
