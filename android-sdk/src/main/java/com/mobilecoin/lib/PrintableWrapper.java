// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.api.Printable;
import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.uri.MobUri;

public final class PrintableWrapper extends Native {
    private final static String TAG = PrintableWrapper.class.getName();
    private final PublicAddress publicAddress;
    private final PaymentRequest paymentRequest;
    private final TransferPayload transferPayload;
    private final Printable.PrintableWrapper protoBuf;

    PrintableWrapper(@NonNull Printable.PrintableWrapper protoBuf) throws SerializationException {
        this.protoBuf = protoBuf;
        if (protoBuf.hasPublicAddress()) {
            publicAddress = PublicAddress.fromProtoBufObject(protoBuf.getPublicAddress());
        } else {
            publicAddress = null;
        }
        if (protoBuf.hasPaymentRequest()) {
            paymentRequest = PaymentRequest.fromProtoBufObject(protoBuf.getPaymentRequest());
        } else {
            paymentRequest = null;
        }
        if (protoBuf.hasTransferPayload()) {
            transferPayload = TransferPayload.fromProtoBufObject(protoBuf.getTransferPayload());
        } else {
            transferPayload = null;
        }
    }

    @NonNull
    public static PrintableWrapper fromPublicAddress(@NonNull PublicAddress publicAddress)
            throws SerializationException {
        Logger.i(TAG, "Creating printable wrapper from public address", null, publicAddress);
        Printable.PrintableWrapper protoBuf = Printable.PrintableWrapper.newBuilder()
                .setPublicAddress(publicAddress.toProtoBufObject()).build();
        return new PrintableWrapper(protoBuf);
    }

    @NonNull
    public static PrintableWrapper fromPaymentRequest(@NonNull PaymentRequest paymentRequest)
            throws SerializationException {
        Logger.i(TAG, "Creating printable wrapper from payment request", null, paymentRequest);
        Printable.PrintableWrapper protoBuf = Printable.PrintableWrapper.newBuilder()
                .setPaymentRequest(paymentRequest.toProtoBufObject()).build();
        return new PrintableWrapper(protoBuf);
    }

    @NonNull
    public static PrintableWrapper fromTransferPayload(@NonNull TransferPayload transferPayload)
            throws SerializationException {
        Logger.i(TAG, "Creating printable wrapper from transfer payload", null, transferPayload);
        Printable.PrintableWrapper protoBuf = Printable.PrintableWrapper.newBuilder()
                .setTransferPayload(transferPayload.toProtoBufObject()).build();
        return new PrintableWrapper(protoBuf);
    }

    @NonNull
    public static PrintableWrapper fromB58String(@NonNull String base58_string)
            throws SerializationException {
        Logger.i(TAG, "Creating printable wrapper from base58_string", null, base58_string);
        try {
            byte[] serialized = b58_decode(base58_string);
            Printable.PrintableWrapper printableWrapper =
                    Printable.PrintableWrapper.parseFrom(serialized);
            return new PrintableWrapper(printableWrapper);
        } catch (Exception ex) {
            SerializationException serializationException =
                    new SerializationException(ex.getLocalizedMessage());
            Util.logException(TAG, serializationException);
            throw serializationException;
        }
    }

    @NonNull
    private static native byte[] b58_decode(@NonNull String b58_string);

    @NonNull
    private static native String b58_encode(@NonNull byte[] serialized);

    @NonNull
    public String toB58String() throws SerializationException {
        Logger.i(TAG, "Converting printable wrapper to base58");
        try {
            byte[] serialized = protoBuf.toByteString().toByteArray();
            return b58_encode(serialized);
        } catch (Exception ex) {
            SerializationException serializationException =
                    new SerializationException(ex.getLocalizedMessage());
            Util.logException(TAG, serializationException);
            throw serializationException;
        }
    }

    @NonNull
    public Uri toUri() throws SerializationException {
        Logger.i(TAG, "Converting printable wrapper to URI");
        try {
            String b58String = toB58String();
            MobUri mobUri = MobUri.fromB58(b58String);
            return mobUri.getUri();
        } catch (InvalidUriException exception) {
            IllegalStateException illegalStateException =
                    new IllegalStateException("BUG: unreachable code");
            Util.logException(TAG, illegalStateException);
            throw illegalStateException;
        }
    }

    @NonNull
    public static PrintableWrapper fromUri(@NonNull Uri uri) throws InvalidUriException,
            SerializationException {
        Logger.i(TAG, "Getting printable wrapper from URI", null, uri);
        MobUri mobUri = MobUri.fromUri(uri);
        String payload = mobUri.getPayload();
        return PrintableWrapper.fromB58String(payload);
    }

    @Nullable
    public PublicAddress getPublicAddress() {
        Logger.i(TAG, "Getting public address", null, publicAddress);
        return publicAddress;
    }

    @Nullable
    public PaymentRequest getPaymentRequest() {
        Logger.i(TAG, "Getting payment request", null, paymentRequest);
        return paymentRequest;
    }

    @Nullable
    public TransferPayload getTransferPayload() {
        Logger.i(TAG, "Getting transfer payload", null, transferPayload);
        return transferPayload;
    }

    public boolean hasPublicAddress() {
        boolean hasPublicAddress = protoBuf.hasPublicAddress() && !hasPaymentRequest();
        Logger.i(TAG, "Checking if printable wrapper has public address", null, hasPublicAddress);
        return hasPublicAddress;
    }

    public boolean hasPaymentRequest() {
        boolean hasPaymentRequest = protoBuf.hasPaymentRequest();
        Logger.i(TAG, "Checking if printable wrapper has payment request", null, hasPaymentRequest);
        return hasPaymentRequest;
    }

    public boolean hasTransferPayload() {
        boolean hasTransferPayload = protoBuf.hasTransferPayload();
         Logger.i(TAG, "Checking if printable wrapper has transfer payload", null, hasTransferPayload);
        return hasTransferPayload;
    }
}
