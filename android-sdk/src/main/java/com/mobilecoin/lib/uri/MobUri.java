// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.uri;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.log.Logger;

import java.util.List;
import java.util.Locale;

public final class MobUri {
    private final static String TAG = MobUri.class.getName();
    private final Uri uri;
    private final String payload;
    private final static MobUriScheme mobScheme = new MobUriScheme();

    private MobUri(@NonNull Uri uri) throws InvalidUriException {
        Logger.i(TAG, "Getting MobUri", null,
                "mobUri:", uri.toString()
        );
        String uriScheme = uri.getScheme();
        if (null == uriScheme) {
            throw new InvalidUriException("Invalid URI scheme (null)", new NullPointerException());
        }
        if (!uriScheme.equals(mobScheme.secureScheme())
                && (!uriScheme.equals(mobScheme.insecureScheme()))) {
            throw new InvalidUriException("Unsupported scheme: " + uriScheme);
        }
        // validate MobUri
        List<String> fragments = uri.getPathSegments();
        if (fragments.size() < 2) {
            throw new InvalidUriException("MobUri must have at least two path fragments");
        }
        String payloadType = fragments.get(0);
        String payload = fragments.get(1);
        if (!payloadType.equals(mobScheme.payloadType())) {
            throw new InvalidUriException("Unsupported payload type");
        }
        if (payload.isEmpty()) {
            throw new InvalidUriException("Empty payload");
        }
        this.uri = uri;
        this.payload = payload;
    }

    public MobUri(@NonNull String uriString) throws InvalidUriException {
        this(Uri.parse(uriString));
    }

    @NonNull
    public static MobUri fromUri(@NonNull Uri uri) throws InvalidUriException {
        return new MobUri(uri);
    }

    @NonNull
    public static MobUri fromB58(@NonNull String b58String) throws InvalidUriException {
        String uriString = String.format(Locale.US,
                "%s:///b58/%s",
                mobScheme.secureScheme(),
                b58String
        );
        return new MobUri(uriString);
    }

    @NonNull
    public String getPayload() {
        return payload;
    }

    @NonNull
    public Uri getUri() {
        return uri;
    }

    @NonNull
    public String toString() {
        return uri.toString();
    }
}
