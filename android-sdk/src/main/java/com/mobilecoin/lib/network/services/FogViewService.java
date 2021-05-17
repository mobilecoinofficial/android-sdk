package com.mobilecoin.lib.network.services;

import attest.Attest;

public interface FogViewService {
    Attest.AuthMessage auth(Attest.AuthMessage authMessage);
    Attest.Message query(Attest.Message message);
}
