package com.mobilecoin.lib;

import com.google.protobuf.ByteString;
import com.mobilecoin.lib.exceptions.KexRngException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

class FogSearchKeyProvider {

    //Map FogSeed to the number of times its search keys have been scaled
    private Map<FogSeed, FogSeedState> fogSeeds;

    FogSearchKeyProvider(Collection<FogSeed> fogSeeds) {
        this.fogSeeds = new TreeMap<FogSeed, FogSeedState>();
        fogSeeds.stream().forEach(seed -> this.fogSeeds.put(seed, new FogSeedState()));
    }

    void addFogSeed(FogSeed fogSeed) {
        this.fogSeeds.put(fogSeed, new FogSeedState());
    }

    Map<ByteString, FogSeed> getNSearchKeys(int n) throws KexRngException {
        HashMap<ByteString, FogSeed> nextKeys = new HashMap<ByteString, FogSeed>();
        if(this.fogSeeds.size() == 0) return nextKeys;
        int numKeysAdded = 0;
        while(n > numKeysAdded) {
            for(FogSeed seed : this.fogSeeds.keySet()) {
                FogSeedState seedState = fogSeeds.get(seed);
                if(seedState.complete) continue;
                int runsForThisSeed = seedState.numRuns;
                int posOfThisSeed = seedState.cursorPos;
                int keysToGenerate = Math.min(getNumKeysForRunCount(runsForThisSeed), n - numKeysAdded);
                if(keysToGenerate <= 0) break;
                this.fogSeeds.put(seed, new FogSeedState(runsForThisSeed + 1, posOfThisSeed + keysToGenerate));
                byte[][] keys = seed.getNextN(posOfThisSeed + keysToGenerate);
                for(int i = posOfThisSeed; i < keysToGenerate + posOfThisSeed; i++) {
                    nextKeys.put(ByteString.copyFrom(keys[i]), seed);
                }
                numKeysAdded += keysToGenerate;
            }
        }
        return nextKeys;
    }

    void markSeedComplete(FogSeed fogSeed) {
        this.fogSeeds.put(fogSeed, new FogSeedState(0, 0, true));
    }

    void resetSeed(FogSeed fogSeed) {
        FogSeedState currentState = fogSeeds.get(fogSeed);
        fogSeeds.put(fogSeed, new FogSeedState(currentState.numRuns, 0));
    }

    boolean hasKeys() {
        for(FogSeedState state : fogSeeds.values()) {
            if(!state.complete) return true;
        }
        return false;
    }

    private static int getNumKeysForRunCount(int runCount) {
        return 3 * runCount + 2;
    }

    private static class FogSeedState {

        int numRuns;
        int cursorPos;
        boolean complete;

        FogSeedState() {
            this(0, 0, false);
        }

        FogSeedState(int numRuns, int cursorPos) {
            this(numRuns, cursorPos, false);
        }

        FogSeedState(int numRuns, int cursorPos, boolean complete) {
            this.numRuns = numRuns;
            this.cursorPos = cursorPos;
            this.complete = complete;
        }

    }

}
