package com.appyads.services;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AppyAdService {

    private static final String TAG = "AppyAdService";
    public static final int MAX_ERROR_ALLOWANCE = 10;
	public static final int ERROR_DELAY_LIMIT = 30;
	public static final int NORMAL_SLEEP_DURATION = 5000;
    public static final String TROYOZ_NAME_SPACE = "http://schemas.appyads.com/attributes";
	private static final String AD_SERVER_HOST = "data.troyoz.info";
	private static final int AD_SERVER_PORT = 30000;
	private static String tozAdServerIP;
	
	public static Handler handler;
	private static boolean adThreadLooper = false;
	private static boolean adThreadRunning = false;
	private static boolean debugOn = false;
    private static String uSpec = "initial";
    private static String mAppId = "initial";

    private String adRootDir;
    private Stack<AppyAdManager> mgrStack = new Stack<AppyAdManager>();

    private static final AppyAdService holder = new AppyAdService();

    private AppyAdService() {
        // Hidden... only one instance guaranteed.
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                debugOut(TAG,"Got Ad response back from server.");
                switch (msg.what) {
                    case 1:
                        debugOut(TAG,"Error from Ad server thread.");
                        showAlert((String[]) msg.obj);
                        break;
                    case 7:
                        updateAdViews();
                        break;
                }
            }
        };
    }

    public static AppyAdService getInstance() {
        return (holder);
    }

    public static Handler getHandler() {
        return handler;
    }

    private void initializeAdService(Context context) {
		if (!adThreadRunning && isNetworkAvailable(context)) {
			new Thread(new AppyAdRetriever()).start();
            adRootDir = context.getFilesDir() + "/AppyAds";
        }
	}
	
	private void showAlert(String[] msg) {
        if (msg != null) {
            if (msg.length > 0) debugOut(TAG,""+msg[0]);
            if (msg.length > 1) debugOut(TAG,""+msg[1]);
        }
	}
	
	private void updateAdViews() {
        if (!mgrStack.empty()) {
            debugOut(TAG,"Updating Ad view from thread....");
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) toam.showNextAd();
        }
	}

    public void registerManager(String uspec, String appid, AppyAdManager toam) {
        if (toam != null) {
            if ((uSpec.equals("initial")) && (uspec !=null)) uSpec = uspec;
            if ((mAppId.equals("initial")) && (appid !=null)) mAppId = appid;
            mgrStack.push(toam);
            initializeAdService(toam.getContext());
        }
    }

    public void unRegisterManager() {
        if (!mgrStack.empty()) {
            mgrStack.pop();
            if (mgrStack.empty()) stopService();
        }
        else stopService();
    }

    // ********************  DNS/TCPIP ******************************

    private boolean checkNetworkPermissions(Context context) {
        boolean hasNetworkAccess = ((context.checkCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE") == PackageManager.PERMISSION_GRANTED) &&
                (context.checkCallingOrSelfPermission("android.permission.INTERNET") == PackageManager.PERMISSION_GRANTED));
        if (!hasNetworkAccess) errorOut(TAG,"Application does NOT have INTERNET and/or ACCESS_NETWORK_STATE permissions!!");
        else debugOut(TAG,"Application permissions ok.");
        return (hasNetworkAccess);
    }

    public boolean isNetworkAvailable(Context context) {
        if ((context != null) && checkNetworkPermissions(context)) {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return (false);
    }

    public static void reSetHostIP() {
		try {
			tozAdServerIP = InetAddress.getByName(AD_SERVER_HOST).getHostAddress(); 
		}
		catch (UnknownHostException uhe) {
			tozAdServerIP = "0.0.0.0";
		}
	}
	
	public static void setHostIP() {
		if (tozAdServerIP == null) reSetHostIP();
		else if (tozAdServerIP.equals("0.0.0.0")) reSetHostIP();
	}
	
	public static void requestRefreshHostIP() {
		tozAdServerIP = null;
	}
	
	public static String getHostIP() {
		setHostIP();
		return (tozAdServerIP);
	}
	
	public static int getHostPort() {
		return (AD_SERVER_PORT);
	}

    // *********************** Ad Tracking ***************************

    public void trackAdCampaign(AppyAdManager toam, AppyAd toa) {
        if (toam != null) {
            AppyAdQuickThread tozqt = new AppyAdQuickThread(AppyAdStatic.TRACKAD,
                    getAccountID(),
                    getApplicationID(),
                    toam.getCampaignID(),
                    toam.getCustomSpec(),
                    getUUID(),
                    toam.getScreenDensity(),
                    toa.mAdID,
                    toa.mLink
            );
            new Thread(tozqt).start();
        }
    }

    public String getApplicationID() {
        return (mAppId);
    }

    public String getUUID() {
        return (uSpec);
    }

    // *********************** Ad Processing *************************

    public void setAdData(ByteBuffer bb) {
        debugOut(TAG,"Received campaign data set with length "+bb.position());
        if (bb.position() > 100) {
            if (!mgrStack.empty()) {
                AppyAdManager toam = mgrStack.peek();
                if (toam != null) {
                    ByteArrayInputStream zippedBuffer = new ByteArrayInputStream(bb.array());
                    String adDir = adRootDir + "/" + toam.getCampaignID();
                    if (unpackAds(adDir, zippedBuffer)) {
                        new AppyAdConfig(toam, adDir, "/AP.xml");
                        toam.markRefreshed();
                        debugOut(TAG,"Set Ad campaign successfully.");
                    }
                }
            }
        }
        else if (bb.position() == 1) {
            if (bb.array()[0] == 77) {
                if (!mgrStack.empty()) {
                    AppyAdManager toam = mgrStack.peek();
                    if (toam != null) {
                        toam.declareNoExternalAdSet();
                        debugOut(TAG,"No Ad campaign found on server.");
                    }
                }
            }
        }
    }

    private boolean unpackAds(String adDir, ByteArrayInputStream zippedBuffer) {
        boolean zipStat = false;
        try {
            ZipInputStream zin = new ZipInputStream(zippedBuffer);
            initAdsDirectory(adDir);

            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                debugOut(TAG,"Unzipping campaign package " + ze.getName());

                if(ze.isDirectory()) {
                    checkMakeDirectory(adDir + "/" + ze.getName());
                }
                else {
                    FileOutputStream fout = new FileOutputStream(new File(adDir + "/" + ze.getName()));
                    for (int c = zin.read(); c != -1; c = zin.read()) {
                        fout.write(c);
                    }

                    zin.closeEntry();
                    fout.close();
                }
            }
            zin.close();
            zipStat = true;
        }
        catch (Exception e) {
            errorOut(TAG,"Unable to unpack campaign package.\n - "+e.getMessage());
        }
        return (zipStat);
    }

    private void checkMakeDirectory(String dir) {
        File f = new File(dir);

        if (!f.isDirectory()) {
            if (!f.mkdirs()) errorOut(TAG,"Unable to create directory for Ad campaign.");
        }
    }

    private void initAdsDirectory(String adDir) {
        File f = new File (adDir);
        if (!f.isDirectory()) {
            if (!f.mkdirs()) errorOut(TAG,"Unable to create directory for Ad campaign (init A).");
        }
        else {
            recurseDeleteFiles(f);
            if (!f.mkdirs()) errorOut(TAG,"Unable to create directory for Ad campaign (init B).");
        }
    }

    private void recurseDeleteFiles(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                recurseDeleteFiles(new File(dir,children[i]));
            }
        }
        if (dir.delete()) debugOut(TAG,"Deleted file " + dir.getAbsolutePath());
        else errorOut(TAG,"Unable to delete file "+dir.getAbsolutePath());
    }

    // ********************* Original Service stuff *******************

    public Integer getInterval() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getRefreshInterval());
            else return (0);
        }
        else return (0);
    }

    public String getAccountID() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getAccountID());
            else return (null);
        }
        else return (null);
    }

    public String getCampaignID() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getCampaignID());
            else return (null);
        }
        else return (null);
    }

    public String getScreenDensity() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getScreenDensity());
            else return (null);
        }
        else return (null);
    }
	
	public void turnAdsOn() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) toam.setAdProcessing(true);
        }
	}
	
	public void turnAdsOff() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) toam.setAdProcessing(false);
        }
	}
	
	public boolean AdsAreOn() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            return ((toam != null) && (toam.AdsAreOn()));
        }
        else return (false);
	}

    public boolean adsNeedRefreshing() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            return ((toam != null) && (toam.timeToRefresh()));
        }
        else return (false);
    }

    public String getDefaultInAnimation() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getDefaultInAnimation());
        }
        return (AppyAdStatic.NORMAL_IN_ANIMATION);
    }

    public String getDefaultOutAnimation() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getDefaultOutAnimation());
        }
        return (AppyAdStatic.NORMAL_OUT_ANIMATION);
    }

    public int getDefaultAnimationDuration() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getDefaultAnimationDuration());
        }
        return (AppyAdStatic.NORMAL_ANIMATION_DURATION);
    }

    public int getDefaultDisplayInterval() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getDefaultDisplayInterval());
        }
        return (AppyAdStatic.NORMAL_AD_DURATION);
    }

    public String getCustomSpec() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getCustomSpec());
        }
        return (null);
    }

    public boolean getDefaultTracking() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            return ((toam != null) && (toam.getDefaultTracking()));
        }
        else return (false);
    }
	
	public int getDefaultSleepInterval() {
		return (NORMAL_SLEEP_DURATION);
	}

    public int getErrorWaitInterval() {
        return (NORMAL_SLEEP_DURATION);
    }

    public int getCurrentSleepInterval() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getSleepInterval());
            else return (getDefaultSleepInterval());
        }
        else return (getDefaultSleepInterval());
    }

    public boolean prepareNextAd() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            return ((toam != null) && (toam.prepareNextAd()));
        }
        else return (false);
    }

    public void incrementErrorCounter(int increment) {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) toam.incrementErrorCounter(increment);
        }
    }

    public void resetCounters() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) toam.resetCounters();
        }
    }

    public void initializeCounters() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) toam.initializeCounters();
        }
    }

    public void checkMaxErrors() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) toam.checkErrorLimit();
        }
    }

    public int maxErrors() {
        return (MAX_ERROR_ALLOWANCE);
    }

    public void checkDelayMaxed() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) toam.processDelay();
        }
    }

    public Integer getErrorCount() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getErrorCounter());
            else return (0);
        }
        else return (0);
    }

    public Integer getDelayCount() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getDelayCounter());
            else return (0);
        }
        else return (0);
    }

    public int maxDelay() {
        return (ERROR_DELAY_LIMIT);
    }

	public boolean adThreadIsRunning() {
		return (adThreadRunning);
	}
	
	public void startAdThread() {
		adThreadRunning = true;
	}
	
	public void stopAdThread() {
		adThreadRunning = false;
	}
	
	public void startService() {
		adThreadLooper = true;
	}
	
	public void stopService() {
		adThreadLooper = false;
        recurseDeleteFiles(new File(adRootDir));
	}
	
	public boolean AdServiceIsOn() {
		return (adThreadLooper);
	}
	
	public int setCurErrorCount() {
		return (MAX_ERROR_ALLOWANCE);
	}

	public void setDebug(boolean onoff) {
		debugOn = onoff;
	}

    public void debugOut(String from, String out) {
        if (debugOn) {
            Log.i(from,out);
        }
    }

    public void errorOut(String from, String out) {
        if (debugOn) {
            Log.e(from,out);
        }
    }

	// ********************** Animation Routines ***************************

    public Animation setAnimation(String dir, AppyAd toa) {
        Animation a;
        int animType;
        if (dir.equals("in")) animType = toa.mAnimationIn;
        else animType = toa.mAnimationOut;
        switch (animType) {
            case AppyAdStatic.FADE_IN:
                a = new AlphaAnimation(0, 1);
                break;
            case AppyAdStatic.ZOOM_IN_FROM_LEFT:
                a = new ScaleAnimation (0, 1, 0, 1, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, new Float(0.5));
                break;
            case AppyAdStatic.ZOOM_IN_FROM_RIGHT:
                a = new ScaleAnimation (0, 1, 0, 1, Animation.RELATIVE_TO_PARENT, 1, Animation.RELATIVE_TO_PARENT, new Float(0.5));
                break;
            case AppyAdStatic.ZOOM_IN_FROM_CENTER:
                a = new ScaleAnimation (0, 1, 0, 1, Animation.RELATIVE_TO_PARENT, new Float(0.5), Animation.RELATIVE_TO_PARENT, new Float(0.5));
                break;
            case AppyAdStatic.SLIDE_IN_FROM_LEFT:
                a = new TranslateAnimation (Animation.RELATIVE_TO_PARENT, -1, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0);
                break;
            case AppyAdStatic.SLIDE_IN_FROM_TOP:
                a = new TranslateAnimation (Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, -1, Animation.RELATIVE_TO_PARENT, 0);
                break;
            case AppyAdStatic.SLIDE_IN_FROM_RIGHT:
                a = new TranslateAnimation (Animation.RELATIVE_TO_PARENT, 1, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0);
                break;
            case AppyAdStatic.SLIDE_IN_FROM_BOTTOM:
                a = new TranslateAnimation (Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 1, Animation.RELATIVE_TO_PARENT, 0);
                break;
            case AppyAdStatic.FADE_OUT:
                a = new AlphaAnimation(1, 0);
                break;
            case AppyAdStatic.ZOOM_OUT_TO_LEFT:
                a = new ScaleAnimation (1, 0, 1, 0, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, new Float(0.5));
                break;
            case AppyAdStatic.ZOOM_OUT_TO_RIGHT:
                a = new ScaleAnimation (1, 0, 1, 0, Animation.RELATIVE_TO_PARENT, 1, Animation.RELATIVE_TO_PARENT, new Float(0.5));
                break;
            case AppyAdStatic.ZOOM_OUT_TO_CENTER:
                a = new ScaleAnimation (1, 0, 1, 0, Animation.RELATIVE_TO_PARENT, new Float(0.5), Animation.RELATIVE_TO_PARENT, new Float(0.5));
                break;
            case AppyAdStatic.SLIDE_OUT_TO_RIGHT:
                a = new TranslateAnimation (Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 1, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0);
                break;
            case AppyAdStatic.SLIDE_OUT_TO_BOTTOM:
                a = new TranslateAnimation (Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 1);
                break;
            case AppyAdStatic.SLIDE_OUT_TO_LEFT:
                a = new TranslateAnimation (Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, -1, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0);
                break;
            case AppyAdStatic.SLIDE_OUT_TO_TOP:
                a = new TranslateAnimation (Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, -1);
                break;
            case AppyAdStatic.NO_ANIMATION:
                a = null;
                break;
            default:
                if (dir.equals("in")) a = new AlphaAnimation(0, 1);
                else a = new AlphaAnimation(1, 0);
                break;
        }
        if (a != null) a.setDuration(toa.mAnimationDuration);
        return (a);
    }
}
