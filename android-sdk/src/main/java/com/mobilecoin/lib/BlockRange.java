// Copyright (c) 2020-2021 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import java.util.Objects;

import fog_common.FogCommon;

/**
 * A class for representing a range of blocks [start, end)
 */
class BlockRange implements Comparable<BlockRange> {
    private final UnsignedLong start;
    private final UnsignedLong end;

    public BlockRange(
            @NonNull UnsignedLong start,
            @NonNull UnsignedLong end
    ) {
        if (start.compareTo(end) > 0) {
            throw new IllegalArgumentException("Invalid range");
        }
        this.start = start;
        this.end = end;
    }

    BlockRange(
            long start,
            long end
    ) {
        this(UnsignedLong.fromLongBits(start), UnsignedLong.fromLongBits(end));
    }

    BlockRange(@NonNull FogCommon.BlockRange protoBuf) {
        this(protoBuf.getStartBlock(), protoBuf.getEndBlock());
    }

    @NonNull
    public UnsignedLong getStart() {
        return this.start;
    }

    @NonNull
    public UnsignedLong getEnd() {
        return this.end;
    }

    @NonNull
    public UnsignedLong size() {
        return getEnd().sub(getStart());
    }

    @NonNull
    FogCommon.BlockRange toProtoBuf() {
        return FogCommon.BlockRange.newBuilder()
                .setStartBlock(start.longValue())
                .setEndBlock(end.longValue())
                .build();
    }

    @NonNull
    @Override
    public String toString() {
        return "BlockRange{start=" + start + ", end=" + end + '}';
    }

    @Override
    public int compareTo(BlockRange other) {
        if (start.equals(other.start)) {
            return end.compareTo(other.end);
        }
        return start.compareTo(other.start);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStart(), getEnd());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof BlockRange)) {
            return false;
        }
        BlockRange other = (BlockRange) o;
        return this.compareTo(other) == 0;
    }
}
