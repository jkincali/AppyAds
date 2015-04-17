package com.appyads.services;

import android.support.v4.app.Fragment;

public class AppyAdThreadMsg {

    public Fragment mFragment;
    public String mData;

    public AppyAdThreadMsg(Fragment ts, String md) {
        mFragment = ts;
        mData = md;
    }
}
