package com.platform.addressBook.event;

import com.platform.addressBook.AddressBookItem;
import com.ravenwallet.core.BRCoreAddress;

public interface OnAddressClickedListener {

    void onAddressClicked(AddressBookItem address);

    void onAddressClicked(BRCoreAddress address);
}
