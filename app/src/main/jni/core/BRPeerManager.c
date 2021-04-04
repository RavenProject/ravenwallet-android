#include "BRPeerManager.h"
#include "BRBloomFilter.h"
#include "BRSet.h"
#include "BRArray.h"
#include "BRInt.h"
#include <stdlib.h>
#include <stdio.h>
#include <inttypes.h>
#include <limits.h>
#include <time.h>
#include <pthread.h>
#include <errno.h>
#include <netdb.h>
#include <sys/socket.h>
#include <netinet/in.h>

#define PROTOCOL_TIMEOUT        20.0
#define MAX_CONNECT_FAILURES    20 // notify user of network problems after this many connect failures in a row
#define CHECKPOINT_COUNT        (sizeof(checkpoint_array)/sizeof(*checkpoint_array))
#define DNS_SEEDS_COUNT         (sizeof(dns_seeds)/sizeof(*dns_seeds))
#define GENESIS_BLOCK_HASH      (UInt256Reverse(u256_hex_decode(checkpoint_array[0].hash)))
#define PEER_FLAG_SYNCED        0x01
#define PEER_FLAG_NEEDSUPDATE   0x02
#define OLDEST_INTERVAL         1 * 24 * 60 * 60

#if TESTNET

static const struct {
    uint32_t height;
    const char *hash;
    uint32_t timestamp;
    uint32_t target;
} checkpoint_array[] = {
        {      0, "000000ecfc5e6324a079542221d00e10362bdc894d56500c414060eea8a3ad5a", 1537466400, 0x1e00ffff },
        { 200000, "00000193aa316faba95ed25accf8da2c1f3783881e7978ba8674bd4b0a409a05", 1583232224, 0x1e041a01 },
        { 230000, "00000000ce30881250378687ea468bbcd5f2b599b07f31fce487b9192d00d36b", 1585030961, 0x1d0130d0 },
        { 240000, "00000000217e2446d26c65e0f28ca721206253301d98b31b5122ed94bf21db74", 1585665582, 0x1c2f4b8d }
}; // New testnet, port:18770 useragent:"/Ravencoin2.2.0/"

static const char *dns_seeds[] = {
//       "127.0.0.1", NULL
//        "192.168.86.38", NULL
        "seed-testnet-raven.ravencoin.org.", "seed-testnet-raven.ravencoin.com.",
        "seed-testnet-raven.bitactivate.com.", NULL
};

#else // main net

// blockchain checkpoints - these are also used as starting points for partial chain downloads, so they need to be at
// difficulty transition boundaries in order to verify the block difficulty at the immediately following transition
static const struct { uint32_t height; const char *hash; uint32_t timestamp; uint32_t target; } checkpoint_array[] = {
        {      0, "0000006b444bc2f2ffe627be9d9e7e7a0730000870ef6eb6da46c8eae389df90", 1514999494, 0x1e00ffff },
        {      1, "00000058bcc33dea08b53691edb9e49a9eb8bac36a0db17eb5a7588860b1f590", 1515015723, 0x1e00ffff },
        {  20160, "00000000146e792b63f2a18db16f32d2afc9f0b332839eb502cb9c9a8f1bc033", 1515665731, 0x1c53dd22 },
        {  40320, "00000000085e7d049938d66a08d151891c0087a6b3d78d400f1ca0944991ffde", 1516664426, 0x1c0a0075 },
        {  60480, "0000000000683f2d1bb44dd545eb4fea28c0f51eb513ea32b4e813f185a1f6ab", 1517740553, 0x1c01b501 },
        {  80640, "00000000000735f443ea62266bb7799a760c8336da0c7b7a987c895e83c9ea73", 1518771490, 0x1b43e935 },
        { 100800, "00000000000bf40aa747ca97da99e1e6878efff28f709d1969f0a2d95dda1414", 1519826997, 0x1b0fabc1 },
        { 120960, "000000000000203f20f1f2fc50546b4f3d0693a53e781b499884661e6762eb05", 1520934202, 0x1b060077 },
        { 141120, "00000000000367e05ceca64ebf6b72a87510bdcb6252ff071b7f4971661e9acf", 1522092453, 0x1b03cc83 },
        { 161280, "0000000000024a1d42423dd3e1cde28c78fe34857db63f08d21f11fc13e594c3", 1523259269, 0x1b028d7d },
        { 181440, "000000000000d202bdeb7993a1de022f82231fdce97e22f054626291eb79f4cb", 1524510281, 0x1b038153 },
        { 201600, "000000000001a16d8b86e19ac87df227458d29b5fb70dfef7e5b0203df085617", 1525709579, 0x1b0306f4 },
        { 221760, "000000000002b4a1ef811a31e58489794dba047e4e78e18d5611c94d7fc60174", 1526920402, 0x1b02ff59 },
        { 241920, "000000000001e64a356c6665afcb2871bc7f18e5609663b5b54a82fa204ee9b1", 1528150015, 0x1b037c77 },
        { 262080, "0000000000014a11d3aacdc5ee21e69fd8aefe10f0e617508dfb3e78d1ca82be", 1529359488, 0x1b037276 },
        { 282240, "00000000000182bbfada9dd47003bed09880b7a1025edcb605f9c048f2bad49e", 1530594496, 0x1b042cda },
        { 302400, "000000000001e9862c28d3359f2b568b03811988f2db2f91ab8b412acac891ed", 1531808927, 0x1b0422c8 },
        { 322560, "000000000001d50eaf12266c6ecaefec473fecd9daa7993db05b89e6ab381388", 1533209846, 0x1b04cb9e },
        { 338778, "000000000003198106731cb28fc24e9ace995a37709b026b25dfa905aea54517", 1535599185, 0x1b07cf3a },
        { 340704, "000000000001c5e10e9e94b761464548efd2bbcf22509ac6bfec35757da58687", 1535711279, 0x1b024145 },
        { 673344, "00000000000041bbd71687002a65c4dac458cb8a775e7ca094d623ac50300979", 1555813777, 0x1a62c6e4 },
        { 844704, "0000000000006361abddc32b21bef2bf1df669f731ec18a13adbd426cced17b6", 1566164045, 0x1a78e5ab },
        { 899136, "00000000000015d1f71d3fac92e98d3a01f7ec310c10e90a966dcf1b64369239", 1569424946, 0x1a379754 },  //Sep 15 2019
        { 1196000, "0000000000000636f7177046a677203ea191d540b1aefdff63fd43eec538dd3b", 1587354658, 0x1a254bfb }, //Apr 19 2020
        //I'm not sure if there is a major problem with checkpoints each month (40,320 blocks)
        //Mostly doing it just to give variable-start wallet restore plenty of times to work with
        { 1216160, "00000000000020daebec36516ef5f867b0fccce33e68646e7652f80ea96186e7", 1588572488, 0x1a259da6 },  //May 4 2020
        { 1256480, "000000000000e62500d1e41ee8c468d626ccf475eea3f291f76c142560a3ad83", 1590977350, 0x1b016954 },  //May 31 2020
        { 1296800, "000000000001846f32ad426f8a7576bd49b06861aa6cd506cb0dbc9cf105cd85", 1593411973, 0x1b018cff },  //Jun 29 2020
        { 1337120, "0000000000013b6afd6fa8c4cd57143b33138d648bba5b9aefc4be2381611a50", 1595848826, 0x1b02425d },  //Jul 27 2020
        { 1377440, "0000000000017dce6b055831e2eec4a62d529e870d0fc9a7ff4f896a18786c8f", 1598283070, 0x1b01fc40 },  //Aug 24 2020
        { 1417760, "000000000000cc0ad4fc9c2adc673f57bbd09812e1f5f3fda2b2e3d0eb1c5a93", 1600720000, 0x1b02927b },  //Sep 21 2020
        { 1458080, "0000000000002668ed1ffdd0bf1982a48d637a9175c8b030f1d5e300a4a0b029", 1603153479, 0x1b023835 },  //Oct 20 2020
        { 1498400, "00000000000036f29125709aced6507829b96138ea6c1e07bdfbed2f703fffb8", 1605588946, 0x1b025e47 },  //Nov 17 2020
        { 1538720, "000000000000d8755b12242895c515c93f7cfe8094bcb85138954728e7f5dc85", 1608024058, 0x1b02df01 },  //Dec 15 2020
        { 1579040, "000000000001b776ee39f65d67c8d4cfa2f200c359a9b5787c85f31503bb0fab", 1610459367, 0x1b02be6f },  //Jan 12 2021
        { 1619360, "000000000001cb5fde4bb9c89b5c0060a1903ae2ba5f170d7459022ef59965a6", 1612893283, 0x1b023b77 },  //Feb 9 2021
        { 1659680, "000000000000258aa2b5bf5d0003b5717d066303819553b64296e935154773c4", 1615320605, 0x1b0086e1 },  //Mar 9 2021
        //{1700000},      //Almost....
};

//1 week = 10,080, 4 weeks = 40,320

static const char *dns_seeds[] = {
//    "127.0.0.1", NULL
        "seed-raven.ravencoin.com", "seed-raven.ravencoin.org.", "seed-raven.bitactivate.com.", NULL
};

#endif

typedef struct {
    BRPeerManager *manager;
    const char *hostname;
    uint64_t services;
} FindPeersInfo;

typedef struct {
    BRPeer *peer;
    BRPeerManager *manager;
    UInt256 hash;
} PeerCallbackInfo;

typedef struct {
    BRTransaction *tx;
    void *info;

    void (*callback)(void *info, int error);
} PublishedTx;

typedef struct {
    UInt256 txHash;
    BRPeer *peers;
} TxPeerList;

// true if peer is contained in the list of peers associated with txHash
static int _TxPeerListHasPeer(const TxPeerList *list, UInt256 txHash, const BRPeer *peer) {
    for (size_t i = array_count(list); i > 0; i--) {
        if (!UInt256Eq(list[i - 1].txHash, txHash)) continue;

        for (size_t j = array_count(list[i - 1].peers); j > 0; j--) {
            if (BRPeerEq(&list[i - 1].peers[j - 1], peer)) return 1;
        }

        break;
    }

    return 0;
}

// number of peers associated with txHash
static size_t _TxPeerListCount(const TxPeerList *list, UInt256 txHash) {
    for (size_t i = array_count(list); i > 0; i--) {
        if (UInt256Eq(list[i - 1].txHash, txHash)) return array_count(list[i - 1].peers);
    }

    return 0;
}

// adds peer to the list of peers associated with txHash and returns the new total number of peers
static size_t _TxPeerListAddPeer(TxPeerList **list, UInt256 txHash, const BRPeer *peer) {
    for (size_t i = array_count(*list); i > 0; i--) {
        if (!UInt256Eq((*list)[i - 1].txHash, txHash)) continue;

        for (size_t j = array_count((*list)[i - 1].peers); j > 0; j--) {
            if (BRPeerEq(&(*list)[i - 1].peers[j - 1], peer))
                return array_count((*list)[i - 1].peers);
        }

        array_add((*list)[i - 1].peers, *peer);
        return array_count((*list)[i - 1].peers);
    }

    array_add(*list, ((const TxPeerList) {txHash, NULL}));
    array_new((*list)[array_count(*list) - 1].peers, PEER_MAX_CONNECTIONS);
    array_add((*list)[array_count(*list) - 1].peers, *peer);
    return 1;
}

// removes peer from the list of peers associated with txHash, returns true if peer was found
static int _TxPeerListRemovePeer(TxPeerList *list, UInt256 txHash, const BRPeer *peer) {
    for (size_t i = array_count(list); i > 0; i--) {
        if (!UInt256Eq(list[i - 1].txHash, txHash)) continue;

        for (size_t j = array_count(list[i - 1].peers); j > 0; j--) {
            if (!BRPeerEq(&list[i - 1].peers[j - 1], peer)) continue;
            array_rm(list[i - 1].peers, j - 1);
            return 1;
        }

        break;
    }

    return 0;
}

// comparator for sorting peers by timestamp, most recent first
inline static int _peerTimestampCompare(const void *peer, const void *otherPeer) {
    if (((const BRPeer *) peer)->timestamp < ((const BRPeer *) otherPeer)->timestamp) return 1;
    if (((const BRPeer *) peer)->timestamp > ((const BRPeer *) otherPeer)->timestamp) return -1;
    return 0;
}

// returns a hash value for a block's prevBlock value suitable for use in a hashtable
inline static size_t _PrevBlockHash(const void *block) {
    return (size_t) ((const BRMerkleBlock *) block)->prevBlock.u32[0];
}

// true if block and otherBlock have equal prevBlock values
inline static int _PrevBlockEq(const void *block, const void *otherBlock) {
    return UInt256Eq(((const BRMerkleBlock *) block)->prevBlock,
                     ((const BRMerkleBlock *) otherBlock)->prevBlock);
}

