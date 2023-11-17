// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.AbstractMessageLite;
import com.google.protobuf.ByteString;
import com.mobilecoin.lib.ClientConfig.Service;
import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.exceptions.NetworkException;
import com.mobilecoin.lib.log.Logger;
import com.mobilecoin.lib.network.TransportProtocol;
import com.mobilecoin.lib.network.services.transport.Transport;
import com.mobilecoin.lib.network.uri.MobileCoinUri;

import attest.Attest;

/**
 * Base class for attested communication with View/Ledger/Consensus servers
 */

public abstract class AttestedClient extends AnyClient {
    private final static String TAG = AttestedClient.class.getName();
    // How long to wait for the managed connection to gracefully shutdown in milliseconds

    /**
     * Creates and initializes an instance of {@link AttestedClient}
     *  @param loadBalancer           a complete {@link Uri} of the service including port.
     * @param serviceConfig service configuration passed to MobileCoinClient
     */
    protected AttestedClient(@NonNull LoadBalancer loadBalancer,
                             @NonNull Service serviceConfig,
                             @NonNull TransportProtocol transportProtocol) {
        super(loadBalancer, serviceConfig, transportProtocol);
    }

    /**
     * Attest service connection or throw an exception if error occurs.
     */
    public abstract void attest(@NonNull Transport transport)
            throws AttestationException, NetworkException;

    /**
     * Reset service connection
     */
    public synchronized void deattest() {
        Logger.i(TAG, "De-attesting the managed channel");
        attestReset();
    }

    /**
     * Check if the client is attested. Attestation happens lazily and the connection may not be
     * attested until it is actually needed.
     *
     * @return whether or no the client is attested
     */
    public synchronized boolean isAttested() {
        Logger.d(TAG, "Is channel attested?", null, (rustObj != 0) ? "Yes" : "No");
        return (rustObj != 0);
    }

    /**
     * Encrypt protocol request and wrap it into an {@link Attest.Message}
     *
     * @return encrypted {@link Attest.Message}
     */
    @NonNull
    public synchronized Attest.Message encryptMessage(
            @Nullable AbstractMessageLite<?, ?> message,
            @Nullable AbstractMessageLite<?, ?> aadMessage
    ) throws AttestationException {
        Logger.i(TAG, "Encrypting request message for attested channel");
        // Nullable parameters are allowed in this method
        // but encryptPayload requires parameters to be nonnull even if empty
        byte[] aad = (aadMessage != null)
                ? aadMessage.toByteArray()
                : new byte[0];
        byte[] payload = (message != null)
                ? message.toByteArray()
                : new byte[0];

        byte[] encryptedPayload = encryptPayload(payload, aad);
        return Attest.Message.newBuilder()
                .setData(ByteString.copyFrom(encryptedPayload))
                .setChannelId(ByteString.copyFrom(getBinding()))
                .setAad(ByteString.copyFrom(aad))
                .build();
    }

    @Override
    @NonNull
    public synchronized Transport getNetworkTransport() throws NetworkException, AttestationException {
        Transport transport = super.getNetworkTransport();
        if(!isAttested()) {
            attest(transport);
        }
        return transport;
    }

    @NonNull
    public synchronized Attest.Message encryptMessage(@NonNull AbstractMessageLite<?, ?> message)
            throws AttestationException {
        return encryptMessage(message, null);
    }

    /**
     * Decrypt an attested response {@link Attest.Message}
     *
     * @return decrypted {@link Attest.Message}
     */
    @NonNull
    public synchronized Attest.Message decryptMessage(@NonNull Attest.Message message)
            throws AttestationException {
        Logger.i(TAG, "Decrypt response message");
        try {
            byte[] encrypted = message.getData().toByteArray();
            byte[] decrypted = decryptPayload(
                    encrypted,
                    message.getAad().toByteArray()
            );
            return Attest.Message.newBuilder(message).setData(ByteString.copyFrom(decrypted))
                    .build();
        } catch (Exception exception) {
            AttestationException attestationException =
                    new AttestationException("Unable to decrypt response message", exception);
            Util.logException(TAG, attestationException);
            throw attestationException;
        }
    }

