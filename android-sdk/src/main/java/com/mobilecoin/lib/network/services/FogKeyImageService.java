package com.mobilecoin.lib.network.services;

import com.mobilecoin.lib.exceptions.NetworkException;

import attest.Attest;

public interface FogKeyImageService {
    Attest.AuthMessage auth(Attest.AuthMessage authMessage) throws NetworkException;
    Attest.Message checkKeyImages(Attest.Message request) throws NetworkException;
}
