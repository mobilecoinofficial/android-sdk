package com.mobilecoin.lib;

import com.mobilecoin.api.MobileCoinAPI;

public class TxOutContext extends Native {

    public TxOutContext(long existingRustObj) {
        this.rustObj = existingRustObj;
    }

    public static TxOutContext fromJNI(long rustObj) {
        return new TxOutContext(rustObj);
    }

    public TxOut getTxOut() {
        return get_tx_out();
    }

    public MobileCoinAPI.TxOutConfirmationNumber getConfirmationNumber() {
        return get_confirmation_number();
    }

    public RistrettoPublic getSharedSecret() {
        return get_shared_secret();
    }

    private native TxOut get_tx_out();

    private native MobileCoinAPI.TxOutConfirmationNumber get_confirmation_number();

    private native RistrettoPublic get_shared_secret();

}
