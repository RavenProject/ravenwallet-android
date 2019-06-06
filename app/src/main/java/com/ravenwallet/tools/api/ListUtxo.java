package com.ravenwallet.tools.api;


import java.util.List;

public class ListUtxo {

    List<ApiUTxo> apiUTxos;

    public List<ApiUTxo> getApiUTxos() {
        return apiUTxos;
    }

    public void setApiUTxos(List<ApiUTxo> apiUTxos) {
        this.apiUTxos = apiUTxos;
    }
}
