// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.uri;

import android.net.Uri;

import com.mobilecoin.lib.exceptions.InvalidUriException;

import androidx.annotation.NonNull;

public class ConsensusUri extends MobileCoinUri {

    public ConsensusUri(@NonNull Uri uri) throws InvalidUriException {
        super(uri, new ConsensusUriScheme());
    }

    public ConsensusUri(@NonNull String uriString) throws InvalidUriException {
        super(uriString, new ConsensusUriScheme());
    }
}
