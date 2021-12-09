package com.mobilecoin.lib.network;

import com.google.protobuf.Api;

import java.util.Objects;

import io.grpc.Status;

public class ApiResult {


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

    public ApiResult(ResultCode code) {
        this(code, null, null);
    }

    public ApiResult(ResultCode code, String description, Throwable cause) {
        this.code = code;
        this.description = description;
        this.cause = cause;
    }

    public ApiResult withDescription(String description) {
        if(!Objects.equals(description, this.description)) {
            return this;
        }
        else {
            return new ApiResult(this.code, description, this.cause);
        }
    }

    public ApiResult withCause(Throwable cause) {
        if (!Objects.equals(cause, this.cause)) {
            return this;
        } else {
            return new ApiResult(this.code, this.description, cause);
        }
    }

    public static ApiResult from(Status status) {
        ApiResult apiResult;
        switch (status.getCode()) {
            case OK:
                apiResult = ApiResult.OK;
                break;
            case INVALID_ARGUMENT:
                apiResult = ApiResult.INVALID_ARGUMENT;
                break;
            case FAILED_PRECONDITION:
                apiResult = ApiResult.FAILED_PRECONDITION;
                break;
            case OUT_OF_RANGE:
                apiResult = ApiResult.OUT_OF_RANGE;
                break;
            case UNAUTHENTICATED:
                apiResult = ApiResult.UNAUTHENTICATED;
                break;
            case PERMISSION_DENIED:
                apiResult = ApiResult.PERMISSION_DENIED;
                break;
            case NOT_FOUND:
                apiResult = ApiResult.NOT_FOUND;
                break;
            case ABORTED:
                apiResult = ApiResult.ABORTED;
                break;
            case ALREADY_EXISTS:
                apiResult = ApiResult.ALREADY_EXISTS;
                break;
            case RESOURCE_EXHAUSTED:
                apiResult = ApiResult.RESOURCE_EXHAUSTED;
                break;
            case CANCELLED:
                apiResult = ApiResult.CANCELED;
                break;
            case DATA_LOSS:
                apiResult = ApiResult.DATA_LOSS;
                break;
            case INTERNAL:
                apiResult = ApiResult.INTERNAL;
                break;
            case UNAVAILABLE:
                apiResult = ApiResult.UNAVAILABLE;
                break;
            case DEADLINE_EXCEEDED:
                apiResult = ApiResult.DEADLINE_EXCEEDED;
                break;
            default:
                // UNIMPLEMENTED
                apiResult = ApiResult.UNKNOWN;
        }
        return apiResult.withDescription(status.getDescription())
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

    public static final ApiResult OK = new ApiResult(ResultCode.OK);
    public static final ApiResult CANCELED = new ApiResult(ResultCode.CANCELLED);
    public static final ApiResult UNKNOWN = new ApiResult(ResultCode.UNKNOWN);
    public static final ApiResult INVALID_ARGUMENT = new ApiResult(ResultCode.INVALID_ARGUMENT);
    public static final ApiResult DEADLINE_EXCEEDED = new ApiResult(ResultCode.DEADLINE_EXCEEDED);
    public static final ApiResult NOT_FOUND = new ApiResult(ResultCode.NOT_FOUND);
    public static final ApiResult ALREADY_EXISTS = new ApiResult(ResultCode.ALREADY_EXISTS);
    public static final ApiResult PERMISSION_DENIED = new ApiResult(ResultCode.PERMISSION_DENIED);
    public static final ApiResult RESOURCE_EXHAUSTED = new ApiResult(ResultCode.RESOURCE_EXHAUSTED);
    public static final ApiResult FAILED_PRECONDITION = new ApiResult(ResultCode.FAILED_PRECONDITION);
    public static final ApiResult ABORTED = new ApiResult(ResultCode.ABORTED);
    public static final ApiResult OUT_OF_RANGE = new ApiResult(ResultCode.OUT_OF_RANGE);
    public static final ApiResult UNIMPLEMENTED = new ApiResult(ResultCode.UNIMPLEMENTED);
    public static final ApiResult INTERNAL = new ApiResult(ResultCode.INTERNAL);
    public static final ApiResult UNAVAILABLE = new ApiResult(ResultCode.UNAVAILABLE);
    public static final ApiResult DATA_LOSS = new ApiResult(ResultCode.DATA_LOSS);
    public static final ApiResult UNAUTHENTICATED = new ApiResult(ResultCode.UNAUTHENTICATED);

}