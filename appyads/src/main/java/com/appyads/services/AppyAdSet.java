package com.appyads.services;

import java.util.HashMap;
import java.util.Map;

public class AppyAdSet {
    private Map<String, AppyAd> tozAdSets = new HashMap<String, AppyAd>();

    public void addAd(String key, AppyAd ad) {
        if ((key != null) && (ad != null)) tozAdSets.put(key,ad);
    }

    public AppyAd getAd(String key) {
        return (tozAdSets.get(key));
    }

    public void clearAds() {
        tozAdSets.clear();
    }
}