    /**
     * Generate auth data for the attestation
     *
     * @param serviceUri must include the port as well
     */
    @NonNull
    public byte[] attestStart(@NonNull MobileCoinUri serviceUri) throws AttestationException {
        Logger.i(TAG, "FFI: attest_start call");
        try {
            ResponderId responderId;
            String responderIdString = serviceUri.getUri().getQueryParameter("responder-id");
            if (responderIdString != null && !responderIdString.isEmpty()) {
                responderId = ResponderId.fromStringRepresentation(responderIdString);
            } else {
                responderId = ResponderId.fromUri(serviceUri.getUri());
            }
            return attest_start(responderId);
        } catch (Exception exception) {
            AttestationException attestationException =
                    new AttestationException("Unable to start attestation", exception);
            Util.logException(TAG, attestationException);
            attestReset();
            throw attestationException;
        }
    }

    /**
     * Verify the response from the attested server and finish the attestation
     *
     * @param authResponse an auth response obtained from the attested service
     */
    public void attestFinish(@NonNull byte[] authResponse,
                                @NonNull TrustedIdentities trustedIdentities
    ) throws AttestationException {
        Logger.i(TAG, "FFI: attest_finish call");
        try {
            attest_finish(
                    authResponse,
                    trustedIdentities
            );
        } catch (Exception exception) {
            AttestationException attestationException =
                    new AttestationException("Unable to finish attestation", exception);
            Util.logException(TAG, attestationException);
            attestReset();
            throw attestationException;
        }
    }

    /**
     * Reset the attestation state of the client
     * <p>
     * It is needed to dispose of the invalid pending state, if the attestation was unsuccessful
     * (i.e. attestation server is unreachable)
     */
    public synchronized void attestReset() {
        Logger.i(TAG, "Reset attested state");
        resetNetworkTransport();
        if (rustObj != 0) {
            try {
                finalize_jni();
            } catch (Exception exception) {
                Logger.e(TAG, "Unable to free attested state", exception);
            }
            rustObj = 0;
        }
        shutdown();
    }


    /**
     * Authorize requests using the provided credentials.
     * <p>
     * Credentials are encoded and attached as an HTTP header field in the form of Authorization:
     * Basic <credentials>, where credentials is the Base64 encoding of ID and password joined by a
     * single colon :
     */
    public void setAuthorization(
            @NonNull String username,
            @NonNull String password
    ) {
        Logger.i(TAG, "Set API authorization");
        getAPIManager().setAuthorization(
                username,
                password
        );
    }

    @Override
    protected void finalize() throws Throwable {
        Logger.i(TAG, "Destroying attested client object");
        deattest();
        if (rustObj != 0) {
            finalize_jni();
        }
        super.finalize();
    }

    @NonNull
    private synchronized byte[] getBinding() throws AttestationException {
        try {
            Logger.i(TAG, "FFI: get_binding call");
            return get_binding();
        } catch (Exception exception) {
            AttestationException attestationException =
                    new AttestationException("Unable to get connection binding", exception);
            Util.logException(TAG, attestationException);
            attestReset();
            throw attestationException;
        }
    }

    @NonNull
    private synchronized byte[] decryptPayload(
            @NonNull byte[] payload,
            @NonNull byte[] aad
    ) throws AttestationException {
        Logger.i(TAG, "FFI: decrypt_payload call");
        try {
            return decrypt_payload(
                    payload,
                    aad
            );
        } catch (Exception exception) {
            AttestationException attestationException =
                    new AttestationException("Unable to decrypt payload", exception);
            Util.logException(TAG, attestationException);
            attestReset();
            throw attestationException;
        }
    }

    @NonNull
    private synchronized byte[] encryptPayload(@NonNull byte[] payload, @NonNull byte[] aad)
            throws AttestationException {
        Logger.i(TAG, "FFI: encrypt_payload call");
        try {
            return encrypt_payload(payload, aad);
        } catch (Exception exception) {
            AttestationException attestationException =
                    new AttestationException("Unable to encrypt payload", exception);
            Util.logException(TAG, attestationException);
            attestReset();
            throw attestationException;
        }
    }

    private native void finalize_jni();

    @NonNull
    private native byte[] attest_start(@NonNull ResponderId responderId);

    private native void attest_finish(
            @NonNull byte[] auth,
            @NonNull TrustedIdentities trusted_identities
    );

    @NonNull
    private native byte[] encrypt_payload(@NonNull byte[] payload, @NonNull byte[] aad);

    @NonNull
    private native byte[] decrypt_payload(
            @NonNull byte[] payload,
            @NonNull byte[] aad
    );

    @NonNull
    private native byte[] get_binding();
}
