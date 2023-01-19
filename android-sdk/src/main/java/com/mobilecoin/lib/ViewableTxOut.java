package com.mobilecoin.lib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobilecoin.api.MobileCoinAPI;
import com.mobilecoin.lib.exceptions.AmountDecoderException;
import com.mobilecoin.lib.exceptions.SerializationException;
import com.mobilecoin.lib.exceptions.TransactionBuilderException;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import fog_view.View;

public class ViewableTxOut {
    private static final String TAG = ViewableTxOut.class.getName();

    private final UnsignedLong txOutGlobalIndex;

    // The block index at which this TxOut appeared.
    private final UnsignedLong receivedBlockIndex;

    private final Date receivedBlockTimestamp;
    private Date spentBlockTimestamp;
    private UnsignedLong spentBlockIndex;

    private final Amount amount;
    private final RistrettoPublic txOutPublicKey;
    private final RistrettoPublic txOutTargetKey;

    ViewableTxOut(
            @NonNull final View.TxOutRecord txOutRecord,
            @NonNull final ViewAccountKey viewAccountKey
    ) {
        try {
            txOutGlobalIndex = UnsignedLong.fromLongBits(txOutRecord.getTxOutGlobalIndex());
            long longTimestampSeconds = txOutRecord.getTimestamp();
            // when the timestamp is missing U64::MAX is returned
            UnsignedLong timestampSeconds = UnsignedLong.fromLongBits(longTimestampSeconds);
            if (!timestampSeconds.equals(UnsignedLong.MAX_VALUE)) {
                long longTimestampMillis = TimeUnit.SECONDS.toMillis(timestampSeconds.longValue());
                receivedBlockTimestamp = new Date(longTimestampMillis);
            } else {
                receivedBlockTimestamp = null;
            }
            receivedBlockIndex = UnsignedLong.fromLongBits(txOutRecord.getBlockIndex());
            MobileCoinAPI.CompressedRistretto txOutPublicKeyProto =
                    MobileCoinAPI.CompressedRistretto.newBuilder()
                            .setData(txOutRecord.getTxOutPublicKeyData())
                            .build();
            txOutPublicKey = RistrettoPublic.fromProtoBufObject(txOutPublicKeyProto);
            MobileCoinAPI.CompressedRistretto txOutTargetKeyProto =
                    MobileCoinAPI.CompressedRistretto.newBuilder()
                            .setData(txOutRecord.getTxOutTargetKeyData())
                            .build();
            txOutTargetKey = RistrettoPublic.fromProtoBufObject(txOutTargetKeyProto);
            RistrettoPublic txOutSharedSecret = Util.getSharedSecret(viewAccountKey.getViewPrivateKey(), txOutPublicKey);
            long maskedValue = txOutRecord.getTxOutAmountMaskedValue();
            final MaskedAmount maskedAmount = txOutRecord.hasTxOutAmountMaskedV2TokenId() ?
                    new MaskedAmountV2(txOutSharedSecret, maskedValue, txOutRecord.getTxOutAmountMaskedV2TokenId().toByteArray()) :
                    new MaskedAmountV1(txOutSharedSecret, maskedValue, txOutRecord.getTxOutAmountMaskedV1TokenId().toByteArray());
            amount = maskedAmount.unmaskAmount(
                    viewAccountKey.getViewPrivateKey(),
                    txOutPublicKey
            );

            // Verify reconstructed commitment
            final byte reconstructedCommitment[] = maskedAmount.getCommitment();
            final int reconstructedCrc32 = Util.computeCommittmentCrc32(reconstructedCommitment);
            if(txOutRecord.getTxOutAmountCommitmentData().size() > 0) {
                final byte commitmentBytes[] = txOutRecord.getTxOutAmountCommitmentData().toByteArray();
                if(reconstructedCrc32 != Util.computeCommittmentCrc32(commitmentBytes)) {
                    throw(new SerializationException("Commitment CRC mismatch"));
                }
            }
            else {
                if(reconstructedCrc32 != txOutRecord.getTxOutAmountCommitmentDataCrc32()) {
                    throw(new SerializationException("Commitment CRC mismatch"));
                }
            }

            MobileCoinAPI.TxOut.Builder txOutProtoBuilder = MobileCoinAPI.TxOut.newBuilder()
                    .setPublicKey(txOutPublicKeyProto)
                    .setTargetKey(txOutTargetKeyProto);
            if(maskedAmount instanceof MaskedAmountV2) {
                txOutProtoBuilder.setMaskedAmountV2(maskedAmount.toProtoBufObject());
            } else {
                txOutProtoBuilder.setMaskedAmountV1(maskedAmount.toProtoBufObject());
            }
            if (!txOutRecord.getTxOutEMemoData().isEmpty()) {
                MobileCoinAPI.EncryptedMemo encryptedMemo = MobileCoinAPI.EncryptedMemo.newBuilder()
                        .setData(txOutRecord.getTxOutEMemoData()).build();
                txOutProtoBuilder.setEMemo(encryptedMemo);
            }

            // Calculated fields
            // TODO: memo stuff can wait
            /*TxOut nativeTxOut = TxOut.fromProtoBufObject(txOutProtoBuilder.build());
            byte decryptedMemoPayload[] = nativeTxOut.decryptMemoPayload(accountKey);
            cachedTxOutMemo = TxOutMemoParser
                    .parseTxOutMemo(decryptedMemoPayload, accountKey, nativeTxOut);*/
        } catch (SerializationException | AmountDecoderException | TransactionBuilderException e) {
            IllegalArgumentException illegalArgumentException =
                    new IllegalArgumentException("Unable to decode the TxOutRecord", e);
            Util.logException(TAG, illegalArgumentException);
            throw illegalArgumentException;
        }
    }

    synchronized void setSpent(
            @NonNull UnsignedLong spentBlockIndex,
            @Nullable Date spentBlockTimestamp
    ) {
        this.spentBlockIndex = spentBlockIndex;
        this.spentBlockTimestamp = spentBlockTimestamp;
    }

    public UnsignedLong getTxOutGlobalIndex() {
        return txOutGlobalIndex;
    }

    public UnsignedLong getReceivedBlockIndex() {
        return receivedBlockIndex;
    }

    public Date getReceivedBlockTimestamp() {
        return receivedBlockTimestamp;
    }

    public Date getSpentBlockTimestamp() {
        return spentBlockTimestamp;
    }

    public UnsignedLong getSpentBlockIndex() {
        return spentBlockIndex;
    }

    public Amount getAmount() {
        return amount;
    }

    public RistrettoPublic getPublicKey() {
        return txOutPublicKey;
    }

    public RistrettoPublic getTxOutTargetKey() {
        return txOutTargetKey;
    }

    public synchronized boolean isSpent(@NonNull UnsignedLong atIndex) {
        return (spentBlockIndex != null) && (spentBlockIndex.compareTo(atIndex) <= 0);
    }

}
