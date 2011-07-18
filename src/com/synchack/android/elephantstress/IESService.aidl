package com.synchack.android.elephantstress;
import com.synchack.android.elephantstress.ESStatus;

interface IESService {
    void start();
    void stop();
    ESStatus getESStatus();
}

