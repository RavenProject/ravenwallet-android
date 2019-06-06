package com.ravenwallet.presenter.interfaces;

public interface WalletManagerListener {

    void close();

    void error(String error);
}
