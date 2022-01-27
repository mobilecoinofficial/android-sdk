package com.mobilecoin.lib.network.grpc;

import com.mobilecoin.lib.network.NetworkResult;
import com.mobilecoin.lib.network.NetworkStatusResponse;

import io.grpc.Status;

public class GRPCStatusResponse implements NetworkStatusResponse {

    private final NetworkResult.ResultCode resultCode;

    public GRPCStatusResponse(Status status) {
        switch (status.getCode()) {
            case OK:
                this.resultCode = NetworkResult.ResultCode.OK;
                break;
            case INVALID_ARGUMENT:
                this.resultCode = NetworkResult.ResultCode.INVALID_ARGUMENT;
                break;
            case FAILED_PRECONDITION:
                this.resultCode = NetworkResult.ResultCode.FAILED_PRECONDITION;
                break;
            case OUT_OF_RANGE:
                this.resultCode = NetworkResult.ResultCode.OUT_OF_RANGE;
                break;
            case UNAUTHENTICATED:
                this.resultCode = NetworkResult.ResultCode.UNAUTHENTICATED;
                break;
            case PERMISSION_DENIED:
                this.resultCode = NetworkResult.ResultCode.PERMISSION_DENIED;
                break;
            case NOT_FOUND:
                this.resultCode = NetworkResult.ResultCode.NOT_FOUND;
                break;
            case ABORTED:
                this.resultCode = NetworkResult.ResultCode.ABORTED;
                break;
            case ALREADY_EXISTS:
                this.resultCode = NetworkResult.ResultCode.ALREADY_EXISTS;
                break;
            case RESOURCE_EXHAUSTED:
                this.resultCode = NetworkResult.ResultCode.RESOURCE_EXHAUSTED;
                break;
            case CANCELLED:
                this.resultCode = NetworkResult.ResultCode.CANCELED;
                break;
            case DATA_LOSS:
                this.resultCode = NetworkResult.ResultCode.DATA_LOSS;
                break;
            case INTERNAL:
                this.resultCode = NetworkResult.ResultCode.INTERNAL;
                break;
            case UNAVAILABLE:
                this.resultCode = NetworkResult.ResultCode.UNAVAILABLE;
                break;
            case DEADLINE_EXCEEDED:
                this.resultCode = NetworkResult.ResultCode.DEADLINE_EXCEEDED;
                break;
            default:
                // UNIMPLEMENTED
                this.resultCode = NetworkResult.ResultCode.UNKNOWN;
        }
    }

    @Override
    public NetworkResult.ResultCode getResultCode() {
        return this.resultCode;
    }
}
