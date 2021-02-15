package com.georgev22.voterewards.utilities.interfaces;

public interface Callback {
    void onSuccess();

    void onFailure(Throwable throwable);
}