// returns a hash value for a block's height value suitable for use in a hashtable
inline static size_t _BlockHeightHash(const void *block) {
    // (FNV_OFFSET xor height)*FNV_PRIME
    return (size_t) ((0x811C9dc5 ^ ((const BRMerkleBlock *) block)->height) * 0x01000193);
}

// true if block and otherBlock have equal height values
inline static int _BlockHeightEq(const void *block, const void *otherBlock) {
    return (((const BRMerkleBlock *) block)->height ==
            ((const BRMerkleBlock *) otherBlock)->height);
}

struct PeerManagerStruct {
    const ChainParams *params;
    BRWallet *wallet;
    int isConnected, connectFailureCount, misbehavinCount, dnsThreadCount, maxConnectCount;
    BRPeer *peers, *downloadPeer, fixedPeer, **connectedPeers;
    char downloadPeerName[INET6_ADDRSTRLEN + 6];
    uint32_t earliestKeyTime, syncStartHeight, filterUpdateHeight, estimatedHeight;
    BRBloomFilter *bloomFilter;
    double fpRate, averageTxPerBlock;
    BRSet *blocks, *orphans, *checkpoints;
    BRMerkleBlock *lastBlock, *lastOrphan;
    TxPeerList *txRelays, *txRequests;
    PublishedTx *publishedTx;
    UInt256 *publishedTxHashes;
    void *info;

    void (*syncStarted)(void *info);

    void (*syncStopped)(void *info, int error);

    void (*txStatusUpdate)(void *info);

    void (*saveBlocks)(void *info, int replace, BRMerkleBlock *blocks[], size_t blocksCount);

    void (*savePeers)(void *info, int replace, const BRPeer peers[], size_t peersCount);

    int (*networkIsReachable)(void *info);

    void (*threadCleanup)(void *info);

    pthread_mutex_t lock;
};

static void _PeerManagerPeerMisbehavin(BRPeerManager *manager, BRPeer *peer) {
    for (size_t i = array_count(manager->peers); i > 0; i--) {
        if (BRPeerEq(&manager->peers[i - 1], peer)) array_rm(manager->peers, i - 1);
    }

    if (++manager->misbehavinCount >=
        10) { // clear out stored peers so we get a fresh list from DNS for next connect
        manager->misbehavinCount = 0;
        array_clear(manager->peers);
    }

    BRPeerDisconnect(peer);
}

static void _PeerManagerSyncStopped(BRPeerManager *manager) {
    manager->syncStartHeight = 0;

    if (manager->downloadPeer) {
        // don't cancel timeout if there's a pending tx publish callback
        for (size_t i = array_count(manager->publishedTx); i > 0; i--) {
            if (manager->publishedTx[i - 1].callback != NULL) return;
        }

        BRPeerScheduleDisconnect(manager->downloadPeer, -1); // cancel sync timeout
    }
}

// adds transaction to list of tx to be published, along with any unconfirmed inputs
static void _PeerManagerAddTxToPublishList(BRPeerManager *manager, BRTransaction *tx, void *info,
                                           void (*callback)(void *, int)) {
    if (tx && tx->blockHeight == TX_UNCONFIRMED) {
        for (size_t i = array_count(manager->publishedTx); i > 0; i--) {
            if (BRTransactionEq(manager->publishedTx[i - 1].tx, tx)) return;
        }

        array_add(manager->publishedTx, ((PublishedTx) {tx, info, callback}));
        array_add(manager->publishedTxHashes, tx->txHash);

        for (size_t i = 0; i < tx->inCount; i++) {
            _PeerManagerAddTxToPublishList(manager, BRWalletTransactionForHash(manager->wallet,
                                                                               tx->inputs[i].txHash),
                                           NULL, NULL);
        }
    }
}

static size_t
_PeerManagerBlockLocators(BRPeerManager *manager, UInt256 *locators, size_t locatorsCount) {
    // append 10 most recent block hashes, decending, then continue appending, doubling the step back each time,
    // finishing with the genesis block (top, -1, -2, -3, -4, -5, -6, -7, -8, -9, -11, -15, -23, -39, -71, -135, ..., 0)
    BRMerkleBlock *block = manager->lastBlock;

    int32_t step = 1, i = 0, j;

    while (block && block->height > 0) {
        if (locators && i < locatorsCount) locators[i] = block->blockHash;
        if (++i >= 10) step *= 2;

        for (j = 0; block && j < step; j++) {
            block = BRSetGet(manager->blocks, &block->prevBlock);
        }
    }

    if (locators && i < locatorsCount) locators[i] = GENESIS_BLOCK_HASH;
    return ++i;
}

static void _setApplyFreeBlock(void *info, void *block) {
    BRMerkleBlockFree(block);
}

static void _PeerManagerLoadBloomFilter(BRPeerManager *manager, BRPeer *peer) {
    // every time a new wallet address is added, the bloom filter has to be rebuilt, and each address is only used
    // for one transaction, so here we generate some spare addresses to avoid rebuilding the filter each time a
    // wallet transaction is encountered during the chain sync
    BRWalletUnusedAddrs(manager->wallet, NULL, SEQUENCE_GAP_LIMIT_EXTERNAL + 100, 0);
    BRWalletUnusedAddrs(manager->wallet, NULL, SEQUENCE_GAP_LIMIT_INTERNAL + 100, 1);

    BRSetApply(manager->orphans, NULL, _setApplyFreeBlock);
    BRSetClear(manager->orphans); // clear out orphans that may have been received on an old filter
    manager->lastOrphan = NULL;
    manager->filterUpdateHeight = manager->lastBlock->height;
    manager->fpRate = BLOOM_REDUCED_FALSEPOSITIVE_RATE;

    size_t addrsCount = BRWalletAllAddrs(manager->wallet, NULL, 0);
    BRAddress *addrs = malloc(addrsCount * sizeof(*addrs));
    size_t utxosCount = BRWalletUTXOs(manager->wallet, NULL, 0);
    UTXO *utxos = malloc(utxosCount * sizeof(*utxos));
    uint32_t blockHeight = (manager->lastBlock->height > 100) ? manager->lastBlock->height - 100
                                                              : 0;
    size_t txCount = BRWalletTxUnconfirmedBefore(manager->wallet, NULL, 0, blockHeight);
    BRTransaction **transactions = malloc(txCount * sizeof(*transactions));
    BRBloomFilter *filter;

    assert(addrs != NULL);
    assert(utxos != NULL);
    assert(transactions != NULL);
    addrsCount = BRWalletAllAddrs(manager->wallet, addrs, addrsCount);
    utxosCount = BRWalletUTXOs(manager->wallet, utxos, utxosCount);
    txCount = BRWalletTxUnconfirmedBefore(manager->wallet, transactions, txCount, blockHeight);
    filter = BRBloomFilterNew(manager->fpRate, addrsCount + utxosCount + txCount + 100,
                              (uint32_t) BRPeerHash(peer),
                              BLOOM_UPDATE_ALL); // BUG: XXX txCount not the same as number of spent wallet outputs

    for (size_t i = 0;
         i < addrsCount; i++) { // add addresses to watch for tx receiveing money to the wallet
        UInt160 hash = UINT160_ZERO;

        BRAddressHash160(&hash, addrs[i].s);

        if (!UInt160IsZero(hash) && !BRBloomFilterContainsData(filter, hash.u8, sizeof(hash))) {
            BRBloomFilterInsertData(filter, hash.u8, sizeof(hash));
        }
    }

    free(addrs);

    for (size_t i = 0;
         i < utxosCount; i++) { // add UTXOs to watch for tx sending money from the wallet
        uint8_t o[sizeof(UInt256) + sizeof(uint32_t)];

        UInt256Set(o, utxos[i].hash);
        UInt32SetLE(&o[sizeof(UInt256)], utxos[i].n);
        if (!BRBloomFilterContainsData(filter, o, sizeof(o)))
            BRBloomFilterInsertData(filter, o, sizeof(o));
    }

    free(utxos);

    for (size_t i = 0; i < txCount; i++) { // also add TXOs spent within the last 100 blocks
        for (size_t j = 0; j < transactions[i]->inCount; j++) {
            BRTxInput *input = &transactions[i]->inputs[j];
            BRTransaction *tx = BRWalletTransactionForHash(manager->wallet, input->txHash);
            uint8_t o[sizeof(UInt256) + sizeof(uint32_t)];

            if (tx && input->index < tx->outCount &&
                BRWalletContainsAddress(manager->wallet, tx->outputs[input->index].address)) {
                UInt256Set(o, input->txHash);
                UInt32SetLE(&o[sizeof(UInt256)], input->index);
                if (!BRBloomFilterContainsData(filter, o, sizeof(o)))
                    BRBloomFilterInsertData(filter, o, sizeof(o));
            }
        }
    }

    free(transactions);
    if (manager->bloomFilter) BRBloomFilterFree(manager->bloomFilter);
    manager->bloomFilter = filter;
    // TODO: XXX if already synced, recursively add inputs of unconfirmed receives

    uint8_t data[BRBloomFilterSerialize(filter, NULL, 0)];
    size_t len = BRBloomFilterSerialize(filter, data, sizeof(data));

    BRPeerSendFilterload(peer, data, len);
}

static void _updateFilterRerequestDone(void *info, int success) {
    BRPeer *peer = ((PeerCallbackInfo *) info)->peer;
    BRPeerManager *manager = ((PeerCallbackInfo *) info)->manager;

    free(info);

    if (success) {
        pthread_mutex_lock(&manager->lock);

        if ((peer->flags & PEER_FLAG_NEEDSUPDATE) == 0) {
            UInt256 locators[_PeerManagerBlockLocators(manager, NULL, 0)];
            size_t count = _PeerManagerBlockLocators(manager, locators,
                                                     sizeof(locators) / sizeof(*locators));
            BRPeerSendGetblocks(peer, locators, count, UINT256_ZERO);
        }

        pthread_mutex_unlock(&manager->lock);
    }
}

static void _updateFilterLoadDone(void *info, int success) {
    BRPeer *peer = ((PeerCallbackInfo *) info)->peer;
    BRPeerManager *manager = ((PeerCallbackInfo *) info)->manager;
    PeerCallbackInfo *peerInfo;

    free(info);

    if (success) {
        pthread_mutex_lock(&manager->lock);
        BRPeerSetNeedsFilterUpdate(peer, 0);
        peer->flags &= ~PEER_FLAG_NEEDSUPDATE;

        if (manager->lastBlock->height < manager->estimatedHeight) { // if syncing, rerequest blocks
            peerInfo = calloc(1, sizeof(*peerInfo));
            assert(peerInfo != NULL);
            peerInfo->peer = peer;
            peerInfo->manager = manager;
            BRPeerRerequestBlocks(manager->downloadPeer, manager->lastBlock->blockHash);
            BRPeerSendPing(manager->downloadPeer, peerInfo, _updateFilterRerequestDone);
        } else BRPeerSendMempool(peer, NULL, 0, NULL, NULL); // if not syncing, request mempool

        pthread_mutex_unlock(&manager->lock);
    }
}

static void _updateFilterPingDone(void *info, int success) {
    BRPeer *peer = ((PeerCallbackInfo *) info)->peer;
    BRPeerManager *manager = ((PeerCallbackInfo *) info)->manager;
    PeerCallbackInfo *peerInfo;

    if (success) {
        pthread_mutex_lock(&manager->lock);
        peer_log(peer, "updating filter with newly created wallet addresses");
        if (manager->bloomFilter) BRBloomFilterFree(manager->bloomFilter);
        manager->bloomFilter = NULL;

        if (manager->lastBlock->height <
            manager->estimatedHeight) { // if we're syncing, only update download peer
            if (manager->downloadPeer) {
                _PeerManagerLoadBloomFilter(manager, manager->downloadPeer);
                BRPeerSendPing(manager->downloadPeer, info,
                               _updateFilterLoadDone); // wait for pong so filter is loaded
            } else free(info);
        } else {
            free(info);

            for (size_t i = array_count(manager->connectedPeers); i > 0; i--) {
                if (BRPeerConnectStatus(manager->connectedPeers[i - 1]) !=
                    BRPeerStatusConnected)
                    continue;
                peerInfo = calloc(1, sizeof(*peerInfo));
                assert(peerInfo != NULL);
                peerInfo->peer = manager->connectedPeers[i - 1];
                peerInfo->manager = manager;
                _PeerManagerLoadBloomFilter(manager, peerInfo->peer);
                BRPeerSendPing(peerInfo->peer, peerInfo,
                               _updateFilterLoadDone); // wait for pong so filter is loaded
            }
        }

        pthread_mutex_unlock(&manager->lock);
    } else free(info);
}

