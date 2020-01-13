# Ravencoin Core SPV (with assets features support)

## Sync performance / Bip32 integration with Bloom Filter:
- Local peer sends getheaders
- Remote peer responds with up to 2000 headers
- Local peer immediately sends getheaders again and then processes the headers
- Previous two steps repeat until a header within a week of earliestKeyTime is reached (further headers are ignored)
- Local peer sends getblocks
- Remote peer responds with inv containing up to 500 block hashes
- Local peer sends getdata with the block hashes
- If there were 500 hashes, local peer sends getblocks again without waiting for remote peer
- Remote peer responds with multiple merkleblock and tx messages, followed by inv containing up to 500 block hashes
- Previous two steps repeat until an inv with fewer than 500 block hashes is received
- Local peer sends just getdata for the final set of fewer than 500 block hashes
- Remote peer responds with multiple merkleblock and tx messages
- If at any point tx messages consume enough wallet addresses to drop below the bip32 chain gap limit, more addresses are generated and local peer sends filterload with an updated bloom filter
- After filterload is sent, getdata is sent to re-request recent blocks that may contain new tx matching the filter

