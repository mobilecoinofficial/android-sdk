// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.uri;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.mobilecoin.lib.exceptions.InvalidUriException;
import com.mobilecoin.lib.log.Logger;

import java.util.Objects;

public class MobileCoinUri {
    private final static String TAG = MobileCoinUri.class.getName();
    final private Uri uri;
    final private boolean useTls;

    public MobileCoinUri(@NonNull String uriString, @NonNull MobileCoinScheme scheme) throws InvalidUriException {
        this(Uri.parse(uriString), scheme);
    }

    public MobileCoinUri(@NonNull Uri uri, @NonNull MobileCoinScheme scheme) throws InvalidUriException {
        Logger.i(TAG, "Getting MobileCoin Uri");
        String uriScheme = uri.getScheme();
        if (null == uriScheme) {
            throw new InvalidUriException("Invalid URI scheme (null)", new NullPointerException());
        }
        if (uriScheme.equals(scheme.secureScheme())) {
            useTls = true;
        } else if (uriScheme.equals(scheme.insecureScheme())) {
            useTls = false;
        } else {
            throw new InvalidUriException("Unsupported scheme: " + uriScheme);
        }

        String host = uri.getHost();
        // if host is null the right part of OR is not executed
        if (null == host || host.length() == 0) {
            throw new InvalidUriException("URI host cannot be empty");
        }

        // add the default port if needed
        if (uri.getPort() == -1) {
            uri = uri.buildUpon().encodedAuthority(host + ":" +
                    (useTls
                            ? scheme.securePort()
                            : scheme.insecurePort())
            ).build();
        }
        this.uri = uri;
    }

    @NonNull
    public Uri getUri() {
        return uri;
    }

    @NonNull
    public String toString() {
        return uri.toString();
    }

    public boolean isTlsEnabled() {
        return useTls;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MobileCoinUri that = (MobileCoinUri) o;
        return getUri().equals(that.getUri());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUri());
    }
}