static void _PeerManagerUpdateFilter(BRPeerManager *manager) {
    PeerCallbackInfo *info;

    if (manager->downloadPeer && (manager->downloadPeer->flags & PEER_FLAG_NEEDSUPDATE) == 0) {
        BRPeerSetNeedsFilterUpdate(manager->downloadPeer, 1);
        manager->downloadPeer->flags |= PEER_FLAG_NEEDSUPDATE;
        peer_log(manager->downloadPeer, "filter update needed, waiting for pong");
        info = calloc(1, sizeof(*info));
        assert(info != NULL);
        info->peer = manager->downloadPeer;
        info->manager = manager;
        // wait for pong so we're sure to include any tx already sent by the peer in the updated filter
        BRPeerSendPing(manager->downloadPeer, info, _updateFilterPingDone);
    }
}

static void _PeerManagerUpdateTx(BRPeerManager *manager, const UInt256 *txHashes, size_t txCount,
                                 uint32_t blockHeight, uint32_t timestamp) {
    if (blockHeight != TX_UNCONFIRMED) { // remove confirmed tx from publish list and relay counts
        for (size_t i = 0; i < txCount; i++) {
            for (size_t j = array_count(manager->publishedTx); j > 0; j--) {
                BRTransaction *tx = manager->publishedTx[j - 1].tx;

                if (!UInt256Eq(txHashes[i], tx->txHash)) continue;
                array_rm(manager->publishedTx, j - 1);
                array_rm(manager->publishedTxHashes, j - 1);
                if (!BRWalletTransactionForHash(manager->wallet, tx->txHash)) BRTransactionFree(tx);
            }

            for (size_t j = array_count(manager->txRelays); j > 0; j--) {
                if (!UInt256Eq(txHashes[i], manager->txRelays[j - 1].txHash)) continue;
                array_free(manager->txRelays[j - 1].peers);
                array_rm(manager->txRelays, j - 1);
            }
        }
    }

    BRWalletUpdateTransactions(manager->wallet, txHashes, txCount, blockHeight, timestamp);
}

// unconfirmed transactions that aren't in the mempools of any of connected peers have likely dropped off the network
static void _requestUnrelayedTxGetdataDone(void *info, int success) {
    BRPeer *peer = ((PeerCallbackInfo *) info)->peer;
    BRPeerManager *manager = ((PeerCallbackInfo *) info)->manager;
    int isPublishing;
    size_t count = 0;

    free(info);
    pthread_mutex_lock(&manager->lock);
    if (success) peer->flags |= PEER_FLAG_SYNCED;

    for (size_t i = array_count(manager->connectedPeers); i > 0; i--) {
        peer = manager->connectedPeers[i - 1];
        if (BRPeerConnectStatus(peer) == BRPeerStatusConnected) count++;
        if ((peer->flags & PEER_FLAG_SYNCED) != 0) continue;
        count = 0;
        break;
    }

    // don't remove transactions until we're connected to maxConnectCount peers, and all peers have finished
    // relaying their mempools
    if (count >= manager->maxConnectCount) {
        size_t txCount = BRWalletTxUnconfirmedBefore(manager->wallet, NULL, 0, TX_UNCONFIRMED);
        BRTransaction *tx[(txCount < 10000) ? txCount : 10000];

        txCount = BRWalletTxUnconfirmedBefore(manager->wallet, tx, sizeof(tx) / sizeof(*tx),
                                              TX_UNCONFIRMED);

        for (size_t i = 0; i < txCount; i++) {
            isPublishing = 0;

            for (size_t j = array_count(manager->publishedTx); !isPublishing && j > 0; j--) {
                if (BRTransactionEq(manager->publishedTx[j - 1].tx, tx[i]) &&
                    manager->publishedTx[j - 1].callback != NULL)
                    isPublishing = 1;
            }

            if (!isPublishing && _TxPeerListCount(manager->txRelays, tx[i]->txHash) == 0 &&
                _TxPeerListCount(manager->txRequests, tx[i]->txHash) == 0) {
                BRWalletRemoveTransaction(manager->wallet, tx[i]->txHash);
            } else if (!isPublishing && _TxPeerListCount(manager->txRelays, tx[i]->txHash) <
                                        manager->maxConnectCount) {
                // set timestamp 0 to mark as unverified
                _PeerManagerUpdateTx(manager, &tx[i]->txHash, 1, TX_UNCONFIRMED, 0);
            }
        }
    }

    pthread_mutex_unlock(&manager->lock);
}

static void _PeerManagerRequestUnrelayedTx(BRPeerManager *manager, BRPeer *peer) {
    PeerCallbackInfo *info;
    size_t hashCount = 0, txCount = BRWalletTxUnconfirmedBefore(manager->wallet, NULL, 0,
                                                                TX_UNCONFIRMED);
    BRTransaction *tx[txCount];
    UInt256 txHashes[txCount];

    txCount = BRWalletTxUnconfirmedBefore(manager->wallet, tx, txCount, TX_UNCONFIRMED);

    for (size_t i = 0; i < txCount; i++) {
        if (!_TxPeerListHasPeer(manager->txRelays, tx[i]->txHash, peer) &&
            !_TxPeerListHasPeer(manager->txRequests, tx[i]->txHash, peer)) {
            txHashes[hashCount++] = tx[i]->txHash;
            _TxPeerListAddPeer(&manager->txRequests, tx[i]->txHash, peer);
        }
    }

    if (hashCount > 0) {
        BRPeerSendGetdata(peer, txHashes, hashCount, NULL, 0);

        if ((peer->flags & PEER_FLAG_SYNCED) == 0) {
            info = calloc(1, sizeof(*info));
            assert(info != NULL);
            info->peer = peer;
            info->manager = manager;
            BRPeerSendPing(peer, info, _requestUnrelayedTxGetdataDone);
        }
    } else peer->flags |= PEER_FLAG_SYNCED;
}

static void _PeerManagerPublishPendingTx(BRPeerManager *manager, BRPeer *peer) {
    for (size_t i = array_count(manager->publishedTx); i > 0; i--) {
        if (manager->publishedTx[i - 1].callback == NULL) continue;
        BRPeerScheduleDisconnect(peer, PROTOCOL_TIMEOUT); // schedule publish timeout
        break;
    }

    BRPeerSendInv(peer, manager->publishedTxHashes, array_count(manager->publishedTxHashes));
}

static void _mempoolDone(void *info, int success) {
    BRPeer *peer = ((PeerCallbackInfo *) info)->peer;
    BRPeerManager *manager = ((PeerCallbackInfo *) info)->manager;
    int syncFinished = 0;

    free(info);

    if (success) {
        peer_log(peer, "mempool request finished");
        pthread_mutex_lock(&manager->lock);
        if (manager->syncStartHeight > 0) {
            peer_log(peer, "sync succeeded");
            syncFinished = 1;
            _PeerManagerSyncStopped(manager);
        }

        _PeerManagerRequestUnrelayedTx(manager, peer);
        BRPeerSendGetaddr(peer); // request a list of other ravenwallet peers
        pthread_mutex_unlock(&manager->lock);
        if (manager->txStatusUpdate) manager->txStatusUpdate(manager->info);
        if (syncFinished && manager->syncStopped) manager->syncStopped(manager->info, 0);
    } else
        peer_log(peer, "mempool request failed");
}

static void _loadBloomFilterDone(void *info, int success) {
    BRPeer *peer = ((PeerCallbackInfo *) info)->peer;
    BRPeerManager *manager = ((PeerCallbackInfo *) info)->manager;

    pthread_mutex_lock(&manager->lock);

    if (success) {
        BRPeerSendMempool(peer, manager->publishedTxHashes, array_count(manager->publishedTxHashes),
                          info,
                          _mempoolDone);
        pthread_mutex_unlock(&manager->lock);
    } else {
        free(info);

        if (peer == manager->downloadPeer) {
            peer_log(peer, "sync succeeded");
            _PeerManagerSyncStopped(manager);
            pthread_mutex_unlock(&manager->lock);
            if (manager->syncStopped) manager->syncStopped(manager->info, 0);
        } else pthread_mutex_unlock(&manager->lock);
    }
}

static void _PeerManagerLoadMempools(BRPeerManager *manager) {
    // after syncing, load filters and get mempools from other peers
    for (size_t i = array_count(manager->connectedPeers); i > 0; i--) {
        BRPeer *peer = manager->connectedPeers[i - 1];
        PeerCallbackInfo *info;

        if (BRPeerConnectStatus(peer) != BRPeerStatusConnected) continue;
        info = calloc(1, sizeof(*info));
        assert(info != NULL);
        info->peer = peer;
        info->manager = manager;

        if (peer != manager->downloadPeer ||
            manager->fpRate > BLOOM_REDUCED_FALSEPOSITIVE_RATE * 5.0) {
            _PeerManagerLoadBloomFilter(manager, peer);
            _PeerManagerPublishPendingTx(manager, peer);
            BRPeerSendPing(peer, info,
                           _loadBloomFilterDone); // load mempool after updating bloomfilter
        } else
            BRPeerSendMempool(peer, manager->publishedTxHashes,
                              array_count(manager->publishedTxHashes), info,
                              _mempoolDone);
    }
}

// returns a UINT128_ZERO terminated array of addresses for hostname that must be freed, or NULL if lookup failed
static UInt128 *_addressLookup(const char *hostname) {
    struct addrinfo *servinfo, *p;
    UInt128 *addrList = NULL;
    size_t count = 0, i = 0;

    if (getaddrinfo(hostname, NULL, NULL, &servinfo) == 0) {
        for (p = servinfo; p != NULL; p = p->ai_next) count++;
        if (count > 0) addrList = calloc(count + 1, sizeof(*addrList));
        assert(addrList != NULL || count == 0);

        for (p = servinfo; p != NULL; p = p->ai_next) {
            if (p->ai_family == AF_INET) {
                addrList[i].u16[5] = 0xffff;
                addrList[i].u32[3] = ((struct sockaddr_in *) p->ai_addr)->sin_addr.s_addr;
                i++;
            } else if (p->ai_family == AF_INET6) {
                addrList[i++] = *(UInt128 *) &((struct sockaddr_in6 *) p->ai_addr)->sin6_addr;
            }
        }

        freeaddrinfo(servinfo);
    }

    return addrList;
}

static void *_findPeersThreadRoutine(void *arg) {
    BRPeerManager *manager = ((FindPeersInfo *) arg)->manager;
    uint64_t services = ((FindPeersInfo *) arg)->services;
    UInt128 *addrList, *addr;
    time_t now = time(NULL), age;

    pthread_cleanup_push(manager->threadCleanup, manager->info);
        addrList = _addressLookup(((FindPeersInfo *) arg)->hostname);
        free(arg);
        pthread_mutex_lock(&manager->lock);

        for (addr = addrList; addr && !UInt128IsZero(*addr); addr++) {
            age = 24 * 60 * 60 + BRRand(2 * 24 * 60 * 60); // add between 1 and 3 days
            array_add(manager->peers,
                      ((const BRPeer) {*addr, STANDARD_PORT, services, now - age, 0}));
        }

        manager->dnsThreadCount--;
        pthread_mutex_unlock(&manager->lock);
        if (addrList) free(addrList);
            pthread_cleanup_pop(1);
    return NULL;
}

// DNS peer discovery
static void _PeerManagerFindPeers(BRPeerManager *manager) {
    static const uint64_t services = SERVICES_NODE_NETWORK | SERVICES_NODE_BLOOM;
    time_t now = time(NULL);
    struct timespec ts;
    pthread_t thread;
    pthread_attr_t attr;
    UInt128 *addr, *addrList;
    FindPeersInfo *info;

    if (!UInt128IsZero(manager->fixedPeer.address)) {
        array_set_count(manager->peers, 1);
        manager->peers[0] = manager->fixedPeer;
        manager->peers[0].services = services;
        manager->peers[0].timestamp = now;
    } else {
        for (size_t i = 1; i < DNS_SEEDS_COUNT; i++) {
            info = calloc(1, sizeof(FindPeersInfo));
            assert(info != NULL);
            info->manager = manager;
            info->hostname = dns_seeds[i];
            info->services = services;
            if (pthread_attr_init(&attr) == 0 &&
                pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED) == 0 &&
                pthread_create(&thread, &attr, _findPeersThreadRoutine, info) == 0)
                manager->dnsThreadCount++;
        }

        for (addr = addrList = _addressLookup(dns_seeds[0]);
             addr && !UInt128IsZero(*addr); addr++) {
            array_add(manager->peers, ((const BRPeer) {*addr, STANDARD_PORT, services, now, 0}));
        }

        if (addrList) free(addrList);
        ts.tv_sec = 0;
        ts.tv_nsec = 1;

        do {
            pthread_mutex_unlock(&manager->lock);
            nanosleep(&ts, NULL); // pthread_yield() isn't POSIX standard :(
            pthread_mutex_lock(&manager->lock);
        } while (manager->dnsThreadCount > 0 && array_count(manager->peers) < PEER_MAX_CONNECTIONS);

        qsort(manager->peers, array_count(manager->peers), sizeof(*manager->peers),
              _peerTimestampCompare);
    }
}

