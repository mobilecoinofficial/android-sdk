package com.mobilecoin.lib.network.services;

import com.mobilecoin.lib.exceptions.NetworkException;

import attest.Attest;

public interface FogViewService {
    Attest.AuthMessage auth(Attest.AuthMessage authMessage) throws NetworkException;
    Attest.Message query(Attest.Message message) throws NetworkException;
}
