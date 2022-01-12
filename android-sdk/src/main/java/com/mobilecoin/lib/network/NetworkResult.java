package com.mobilecoin.lib.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public class NetworkResult implements NetworkStatusResponse {

    public enum ResultCode {
        OK(0, 200),
        CANCELED(1, 499),
        UNKNOWN(2, 500),
        INVALID_ARGUMENT(3, 400),
        DEADLINE_EXCEEDED(4, 504),
        NOT_FOUND(5, 404),
        ALREADY_EXISTS(6, 409),
        PERMISSION_DENIED(7, 403),
        RESOURCE_EXHAUSTED(8, 429),
        FAILED_PRECONDITION(9, 400),
        ABORTED(10, 409),
        OUT_OF_RANGE(11, 400),
        UNIMPLEMENTED(12, 501),
        INTERNAL(13, 500),
        UNAVAILABLE(14, 503),
        DATA_LOSS(15, 500),
        UNAUTHENTICATED(16, 401);

        private static final ResultCode LOOKUP[] = {
                OK,
                CANCELED,
                UNKNOWN,
                INVALID_ARGUMENT,
                DEADLINE_EXCEEDED,
                NOT_FOUND,
                ALREADY_EXISTS,
                PERMISSION_DENIED,
                RESOURCE_EXHAUSTED,
                FAILED_PRECONDITION,
                ABORTED,
                OUT_OF_RANGE,
                UNIMPLEMENTED,
                INTERNAL,
                UNAVAILABLE,
                DATA_LOSS,
                UNAUTHENTICATED};

        ResultCode(int id, int code){
            this.id = id;
            this.code = code;
        }

        private final int id;
        private final int code;

        public final int getId() {
            return this.id;
        }

        public final int intValue() {
            return this.code;
        }

    }

    public NetworkResult(@NonNull NetworkStatusResponse code) {
        this(code.getResultCode());
    }

    public NetworkResult(@NonNull ResultCode code) {
        this(code, null, null);
    }

    public NetworkResult(@NonNull ResultCode code, @Nullable String description, @Nullable Throwable cause) {
        this.code = code;
        this.description = description;
        this.cause = cause;
    }

    public NetworkResult withDescription(String description) {
        if(!Objects.equals(description, this.description)) {
            return this;
        }
        else {
            return new NetworkResult(this.code, description, this.cause);
        }
    }

    public NetworkResult withCause(Throwable cause) {
        if (!Objects.equals(cause, this.cause)) {
            return this;
        }
        else {
            return new NetworkResult(this.code, this.description, cause);
        }
    }

    @Override
    public ResultCode getResultCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }

    public Throwable getCause() {
        return this.cause;
    }

    private final ResultCode code;
    private final String description;
    private final Throwable cause;

    public static final NetworkResult OK = new NetworkResult(ResultCode.OK);
    public static final NetworkResult CANCELED = new NetworkResult(ResultCode.CANCELED);
    public static final NetworkResult UNKNOWN = new NetworkResult(ResultCode.UNKNOWN);
    public static final NetworkResult INVALID_ARGUMENT = new NetworkResult(ResultCode.INVALID_ARGUMENT);
    public static final NetworkResult DEADLINE_EXCEEDED = new NetworkResult(ResultCode.DEADLINE_EXCEEDED);
    public static final NetworkResult NOT_FOUND = new NetworkResult(ResultCode.NOT_FOUND);
    public static final NetworkResult ALREADY_EXISTS = new NetworkResult(ResultCode.ALREADY_EXISTS);
    public static final NetworkResult PERMISSION_DENIED = new NetworkResult(ResultCode.PERMISSION_DENIED);
    public static final NetworkResult RESOURCE_EXHAUSTED = new NetworkResult(ResultCode.RESOURCE_EXHAUSTED);
    public static final NetworkResult FAILED_PRECONDITION = new NetworkResult(ResultCode.FAILED_PRECONDITION);
    public static final NetworkResult ABORTED = new NetworkResult(ResultCode.ABORTED);
    public static final NetworkResult OUT_OF_RANGE = new NetworkResult(ResultCode.OUT_OF_RANGE);
    public static final NetworkResult UNIMPLEMENTED = new NetworkResult(ResultCode.UNIMPLEMENTED);
    public static final NetworkResult INTERNAL = new NetworkResult(ResultCode.INTERNAL);
    public static final NetworkResult UNAVAILABLE = new NetworkResult(ResultCode.UNAVAILABLE);
    public static final NetworkResult DATA_LOSS = new NetworkResult(ResultCode.DATA_LOSS);
    public static final NetworkResult UNAUTHENTICATED = new NetworkResult(ResultCode.UNAUTHENTICATED);

}