static void _peerConnected(void *info) {
    BRPeer *peer = ((PeerCallbackInfo *) info)->peer;
    BRPeerManager *manager = ((PeerCallbackInfo *) info)->manager;
    PeerCallbackInfo *peerInfo;
    time_t now = time(NULL);

    pthread_mutex_lock(&manager->lock);
    if (peer->timestamp > now + 2 * 60 * 60 || peer->timestamp < now - 2 * 60 * 60)
        peer->timestamp = now; // sanity check

    // TODO: XXX does this work with 0.11 pruned nodes?
    if (!(peer->services & SERVICES_NODE_NETWORK)) {
        peer_log(peer, "node doesn't carry full blocks");
        BRPeerDisconnect(peer);
    } else if (BRPeerLastBlock(peer) + 10 < manager->lastBlock->height) {
        peer_log(peer, "node isn't synced");
        BRPeerDisconnect(peer);
    } else if (BRPeerVersion(peer) >= 70011 && !(peer->services & SERVICES_NODE_BLOOM)) {
        peer_log(peer, "node doesn't support SPV mode");
        BRPeerDisconnect(peer);
    } else if (manager->downloadPeer && // check if we should stick with the existing download peer
               (BRPeerLastBlock(manager->downloadPeer) >= BRPeerLastBlock(peer) ||
                manager->lastBlock->height >= BRPeerLastBlock(peer))) {
        if (manager->lastBlock->height >=
            BRPeerLastBlock(peer)) { // only load bloom filter if we're done syncing
            manager->connectFailureCount = 0; // also reset connect failure count if we're already synced
            _PeerManagerLoadBloomFilter(manager, peer);
            _PeerManagerPublishPendingTx(manager, peer);
            peerInfo = calloc(1, sizeof(*peerInfo));
            assert(peerInfo != NULL);
            peerInfo->peer = peer;
            peerInfo->manager = manager;
            BRPeerSendPing(peer, peerInfo, _loadBloomFilterDone);
        }
    } else { // select the peer with the lowest ping time to download the chain from if we're behind
        // BUG: XXX a malicious peer can report a higher lastblock to make us select them as the download peer, if
        // two peers agree on lastblock, use one of those two instead
        for (size_t i = array_count(manager->connectedPeers); i > 0; i--) {
            BRPeer *p = manager->connectedPeers[i - 1];

            if (BRPeerConnectStatus(p) != BRPeerStatusConnected) continue;
            if ((BRPeerPingTime(p) < BRPeerPingTime(peer) &&
                 BRPeerLastBlock(p) >= BRPeerLastBlock(peer)) ||
                BRPeerLastBlock(p) > BRPeerLastBlock(peer))
                peer = p;
        }

        if (manager->downloadPeer) BRPeerDisconnect(manager->downloadPeer);
        manager->downloadPeer = peer;
        manager->isConnected = 1;
        manager->estimatedHeight = BRPeerLastBlock(peer);
        _PeerManagerLoadBloomFilter(manager, peer);
        BRPeerSetCurrentBlockHeight(peer, manager->lastBlock->height);
        _PeerManagerPublishPendingTx(manager, peer);

        if (manager->lastBlock->height < BRPeerLastBlock(peer)) { // start blockchain sync
            UInt256 locators[_PeerManagerBlockLocators(manager, NULL, 0)];
            size_t count = _PeerManagerBlockLocators(manager, locators,
                                                     sizeof(locators) / sizeof(*locators));

            BRPeerScheduleDisconnect(peer, PROTOCOL_TIMEOUT); // schedule sync timeout

            // request just block headers up to a week before earliestKeyTime, and then merkleblocks after that
            // we do not reset connect failure count yet incase this request times out
            if (manager->lastBlock->timestamp + 7 * 24 * 60 * 60 >= manager->earliestKeyTime) {
                BRPeerSendGetblocks(peer, locators, count, UINT256_ZERO);
            } else BRPeerSendGetheaders(peer, locators, count, UINT256_ZERO);
        } else { // we're already synced
            manager->connectFailureCount = 0; // reset connect failure count
            _PeerManagerLoadMempools(manager);
        }
    }

    //BRPeerSendGetAsset(peer, (const uint8_t) "ROSHII");

    pthread_mutex_unlock(&manager->lock);
}

static void _peerDisconnected(void *info, int error) {
    BRPeer *peer = ((PeerCallbackInfo *) info)->peer;
    BRPeerManager *manager = ((PeerCallbackInfo *) info)->manager;
    TxPeerList *peerList;
    int willSave = 0, willReconnect = 0, txError = 0;
    size_t txCount = 0;

    //free(info);
    pthread_mutex_lock(&manager->lock);

    void *txInfo[array_count(manager->publishedTx)];
    void (*txCallback[array_count(manager->publishedTx)])(void *, int);

    if (error == EPROTO) { // if it's protocol error, the peer isn't following standard policy
        _PeerManagerPeerMisbehavin(manager, peer);
    } else if (error) { // timeout or some non-protocol related network error
        for (size_t i = array_count(manager->peers); i > 0; i--) {
            if (BRPeerEq(&manager->peers[i - 1], peer)) array_rm(manager->peers, i - 1);
        }

        manager->connectFailureCount++;

        // if it's a timeout and there's pending tx publish callbacks, the tx publish timed out
        // BUG: XXX what if it's a connect timeout and not a publish timeout?
        if (error == ETIMEDOUT && (peer != manager->downloadPeer || manager->syncStartHeight == 0 ||
                                   array_count(manager->connectedPeers) == 1))
            txError = ETIMEDOUT;
    }

    for (size_t i = array_count(manager->txRelays); i > 0; i--) {
        peerList = &manager->txRelays[i - 1];

        for (size_t j = array_count(peerList->peers); j > 0; j--) {
            if (BRPeerEq(&peerList->peers[j - 1], peer)) array_rm(peerList->peers, j - 1);
        }
    }

    if (peer == manager->downloadPeer) { // download peer disconnected
        manager->isConnected = 0;
        manager->downloadPeer = NULL;
        if (manager->connectFailureCount > MAX_CONNECT_FAILURES)
            manager->connectFailureCount = MAX_CONNECT_FAILURES;
    }

    if (!manager->isConnected && manager->connectFailureCount == MAX_CONNECT_FAILURES) {
        _PeerManagerSyncStopped(manager);

        // clear out stored peers so we get a fresh list from DNS on next connect attempt
        array_clear(manager->peers);
        txError = ENOTCONN; // trigger any pending tx publish callbacks
        willSave = 1;
        peer_log(peer, "sync failed");
    } else if (manager->connectFailureCount < MAX_CONNECT_FAILURES) willReconnect = 1;

    if (txError) {
        for (size_t i = array_count(manager->publishedTx); i > 0; i--) {
            if (manager->publishedTx[i - 1].callback == NULL) continue;
            peer_log(peer, "transaction canceled: %s", strerror(txError));
            txInfo[txCount] = manager->publishedTx[i - 1].info;
            txCallback[txCount] = manager->publishedTx[i - 1].callback;
            txCount++;
            BRTransactionFree(manager->publishedTx[i - 1].tx);
            array_rm(manager->publishedTxHashes, i - 1);
            array_rm(manager->publishedTx, i - 1);
        }
    }

    for (size_t i = array_count(manager->connectedPeers); i > 0; i--) {
        if (manager->connectedPeers[i - 1] != peer) continue;
        array_rm(manager->connectedPeers, i - 1);
        break;
    }

    BRPeerFree(peer);
    pthread_mutex_unlock(&manager->lock);

    for (size_t i = 0; i < txCount; i++) {
        txCallback[i](txInfo[i], txError);
    }

//    if (willSave && manager->savePeers) manager->savePeers(manager->info, 1, NULL, 0);
//    if (willSave && manager->syncStopped) manager->syncStopped(manager->info, error);
    if (willSave && manager->savePeers && manager->isConnected)
        manager->savePeers(manager->info, 1, NULL, 0);
    if (willSave && manager->syncStopped && manager->isConnected)
        manager->syncStopped(manager->info, error);
    if (willReconnect) BRPeerManagerConnect(manager); // try connecting to another peer
//    if (manager->txStatusUpdate) manager->txStatusUpdate(manager->info);
    if (manager->txStatusUpdate && manager->isConnected) manager->txStatusUpdate(manager->info);
}

static void _peerRelayedPeers(void *info, const BRPeer peers[], size_t peersCount) {
    BRPeer *peer = ((PeerCallbackInfo *) info)->peer;
    BRPeerManager *manager = ((PeerCallbackInfo *) info)->manager;
    time_t now = time(NULL);

    pthread_mutex_lock(&manager->lock);
    peer_log(peer, "relayed %zu peer(s)", peersCount);

    array_add_array(manager->peers, peers, peersCount);
    qsort(manager->peers, array_count(manager->peers), sizeof(*manager->peers),
          _peerTimestampCompare);

    // limit total to 2500 peers
    if (array_count(manager->peers) > 2500) array_set_count(manager->peers, 2500);
    peersCount = array_count(manager->peers);

    // remove peers more than 3 hours old, or until there are only 1000 left
    while (peersCount > 1000 &&
           manager->peers[peersCount - 1].timestamp + 3 * 60 * 60 < now)
        peersCount--;
    array_set_count(manager->peers, peersCount);

    BRPeer save[peersCount];

    for (size_t i = 0; i < peersCount; i++) save[i] = manager->peers[i];
    pthread_mutex_unlock(&manager->lock);

    // peer relaying is complete when we receive <1000
    if (peersCount > 1 && peersCount < 1000 &&
        manager->savePeers)
        manager->savePeers(manager->info, 1, save, peersCount);
}

static void _peerRelayedTx(void *info, BRTransaction *tx) {
    BRPeer *peer = ((PeerCallbackInfo *) info)->peer;
    BRPeerManager *manager = ((PeerCallbackInfo *) info)->manager;
    void *txInfo = NULL;
    void (*txCallback)(void *, int) = NULL;
    int isWalletTx = 0, hasPendingCallbacks = 0;
    size_t relayCount = 0;

    pthread_mutex_lock(&manager->lock);
    peer_log(peer, "relayed tx: %s", u256_hex_encode(tx->txHash));

    for (size_t i = array_count(manager->publishedTx);
         i > 0; i--) { // see if tx is in list of published tx
        if (UInt256Eq(manager->publishedTxHashes[i - 1], tx->txHash)) {
            txInfo = manager->publishedTx[i - 1].info;
            txCallback = manager->publishedTx[i - 1].callback;
            manager->publishedTx[i - 1].info = NULL;
            manager->publishedTx[i - 1].callback = NULL;
            relayCount = _TxPeerListAddPeer(&manager->txRelays, tx->txHash, peer);
        } else if (manager->publishedTx[i - 1].callback != NULL) hasPendingCallbacks = 1;
    }

    // cancel tx publish timeout if no publish callbacks are pending, and syncing is done or this is not downloadPeer
    if (!hasPendingCallbacks && (manager->syncStartHeight == 0 || peer != manager->downloadPeer)) {
        BRPeerScheduleDisconnect(peer, -1); // cancel publish tx timeout
    }

    if (manager->syncStartHeight == 0 || BRWalletContainsTransaction(manager->wallet, tx)) {
        isWalletTx = BRWalletRegisterTransaction(manager->wallet, tx);
        if (isWalletTx) tx = BRWalletTransactionForHash(manager->wallet, tx->txHash);
    } else {
        BRTransactionFree(tx);
        tx = NULL;
    }

    if (tx && isWalletTx) {
        // reschedule sync timeout
        if (manager->syncStartHeight > 0 && peer == manager->downloadPeer) {
            BRPeerScheduleDisconnect(peer, PROTOCOL_TIMEOUT);
        }

        if (BRWalletAmountSentByTx(manager->wallet, tx) > 0 &&
            BRWalletTransactionIsValid(manager->wallet, tx)) {
            _PeerManagerAddTxToPublishList(manager, tx, NULL,
                                           NULL); // add valid send tx to mempool
        }

        // keep track of how many peers have or relay a tx, this indicates how likely the tx is to confirm
        // (we only need to track this after syncing is complete)
        if (manager->syncStartHeight == 0)
            relayCount = _TxPeerListAddPeer(&manager->txRelays, tx->txHash, peer);

        _TxPeerListRemovePeer(manager->txRequests, tx->txHash, peer);

        if (manager->bloomFilter != NULL) { // check if bloom filter is already being updated
            BRAddress addrs[SEQUENCE_GAP_LIMIT_EXTERNAL + SEQUENCE_GAP_LIMIT_INTERNAL];
            UInt160 hash;

            // the transaction likely consumed one or more wallet addresses, so check that at least the next <gap limit>
            // unused addresses are still matched by the bloom filter
            BRWalletUnusedAddrs(manager->wallet, addrs, SEQUENCE_GAP_LIMIT_EXTERNAL, 0);
            BRWalletUnusedAddrs(manager->wallet, addrs + SEQUENCE_GAP_LIMIT_EXTERNAL,
                                SEQUENCE_GAP_LIMIT_INTERNAL, 1);

            for (size_t i = 0; i < SEQUENCE_GAP_LIMIT_EXTERNAL + SEQUENCE_GAP_LIMIT_INTERNAL; i++) {
                if (!BRAddressHash160(&hash, addrs[i].s) ||
                    BRBloomFilterContainsData(manager->bloomFilter, hash.u8, sizeof(hash)))
                    continue;
                if (manager->bloomFilter) BRBloomFilterFree(manager->bloomFilter);
                manager->bloomFilter = NULL; // reset bloom filter so it's recreated with new wallet addresses
                _PeerManagerUpdateFilter(manager);
                break;
            }
        }
    }

    // set timestamp when tx is verified
    if (tx && relayCount >= manager->maxConnectCount && tx->blockHeight == TX_UNCONFIRMED &&
        tx->timestamp == 0) {
        _PeerManagerUpdateTx(manager, &tx->txHash, 1, TX_UNCONFIRMED, (uint32_t) time(NULL));
    }

    pthread_mutex_unlock(&manager->lock);
    if (txCallback) txCallback(txInfo, 0);
}

