package com.appyads.services;

public class AppyAdQuickThread implements Runnable {

    private static final String TAG = "AppyAdQuickThread";
    private int mOp;
    private String mAccount,mApp,mCampaign,mCustom,mUid,mScreen,mAdid,mAdlink;
    private String mSendString;

    public AppyAdQuickThread(int op, String account, String app, String campaign, String custom, String uid, String screen, String adid, String adlink) {
        mSendString = "|"+op+"|account="+account+"\\app="+app+"\\campaign="+campaign+"\\custom="+custom+"\\user="+uid+"\\screen="+screen+"\\adid="+adid+"\\adlink="+adlink+"\\";
        mOp = op;
    }

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
