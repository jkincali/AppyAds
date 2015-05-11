package com.appyads.services;

/**
 * This class supplies the AppyAds system with a lightweight communication module, which is used
 * to quickly send a small informational packed to be sent to the AppyAds server.  This thread
 * is only used when tracking has been enabled for a particular ad view.
 * Note that there is no status sent back to the UI thread.
 */
public class AppyAdQuickThread implements Runnable {

    private static final String TAG = "AppyAdQuickThread";
    private int mOp;
    private String mAccount,mApp,mCampaign,mCustom,mUid,mScreen,mAdid,mAdlink;
    private String mSendString;

    /**
     * This constructor is used to initialize the thread service with the appropriate parameters.
     * @param op - An int value representing the operational code for execution.
     * @param account - A String value indicating the AppyAds account id.
     * @param app - A String value indicating the appliction id.
     * @param campaign - A String value indicating the ad campaign id.
     * @param custom - A String value reserved for the application developer/owner.
     * @param uid - A String representing a unique user/device identifier.
     * @param screen - A String representing the device's screen density.
     * @param width - An int value representing the view's width.
     * @param height - An int value representing the view's height.
     * @param adid - A String representing the ad's id.
     * @param adlink - A String representing the link that the user was sent to after tapping/clicking on the ad.
     */
    public AppyAdQuickThread(int op, String account, String app, String campaign, String custom, String uid, String screen, int width, int height, String adid, String adlink) {
        mSendString = "|"+op+"|account="+account+"\\app="+app+"\\campaign="+campaign+"\\custom="+custom+"\\user="+uid+"\\screen="+screen+"\\width="+width+"\\height="+height+"\\adid="+adid+"\\adlink="+adlink+"\\";
        mOp = op;
    }

    /**
     * This method runs in the background (non-UI) and sends a tracking request to the AppyAds server and then exits.
     */
    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        AppyAdService.getInstance().debugOut(TAG,"Ad quick thread launch.");

        AppyAdSendServer tgsd = new AppyAdSendServer(mOp, mSendString);

        tgsd.queryServer();
        if (tgsd.mStatus) {
            AppyAdService.getInstance().debugOut(TAG,"Ad tracking sent ok.");
        }
        else {
            AppyAdService.getInstance().errorOut(TAG,"Problem sending ad tracking. "+tgsd.getSpecErrorStr());
        }
    }

}