static void _peerHasTx(void *info, UInt256 txHash) {
    BRPeer *peer = ((PeerCallbackInfo *) info)->peer;
    BRPeerManager *manager = ((PeerCallbackInfo *) info)->manager;
    BRTransaction *tx;
    void *txInfo = NULL;
    void (*txCallback)(void *, int) = NULL;
    int isWalletTx = 0, hasPendingCallbacks = 0;
    size_t relayCount = 0;

    pthread_mutex_lock(&manager->lock);
    tx = BRWalletTransactionForHash(manager->wallet, txHash);
    peer_log(peer, "has tx: %s", u256_hex_encode(txHash));

    for (size_t i = array_count(manager->publishedTx);
         i > 0; i--) { // see if tx is in list of published tx
        if (UInt256Eq(manager->publishedTxHashes[i - 1], txHash)) {
            if (!tx) tx = manager->publishedTx[i - 1].tx;
            txInfo = manager->publishedTx[i - 1].info;
            txCallback = manager->publishedTx[i - 1].callback;
            manager->publishedTx[i - 1].info = NULL;
            manager->publishedTx[i - 1].callback = NULL;
            relayCount = _TxPeerListAddPeer(&manager->txRelays, txHash, peer);
        } else if (manager->publishedTx[i - 1].callback != NULL) hasPendingCallbacks = 1;
    }

    // cancel tx publish timeout if no publish callbacks are pending, and syncing is done or this is not downloadPeer
    if (!hasPendingCallbacks && (manager->syncStartHeight == 0 || peer != manager->downloadPeer)) {
        BRPeerScheduleDisconnect(peer, -1); // cancel publish tx timeout
    }

    if (tx) {
        isWalletTx = BRWalletRegisterTransaction(manager->wallet, tx);
        if (isWalletTx) tx = BRWalletTransactionForHash(manager->wallet, tx->txHash);

        // reschedule sync timeout
        if (manager->syncStartHeight > 0 && peer == manager->downloadPeer && isWalletTx) {
            BRPeerScheduleDisconnect(peer, PROTOCOL_TIMEOUT);
        }

        // keep track of how many peers have or relay a tx, this indicates how likely the tx is to confirm
        // (we only need to track this after syncing is complete)
        if (manager->syncStartHeight == 0)
            relayCount = _TxPeerListAddPeer(&manager->txRelays, txHash, peer);

        // set timestamp when tx is verified
        if (relayCount >= manager->maxConnectCount && tx && tx->blockHeight == TX_UNCONFIRMED &&
            tx->timestamp == 0) {
            _PeerManagerUpdateTx(manager, &txHash, 1, TX_UNCONFIRMED, (uint32_t) time(NULL));
        }

        _TxPeerListRemovePeer(manager->txRequests, txHash, peer);
    }

    pthread_mutex_unlock(&manager->lock);
    if (txCallback) txCallback(txInfo, 0);
}

static void _peerRejectedTx(void *info, UInt256 txHash, uint8_t code) {
    BRPeer *peer = ((PeerCallbackInfo *) info)->peer;
    BRPeerManager *manager = ((PeerCallbackInfo *) info)->manager;
    BRTransaction *tx, *t;

    pthread_mutex_lock(&manager->lock);
    peer_log(peer, "rejected tx: %s", u256_hex_encode(txHash));
    tx = BRWalletTransactionForHash(manager->wallet, txHash);
    _TxPeerListRemovePeer(manager->txRequests, txHash, peer);

    if (tx) {
        if (_TxPeerListRemovePeer(manager->txRelays, txHash, peer) &&
            tx->blockHeight == TX_UNCONFIRMED) {
            // set timestamp 0 to mark tx as unverified
            _PeerManagerUpdateTx(manager, &txHash, 1, TX_UNCONFIRMED, 0);
        }

        // if we get rejected for any reason other than double-spend, the peer is likely misconfigured
        if (code != REJECT_SPENT && BRWalletAmountSentByTx(manager->wallet, tx) > 0) {
            for (size_t i = 0;
                 i < tx->inCount; i++) { // check that all inputs are confirmed before dropping peer
                t = BRWalletTransactionForHash(manager->wallet, tx->inputs[i].txHash);
                if (!t || t->blockHeight != TX_UNCONFIRMED) continue;
                tx = NULL;
                break;
            }

            if (tx) _PeerManagerPeerMisbehavin(manager, peer);
        }
    }

    pthread_mutex_unlock(&manager->lock);
    if (manager->txStatusUpdate) manager->txStatusUpdate(manager->info);
}

static int
_PeerManagerVerifyBlock(BRPeerManager *manager, BRMerkleBlock *block, BRMerkleBlock *prev,
                        BRPeer *peer) {
    uint32_t transitionTime = 0;
    int r = 1;

    // check if we hit a difficulty transition, and find previous transition time
    if ((block->height % (block->height < DGW_START_BLOCK ? BLOCK_DIFFICULTY_INTERVAL
                                                          : DGW_BLOCK_DIFFICULTY_INTERVAL)) == 0) {
        BRMerkleBlock *b = block;
        UInt256 prevBlock;

        for (uint32_t i = 0; b && i < (block->height < DGW_START_BLOCK ? BLOCK_DIFFICULTY_INTERVAL
                                                                       : DGW_BLOCK_DIFFICULTY_INTERVAL); i++) {
            b = BRSetGet(manager->blocks, &b->prevBlock);
        }

        if (!b) {
            peer_log(peer, "missing previous difficulty tansition time, can't verify blockHash: %s",
                     u256_hex_encode(block->blockHash));
            r = 0;
        } else {
            transitionTime = b->timestamp;
            prevBlock = b->prevBlock;
        }

        while (b) { // free up some memory
            b = BRSetGet(manager->blocks, &prevBlock);
            if (b) prevBlock = b->prevBlock;

            if (b && (b->height % (block->height < DGW_START_BLOCK ? BLOCK_DIFFICULTY_INTERVAL
                                                                   : DGW_BLOCK_DIFFICULTY_INTERVAL)) !=
                     0) {
                BRSetRemove(manager->blocks, b);
                BRMerkleBlockFree(b);
            }
        }
    }

    // verify block difficulty
    if (r && !BRMerkleBlockVerifyDifficulty(block, prev, manager->blocks)) {
        peer_log(peer, "relayed block with invalid difficulty target %x, blockHash: %s",
                 block->target,
                 u256_hex_encode(block->blockHash));
        r = 0;
    }

    if (r) {
        BRMerkleBlock *checkpoint = BRSetGet(manager->checkpoints, block);

        // verify blockchain checkpoints
        if (checkpoint && !BRMerkleBlockEq(block, checkpoint)) {
            peer_log(peer, "relayed a block that differs from the checkpoint at height %"
                    PRIu32
                    ", blockHash: %s, "
                    "expected: %s", block->height, u256_hex_encode(block->blockHash),
                     u256_hex_encode(checkpoint->blockHash));
            r = 0;
        }
    }

    return r;
}

