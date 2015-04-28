package com.appyads.services;

import android.os.Handler;
import android.os.Message;

import java.nio.ByteBuffer;

/**
 * AppyAdRetriever
 *
 * This module runs as a background thread and provides the AppyAds system with the following
 * functionality:
 *      - Fetches new ad campaign packages from the AppyAds service
 *      - Sends notifications back to the AppyAdsService module
 *          a) For new ad campaign packages
 *          b) For the next ad view to be shown
 *
 */
public class AppyAdRetriever implements Runnable {

    private static final String TAG = "AppyAdRetriever";
	private String[] errorMsg = new String[] {"",""};
	private int controlRsp = 7;

    /**
     * This method sends a message back to the AppyAdsService module
     *
     * @param mHandle - The Handler object within the AppyAdsService module, which receives the message.
     */
	public void postNextAd(Handler mHandle) {
		Message message = mHandle.obtainMessage();
		if (controlRsp != 7) {
			message.obj = errorMsg;
			message.what = controlRsp; // 7;
			mHandle.sendMessage(message);
		}
		else {
			message.obj = null;
			message.what = controlRsp; // 7;
			mHandle.sendMessage(message);
		}
	}

    /**
     * This method sets the control response code and the String values according to the last error.
     * @param e - An int value representing the value for the control response.
     * @param a - A String array containing String values representing a textual description of the last error.
     */
	private void setErrorMsg(int e, String[] a) {
        controlRsp = e;
        errorMsg = a;
        AppyAdService.getInstance().errorOut(TAG,errorMsg[0]+" - "+errorMsg[1]);
	}

    /**
     * This method is responsible for the main functionality of this object.
     * - Runs as a background thread and ensures only one such thread is ever run by checking the
     *   boolean values in the AppyAdService module.
     * - Initiates requests to the AppyAds server to retrieve ad campaign packages.
     * - Notifies the AppyAdService module (and hence the current AppyAdManager module) when
     *   new ad campaigns have been retrieved.
     * - Notifies the AppyAdService & AppyAdManager modules when the next ad should be displayed.
     * - Sleeps in between all the above activities.
     */
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
                                    //AppyAdService.getInstance().initializeCounters();
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
        AppyAdService.getInstance().debugOut(TAG,"Retriever thread exiting.");
    }

}
