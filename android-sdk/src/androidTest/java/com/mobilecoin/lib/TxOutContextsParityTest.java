package com.mobilecoin.lib;

import static org.junit.Assert.assertEquals;

import android.util.Base64;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Pins the TxOut public keys one seed derives, as a vector shared verbatim
 * with MobileCoin-Swift's {@code TxOutContextsParityTests}.
 *
 * <p>Sentz seals a TxOut public key into offramp credentials on whichever
 * platform the user happens to be on, and the transaction that has to carry
 * that key is built later. Each SDK proving itself self-consistent does not
 * cover that: both can be internally stable and still derive different keys
 * from the same seed, and the mismatch would surface as credentials naming an
 * output that never reaches the chain, after funds have moved.
 *
 * <p>{@link TxOutContextsTest} covers what the derivation ignores — amounts,
 * fees, memos, token id, block version. This covers the one thing those
 * cannot: that the other platform agrees.
 *
 * <p>Every input that reaches a key is shared with the Swift test: the seed,
 * the two serialized identities, the block version and the token id. A
 * deliberate change moves the constants on both sides in the same change;
 * edited on one side alone, the vector proves nothing.
 *
 * <p>Two inputs are not shared, and neither reaches a draw. The two sides
 * reach their fog reports differently — Swift resolves a canned report
 * offline, this fetches TestNet live — because only Swift has a seam below fog
 * resolution. That is deliberate and is not a gap: the report's contents
 * change which key the hint is encrypted to, never how many draws encrypting
 * it takes, so the draw that produces {@code r} lands in the same place either
 * way. The tombstone block index is the other: a constant on each side, but a
 * different one. {@code getTxOutContexts} derives it from
 * {@link TxOutStore#getCurrentBlockIndex()}, which holds
 * {@link UnsignedLong#ZERO} until a refresh this test never runs — and no
 * {@code StorageAdapter} is supplied, so no cached store is restored either —
 * leaving it at a fixed {@code 0 + 50}. It only decides whether a report is
 * fresh enough to resolve at all, and is imposed on the builder after both
 * draws; an index nothing can satisfy fails resolution outright rather than
 * quietly moving a draw.
 *
 * <p>Two paths the vector does not compare. Block version is pinned to 1, so
 * the {@code blockVersion < 1} branch that sends change through
 * {@code addOutput} rather than {@code addChangeOutput} is never run on either
 * side. Both identities carry fog, so the fake-hint path a recipient without
 * fog takes is never run either. A platform divergence in either would pass
 * here unseen.
 */
@RunWith(AndroidJUnit4.class)
public class TxOutContextsParityTest {

    /**
     * Arbitrary, and fixed only so both platforms draw from the same stream:
     * 32 bytes, 31 of ASCII and a trailing zero. Referenced from
     * {@link TxOutContextsTest} rather than copied, so the two tests cannot
     * drift onto different seeds and leave this vector asserting nothing.
     */
    private static final byte[] SEED = TxOutContextsTest.SEED;

    /**
     * Serialized {@code AccountKey} and {@code PublicAddress} protobufs, the
     * wire form both SDKs deserialize, so the two run the same sender and
     * recipient rather than two fixtures that happen to agree today.
     */
    private static final String ACCOUNT_KEY_B64 =
            "CiIKIMM0eFjAenKpb/qWCpMpLBobETadGuaJCNy45N0Ej7sPEiIKIEU+8R66uCyb7xsVbX9qAPUa6cTxIpeQ"
            + "NIEQFoPbWFUKGh1mb2c6Ly9mb2cudGVzdC5tb2JpbGVjb2luLmNvbSqmBDCCAiIwDQYJKoZIhvcNAQEBBQ"
            + "ADggIPADCCAgoCggIBAL5wfcE20zk+bqIs0WGmG8O1yBJCJ5fBOzBjgEI/sZwvhdayF4gp3P7dfuSCFo20"
            + "RoVs6O0QMCObEWo59rE+K0Z/TV2zs2TLyKhOIZoZhM8tWEDQ53wCwFjUPlgW2BlvlaptyJULwBRY1TdWGC"
            + "HWIWy4wD3ZIHlbFn3Cw36Kx5+q0d0AWWGSJUgUEikTGP7csE8Xkwryts1nEtJG2xT7QXFbYe1RRVTwGV4T"
            + "4vcstQL55XTup+yi4rqVZqI5RDLb+BUJJOtOJ2pfo/3TqZUwE1fGvQCQWz0QWf8kIOexBtmNjEYhzkInyc"
            + "dEuVWzcjJvW5EvEw+xqIufWglujk9YMnqLVsC4OtCUWU38ie5WFgUjs4dDp2gsrUaUlrTWem2qz1Hjp37W"
            + "5ybRPKxYRezOBeunrdCyP3Lr12HnMFcMpKLxFQSkReBzivRoEpte5kDLc6w+3OefE22rnDlmm2EdOLoXQH"
            + "N7NdDJLjjVhtMCEIYCAoWFQBpxS70qadv2kBKt8a0UhE8bIsVCI7GcllkTpLgNCBZ3PHewJnJ1Ab0VuxU/"
            + "+bYVspOWoHWFBmfuwtaOvYoUdWMZqBoevXyzDyBDoWIee9vt3JIJdkmleLqPRr5M/DDBkQXDCDJUYq0sIQ"
            + "n6M1dkck+Vp9TYD6cnPMyS+0HToS+0MW/uVo5wla0GByNnAgMBAAE=";

    private static final String RECIPIENT_B64 =
            "CiIKILJgHbpuWJZ6abjlsUrrOQb30Y1VYocTSl4mmf2W4IpQEiIKILQV1C5Bb60d0cwYIwuh5qXks7MtNe4w"
            + "dL/x6KEHehMBGh1mb2c6Ly9mb2cudGVzdC5tb2JpbGVjb2luLmNvbSpA5PqNG7wSNvSF67qGDfhKujwO0x"
            + "+RWzbwR7WW4qH01VXBOwPw0m+z/Z4bb8ZjoyAUaHjbtcAG7NLjSVVLR2/Niw==";

    /** What the inputs above must produce, on either platform. */
    private static final String EXPECTED_PAYLOAD_KEY_B64 =
            "xArSo1TSmrdSOO5z/ChxT1A/asuGT7z3/4I0tTylKH4=";
    private static final String EXPECTED_CHANGE_KEY_B64 =
            "vnTuxXXogiaGAWcaU5HUZ1VYzaP+mjnJbMudemo3qAA=";

    /**
     * Block version and token id are pinned rather than read from the network,
     * so neither can move the vector when the network moves. Both are covered
     * as irrelevant to the keys by {@link TxOutContextsTest}; pinning them here
     * only keeps this test asserting one thing. No entry point takes the
     * tombstone block index, but nothing here moves it either — see the class
     * comment.
     */
    @Test
    public void testSeedDerivesTheKeysSwiftDerives() throws Exception {
        final AccountKey accountKey =
                AccountKey.fromBytes(Base64.decode(ACCOUNT_KEY_B64, Base64.DEFAULT));
        final PublicAddress recipient =
                PublicAddress.fromBytes(Base64.decode(RECIPIENT_B64, Base64.DEFAULT));

        final MobileCoinClient client =
                MobileCoinClientBuilder.newBuilder().setAccountKey(accountKey).build();

        final TxOutContexts derived =
                client.getTxOutContexts(recipient, SEED, 1, TokenId.MOB);

        assertEquals(
                EXPECTED_PAYLOAD_KEY_B64,
                Base64.encodeToString(
                        derived.getPayload().getTxOutPublicKey().getKeyBytes(), Base64.NO_WRAP));
        assertEquals(
                EXPECTED_CHANGE_KEY_B64,
                Base64.encodeToString(
                        derived.getChange().getTxOutPublicKey().getKeyBytes(), Base64.NO_WRAP));
    }
}