static void _peerRelayedBlock(void *info, BRMerkleBlock *block) {
    BRPeer *peer = ((PeerCallbackInfo *) info)->peer;
    BRPeerManager *manager = ((PeerCallbackInfo *) info)->manager;
    size_t txCount = BRMerkleBlockTxHashes(block, NULL, 0);
    UInt256 _txHashes[(sizeof(UInt256) * txCount <= 0x1000) ? txCount : 0],
            *txHashes = (sizeof(UInt256) * txCount <= 0x1000) ? _txHashes : malloc(
            txCount * sizeof(*txHashes));
    size_t i, j, fpCount = 0, saveCount = 0;
    BRMerkleBlock orphan, *b, *b2, *prev, *next = NULL;
    uint32_t txTime = 0;

    assert(txHashes != NULL);
    txCount = BRMerkleBlockTxHashes(block, txHashes, txCount);
    pthread_mutex_lock(&manager->lock);
    prev = BRSetGet(manager->blocks, &block->prevBlock);

    if (prev) {
        txTime = block->timestamp / 2 + prev->timestamp / 2;
        block->height = prev->height + 1;
    }

    // track the observed bloom filter false positive rate using a low pass filter to smooth out variance
    if (peer == manager->downloadPeer && block->totalTx > 0) {
        for (i = 0; i < txCount; i++) { // wallet tx are not false-positives
            if (!BRWalletTransactionForHash(manager->wallet, txHashes[i])) fpCount++;
        }

        // moving average number of tx-per-block
        manager->averageTxPerBlock = manager->averageTxPerBlock * 0.999 + block->totalTx * 0.001;

        // 1% low pass filter, also weights each block by total transactions, compared to the avarage
        manager->fpRate =
                manager->fpRate * (1.0 - 0.01 * block->totalTx / manager->averageTxPerBlock) +
                0.01 * fpCount / manager->averageTxPerBlock;

        // false positive rate sanity check
        if (BRPeerConnectStatus(peer) == BRPeerStatusConnected &&
            manager->fpRate > BLOOM_DEFAULT_FALSEPOSITIVE_RATE * 10.0) {
            peer_log(peer, "bloom filter false positive rate %f too high after %"
                    PRIu32
                    " blocks, disconnecting...",
                     manager->fpRate, manager->lastBlock->height + 1 - manager->filterUpdateHeight);
            BRPeerDisconnect(peer);
        } else if (manager->lastBlock->height + 500 < BRPeerLastBlock(peer) &&
                   manager->fpRate > BLOOM_REDUCED_FALSEPOSITIVE_RATE * 10.0) {
            _PeerManagerUpdateFilter(manager); // rebuild bloom filter when it starts to degrade
        }
    }

    // ignore block headers that are newer than one week before earliestKeyTime (it's a header if it has 0 totalTx)
    if (block->totalTx == 0 &&
        block->timestamp + 7 * 24 * 60 * 60 > manager->earliestKeyTime + 2 * 60 * 60) {
        BRMerkleBlockFree(block);
        block = NULL;
    } else if (manager->bloomFilter ==
               NULL) { // ingore potentially incomplete blocks when a filter update is pending
        BRMerkleBlockFree(block);
        block = NULL;

        if (peer == manager->downloadPeer &&
            manager->lastBlock->height < manager->estimatedHeight) {
            BRPeerScheduleDisconnect(peer, PROTOCOL_TIMEOUT); // reschedule sync timeout
            manager->connectFailureCount = 0; // reset failure count once we know our initial request didn't timeout
        }
    } else if (!prev) { // block is an orphan
//        peer_log(peer, "relayed (by X) Orphan block %s, previous %s, last block is %s, height %"
//                PRIu32,
//                 u256_hex_encode(block->blockHash), u256_hex_encode(block->prevBlock),
//                 u256_hex_encode(manager->lastBlock->blockHash), manager->lastBlock->height);

        if (block->timestamp + 7 * 24 * 60 * 60 <
            time(NULL)) { // ignore orphans older than one week ago
            BRMerkleBlockFree(block);
            block = NULL;
        } else {
            // call getblocks, unless we already did with the previous block, or we're still syncing
            if (manager->lastBlock->height >= BRPeerLastBlock(peer) &&
                (!manager->lastOrphan ||
                 !UInt256Eq(manager->lastOrphan->blockHash, block->prevBlock))) {
                UInt256 locators[_PeerManagerBlockLocators(manager, NULL, 0)];
                size_t locatorsCount = _PeerManagerBlockLocators(manager, locators,
                                                                 sizeof(locators) /
                                                                 sizeof(*locators));
                BRPeerSendGetblocks(peer, locators, locatorsCount, UINT256_ZERO);
            }

            BRSetAdd(manager->orphans,
                     block); // BUG: limit total orphans to avoid memory exhaustion attack
            manager->lastOrphan = block;
        }
    } else if (!_PeerManagerVerifyBlock(manager, block, prev, peer)) { // block is invalid
        peer_log(peer, "relayed invalid block");
        BRMerkleBlockFree(block);
        block = NULL;
        _PeerManagerPeerMisbehavin(manager, peer);
    } else if (UInt256Eq(block->prevBlock,
                         manager->lastBlock->blockHash)) { // new block extends main chain
        if ((block->height % 500) == 0 || txCount > 0 || block->height >= BRPeerLastBlock(peer)) {
            peer_log(peer, "adding block #%"
                    PRIu32
                    ", false positive rate: %f", block->height, manager->fpRate);
        }

        BRSetAdd(manager->blocks, block);
        manager->lastBlock = block;
        if (txCount > 0) _PeerManagerUpdateTx(manager, txHashes, txCount, block->height, txTime);
        if (manager->downloadPeer)
            BRPeerSetCurrentBlockHeight(manager->downloadPeer, block->height);

        if (block->height < manager->estimatedHeight && peer == manager->downloadPeer) {
            BRPeerScheduleDisconnect(peer, PROTOCOL_TIMEOUT); // reschedule sync timeout
            manager->connectFailureCount = 0; // reset failure count once we know our initial request didn't timeout
        }

        if ((block->height % BLOCK_DIFFICULTY_INTERVAL) == 0)
            saveCount = 1; // save transition block immediately

        if (block->height == manager->estimatedHeight) { // chain download is complete
            saveCount = (block->height % BLOCK_DIFFICULTY_INTERVAL) + BLOCK_DIFFICULTY_INTERVAL + 1;
            _PeerManagerLoadMempools(manager);
        }
    } else if (BRSetContains(manager->blocks,
                             block)) { // we already have the block (or at least the header)
        if ((block->height % 500) == 0 || txCount > 0 || block->height >= BRPeerLastBlock(peer)) {
            peer_log(peer, "relayed existing block #%u",block->height);
        }

        b = manager->lastBlock;
        while (b && b->height > block->height)
            b = BRSetGet(manager->blocks, &b->prevBlock); // is block in main chain?

        if (BRMerkleBlockEq(b,
                            block)) { // if it's not on a fork, set block heights for its transactions
            if (txCount > 0)
                _PeerManagerUpdateTx(manager, txHashes, txCount, block->height, txTime);
            if (block->height == manager->lastBlock->height) manager->lastBlock = block;
        }

        b = BRSetAdd(manager->blocks, block);

        if (b != block) {
            if (BRSetGet(manager->orphans, b) == b) BRSetRemove(manager->orphans, b);
            if (manager->lastOrphan == b) manager->lastOrphan = NULL;
            BRMerkleBlockFree(b);
        }
    } else if (manager->lastBlock->height < BRPeerLastBlock(peer) &&
               block->height >
               manager->lastBlock->height + 1) { // special case, new block mined durring rescan
        peer_log(peer, "marking new block #%"
                PRIu32
                " as orphan until rescan completes", block->height);
        BRSetAdd(manager->orphans, block); // mark as orphan til we're caught up
        manager->lastOrphan = block;
    } else if (block->height <= checkpoint_array[CHECKPOINT_COUNT -
                                                 1].height) { // fork is older than last checkpoint
        peer_log(peer, "ignoring block on fork older than most recent checkpoint, block #%"
                PRIu32
                ", hash: %s",
                 block->height, u256_hex_encode(UInt256Reverse(block->blockHash)));
        BRMerkleBlockFree(block);
        block = NULL;
    } else { // new block is on a fork
        peer_log(peer, "chain fork reached height %"
                PRIu32, block->height);
        BRSetAdd(manager->blocks, block);

        if (block->height >
            manager->lastBlock->height) { // check if fork is now longer than main chain
            b = block;
            b2 = manager->lastBlock;

            while (b && b2 &&
                   !BRMerkleBlockEq(b, b2)) { // walk back to where the fork joins the main chain
                b = BRSetGet(manager->blocks, &b->prevBlock);
                if (b && b->height < b2->height) b2 = BRSetGet(manager->blocks, &b2->prevBlock);
            }

            peer_log(peer, "reorganizing chain from height %"
                    PRIu32
                    ", new height is %"
                    PRIu32, b->height, block->height);

            BRWalletSetTxUnconfirmedAfter(manager->wallet,
                                          b->height); // mark tx after the join point as unconfirmed

            b = block;

            while (b && b2 &&
                   b->height > b2->height) { // set transaction heights for new main chain
                size_t count = BRMerkleBlockTxHashes(b, NULL, 0);
                uint32_t height = b->height, timestamp = b->timestamp;

                if (count > txCount) {
                    txHashes = (txHashes != _txHashes) ? realloc(txHashes,
                                                                 count * sizeof(*txHashes)) :
                               malloc(count * sizeof(*txHashes));
                    assert(txHashes != NULL);
                    txCount = count;
                }

                count = BRMerkleBlockTxHashes(b, txHashes, count);
                b = BRSetGet(manager->blocks, &b->prevBlock);
                if (b) timestamp = timestamp / 2 + b->timestamp / 2;
                if (count > 0)
                    BRWalletUpdateTransactions(manager->wallet, txHashes, count, height, timestamp);
            }

            manager->lastBlock = block;

            if (block->height == manager->estimatedHeight) { // chain download is complete
                saveCount =
                        (block->height % BLOCK_DIFFICULTY_INTERVAL) + BLOCK_DIFFICULTY_INTERVAL + 1;
                _PeerManagerLoadMempools(manager);
            }
        }
    }

    if (txHashes != _txHashes) free(txHashes);

    if (block && block->height != BLOCK_UNKNOWN_HEIGHT) {
        if (block->height > manager->estimatedHeight) manager->estimatedHeight = block->height;

        // check if the next block was received as an orphan
        orphan.prevBlock = block->blockHash;
        next = BRSetRemove(manager->orphans, &orphan);
    }

    BRMerkleBlock *saveBlocks[saveCount];

    for (i = 0, b = block; b && i < saveCount; i++) {
        assert(b->height != BLOCK_UNKNOWN_HEIGHT); // verify all blocks to be saved are in the chain
        saveBlocks[i] = b;
        b = BRSetGet(manager->blocks, &b->prevBlock);
    }

    // make sure the set of blocks to be saved starts at a difficulty interval
    j = (i > 0) ? saveBlocks[i - 1]->height % BLOCK_DIFFICULTY_INTERVAL : 0;
    if (j > 0) i -= (i > BLOCK_DIFFICULTY_INTERVAL - j) ? BLOCK_DIFFICULTY_INTERVAL - j : i;
    assert(i == 0 || (saveBlocks[i - 1]->height % BLOCK_DIFFICULTY_INTERVAL) == 0);
    pthread_mutex_unlock(&manager->lock);
    if (i > 0 && manager->saveBlocks)
        manager->saveBlocks(manager->info, (i > 1 ? 1 : 0), saveBlocks, i);

    if (block && block->height != BLOCK_UNKNOWN_HEIGHT && block->height >= BRPeerLastBlock(peer) &&
        manager->txStatusUpdate) {
        manager->txStatusUpdate(
                manager->info); // notify that transaction confirmations may have changed
    }

    if (next) _peerRelayedBlock(info, next);
}

static void _peerDataNotfound(void *info, const UInt256 txHashes[], size_t txCount,
                              const UInt256 blockHashes[], size_t blockCount) {
    BRPeer *peer = ((PeerCallbackInfo *) info)->peer;
    BRPeerManager *manager = ((PeerCallbackInfo *) info)->manager;

    pthread_mutex_lock(&manager->lock);

    for (size_t i = 0; i < txCount; i++) {
        _TxPeerListRemovePeer(manager->txRelays, txHashes[i], peer);
        _TxPeerListRemovePeer(manager->txRequests, txHashes[i], peer);
    }

    pthread_mutex_unlock(&manager->lock);
}

static void _peerSetFeePerKb(void *info, uint64_t feePerKb) {
    BRPeer *p, *peer = ((PeerCallbackInfo *) info)->peer;
    BRPeerManager *manager = ((PeerCallbackInfo *) info)->manager;
    uint64_t maxFeePerKb = 0, secondFeePerKb = 0;

    pthread_mutex_lock(&manager->lock);

    for (size_t i = array_count(manager->connectedPeers);
         i > 0; i--) { // find second highest fee rate
        p = manager->connectedPeers[i - 1];
        if (BRPeerConnectStatus(p) != BRPeerStatusConnected) continue;
        if (BRPeerFeePerKb(p) > maxFeePerKb)
            secondFeePerKb = maxFeePerKb, maxFeePerKb = BRPeerFeePerKb(p);
    }

    if (secondFeePerKb * 3 / 2 > DEFAULT_FEE_PER_KB && secondFeePerKb * 3 / 2 <= MAX_FEE_PER_KB &&
        secondFeePerKb * 3 / 2 > BRWalletFeePerKb(manager->wallet)) {
        peer_log(peer, "increasing feePerKb to %llu based on feefilter messages from peers",
                 secondFeePerKb * 3 / 2);
        BRWalletSetFeePerKb(manager->wallet, secondFeePerKb * 3 / 2);
    }

    pthread_mutex_unlock(&manager->lock);
}

static BRTransaction *_peerRequestedTx(void *info, UInt256 txHash) {
    BRPeer *peer = ((PeerCallbackInfo *) info)->peer;
    BRPeerManager *manager = ((PeerCallbackInfo *) info)->manager;
//    PeerCallbackInfo *pingInfo;
    BRTransaction *tx = NULL;
    void *txInfo = NULL;
    void (*txCallback)(void *, int) = NULL;
    int hasPendingCallbacks = 0, error = 0;

    pthread_mutex_lock(&manager->lock);

    for (size_t i = array_count(manager->publishedTx); i > 0; i--) {
        if (UInt256Eq(manager->publishedTxHashes[i - 1], txHash)) {
            tx = manager->publishedTx[i - 1].tx;
            txInfo = manager->publishedTx[i - 1].info;
            txCallback = manager->publishedTx[i - 1].callback;
            manager->publishedTx[i - 1].info = NULL;
            manager->publishedTx[i - 1].callback = NULL;

            if (tx && !BRWalletTransactionIsValid(manager->wallet, tx)) {
                error = EINVAL;
                array_rm(manager->publishedTx, i - 1);
                array_rm(manager->publishedTxHashes, i - 1);

                if (!BRWalletTransactionForHash(manager->wallet, txHash)) {
                    BRTransactionFree(tx);
                    tx = NULL;
                }
            }
        } else if (manager->publishedTx[i - 1].callback != NULL) hasPendingCallbacks = 1;
    }

    // cancel tx publish timeout if no publish callbacks are pending, and syncing is done or this is not downloadPeer
    if (!hasPendingCallbacks && (manager->syncStartHeight == 0 || peer != manager->downloadPeer)) {
        BRPeerScheduleDisconnect(peer, -1); // cancel publish tx timeout
    }

    if (tx && !error) {
        _TxPeerListAddPeer(&manager->txRelays, txHash, peer);
        BRWalletRegisterTransaction(manager->wallet, tx);
    }

//    pingInfo = calloc(1, sizeof(*pingInfo));
//    assert(pingInfo != NULL);
//    pingInfo->peer = peer;
//    pingInfo->manager = manager;
//    pingInfo->hash = txHash;
//    PeerSendPing(peer, pingInfo, _peerRequestedTxPingDone);
    pthread_mutex_unlock(&manager->lock);
    if (txCallback) txCallback(txInfo, error);
    return tx;
}

static int _peerNetworkIsReachable(void *info) {
    BRPeerManager *manager = ((PeerCallbackInfo *) info)->manager;

    return (manager->networkIsReachable) ? manager->networkIsReachable(manager->info) : 1;
}

static void _peerThreadCleanup(void *info) {
    BRPeerManager *manager = ((PeerCallbackInfo *) info)->manager;

    free(info);
    if (manager->threadCleanup) manager->threadCleanup(manager->info);
}

static void _dummyThreadCleanup(void *info) {
}

