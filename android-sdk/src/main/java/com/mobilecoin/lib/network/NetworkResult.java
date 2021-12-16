package com.mobilecoin.lib.network;

import java.util.Objects;

import io.grpc.Status;

public class NetworkResult {


    public enum ResultCode {
        OK(0),
        CANCELLED(1),
        UNKNOWN(2),
        INVALID_ARGUMENT(3),
        DEADLINE_EXCEEDED(4),
        NOT_FOUND(5),
        ALREADY_EXISTS(6),
        PERMISSION_DENIED(7),
        RESOURCE_EXHAUSTED(8),
        FAILED_PRECONDITION(9),
        ABORTED(10),
        OUT_OF_RANGE(11),
        UNIMPLEMENTED(12),
        INTERNAL(13),
        UNAVAILABLE(14),
        DATA_LOSS(15),
        UNAUTHENTICATED(16);

        private static final ResultCode LOOKUP[] = {OK,
                CANCELLED,
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

        ResultCode(int code){
            this.code = code;
        }

        private final int code;

    }

    public NetworkResult(ResultCode code) {
        this(code, null, null);
    }

    public NetworkResult(ResultCode code, String description, Throwable cause) {
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

    public static NetworkResult from(Status status) {
        NetworkResult networkResult;
        switch (status.getCode()) {
            case OK:
                networkResult = NetworkResult.OK;
                break;
            case INVALID_ARGUMENT:
                networkResult = NetworkResult.INVALID_ARGUMENT;
                break;
            case FAILED_PRECONDITION:
                networkResult = NetworkResult.FAILED_PRECONDITION;
                break;
            case OUT_OF_RANGE:
                networkResult = NetworkResult.OUT_OF_RANGE;
                break;
            case UNAUTHENTICATED:
                networkResult = NetworkResult.UNAUTHENTICATED;
                break;
            case PERMISSION_DENIED:
                networkResult = NetworkResult.PERMISSION_DENIED;
                break;
            case NOT_FOUND:
                networkResult = NetworkResult.NOT_FOUND;
                break;
            case ABORTED:
                networkResult = NetworkResult.ABORTED;
                break;
            case ALREADY_EXISTS:
                networkResult = NetworkResult.ALREADY_EXISTS;
                break;
            case RESOURCE_EXHAUSTED:
                networkResult = NetworkResult.RESOURCE_EXHAUSTED;
                break;
            case CANCELLED:
                networkResult = NetworkResult.CANCELED;
                break;
            case DATA_LOSS:
                networkResult = NetworkResult.DATA_LOSS;
                break;
            case INTERNAL:
                networkResult = NetworkResult.INTERNAL;
                break;
            case UNAVAILABLE:
                networkResult = NetworkResult.UNAVAILABLE;
                break;
            case DEADLINE_EXCEEDED:
                networkResult = NetworkResult.DEADLINE_EXCEEDED;
                break;
            default:
                // UNIMPLEMENTED
                networkResult = NetworkResult.UNKNOWN;
        }
        return networkResult.withDescription(status.getDescription())
                        .withCause(status.getCause());
    }

    public ResultCode getCode() {
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
    public static final NetworkResult CANCELED = new NetworkResult(ResultCode.CANCELLED);
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