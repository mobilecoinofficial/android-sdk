package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.exceptions.SerializationException;

import java.util.Arrays;
import java.util.Objects;

public class TxOutContext extends Native {

    private final TxOut txOut;
    private final MobileCoinAPI.TxOutConfirmationNumber confirmationNumber;
    private final RistrettoPublic sharedSecret;

    public TxOutContext(long existingRustObj) throws SerializationException {
        this.rustObj = existingRustObj;
        try {
            this.txOut = TxOut.fromJNI(get_tx_out());
            this.confirmationNumber = MobileCoinAPI.TxOutConfirmationNumber.parseFrom(get_confirmation_number());
            this.sharedSecret = RistrettoPublic.fromJNI(get_shared_secret());
        } catch(Exception e) {
            SerializationException serializationException =
                    new SerializationException(e.getLocalizedMessage(), e);
            Util.logException(TAG, serializationException);
            throw serializationException;
        }
    }

    public static TxOutContext fromJNI(long rustObj) throws SerializationException {
        return new TxOutContext(rustObj);
    }

    @NonNull
    public TxOut getTxOut() {
        return this.txOut;
    }

    @NonNull
    public MobileCoinAPI.TxOutConfirmationNumber getConfirmationNumber() {
        return this.confirmationNumber;
    }

    @NonNull
    public RistrettoPublic getSharedSecret() {
        return this.sharedSecret;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o instanceof TxOutContext) {
            TxOutContext that = (TxOutContext)o;
            return Objects.equals(this.txOut, that.txOut) &&
                   Arrays.equals(this.confirmationNumber.toByteArray(), that.confirmationNumber.toByteArray()) &&
                   Objects.equals(this.sharedSecret, that.sharedSecret);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.txOut, this.confirmationNumber, this.sharedSecret);
    }

    private native long get_tx_out();

    private native byte[] get_confirmation_number();

    private native long get_shared_secret();

    private static final String TAG = TxOutContext.class.getName();

}
