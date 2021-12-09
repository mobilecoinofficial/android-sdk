package com.mobilecoin.lib.network.services;

import com.mobilecoin.lib.exceptions.NetworkException;

import attest.Attest;

public interface AttestedService {
    Attest.AuthMessage auth(Attest.AuthMessage authMessage) throws NetworkException;
}
