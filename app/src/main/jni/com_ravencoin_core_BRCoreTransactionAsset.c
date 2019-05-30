//  Created by Ed Gamble on 1/31/2018
//  Copyright (c) 2018 ravenwallet LLC.
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy
//  of this software and associated documentation files (the "Software"), to deal
//  in the Software without restriction, including without limitation the rights
//  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//  copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:
//
//  The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//  THE SOFTWARE.

#include "BRCoreJni.h"
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <malloc.h>
#include <BRTransaction.h>
#include "BRAssets.h"
#include <core/BRTransaction.h>
//#include "com_ravencoin_core_BRCoreTransactionAsset.h"

/*
 * Class:     com_ravencoin_core_BRCoreTransactionAsset
 * Method:    getType
 * Signature: ()Ljava/lang/String;
 */
//JNIEXPORT jobject JNICALL
//Java_com_ravenwallet_core_BRCoreTransactionAsset_getType
//        (JNIEnv *env, jobject thisObject) {
//
//    BRAsset *asset = (BRAsset *) getJNIReference (env, thisObject);
//    //jclass cls = (*env)->FindClass(env, "com/ravenwallet/core/BRCoreTransactionAsset$BRAssetType");
//    jclass cls = (*env)->GetObjectClass(env, thisObject);
//
//    jfieldID field;
//    if (asset->type == NEW_ASSET) {
//        //field = (*env)->GetStaticFieldID(env, cls , "NEW_ASSET", "Lcom/ravenwallet/core/BRCoreTransactionAsset$BRAssetType");
//        field = (*env)->GetStaticFieldID(env, cls , "NEW_ASSET", "Ljava/util/Enumeration");
//    } else {
//        field = (*env)->GetStaticFieldID(env, cls , "TRANSFER", "Ljava/util/Enumeration");
//    }
//
//    jobject assetType = (*env)->GetStaticObjectField(env, cls, field);
//
//    return assetType;
//}

JNIEXPORT jstring JNICALL
Java_com_ravenwallet_core_BRCoreTransactionAsset_getType
        (JNIEnv *env, jobject thisObject) {

    BRAsset *asset = (BRAsset *) getJNIReference(env, thisObject);

    char *type = GetAssetScriptType(asset->type);
//    size_t assetTypeLen = sizeof(type);
//    char assetType[1 + assetTypeLen];
//    memcpy(assetType, type, assetTypeLen);
//    assetType[assetTypeLen] = '\0';

    return (*env)->NewStringUTF(env, type);
}

