package com.mobilecoin.lib;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mobilecoin.lib.exceptions.AttestationException;
import com.mobilecoin.lib.util.Hex;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class VerifierTest {

    @Rule
    public ExpectedException exceptionChecker = ExpectedException.none();

    private static final short PRODUCT_ID = 3;
    private static final short SECURITY_VERSION = 1;
    private static final String[] CONFIG_ADVISORIES = {"INTEL-SA-00391"};
    private static final String[] HARDENING_ADVISORIES = {"INTEL-SA-00334"};

    private TrustedIdentities trustedIdentities;

    @Before
    public void setUp() throws Exception {
        trustedIdentities = new TrustedIdentities();
    }

    @Test
    public void withMrSigner_mrSignerIsNull_throwsAttestationException() throws Exception {
        exceptionChecker.expect(AttestationException.class);
        trustedIdentities.addMrSignerIdentity(/* mrSigner= */ null, PRODUCT_ID, SECURITY_VERSION,
                CONFIG_ADVISORIES, HARDENING_ADVISORIES);
    }

    @Test
    public void withMrSigner_emptyMrSigner_throwsAttestationException() throws Exception {
        byte[] emptyMrSigner = {};

        exceptionChecker.expect(AttestationException.class);
        trustedIdentities.addMrSignerIdentity(emptyMrSigner, PRODUCT_ID, SECURITY_VERSION,
                CONFIG_ADVISORIES, HARDENING_ADVISORIES);
    }

    @Test
    public void withMrSigner_mrSignerHasLessThan32Digits_throwsAttestationException() throws Exception {
        // Corresponds to 9 unsigned 8 bit integers.
        String lessThan32DigitsMrSignerHex = "7ee5e29d74623fdb1f";
        exceptionChecker.expect(AttestationException.class);

        trustedIdentities.addMrSignerIdentity(Hex.toByteArray(lessThan32DigitsMrSignerHex), PRODUCT_ID,
                SECURITY_VERSION,
                CONFIG_ADVISORIES, HARDENING_ADVISORIES);
    }

    @Test
    public void withMrSigner_mrSignerHasMoreThan32Digits_throwsAttestationException() throws Exception {
        // Corresponds to 33 unsigned 8 bit integers.
        String moreThan32DigitsMrSignerHex =
                "7ee5e29d74623fdbc6fbf1454be6f3bb0b86c12366b7b478ad13353e44de8411ff";
        exceptionChecker.expect(AttestationException.class);

        trustedIdentities.addMrSignerIdentity(Hex.toByteArray(moreThan32DigitsMrSignerHex), PRODUCT_ID,
                SECURITY_VERSION,
                CONFIG_ADVISORIES, HARDENING_ADVISORIES);
    }

    @Test
    public void withMrSigner_validMrSigner_doesNotThrowException() throws Exception {
        // Corresponds to 32 unsigned 8 bit integers.
        String validMrSigner =
                "7ee5e29d74623fdbc6fbf1454be6f3bb0b86c12366b7b478ad13353e44de8411";
        trustedIdentities.addMrSignerIdentity(Hex.toByteArray(validMrSigner), PRODUCT_ID, SECURITY_VERSION,
                CONFIG_ADVISORIES, HARDENING_ADVISORIES);
    }

    @Test
    public void withMrEnclave_mrEnclaveIsNull_throwsAttestationException() throws Exception {
        exceptionChecker.expect(AttestationException.class);
        trustedIdentities.addMrEnclaveIdentity(/* mrEnclave= */ null,
                CONFIG_ADVISORIES, HARDENING_ADVISORIES);
    }

    @Test
    public void withMrEnclave_mrEnclaveIsEmpty_throwsAttestationException() throws Exception {
        byte[] emptyMrEnclave = {};

        exceptionChecker.expect(AttestationException.class);
        trustedIdentities.addMrEnclaveIdentity(emptyMrEnclave,
                CONFIG_ADVISORIES, HARDENING_ADVISORIES);
    }

    @Test
    public void withMrEnclave_mrEnclaveHasLessThan32Digits_throwsAttestationException() throws Exception {
        // Corresponds to 9 unsigned 8 bit integers.
        String lessThan32DigitsMrEnclaveHex = "7ee5e29d74623fdb1f";

        exceptionChecker.expect(AttestationException.class);
        trustedIdentities.addMrEnclaveIdentity(Hex.toByteArray(lessThan32DigitsMrEnclaveHex),
                CONFIG_ADVISORIES, HARDENING_ADVISORIES);
    }

    @Test
    public void withMrEnclave_mrEnclaveHasMoreThan32Digits_throwsAttestationException() throws Exception {
        // Corresponds to 33 unsigned 8 bit integers.
        String moreThan32DigitsMrEnclaveHex =
                "7ee5e29d74623fdbc6fbf1454be6f3bb0b86c12366b7b478ad13353e44de8411ff";

        exceptionChecker.expect(AttestationException.class);
        trustedIdentities.addMrEnclaveIdentity(Hex.toByteArray(moreThan32DigitsMrEnclaveHex),
                CONFIG_ADVISORIES, HARDENING_ADVISORIES);
    }

    @Test
    public void withMrEnclave_validMrEnclave_doesNotThrowException() throws Exception {
        // Corresponds to 32 unsigned 8 bit integers.
        String validMrEnclaveHex =
                "7ee5e29d74623fdbc6fbf1454be6f3bb0b86c12366b7b478ad13353e44de8411";

        trustedIdentities.addMrEnclaveIdentity(Hex.toByteArray(validMrEnclaveHex),
                CONFIG_ADVISORIES, HARDENING_ADVISORIES);
    }
}
