package com.appyads.services;

/**
 * This class supplies the AppyAds system with a lightweight communication module, which is used
 * to quickly send a small informational packed to be sent to the AppyAds server.  This thread
 * is only used when tracking has been enabled for a particular ad view.
 * Note that there is no status sent back to the UI thread.
 */
public class AppyAdQuickThread implements Runnable {

    private static final String TAG = "AppyAdQuickThread";
    private AppyAdRequest mAppyAdRequest;

    /**
     * This constructor is used to initialize the thread service with the appropriate parameters.
     * @param aRequest - An object containing all parameters necessary to call AppyAds server.
     */
    public AppyAdQuickThread(AppyAdRequest aRequest) {
        mAppyAdRequest = aRequest;
    }

    /**
     * This method runs in the background (non-UI) and sends a tracking request to the AppyAds server and then exits.
     */
    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        AppyAdService.getInstance().debugOut(TAG,"Ad quick thread launch.");

        AppyAdSendServer tgsd = new AppyAdSendServer(mAppyAdRequest);

        tgsd.queryServer();
        if (tgsd.mStatus) {
            AppyAdService.getInstance().debugOut(TAG,"Ad tracking sent ok.");
        }
        else {
            AppyAdService.getInstance().errorOut(TAG,"Problem sending ad tracking. "+tgsd.getSpecErrorStr());
        }
    }

}
