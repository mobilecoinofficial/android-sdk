// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

class DefaultFogQueryScalingStrategy implements FogQueryScalingStrategy {
    private final static int MIN_QUERY_SIZE = 10;
    private final static int MAX_QUERY_SIZE = 200;
    private final static int MULTIPLIER = 3;
    private int currentQuerySize = MIN_QUERY_SIZE;

    @Override
    public int nextQuerySize() {
        int result = currentQuerySize;
        currentQuerySize = Math.min(currentQuerySize * MULTIPLIER, MAX_QUERY_SIZE);
        return result;
    }
}
