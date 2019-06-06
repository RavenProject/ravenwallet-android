package com.platform.assets;

public enum AssetType {
    INVALID(-1),
    UNIQUE(4),
    CHANNEL(6),
    OWNER(5),
    SUB(3),
    ROOT(7),
    NEW_ASSET(0),
    TRANSFER(1),
    REISSUE(2),
    BURN(-2);
    private int index;

    AssetType(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
