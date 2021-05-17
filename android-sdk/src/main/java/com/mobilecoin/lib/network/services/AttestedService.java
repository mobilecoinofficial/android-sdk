package com.mobilecoin.lib.network.services;

import attest.Attest;

public interface AttestedService {
    Attest.AuthMessage auth(Attest.AuthMessage authMessage);
}
