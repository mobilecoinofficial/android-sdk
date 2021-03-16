// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib.exceptions;

import androidx.annotation.Nullable;

public class FragmentedAccountException extends MobileCoinException {
    public FragmentedAccountException(@Nullable String message) {
        super(message);
    }
}
