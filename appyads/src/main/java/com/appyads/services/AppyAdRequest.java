package com.appyads.services;

/**
 * This class is used as a storage device for parameters needed in communication with the AppyAds server.
 */
public class AppyAdRequest {

    public int operation = 0;
    public String track = null;
    public String accId = null;
    public String appId = null;
    public String campAcct = null;
    public String campId = null;
    public String campSize = null;
    public String adId = null;
    public String adLink = null;
    public String custom = null;
    public String uSpec = null;
    public String screen = null;
    public int width = 0;
    public int height = 0;


    public AppyAdRequest(int op, String accId, String campSize, String track) {
        this.operation = op;
        this.accId = accId;
        this.campSize = campSize;
        this.track = track;
    }

    public AppyAdRequest(int op, String accId, String appId, String campAcct, String campId, String campSize, String custom, String uSpec, String screen, int width, int height, String adId, String adLink) {
        this.operation = op;
        this.accId = accId;
        this.appId = appId;
        this.campAcct = campAcct;
        this.campId = campId;
        this.campSize = campSize;
        this.custom = custom;
        this.uSpec = uSpec;
        this.screen = screen;
        this.width = width;
        this.height = height;
        this.adId = adId;
        this.adLink = adLink;
    }
}