// returns a newly allocated PeerManager struct that must be freed by calling PeerManagerFree()
BRPeerManager *BRPeerManagerNew(BRWallet *wallet, uint32_t earliestKeyTime, BRMerkleBlock **blocks,
                                size_t blocksCount,
                                const BRPeer *peers, size_t peersCount) {
    BRPeerManager *manager = calloc(1, sizeof(*manager));
    BRMerkleBlock orphan, *block = NULL;

    assert(manager != NULL);
    assert(wallet != NULL);
    assert(blocks != NULL || blocksCount == 0);
    assert(peers != NULL || peersCount == 0);
    manager->wallet = wallet;
    manager->earliestKeyTime = earliestKeyTime;
    manager->averageTxPerBlock = 1400;
    manager->maxConnectCount = PEER_MAX_CONNECTIONS;
    array_new(manager->peers, peersCount);
    if (peers) array_add_array(manager->peers, peers, peersCount);
    qsort(manager->peers, array_count(manager->peers), sizeof(*manager->peers),
          _peerTimestampCompare);
    array_new(manager->connectedPeers, PEER_MAX_CONNECTIONS);
    manager->blocks = BRSetNew(BRMerkleBlockHash, BRMerkleBlockEq, blocksCount);
    manager->orphans = BRSetNew(_PrevBlockHash, _PrevBlockEq,
                                blocksCount); // orphans are indexed by prevBlock
    manager->checkpoints = BRSetNew(_BlockHeightHash, _BlockHeightEq,
                                    100); // checkpoints are indexed by height

    for (size_t i = 0; i < CHECKPOINT_COUNT; i++) {
        block = BRMerkleBlockNew();
        block->height = checkpoint_array[i].height;
        block->blockHash = UInt256Reverse(u256_hex_decode(checkpoint_array[i].hash));
        block->timestamp = checkpoint_array[i].timestamp;
        block->target = checkpoint_array[i].target;
        BRSetAdd(manager->checkpoints, block);
        BRSetAdd(manager->blocks, block);
        if (i == 0 || block->timestamp + 7 * 24 * 60 * 60 < manager->earliestKeyTime)
            manager->lastBlock = block;
    }

    block = NULL;

    for (size_t i = 0; blocks && i < blocksCount; i++) {
        assert(blocks[i]->height !=
               BLOCK_UNKNOWN_HEIGHT); // height must be saved/restored along with serialized block
        BRSetAdd(manager->orphans, blocks[i]);

        if ((blocks[i]->height % BLOCK_DIFFICULTY_INTERVAL) == 0 &&
            (!block || blocks[i]->height > block->height))
            block = blocks[i]; // find last transition block
    }

    while (block) {
        BRSetAdd(manager->blocks, block);
        manager->lastBlock = block;
        orphan.prevBlock = block->prevBlock;
        BRSetRemove(manager->orphans, &orphan);
        orphan.prevBlock = block->blockHash;
        block = BRSetGet(manager->orphans, &orphan);
    }

    array_new(manager->txRelays, 10);
    array_new(manager->txRequests, 10);
    array_new(manager->publishedTx, 10);
    array_new(manager->publishedTxHashes, 10);
    pthread_mutex_init(&manager->lock, NULL);
    manager->threadCleanup = _dummyThreadCleanup;
    return manager;
}

// not thread-safe, set callbacks once before calling PeerManagerConnect()
// info is a void pointer that will be passed along with each callback call
// void syncStarted(void *) - called when blockchain syncing starts
// void syncStopped(void *, int) - called when blockchain syncing stops, error is an errno.h code
// void txStatusUpdate(void *) - called when transaction status may have changed such as when a new block arrives
// void saveBlocks(void *, int, MerkleBlock *[], size_t) - called when blocks should be saved to the persistent store
// - if replace is true, remove any previously saved blocks first
// void savePeers(void *, int, const Peer[], size_t) - called when peers should be saved to the persistent store
// - if replace is true, remove any previously saved peers first
// int networkIsReachable(void *) - must return true when networking is available, false otherwise
// void threadCleanup(void *) - called before a thread terminates to faciliate any needed cleanup
void BRPeerManagerSetCallbacks(BRPeerManager *manager, void *info,
                               void (*syncStarted)(void *info),
                               void (*syncStopped)(void *info, int error),
                               void (*txStatusUpdate)(void *info),
                               void (*saveBlocks)(void *info, int replace, BRMerkleBlock *blocks[],
                                                  size_t blocksCount),
                               void (*savePeers)(void *info, int replace, const BRPeer peers[],
                                                 size_t peersCount),
                               int (*networkIsReachable)(void *info),
                               void (*threadCleanup)(void *info)) {
    assert(manager != NULL);
    manager->info = info;
    manager->syncStarted = syncStarted;
    manager->syncStopped = syncStopped;
    manager->txStatusUpdate = txStatusUpdate;
    manager->saveBlocks = saveBlocks;
    manager->savePeers = savePeers;
    manager->networkIsReachable = networkIsReachable;
    manager->threadCleanup = (threadCleanup) ? threadCleanup : _dummyThreadCleanup;
}

static int _PeerManagerRescan(BRPeerManager *manager, BRMerkleBlock *newLastBlock) {
    if (NULL == newLastBlock) return 0;

    manager->lastBlock = newLastBlock;

    if (manager->downloadPeer) { // disconnect the current download peer so a new random one will be selected
        for (size_t i = array_count(manager->peers); i > 0; i--) {
            if (BRPeerEq(&manager->peers[i - 1], manager->downloadPeer))
                array_rm(manager->peers, i - 1);
        }
        BRPeerDisconnect(manager->downloadPeer);
    }

    manager->syncStartHeight = 0; // a syncStartHeight of 0 indicates that syncing hasn't started yet
    return 1;
}

// specifies a single fixed peer to use when connecting to the ravenwallet network
// set address to UINT128_ZERO to revert to default behavior
void BRPeerManagerSetFixedPeer(BRPeerManager *manager, UInt128 address, uint16_t port) {
    assert(manager != NULL);
    BRPeerManagerDisconnect(manager);
    pthread_mutex_lock(&manager->lock);
    manager->maxConnectCount = UInt128IsZero(address) ? PEER_MAX_CONNECTIONS : 1;
    manager->fixedPeer = ((const BRPeer) {address, port, 0, 0, 0});
    array_clear(manager->peers);
    pthread_mutex_unlock(&manager->lock);
}

// current connect status
BRPeerStatus BRPeerManagerConnectStatus(BRPeerManager *manager) {
    BRPeerStatus status = BRPeerStatusDisconnected;

    assert(manager != NULL);
    pthread_mutex_lock(&manager->lock);
    if (manager->isConnected != 0) status = BRPeerStatusConnected;

    for (size_t i = array_count(manager->connectedPeers);
         i > 0 && status == BRPeerStatusDisconnected; i--) {
        if (BRPeerConnectStatus(manager->connectedPeers[i - 1]) == BRPeerStatusDisconnected)
            continue;
        status = BRPeerStatusConnecting;
    }

    pthread_mutex_unlock(&manager->lock);
    return status;
}

// true if currently connected to at least one peer
int BRPeerManagerIsConnected(BRPeerManager *manager) {
    int isConnected;

    assert(manager != NULL);
    pthread_mutex_lock(&manager->lock);
    isConnected = manager->isConnected;
    pthread_mutex_unlock(&manager->lock);
    return isConnected;
}

// connect to ravencoin peer-to-peer network (also call this whenever networkIsReachable() status changes)
void BRPeerManagerConnect(BRPeerManager *manager) {
    assert(manager != NULL);
    pthread_mutex_lock(&manager->lock);
    if (manager->connectFailureCount >= MAX_CONNECT_FAILURES)
        manager->connectFailureCount = 0; //this is a manual retry

    if ((!manager->downloadPeer || manager->lastBlock->height < manager->estimatedHeight) &&
        manager->syncStartHeight == 0) {
        manager->syncStartHeight = manager->lastBlock->height + 1;
        pthread_mutex_unlock(&manager->lock);
        if (manager->syncStarted) manager->syncStarted(manager->info);
        pthread_mutex_lock(&manager->lock);
    }

    for (size_t i = array_count(manager->connectedPeers); i > 0; i--) {
        BRPeer *p = manager->connectedPeers[i - 1];

        if (BRPeerConnectStatus(p) == BRPeerStatusConnecting) BRPeerConnect(p);
    }

    if (array_count(manager->connectedPeers) < manager->maxConnectCount) {
        time_t now = time(NULL);
        BRPeer *peers;

        if (array_count(manager->peers) < manager->maxConnectCount ||
            manager->peers[manager->maxConnectCount - 1].timestamp + 3 * 24 * 60 * 60 < now) {
            _PeerManagerFindPeers(manager);
        }

        array_new(peers, 100);
        array_add_array(peers, manager->peers,
                        (array_count(manager->peers) < 100) ? array_count(manager->peers) : 100);

        while (array_count(peers) > 0 &&
               array_count(manager->connectedPeers) < manager->maxConnectCount) {
            size_t i = BRRand((uint32_t) array_count(peers)); // index of random peer
            PeerCallbackInfo *info;

            i = i * i / array_count(
                    peers); // bias random peer selection toward peers with more recent timestamp

            for (size_t j = array_count(manager->connectedPeers); i != SIZE_MAX && j > 0; j--) {
                if (!BRPeerEq(&peers[i], manager->connectedPeers[j - 1])) continue;
                array_rm(peers, i); // already in connectedPeers
                i = SIZE_MAX;
            }

            if (i != SIZE_MAX) {
                info = calloc(1, sizeof(*info));
                assert(info != NULL);
                info->manager = manager;
                info->peer = BRPeerNew();
                *info->peer = peers[i];
                array_rm(peers, i);
                array_add(manager->connectedPeers, info->peer);
                BRPeerSetCallbacks(info->peer, info, _peerConnected, _peerDisconnected,
                                   _peerRelayedPeers,
                                   _peerRelayedTx, _peerHasTx, _peerRejectedTx, _peerRelayedBlock,
                                   _peerDataNotfound,
                                   _peerSetFeePerKb, _peerRequestedTx, _peerNetworkIsReachable,
                                   _peerThreadCleanup);
                BRPeerSetEarliestKeyTime(info->peer, manager->earliestKeyTime);
                BRPeerConnect(info->peer);
            }
        }

        array_free(peers);
    }

    if (array_count(manager->connectedPeers) == 0) {
//        peer_log(&PEER_NONE, "sync failed");
        _PeerManagerSyncStopped(manager);
        pthread_mutex_unlock(&manager->lock);
        if (manager->syncStopped) manager->syncStopped(manager->info, ENETUNREACH);
    } else pthread_mutex_unlock(&manager->lock);
}

void BRPeerManagerDisconnect(BRPeerManager *manager) {
    struct timespec ts;
    size_t peerCount, dnsThreadCount;

    assert(manager != NULL);
    pthread_mutex_lock(&manager->lock);
    peerCount = array_count(manager->connectedPeers);
    dnsThreadCount = manager->dnsThreadCount;

    for (size_t i = peerCount; i > 0; i--) {
        manager->connectFailureCount = MAX_CONNECT_FAILURES; // prevent futher automatic reconnect attempts
        BRPeerDisconnect(manager->connectedPeers[i - 1]);
    }

    pthread_mutex_unlock(&manager->lock);
    ts.tv_sec = 0;
    ts.tv_nsec = 1;

    while (peerCount > 0 || dnsThreadCount > 0) {
        nanosleep(&ts, NULL); // pthread_yield() isn't POSIX standard :(
        pthread_mutex_lock(&manager->lock);
        peerCount = array_count(manager->connectedPeers);
        dnsThreadCount = manager->dnsThreadCount;
        pthread_mutex_unlock(&manager->lock);
    }
}

static int _BRPeerManagerRescan(BRPeerManager *manager, BRMerkleBlock *newLastBlock) {
    if (NULL == newLastBlock) return 0;

    manager->lastBlock = newLastBlock;

    if (manager->downloadPeer) { // disconnect the current download peer so a new random one will be selected
        for (size_t i = array_count(manager->peers); i > 0; i--) {
            if (BRPeerEq(&manager->peers[i - 1], manager->downloadPeer))
                array_rm(manager->peers, i - 1);
        }

        BRPeerDisconnect(manager->downloadPeer);
    }

    manager->syncStartHeight = 0; // a syncStartHeight of 0 indicates that syncing hasn't started yet
    return 1;
}

