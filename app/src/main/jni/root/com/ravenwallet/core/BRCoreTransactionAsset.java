/*
 * RavenWallet
 *
 * Created by Ed Gamble <ed@breadwallet.com> on 1/31/18.
 * Copyright (c) 2018 breadwallet LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.ravenwallet.core;

public class BRCoreTransactionAsset extends BRCoreJniReference {

    public BRCoreTransactionAsset() {
        this(createJniCoreAssetEmpty());
    }

    public BRCoreTransactionAsset(long jniReferenceAddress) {
        super(jniReferenceAddress);
    }

    public native String getType();

    public native void setType(int typeIndex);

    public native String getName();

    public native int getNameLen();

    public native void setNamelen(int namelen);

    public native void setName(String name);

    public native long getAmount();

    public native void setAmount(long amount);

    public native long getUnit();

    public native void setUnit(long unit);

    public native long getReissuable();

    public native void setReissuable(long reissuable);

    public native long getHasIPFS();

    public native void setHasIPFS(long hasIPFS);

    public native String getIPFSHash();

    public native void setIPFSHash(String amount);

    public static native long createJniCoreAssetEmpty();


}
