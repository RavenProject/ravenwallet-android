package com.platform.assets;

public class AssetsValidation {

    private final static int MAX_NAME_LENGTH = 30;
    public final int MAX_IPFSHASH_LENGTH = 46;

    public final static String ROOT_NAME_CHARACTERS = "^[A-Z0-9._]{3,}$";
    private final static String SUB_NAME_CHARACTERS = "^[A-Z0-9._]+$";
    //private final static String UNIQUE_TAG_CHARACTERS = "^[-A-Za-z0-9@$%&*()\\]{}<>_.;?\\\\:]+$";
//    private final static String UNIQUE_TAG_CHARACTERS = "^[-A-Za-z0-9@$%&*()\\[\\]{}<>_.;?\\\\:]+$";
    private final static String UNIQUE_TAG_CHARACTERS = "^[-A-Za-z0-9@$%&*()\\\\{}_.?:]+$";
    //private final static String UNIQUE_TAG_CHARACTERS = "^[-A-Za-z0-9@$%&*()[\\]{}<>_.;?\\\\:]+$";
    private final static String CHANNEL_TAG_CHARACTERS = "^[A-Z0-9._]+$";

    private final static String DOUBLE_PUNCTUATION = "^.*[._]{2,}.*$";
    private final static String LEADING_PUNCTUATION = "^[._].*$";
    private final static String TRAILING_PUNCTUATION = "^.*[._]$";

    public final static String SUB_NAME_DELIMITER = "/";
    public final static String UNIQUE_TAG_DELIMITER = "#";
    private final static String CHANNEL_TAG_DELIMITER = "~";

    private final static String UNIQUE_INDICATOR = "^[^#]+#[^#]+$";
    private final static String CHANNEL_INDICATOR = "^[^~]~[^~]$";
    private final static String OWNER_INDICATOR = "^[^!]+!$";

    private final static String RAVEN_NAMES = "^RVN|^RAVEN|^RAVENCOIN|^RAVENC0IN|^RAVENCO1N|^RAVENC01N";

    private static boolean isRootNameValid(final String name) {
        return name.matches(ROOT_NAME_CHARACTERS) &&
                !name.matches(DOUBLE_PUNCTUATION) &&
                !name.matches(LEADING_PUNCTUATION) &&
                !name.matches(TRAILING_PUNCTUATION) &&
                !name.matches(RAVEN_NAMES);
    }

    private static boolean isSubNameValid(final String name) {
        return name.matches(SUB_NAME_CHARACTERS) &&
                !name.matches(DOUBLE_PUNCTUATION) &&
                !name.matches(LEADING_PUNCTUATION) &&
                !name.matches(TRAILING_PUNCTUATION);
    }

    private static boolean isUniqueTagValid(final String tag) {
        return tag.matches(UNIQUE_TAG_CHARACTERS);
    }

    private static boolean isChannelTagValid(final String tag) {
        return tag.matches(CHANNEL_TAG_CHARACTERS) &&
                tag.matches(DOUBLE_PUNCTUATION) &&
                tag.matches(LEADING_PUNCTUATION) &&
                tag.matches(TRAILING_PUNCTUATION);
    }

    private static boolean isNameValidBeforeTag(final String name) {
        String[] parts = name.split(SUB_NAME_DELIMITER,-1);
        if (parts.length > 0 && !isRootNameValid(parts[0])) {
            return false;
        }
        if (parts.length > 1) {
            for (int i = 1; i < parts.length; i++) {
                if (!isSubNameValid(parts[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isAssetNameASubasset(final String name) {
        String[] parts = name.split(SUB_NAME_DELIMITER,-1);
        if (parts.length > 0 && !isRootNameValid(parts[0])) {
            return false;
        }
        return parts.length > 1;
    }

    public static boolean isAssetNameValid(final String name, AssetType assetType) {
        assetType = AssetType.INVALID;
        if (name.matches(UNIQUE_INDICATOR)) {
            if (name.length() > MAX_NAME_LENGTH) {
                return false;
            }
            String[] parts = name.split(UNIQUE_TAG_DELIMITER,-1);

            if (parts.length > 0) {
                int lastItemIndex = parts.length - 1;
                boolean valid = isNameValidBeforeTag(parts[0]) && isUniqueTagValid(parts[lastItemIndex]);

                if (!valid) {
                    return false;
                }
            }
            assetType = AssetType.UNIQUE;
            return true;
        } else if (name.matches(CHANNEL_INDICATOR)) {
            if (name.length() > MAX_NAME_LENGTH) {
                return false;
            }

            String[] parts = name.split(CHANNEL_TAG_DELIMITER,-1);

            if (parts.length > 0) {
                int lastItemIndex = parts.length - 1;
                boolean valid = isNameValidBeforeTag(parts[0]) && isChannelTagValid(parts[lastItemIndex]);

                if (!valid) {
                    return false;
                }
            }
            assetType = AssetType.CHANNEL;
            return true;
        } else if (name.matches(OWNER_INDICATOR)) {
            if (name.length() > MAX_NAME_LENGTH + 1) {
                return false;
            }
            boolean valid = isNameValidBeforeTag(name.substring(0, name.length() - 1));
            if (!valid) {
                return false;
            }
            assetType = AssetType.OWNER;
            return true;
        } else {
            if (name.length() > MAX_NAME_LENGTH) {
                return false;
            }
            boolean valid = isNameValidBeforeTag(name);
            if (!valid) {
                return false;
            }
            assetType = isAssetNameASubasset(name) ? AssetType.SUB : AssetType.ROOT;
            return true;
        }
    }

    public static boolean isAssetNameValid(final String name) {
        AssetType assetType = null;
        return isAssetNameValid(name, assetType);
    }
    public static boolean isAssetNameAnOwner(final String name) {
        return isAssetNameValid(name) && name.matches(OWNER_INDICATOR);
    }
}
