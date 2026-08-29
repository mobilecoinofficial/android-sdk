// Copyright (c) 2020-2026 MobileCoin. All rights reserved.

package com.mobilecoin.lib;

import org.junit.Rule;

/**
 * Base for the tests that spend real balance.
 * <p>
 * JUnit picks up {@code @Rule} fields declared on a superclass, so extending
 * this is all such a test needs to have {@link FundingDiagnosticRule} name the
 * wallet to fund when it fails for want of funds.
 */
public abstract class FundedTest {

    @Rule
    public final FundingDiagnosticRule fundingDiagnostic = new FundingDiagnosticRule();

}
