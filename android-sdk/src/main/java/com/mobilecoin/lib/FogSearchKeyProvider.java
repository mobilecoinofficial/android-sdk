package com.mobilecoin.lib;

import com.google.protobuf.ByteString;
import com.mobilecoin.lib.exceptions.KexRngException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

class FogSearchKeyProvider {

    //Map FogSeed to the number of times its search keys have been scaled
    Map<FogSeed, Integer> fogSeeds;

    FogSearchKeyProvider(Collection<FogSeed> fogSeeds) {
        this.fogSeeds = new TreeMap<FogSeed, Integer>();
        fogSeeds.stream().forEach(seed -> this.fogSeeds.put(seed, 0));
    }

    void addFogSeed(FogSeed fogSeed) {
        this.fogSeeds.put(fogSeed, 0);
    }

    void addAllFogSeeds(Collection<FogSeed> fogSeeds) {
        fogSeeds.stream().forEach(seed -> this.fogSeeds.put(seed, 0));
    }

    Map<ByteString, FogSeed> getNSearchKeys(int n) throws KexRngException {
        HashMap<ByteString, FogSeed> nextKeys = new HashMap<ByteString, FogSeed>();
        if(this.fogSeeds.size() == 0) return nextKeys;
        int numKeysAdded = 0;
        while(n > numKeysAdded) {
            for(FogSeed seed : this.fogSeeds.keySet()) {
                int runsForThisSeed = this.fogSeeds.get(seed);
                int keysToGenerate = Math.min(getNumKeysForRunCount(runsForThisSeed), n - numKeysAdded);
                this.fogSeeds.put(seed, runsForThisSeed + 1);
                if(keysToGenerate <= 0) break;
                //Arrays.stream(seed.getNextN(keysToGenerate)).forEach(key -> nextKeys.put(ByteString.copyFrom(key), seed));//TODO: bindings patch for ClientKexRng
                for(byte key[] : seed.getNextN(keysToGenerate)) {
                    nextKeys.put(ByteString.copyFrom(key), seed);
                    seed.advance();//TODO: bindings patch for ClientKexRng
                }
                numKeysAdded += keysToGenerate;
            }
        }
        return nextKeys;
    }

    void removeSeed(FogSeed fogSeed) {
        this.fogSeeds.remove(fogSeed);
    }

    boolean hasSeed(FogSeed fogSeed) {
        return this.fogSeeds.keySet().contains(fogSeed);
    }

    boolean hasKeys() {
        return this.fogSeeds.size() > 0;
    }

    private static class FogSeedStateInfo {
        int runs;
        boolean finished;
        FogSeedStateInfo() {
            this.runs = 0;
            this.finished = false;
        }
    }

    private static int getNumKeysForRunCount(int runCount) {
        return 2 * runCount + 2;
    }

}
