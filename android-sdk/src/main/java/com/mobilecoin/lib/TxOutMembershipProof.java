// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.log.Logger;

final class TxOutMembershipProof extends Native {
    private final static String TAG = TxOutMembershipProof.class.getName();

    TxOutMembershipProof(@NonNull byte[] protobufBytes) throws SerializationException {
        Logger.i(TAG, "Initializing from protobuf");
        try {
            init_from_protobuf_bytes(protobufBytes);
        } catch (Exception ex) {
            SerializationException serializationException =
                    new SerializationException(ex.getLocalizedMessage(), ex);
            Util.logException(TAG, serializationException);
            throw serializationException;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (rustObj != 0) {
            finalize_jni();
        }
        super.finalize();
    }


    private native void init_from_protobuf_bytes(@NonNull byte[] data);

    private native void finalize_jni();
}
