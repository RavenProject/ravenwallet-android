# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add_library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

# now build app's shared lib
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -DANDROID_STL=gnustl_static -DANDROID_TOOLCHAIN=g++")
set(CMAKE_CXX_STANDARD 14)

# -Wimplicit-function-declaration
# -Wno-missing-prototypes -Werror=return-type -Wdocumentation -Wunreachable-code-aggressive -Wno-missing-braces
# -Wparentheses -Wswitch -Wno-unused-function -Wunused-label -Wno-unused-parameter -Wunused-variable -Wunused-value -Wempty-body
# -Wconditional-uninitialized -Wno-unknown-pragmas -pedantic -Wshadow -Wfour-char-constants -Wno-conversion -Wconstant-conversion
# -Wint-conversion -Wbool-conversion -Wenum-conversion -Wassign-enum -Wno-shorten-64-to-32 -Wpointer-sign -Wnewline-eof
# -Wdeprecated-declarations -Wno-sign-conversion

add_library( # The name of the library.
             core

             # Shared library.
             SHARED

             # Core files
             src/main/jni/core/BRAddress.c
             src/main/jni/core/BRAddress.h
             src/main/jni/core/BRArray.h
             src/main/jni/core/BRBase58.c
             src/main/jni/core/BRBase58.h
             src/main/jni/core/BRBIP44Sequence.c
             src/main/jni/core/BRBIP44Sequence.h
             src/main/jni/core/BRBIP38Key.c
             src/main/jni/core/BRBIP38Key.h
             src/main/jni/core/BRBIP39Mnemonic.c
             src/main/jni/core/BRBIP39Mnemonic.h
             src/main/jni/core/BRBIP39WordsEn.h
             src/main/jni/core/BRBloomFilter.c
             src/main/jni/core/BRBloomFilter.h
             src/main/jni/core/BRChainParams.h
             src/main/jni/core/BRCrypto.c
             src/main/jni/core/BRCrypto.h
             src/main/jni/core/BRInt.h
             src/main/jni/core/BRKey.c
             src/main/jni/core/BRKey.h
             src/main/jni/core/BRMerkleBlock.c
             src/main/jni/core/BRMerkleBlock.h
             src/main/jni/core/BRPaymentProtocol.c
             src/main/jni/core/BRPaymentProtocol.h
             src/main/jni/core/BRPeer.c
             src/main/jni/core/BRPeer.h
             src/main/jni/core/BRPeerManager.c
             src/main/jni/core/BRPeerManager.h
             src/main/jni/core/BRSet.c
             src/main/jni/core/BRSet.h
             src/main/jni/core/BRTransaction.c
             src/main/jni/core/BRTransaction.h
             src/main/jni/core/BRWallet.c
             src/main/jni/core/BRWallet.h
             src/main/jni/core/BRAssets.c
             src/main/jni/core/BRAssets.h
             src/main/jni/core/BRScript.c
             src/main/jni/core/BRScript.h

            # X16r files
			src/main/jni/core/crypto/blake.c
			src/main/jni/core/crypto/bmw.c
			src/main/jni/core/crypto/cubehash.c
			src/main/jni/core/crypto/echo.c
			src/main/jni/core/crypto/groestl.c
			src/main/jni/core/crypto/jh.c
			src/main/jni/core/crypto/keccak.c
			src/main/jni/core/crypto/luffa.c
			src/main/jni/core/crypto/shavite.c
			src/main/jni/core/crypto/simd.c
			src/main/jni/core/crypto/skein.c
			src/main/jni/core/crypto/sph_blake.h
			src/main/jni/core/crypto/sph_bmw.h
			src/main/jni/core/crypto/sph_cubehash.h
			src/main/jni/core/crypto/sph_echo.h
			src/main/jni/core/crypto/sph_fugue.c
			src/main/jni/core/crypto/sph_fugue.h
			src/main/jni/core/crypto/sph_groestl.h
			src/main/jni/core/crypto/sph_hamsi.c
			src/main/jni/core/crypto/sph_hamsi.h
			src/main/jni/core/crypto/sph_hamsi_helper.c
			src/main/jni/core/crypto/sph_jh.h
			src/main/jni/core/crypto/sph_keccak.h
			src/main/jni/core/crypto/sph_luffa.h
			src/main/jni/core/crypto/sph_sha2.c
			src/main/jni/core/crypto/sph_sha2.h
			src/main/jni/core/crypto/sph_sha512.c
			src/main/jni/core/crypto/sph_shabal.c
			src/main/jni/core/crypto/sph_shabal.h
			src/main/jni/core/crypto/sph_shavite.h
			src/main/jni/core/crypto/sph_simd.h
			src/main/jni/core/crypto/sph_skein.h
			src/main/jni/core/crypto/sph_tiger.h
			src/main/jni/core/crypto/sph_types.h
			src/main/jni/core/crypto/sph_whirlpool.c
			src/main/jni/core/crypto/sph_whirlpool.h
			src/main/jni/core/crypto/tiger.c

			# KAWPOW files
			src/main/jni/core/crypto/ethash/attributes.h
			src/main/jni/core/crypto/ethash/bit_manipulation.h
			src/main/jni/core/crypto/ethash/builtins.h
			src/main/jni/core/crypto/ethash/endianness.hpp
			src/main/jni/core/crypto/ethash/ethash.h
			src/main/jni/core/crypto/ethash/ethash.hpp
			src/main/jni/core/crypto/ethash/ethash-internal.hpp
			src/main/jni/core/crypto/ethash/hash_types.h
			src/main/jni/core/crypto/ethash/hash_types.hpp
			src/main/jni/core/crypto/ethash/helpers.hpp
			src/main/jni/core/crypto/ethash/keccak.c
			src/main/jni/core/crypto/ethash/keccak.h
			src/main/jni/core/crypto/ethash/keccak.hpp
			src/main/jni/core/crypto/ethash/keccakf800.c
			src/main/jni/core/crypto/ethash/keccakf1600.c
			src/main/jni/core/crypto/ethash/kiss99.hpp
			src/main/jni/core/crypto/ethash/managed.cpp
			src/main/jni/core/crypto/ethash/primes.h
			src/main/jni/core/crypto/ethash/primes.c
			src/main/jni/core/crypto/ethash/progpow.hpp
			src/main/jni/core/crypto/ethash/version.h
			src/main/jni/core/crypto/ethash/progpow_test_vectors.hpp
			src/main/jni/core/crypto/ethash/ethash.cpp
			src/main/jni/core/crypto/ethash/progpow.cpp
			src/main/jni/core/crypto/ethash/portable_endian.h

             # Core JNI files
             src/main/jni/BRCoreJni.h
             src/main/jni/BRCoreJni.c
             src/main/jni/com_ravencoin_core_BRCoreJniReference.c
             src/main/jni/com_ravencoin_core_BRCoreJniReference.h
             src/main/jni/com_ravencoin_core_BRCoreAddress.c
             src/main/jni/com_ravencoin_core_BRCoreAddress.h
             src/main/jni/com_ravencoin_core_BRCoreChainParams.c
             src/main/jni/com_ravencoin_core_BRCoreChainParams.h
             src/main/jni/com_ravencoin_core_BRCorePeer.c
             src/main/jni/com_ravencoin_core_BRCorePeer.h
             src/main/jni/com_ravencoin_core_BRCoreMasterPubKey.c
             src/main/jni/com_ravencoin_core_BRCoreMasterPubKey.h
             src/main/jni/com_ravencoin_core_BRCoreKey.h
             src/main/jni/com_ravencoin_core_BRCoreKey.c
             src/main/jni/com_ravencoin_core_BRCoreMerkleBlock.c
             src/main/jni/com_ravencoin_core_BRCoreMerkleBlock.h
             src/main/jni/com_ravencoin_core_BRCorePeerManager.c
             src/main/jni/com_ravencoin_core_BRCorePeerManager.h
             src/main/jni/com_ravencoin_core_BRCoreTransaction.c
             src/main/jni/com_ravencoin_core_BRCoreTransaction.h
             src/main/jni/com_ravencoin_core_BRCoreTransactionInput.c
             src/main/jni/com_ravencoin_core_BRCoreTransactionInput.h
             src/main/jni/com_ravencoin_core_BRCoreTransactionOutput.c
             src/main/jni/com_ravencoin_core_BRCoreTransactionOutput.h
             src/main/jni/com_ravencoin_core_BRCorePaymentProtocol.c
             src/main/jni/com_ravencoin_core_BRCorePaymentProtocol.h
             src/main/jni/com_ravencoin_core_BRCoreWallet.c
             src/main/jni/com_ravencoin_core_BRCoreWallet.h
             src/main/jni/com_ravencoin_core_BRCoreTransactionAsset.c
             src/main/jni/com_ravencoin_core_BRCoreTransactionAsset.h

             # Bech32 files
             #src/main/jni/core/BRBech32.c
             #src/main/jni/core/BRBech32.h

             )

include_directories(src/main/jni/)
include_directories(src/main/jni/core/)
include_directories(src/main/jni/core/secp256k1/include)
include_directories(src/main/jni/core/secp256k1/src)
include_directories(src/main/jni/core/secp256k1)
include_directories(src/main/jni/core/crypto/ethash)
include_directories(/System/Library/Frameworks/JavaVM.framework/Headers)

include_directories(src/main/secp/secp256k1/include/)
include_directories(src/main/secp/secp256k1/src/)
include_directories(src/main/secp/secp256k1/)
include_directories(src/main/secp/)

# add lib dependencies
target_link_libraries(
    core
    android
    log)
