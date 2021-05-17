package com.mobilecoin.lib.network.services;

import attest.Attest;

public interface FogKeyImageService {
    Attest.AuthMessage auth(Attest.AuthMessage authMessage);
    Attest.Message checkKeyImages(Attest.Message request);
}
