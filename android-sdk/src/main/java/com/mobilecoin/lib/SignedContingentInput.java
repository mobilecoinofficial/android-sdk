package com.mobilecoin.lib;

import androidx.annotation.NonNull;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public class SignedContingentInput extends Native {

    private SignedContingentInput(long rustObj) {
        this.rustObj = rustObj;
    }

    @NonNull
    static SignedContingentInput fromJNI(long rustObj) {
        return new SignedContingentInput(rustObj);
    }

    @NonNull
    public Amount getIncomeAmount() {
        return getPseudoOutputAmount();
    }

    @NonNull
    public Map<TokenId, Amount> getTotalOutlays() {
        final Amount requiredAmounts[] = getRequiredOutputAmounts();
        Map<TokenId, Amount> totalOutlays = new HashMap<>();
        for(Amount amount : requiredAmounts) {
            //TODO: on API level 24, we can use getOrDefault to simplify the logic here
            Amount existingAmount = totalOutlays.get(amount.getTokenId());
            if(null == existingAmount) existingAmount = new Amount(BigInteger.ZERO, amount.getTokenId());
            totalOutlays.put(amount.getTokenId(), existingAmount.add(amount));
        }
        final Amount pseudoOutputAmount = getPseudoOutputAmount();
        Amount changeAdjustment = totalOutlays.get(pseudoOutputAmount.getTokenId());
        if(null == changeAdjustment) changeAdjustment = new Amount(BigInteger.ZERO, pseudoOutputAmount.getTokenId());
        totalOutlays.put(pseudoOutputAmount.getTokenId(), pseudoOutputAmount.subtract(changeAdjustment));
        return totalOutlays;
    }

    public boolean isValid() {
        return is_valid();
    }

    @Override
    protected void finalize() throws Throwable {
        if (rustObj != 0) {
            finalize_jni();
        }
        super.finalize();
    }

    @NonNull
    public Amount[] getRequiredOutputAmounts() {
        return get_required_output_amounts();
    }

    @NonNull
    public Amount getPseudoOutputAmount() {
        return get_pseudo_output_amount();
    }

    @NonNull
    private native Amount[] get_required_output_amounts();

    @NonNull
    private native Amount get_pseudo_output_amount();

    private native boolean is_valid();

    private native void finalize_jni();

}
