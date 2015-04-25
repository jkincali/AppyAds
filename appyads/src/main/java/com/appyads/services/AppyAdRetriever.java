package com.appyads.services;

import android.os.Handler;
import android.os.Message;

import java.nio.ByteBuffer;

public class AppyAdRetriever implements Runnable {

    private static final String TAG = "AppyAdRetriever";
	private String[] errorMsg = new String[] {"",""};
	private int controlRsp = 7;

	public void postNextAd(Handler mHandle) {
		Message message = mHandle.obtainMessage();
		if (controlRsp != 7) {
			message.obj = errorMsg;
			message.what = controlRsp; // 7;
			mHandle.sendMessage(message);
		}
		else {
			message.obj = new AppyAdThreadMsg(null,"");
			message.what = controlRsp; // 7;
			mHandle.sendMessage(message);
		}
	}

	private void setErrorMsg(int e, String[] a) {
        controlRsp = e;
        errorMsg = a;
        AppyAdService.getInstance().errorOut(TAG,errorMsg[0]+" - "+errorMsg[1]);
	}

	@Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        AppyAdService.getInstance().debugOut(TAG,"Ad service thread launch.");
		if (!AppyAdService.getInstance().adThreadIsRunning()) {
            AppyAdService.getInstance().debugOut(TAG,"Ad service thread running.");
			AppyAdService.getInstance().startAdThread();

			AppyAdService.getInstance().startService();
		
			while (AppyAdService.getInstance().AdServiceIsOn()) {
                AppyAdService.getInstance().debugOut(TAG,"Ad service thread - normal loop. Service is ON. Error count is "+ AppyAdService.getInstance().getErrorCount());
				if (AppyAdService.getInstance().AdsAreOn()) {
                    if (AppyAdService.getInstance().adsNeedRefreshing()) {
                        if (AppyAdService.getInstance().isNetworkAvailable()) {
                            ByteBuffer retBuf;
                            AppyAdSendServer tgsd = new AppyAdSendServer(AppyAdStatic.GETADSET,
                                    AppyAdService.getInstance().getDefaultTracking() ? "true" : "false",
                                    AppyAdService.getInstance().getAccountID(),
                                    AppyAdService.getInstance().getApplicationID(),
                                    AppyAdService.getInstance().getCampaignID(),
                                    AppyAdService.getInstance().getCustomSpec(),
                                    AppyAdService.getInstance().getUUID(),
                                    AppyAdService.getInstance().getScreenDensity());
                            retBuf = tgsd.queryServer();
                            if (tgsd.mStatus) {
                                AppyAdService.getInstance().setAdData(retBuf);
                                if (AppyAdService.getInstance().hasValidAdCampaign()) {
                                    AppyAdService.getInstance().initializeCounters();
                                    controlRsp = 7;
                                }
                                else {
                                    setErrorMsg(1, new String[] {"Non-existent Ad Campaign.","Entering holding pattern."});
                                    AppyAdService.getInstance().checkMaxErrors(AppyAdService.getInstance().maxErrors());
                                }
                            } else {
                                setErrorMsg(1, tgsd.getSpecError());
                                AppyAdService.getInstance().checkMaxErrors();
                            }
                        }
                        else {
                            setErrorMsg(1, new String[] {"Network access unavailable.",""});
                            AppyAdService.getInstance().checkMaxErrors();
                        }
					}

					if (controlRsp == 7) {
                        try { Thread.sleep(AppyAdService.getInstance().getCurrentSleepInterval()); } catch(InterruptedException e) { }

                        if (AppyAdService.getInstance().prepareNextAd()) {
							postNextAd(AppyAdService.getHandler());
						}
					}

				}
				else {
					AppyAdService.getInstance().checkDelayMaxed();
					AppyAdService.getInstance().debugOut(TAG,"Current error count is "+ AppyAdService.getInstance().getErrorCount()+" and delay count is "+ AppyAdService.getInstance().getDelayCount());
                }

                if (controlRsp != 7) {
                    try { Thread.sleep(AppyAdService.getInstance().getErrorWaitInterval()); } catch(InterruptedException e) { }
                }
			}
			
			AppyAdService.getInstance().stopAdThread();
		}
	}

}