// rescans blocks and transactions after earliestKeyTime (a new random download peer is also selected due to the
// possibility that a malicious node might lie by omitting transactions that match the bloom filter)
void BRPeerManagerRescan(BRPeerManager *manager) {
    assert(manager != NULL);
    pthread_mutex_lock(&manager->lock);

    if (manager->isConnected) {
        // start the chain download from the most recent checkpoint that's at least a one day older than earliestKeyTime
        for (size_t i = CHECKPOINT_COUNT; i > 0; i--) {
            if (i - 1 == 0 ||
                checkpoint_array[i - 1].timestamp + OLDEST_INTERVAL < manager->earliestKeyTime) {
                UInt256 hash = UInt256Reverse(u256_hex_decode(checkpoint_array[i - 1].hash));

                manager->lastBlock = BRSetGet(manager->blocks, &hash);
                break;
            }
        }

        if (manager->downloadPeer) { // disconnect the current download peer so a new random one will be selected
            for (size_t i = array_count(manager->peers); i > 0; i--) {
                if (BRPeerEq(&manager->peers[i - 1], manager->downloadPeer))
                    array_rm(manager->peers, i - 1);
            }

            BRPeerDisconnect(manager->downloadPeer);
        }

        manager->syncStartHeight = 0; // a syncStartHeight of 0 indicates that syncing hasn't started yet
        pthread_mutex_unlock(&manager->lock);
        BRPeerManagerConnect(manager);
    } else pthread_mutex_unlock(&manager->lock);
}

// rescans blocks and transactions after the last hardcoded checkpoint
void BRPeerManagerRescanFromLastHardcodedCheckpoint(BRPeerManager *manager) {
    assert(manager != NULL);
    pthread_mutex_lock(&manager->lock);

    int needConnect = 0;
    if (manager->isConnected) {
        size_t i = manager->params->checkpointsCount;
        if (i > 0) {
            UInt256 hash = UInt256Reverse(manager->params->checkpoints[i - 1].hash);
            needConnect = _BRPeerManagerRescan(manager, BRSetGet(manager->blocks, &hash));
        }
    }
    pthread_mutex_unlock(&manager->lock);
    if (needConnect) BRPeerManagerConnect(manager);
}

static BRMerkleBlock *
_BRPeerManagerLookupBlockFromBlockNumber(BRPeerManager *manager, uint32_t blockNumber) {
    BRMerkleBlock *block = manager->lastBlock;

    // walk the chain, looking for blockNumber
    while (block) {
        if (block->height == blockNumber) return block;
        block = BRSetGet(manager->blocks, &block->prevBlock);
    }

    // blockNumber not in the (abbreviated) chain - look through checkpoints
    for (int i = 0; i < manager->params->checkpointsCount; i++)
        if (manager->params->checkpoints[i].height == blockNumber) {
            UInt256 hash = UInt256Reverse(manager->params->checkpoints[i].hash);
            return BRSetGet(manager->blocks, &hash);
        }

    return NULL;
}

// rescans blocks and transactions from after the blockNumber.  If blockNumber is not known, then
// rescan from the just prior checkpoint.
void BRPeerManagerRescanFromBlockNumber(BRPeerManager *manager, uint32_t blockNumber) {
    assert(manager != NULL);
    pthread_mutex_lock(&manager->lock);

    int needConnect = 0;
    if (manager->isConnected) {
        BRMerkleBlock *block = _BRPeerManagerLookupBlockFromBlockNumber(manager, blockNumber);

        // If there was no block, find the preceeding hardcoded checkpoint.
        if (NULL == block) {
            for (size_t i = manager->params->checkpointsCount; i > 0; i--) {
                if (i - 1 == 0 || manager->params->checkpoints[i - 1].height < blockNumber) {
                    UInt256 hash = UInt256Reverse(manager->params->checkpoints[i - 1].hash);
                    block = BRSetGet(manager->blocks, &hash);
                    break;
                }
            }
        }

        needConnect = _BRPeerManagerRescan(manager, block);
    }
    pthread_mutex_unlock(&manager->lock);
    if (needConnect) BRPeerManagerConnect(manager);
}

// the (unverified) best block height reported by connected peers
uint32_t BRPeerManagerEstimatedBlockHeight(BRPeerManager *manager) {
    uint32_t height;

    assert(manager != NULL);
    pthread_mutex_lock(&manager->lock);
    height = (manager->lastBlock->height < manager->estimatedHeight) ? manager->estimatedHeight :
             manager->lastBlock->height;
    pthread_mutex_unlock(&manager->lock);
    return height;
}

// current proof-of-work verified best block height
uint32_t BRPeerManagerLastBlockHeight(BRPeerManager *manager) {
    uint32_t height;

    assert(manager != NULL);
    pthread_mutex_lock(&manager->lock);
    height = manager->lastBlock->height;
    pthread_mutex_unlock(&manager->lock);
    return height;
}

// current proof-of-work verified best block timestamp (time interval since unix epoch)
uint32_t BRPeerManagerLastBlockTimestamp(BRPeerManager *manager) {
    uint32_t timestamp;

    assert(manager != NULL);
    pthread_mutex_lock(&manager->lock);
    timestamp = manager->lastBlock->timestamp;
    pthread_mutex_unlock(&manager->lock);
    return timestamp;
}

// current network sync progress from 0 to 1
// startHeight is the block height of the most recent fully completed sync
double BRPeerManagerSyncProgress(BRPeerManager *manager, uint32_t startHeight) {
    double progress;

    assert(manager != NULL);
    pthread_mutex_lock(&manager->lock);
    if (startHeight == 0) startHeight = manager->syncStartHeight;

    if (!manager->downloadPeer && manager->syncStartHeight == 0) {
        progress = 0.0;
    } else if (!manager->downloadPeer || manager->lastBlock->height < manager->estimatedHeight) {
        if (manager->lastBlock->height > startHeight && manager->estimatedHeight > startHeight) {
            progress = 0.1 + 0.9 * (manager->lastBlock->height - startHeight) /
                             (manager->estimatedHeight - startHeight);
        } else
            progress = 0.05;
    } else
        progress = 1.0;

    pthread_mutex_unlock(&manager->lock);
    return progress;
}

// returns the number of currently connected peers
size_t BRPeerManagerPeerCount(BRPeerManager *manager) {
    size_t count = 0;

    assert(manager != NULL);
    pthread_mutex_lock(&manager->lock);

    for (size_t i = array_count(manager->connectedPeers); i > 0; i--) {
        if (BRPeerConnectStatus(manager->connectedPeers[i - 1]) == BRPeerStatusConnected) count++;
    }

    pthread_mutex_unlock(&manager->lock);
    return count;
}

// description of the peer most recently used to sync blockchain data
const char *BRPeerManagerDownloadPeerName(BRPeerManager *manager) {
    assert(manager != NULL);
    pthread_mutex_lock(&manager->lock);

    if (manager->downloadPeer) {
        sprintf(manager->downloadPeerName, "%s:%d", BRPeerHost(manager->downloadPeer),
                manager->downloadPeer->port);
    } else manager->downloadPeerName[0] = '\0';

    pthread_mutex_unlock(&manager->lock);
    return manager->downloadPeerName;
}

static void _publishTxInvDone(void *info, int success) {
    BRPeer *peer = ((PeerCallbackInfo *) info)->peer;
    BRPeerManager *manager = ((PeerCallbackInfo *) info)->manager;

    free(info);
    pthread_mutex_lock(&manager->lock);
    _PeerManagerRequestUnrelayedTx(manager, peer);
    pthread_mutex_unlock(&manager->lock);
}

// publishes tx to ravenwallet network (do not call TransactionFree() on tx afterward)
void BRPeerManagerPublishTx(BRPeerManager *manager, BRTransaction *tx, void *info,
                            void (*callback)(void *info, int error)) {
    assert(manager != NULL);
    assert(tx != NULL && BRTransactionIsSigned(tx));
    if (tx) pthread_mutex_lock(&manager->lock);

    if (tx && !BRTransactionIsSigned(tx)) {
        pthread_mutex_unlock(&manager->lock);
        BRTransactionFree(tx);
        tx = NULL;
        if (callback)
            callback(info, EINVAL); // transaction not signed
    } else if (tx && !manager->isConnected) {
        int connectFailureCount = manager->connectFailureCount;

        pthread_mutex_unlock(&manager->lock);

        if (connectFailureCount >= MAX_CONNECT_FAILURES ||
            (manager->networkIsReachable && !manager->networkIsReachable(manager->info))) {
            BRTransactionFree(tx);
            tx = NULL;
            if (callback) callback(info, ENOTCONN); // not connected to the network
        } else pthread_mutex_lock(&manager->lock);
    }

    if (tx) {
        size_t i, count = 0;

        tx->timestamp = (uint32_t) time(NULL); // set timestamp to publish time
        _PeerManagerAddTxToPublishList(manager, tx, info, callback);

        for (i = array_count(manager->connectedPeers); i > 0; i--) {
            if (BRPeerConnectStatus(manager->connectedPeers[i - 1]) == BRPeerStatusConnected)
                count++;
        }

        for (i = array_count(manager->connectedPeers); i > 0; i--) {
            BRPeer *peer = manager->connectedPeers[i - 1];
            PeerCallbackInfo *peerInfo;

            if (BRPeerConnectStatus(peer) != BRPeerStatusConnected) continue;

            // instead of publishing to all peers, leave out downloadPeer to see if tx propogates/gets relayed back
            // TODO: XXX connect to a random peer with an empty or fake bloom filter just for publishing
            if (peer != manager->downloadPeer || count == 1) {
                _PeerManagerPublishPendingTx(manager, peer);
                peerInfo = calloc(1, sizeof(*peerInfo));
                assert(peerInfo != NULL);
                peerInfo->peer = peer;
                peerInfo->manager = manager;
                BRPeerSendPing(peer, peerInfo, _publishTxInvDone);
            }
        }

        pthread_mutex_unlock(&manager->lock);
    }
}

// number of connected peers that have relayed the given unconfirmed transaction
size_t BRPeerManagerRelayCount(BRPeerManager *manager, UInt256 txHash) {
    size_t count = 0;

    assert(manager != NULL);
    assert(!UInt256IsZero(txHash));
    pthread_mutex_lock(&manager->lock);

    for (size_t i = array_count(manager->txRelays); i > 0; i--) {
        if (!UInt256Eq(manager->txRelays[i - 1].txHash, txHash)) continue;
        count = array_count(manager->txRelays[i - 1].peers);
        break;
    }

    pthread_mutex_unlock(&manager->lock);
    return count;
}

const ChainParams *BRPeerManagerChainParams(BRPeerManager *manager) {
    return manager->params;
}

void
PeerManagerGetAssetData(BRPeerManager *manager, void *infoManager, char *assetName, size_t nameLen,
                        void (*receivedAssetData)(void *info, BRAsset *asset)) {

    for (size_t i = array_count(manager->connectedPeers); i > 0; i--) {
        BRPeer *peer = manager->connectedPeers[i - 1];

        if (BRPeerConnectStatus(peer) != BRPeerStatusConnected) continue;

        peer->assetCallbackInfo = infoManager;
        // TODO: get it back
        if (BRPeerVersion(peer) >= /*PROTOCOL_VERSION*/ 70020) {
            BRPeerSendGetAsset(peer, assetName, nameLen, receivedAssetData);
            break;
        } else
            peer_log(peer, "node doesn't support Assets MSG Protocol");
    }
}


// frees memory allocated for manager
void BRPeerManagerFree(BRPeerManager *manager) {
    BRTransaction *tx;

    assert(manager != NULL);
    pthread_mutex_lock(&manager->lock);
    array_free(manager->peers);
    for (size_t i = array_count(manager->connectedPeers); i > 0; i--)
        BRPeerFree(manager->connectedPeers[i - 1]);
    array_free(manager->connectedPeers);
    BRSetApply(manager->blocks, NULL, _setApplyFreeBlock);
    BRSetFree(manager->blocks);
    BRSetApply(manager->orphans, NULL, _setApplyFreeBlock);
    BRSetFree(manager->orphans);
    BRSetFree(manager->checkpoints);
    for (size_t i = array_count(manager->txRelays); i > 0; i--)
        array_free(manager->txRelays[i - 1].peers);
    array_free(manager->txRelays);
    for (size_t i = array_count(manager->txRequests); i > 0; i--)
        array_free(manager->txRequests[i - 1].peers);
    array_free(manager->txRequests);

    for (size_t i = array_count(manager->publishedTx); i > 0; i--) {
        tx = manager->publishedTx[i - 1].tx;
        if (tx && tx != BRWalletTransactionForHash(manager->wallet, tx->txHash))
            BRTransactionFree(tx);
    }

    if (manager->bloomFilter) BRBloomFilterFree(manager->bloomFilter);

    array_free(manager->publishedTx);
    array_free(manager->publishedTxHashes);
    pthread_mutex_unlock(&manager->lock);
    pthread_mutex_destroy(&manager->lock);
    free(manager);
}