/*
 * Class:     com_ravencoin_core_BRCoreTransactionAsset
 * Method:    setType
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_com_ravenwallet_core_BRCoreTransactionAsset_setType
        (JNIEnv *env, jobject thisObject, jint typeIndex) {
    BRAsset *asset = (BRAsset *) getJNIReference(env, thisObject);
//    jclass assetType_cls = (*env)->FindClass(env, "Lcom/platform/assets/AssetType");
//    jmethodID assetTypeGetValueMethod = (*env)->GetMethodID(env,
//                                                            (*env)->FindClass(env, assetType_cls),
//                                                            "ordinal",
//                                                            "()Lcom/platform/assets/AssetType;");
//    jstring value = (*env)->CallIntMethod(env, typeObject, assetTypeGetValueMethod);

    switch (typeIndex) {
        case 0:
            asset->type = NEW_ASSET;
            break;
        case 1:
            asset->type = TRANSFER;
            break;
        case 2:
            asset->type = REISSUE;
            break;
        case 3:
            asset->type = SUB;
            break;
        case 4:
            asset->type = UNIQUE;
            break;
        case 5:
            asset->type = OWNER;
            break;
        case 6:
            asset->type = CHANNEL;
            break;
        case 7:
            asset->type = ROOT;
            break;
    }
}
//
//Java_com_ravenwallet_core_BRCoreTransactionAsset_setType
//        (JNIEnv *env, jobject thisObject, jstring typeObject) {
//    BRAsset *asset = (BRAsset *) getJNIReference(env, thisObject);
//
//    const char *typeObjectString = (*env)->GetStringUTFChars(env, typeObject, 0);
////    int res = strncmp(typeObjectString, "NEW_ASSET", strlen(typeObjectString));
//    switch (typeObjectString.) {
//        case "NEW_ASSET":
//            asset->type = NEW_ASSET;
//            break;
//
//        case "TRANSFER":
//            asset->type = NEW_ASSET;
//            break;
//        case :
//    }
//}

/*
 * Class:     com_ravencoin_core_BRCoreTransactionAsset
 * Method:    getName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_com_ravenwallet_core_BRCoreTransactionAsset_getName
        (JNIEnv *env, jobject thisObject) {
    BRAsset *asset = (BRAsset *) getJNIReference(env, thisObject);

//    size_t nameLen = sizeof(asset->name);
//    char name[1 + nameLen];
//    memcpy(name, asset->name, nameLen);
//    name[nameLen] = '\0';

    return (*env)->NewStringUTF(env, asset->name);
}

/*
 * Class:     com_ravencoin_core_BRCoreTransactionAsset
 * Method:    setAddress
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_com_ravenwallet_core_BRCoreTransactionAsset_setName
        (JNIEnv *env, jobject thisObject, jstring nameObject) {
    BRAsset *asset = (BRAsset *) getJNIReference(env, thisObject);
    //asset->name = nameObject;
    asset->name = (*env)->GetStringUTFChars(env, nameObject, 0);
    //need to release this string when done with it in order to
    //avoid memory leak
    // (*env)->ReleaseStringUTFChars(env, nameObject, asset->name);
}

JNIEXPORT jint JNICALL
Java_com_ravenwallet_core_BRCoreTransactionAsset_getNameLen(JNIEnv *env, jobject instance) {

    BRAsset *asset = (BRAsset *) getJNIReference(env, instance);
    return (jint) asset->nameLen;
}

JNIEXPORT void JNICALL
Java_com_ravenwallet_core_BRCoreTransactionAsset_setNamelen(JNIEnv *env, jobject instance,
                                                            jint namelen) {
    BRAsset *asset = (BRAsset *) getJNIReference(env, instance);
    asset->nameLen = (size_t) namelen;

}

/*
 * Class:     com_ravencoin_core_BRCoreTransactionAsset
 * Method:    getAmount
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL
Java_com_ravenwallet_core_BRCoreTransactionAsset_getAmount
        (JNIEnv *env, jobject thisObject) {
    BRAsset *asset = (BRAsset *) getJNIReference(env, thisObject);
    return (jlong) asset->amount;
}

/*
 * Class:     com_ravencoin_core_BRCoreTransactionAsset
 * Method:    setAmount
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_ravenwallet_core_BRCoreTransactionAsset_setAmount
        (JNIEnv *env, jobject thisObject, jlong amount) {
    BRAsset *asset = (BRAsset *) getJNIReference(env, thisObject);
    asset->amount = (uint64_t) amount;
}

/*
 * Class:     com_ravencoin_core_BRCoreTransactionAsset
 * Method:    getUnit
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL
Java_com_ravenwallet_core_BRCoreTransactionAsset_getUnit
        (JNIEnv *env, jobject thisObject) {
    BRAsset *asset = (BRAsset *) getJNIReference(env, thisObject);
    return (jlong) asset->unit;
}

/*
 * Class:     com_ravencoin_core_BRCoreTransactionAsset
 * Method:    setUnit
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_ravenwallet_core_BRCoreTransactionAsset_setUnit
        (JNIEnv *env, jobject thisObject, jlong unit) {
    BRAsset *asset = (BRAsset *) getJNIReference(env, thisObject);
    asset->unit = (uint64_t) unit;
}

/*
 * Class:     com_ravencoin_core_BRCoreTransactionAsset
 * Method:    getReissuable
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL
Java_com_ravenwallet_core_BRCoreTransactionAsset_getReissuable
        (JNIEnv *env, jobject thisObject) {
    BRAsset *asset = (BRAsset *) getJNIReference(env, thisObject);
    return (jlong) asset->reissuable;
}

/*
 * Class:     com_ravencoin_core_BRCoreTransactionAsset
 * Method:    setReissuable
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_ravenwallet_core_BRCoreTransactionAsset_setReissuable
        (JNIEnv *env, jobject thisObject, jlong reissuable) {
    BRAsset *asset = (BRAsset *) getJNIReference(env, thisObject);
    asset->reissuable = (uint64_t) reissuable;
}

/*
 * Class:     com_ravencoin_core_BRCoreTransactionAsset
 * Method:    getHasIPFS
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL
Java_com_ravenwallet_core_BRCoreTransactionAsset_getHasIPFS
        (JNIEnv *env, jobject thisObject) {
    BRAsset *asset = (BRAsset *) getJNIReference(env, thisObject);
    return (jlong) asset->hasIPFS;
}

/*
 * Class:     com_ravencoin_core_BRCoreTransactionAsset
 * Method:    setHasIPFS
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_ravenwallet_core_BRCoreTransactionAsset_setHasIPFS
        (JNIEnv *env, jobject thisObject, jlong hasIPFS) {
    BRAsset *asset = (BRAsset *) getJNIReference(env, thisObject);
    asset->hasIPFS = (uint64_t) hasIPFS;
}

/*
 * Class:     com_ravencoin_core_BRCoreTransactionAsset
 * Method:    getIPFSHash
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_com_ravenwallet_core_BRCoreTransactionAsset_getIPFSHash
        (JNIEnv *env, jobject thisObject) {
    BRAsset *asset = (BRAsset *) getJNIReference(env, thisObject);

    size_t IPFSHashLen = sizeof(asset->IPFSHash);
    char IPFSHash[1 + IPFSHashLen];
    memcpy(IPFSHash, asset->IPFSHash, IPFSHashLen);
    IPFSHash[IPFSHashLen] = '\0';

    return (*env)->NewStringUTF(env, IPFSHash);
}

/*
 * Class:     com_ravencoin_core_BRCoreTransactionAsset
 * Method:    setIPFSHash
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_com_ravenwallet_core_BRCoreTransactionAsset_setIPFSHash
        (JNIEnv *env, jobject thisObject, jstring IPFSHashObject) {
    BRAsset *asset = (BRAsset *) getJNIReference(env, thisObject);

    size_t IPFSHashLen = sizeof(asset->IPFSHash);

    size_t IPFSHashDataLen = (size_t) (*env)->GetStringLength(env, IPFSHashObject);
    assert (IPFSHashDataLen <= IPFSHashLen);
    const char *IPFSHashData = (*env)->GetStringUTFChars(env, IPFSHashObject, 0);
    strcpy(asset->IPFSHash, IPFSHashData);
}

JNIEXPORT jlong JNICALL
Java_com_ravenwallet_core_BRCoreTransactionAsset_createJniCoreAssetEmpty(JNIEnv *env, jclass type) {
    return (jlong) NewAsset();
}