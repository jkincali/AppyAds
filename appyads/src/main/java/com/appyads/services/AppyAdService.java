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

/**
 * This class represents the control center of the AppyAds system.
 * "There can be only one" instance of this class.  It is a Singleton class and all external references to the AppyAdService object are via the {@link #getInstance() getInstance()} method.
 * This module handles communication between the {@link AppyAdRetriever} thread and the {@link AppyAdManager} object by the use of
 * a static {@link Handler} object, which processes messages from the non-UI thread, which cannot directly
 * manipulate views.
 *
 */
public class AppyAdService {

    private static final String TAG = "AppyAdService";
    public static final int MAX_ERROR_ALLOWANCE = 10;
	public static final int ERROR_DELAY_LIMIT = 30;
    public static final int NORMAL_SLEEP_DURATION = 5000;
    public static final int MINIMUM_REFRESH_TIME = 30000;  // 30 seconds
    public static final int MAXIMUM_REFRESH_TIME = 86400000;  // 24 hours
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

    /**
     * This constructor is private, ensuring it will not be instanciated by any external object.
     * The {@link Handler} object is defined within the constructor, which is the object that receives
     * messages from the non-UI thread and processes them, either with an error to the log or with
     * an update request to the {@link AppyAdManager} view object to which the message referred.
     */
    private AppyAdService() {
        // Hidden... only one instance guaranteed.
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                debugOut(TAG,"Handler method received message from Retriever thread.");
                switch (msg.what) {
                    case 1:
                        errorOut(TAG,"Error from AdRetriever thread.");
                        showAlert((String[]) msg.obj);
                        break;
                    case 7:
                        updateAdViews();
                        break;
                }
            }
        };
    }

    /**
     * This is the method used to access the one and only AppyAdService object.
     *
     * @return holder - a reference to the one and only AppyAdService object.
     */
    public static AppyAdService getInstance() {
        return (holder);
    }

    /**
     * A static reference to the {@link Handler} object.
     *
     * @return handler - a reference to the {@link Handler} object.
     */
    public static Handler getHandler() {
        return handler;
    }

    /**
     * This method checks to see if the non-UI thread is running and, if it is not running, submits
     * the thread and defines the application's file directory (to which new ad campaigns are saved).
     *
     * @param context - The context (Activity) which govern's the {@link AppyAdManager} view object.
     */
    private void initializeAdService(Context context) {
		if (!adThreadRunning) {
			new Thread(new AppyAdRetriever()).start();
            adRootDir = context.getFilesDir() + "/AppyAds";
        }
	}

    /**
     * This method writes errors to the log window. (Note that debug must be set to true to enable logging.)
     *
     * @param msg - An array of String, which contains a textual representation of the error message.
     */
	private void showAlert(String[] msg) {
        if (msg != null) {
            if (msg.length > 1) errorOut(TAG,msg[0]+" "+msg[1]);
            else if (msg.length > 0) errorOut(TAG,msg[0]);
        }
	}

    /**
     * This method get's the currently visible {@link AppyAdManager} view object and calls its method to update its
     * ad views.
     */
	private void updateAdViews() {
        if (!mgrStack.empty()) {
            debugOut(TAG,"Updating Ad view from thread....");
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) toam.showNextAd();
        }
	}

    /**
     * This method is called from a newly instantiated {@link AppyAdManager} view object, which is requesting to
     * be recognized by the AppyAds service.
     *
     * @param uspec - A String representing a unique identifier for the device
     * @param appid - A String representing a unique identifier for the application
     * @param toam - A reference to the {@link AppyAdManager} object making the registration request
     */
    public void registerManager(String uspec, String appid, AppyAdManager toam) {
        if (toam != null) {
            if ((uSpec.equals("initial")) && (uspec !=null)) uSpec = uspec;
            if ((mAppId.equals("initial")) && (appid !=null)) mAppId = appid;
            mgrStack.push(toam);
            initializeAdService(toam.getContext());
        }
    }

    /**
     * This method is called to take the currently visible {@link AppyAdManager} view object off the stack.  It is
     * called when the {@link AppyAdManager} object is being destroyed.  However, it is possible that the
     * user is simply changing orientation, in which case the non-UI thread should not be stopped, because
     * it will just need to be started again.  Hence the need for the parameter, which indicates whether
     * or not to stop the non-UI thread.
     *
     * @param stopIt - A boolean value indicating whether or not to stop the non-UI {@link AppyAdRetriever} thread.
     */
    public void unRegisterManager(boolean stopIt) {
        if (!mgrStack.empty()) {
            mgrStack.pop();
            if ((mgrStack.empty()) && stopIt) stopService();
        }
        else stopService();
    }

    // ********************  DNS/TCPIP ******************************

    /**
     * This method is used to check whether or not network access permissions has been granted to the application
     *
     * @param context - The Context under which this method is being called.
     * @return - A boolean value indicating whether or not the application has appropriate permissions.
     */
    private boolean checkNetworkPermissions(Context context) {
        boolean hasNetworkAccess = ((context.checkCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE") == PackageManager.PERMISSION_GRANTED) &&
                (context.checkCallingOrSelfPermission("android.permission.INTERNET") == PackageManager.PERMISSION_GRANTED));
        if (!hasNetworkAccess) errorOut(TAG,"Application does NOT have INTERNET and/or ACCESS_NETWORK_STATE permissions!!");
        else debugOut(TAG,"Application permissions ok.");
        return (hasNetworkAccess);
    }

    /**
     * This method checks to see if network access is available.
     *
     * @return - A boolean value indicating whether or not network access is available.
     */
    public boolean isNetworkAvailable() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) {
                Context context = toam.getContext();
                if (context != null) {
                    if (checkNetworkPermissions(context)) {
                        ConnectivityManager connectivityManager
                                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
                    }
                }
            }
        }
        return (false);
    }

    /**
     * Normally, in order to enhance network response time, DNS is used only used for the first connection
     * to the server.  After that, only the IP address is needed.  But occasionally, the IP needs to be
     * refreshed.  This method is called to refresh the IP address.
     */
    public static void reSetHostIP() {
		try {
			tozAdServerIP = InetAddress.getByName(AD_SERVER_HOST).getHostAddress(); 
		}
		catch (UnknownHostException uhe) {
			tozAdServerIP = "0.0.0.0";
		}
	}

    /**
     * This method is used to set the IP address of the AppyAds service.
     */
	public static void setHostIP() {
		if (tozAdServerIP == null) reSetHostIP();
		else if (tozAdServerIP.equals("0.0.0.0")) reSetHostIP();
	}

    /**
     * This method is used to force the IP address to be refreshed the next time the server is needed.
     */
	public static void requestRefreshHostIP() {
		tozAdServerIP = null;
	}

    /**
     * This method returns the current server's host IP address.
     *
     * @return ipAddress - A String value representing the IP address of the server.
     */
	public static String getHostIP() {
		setHostIP();
		return (tozAdServerIP);
	}

    /**
     * This method returns the TCP/IP port that is used by the AppyAds server.
     *
     * @return port - An int value representing the TCP/IP port used by the AppyAds server.
     */
	public static int getHostPort() {
		return (AD_SERVER_PORT);
	}

    // *********************** Ad Tracking ***************************

    /**
     * This method is called to record the click-through in the case the user tapped/clicked on the
     * ad view.  Note that tracking must be enabled for the ad view.  If tracking is not enabled for
     * the ad view, this method will not be called.
     *
     * @param toam - The {@link AppyAdManager} object in play
     * @param toa - The {@link AppyAd} object representing the ad view which was tapped/clicked
     */
    public void trackAdCampaign(AppyAdManager toam, AppyAd toa) {
        if (toam != null) {
            AppyAdQuickThread tozqt = new AppyAdQuickThread(AppyAdStatic.TRACKAD,
                    getAccountID(),
                    getApplicationID(),
                    toam.getCampaignID(),
                    toam.getCustomSpec(),
                    getUUID(),
                    toam.getScreenDensity(),
                    toam.getAdViewWidth(),
                    toam.getAdViewHeight(),
                    toa.mAdID,
                    toa.mLink
            );
            new Thread(tozqt).start();
        }
    }

    /**
     * This method returns the current application id.
     *
     * @return mAppId - A String value representing the application id.
     */
    public String getApplicationID() {
        return (mAppId);
    }

    /**
     * This method returns the current user/device specification - a unique string which should be
     * different for each device.
     *
     * @return uSpec - A String representing the unique user/device specification.
     */
    public String getUUID() {
        return (uSpec);
    }

    // *********************** Ad Processing *************************

    /**
     * This method is called by the non-UI thread when a new ad campaign package has be received.
     * The new ad campaign is loaded into the currently visible {@link AppyAdManager} view object.
     *
     * @param bb - A {@link ByteBuffer} object containing the new ad campaign package.
     */
    public void setAdData(ByteBuffer bb) {
        debugOut(TAG,"Received campaign data set with length "+bb.position());
        if (bb.position() > 100) {
            if (!mgrStack.empty()) {
                AppyAdManager toam = mgrStack.peek();
                if (toam != null) {
                    ByteArrayInputStream zippedBuffer = new ByteArrayInputStream(bb.array());
                    String adDir = adRootDir + "/" + toam.getCampaignID();
                    if (unpackAds(adDir, zippedBuffer)) {
                        toam.readyNewCampaign();
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
                        //toam.declareNoExternalAdSet();
                        debugOut(TAG,"Unable to retrieve any ad campaigns for account "+toam.getAccountID()+", campaign "+toam.getCampaignID()+".");
                    }
                }
            }
        }
    }

    /**
     * This method unpacks a new ad campaign package (zip file) to the application's private file
     * directory.
     *
     * @param adDir - A String representing the directory to which the ad campaign files will be saved
     * @param zippedBuffer - A {@link ByteArrayInputStream} containing the ad campaign (in zipped format)
     * @return - A boolean value indicating whether or not the ad campaign was successfully unpacked to the specified directory.
     */
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

    /**
     * This utilitiy method checks to see if the necessary directory already exists and creates it
     * if it doesn't exist.
     * @param dir - A String representing the full file path to the directory.
     */
    private void checkMakeDirectory(String dir) {
        File f = new File(dir);

        if (!f.isDirectory()) {
            if (!f.mkdirs()) errorOut(TAG,"Unable to create directory for Ad campaign.");
        }
    }

    /**
     * This method ensures a completely fresh directory is created for a new ad campaign by removing
     * a campaign if one already exists prior to creating a new one.
     * @param adDir - A String representing the full directory path to the ad directory
     */
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

    /**
     * This method recursively removes files from a directory.
     *
     * @param dir - A {@link File} object representing the directory to be removed, along with all its contents.
     */
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

    /**
     * This method checks to see if the currently visible {@link AppyAdManager} view object has a valid set of
     * {@link AppyAd} ad objects.
     *
     * @return - A boolean value indicating whether or not the {@link AppyAdManager} view object has a valid set of ads.
     */
    public boolean hasValidAdCampaign() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            return ((toam != null) && toam.hasAdCampaign());
        }
        else return (false);
    }

    /**
     * This method returns the current account ID being used for the currently visible {@link AppyAdManager} view object.
     * @return - A String representing the account ID.
     */
    public String getAccountID() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getAccountID());
            else return (null);
        }
        else return (null);
    }

    /**
     * This method returns the current campaign ID being used by the currently visible {@link AppyAdManager} view object.
     * @return - A String representing the campaign ID.
     */
    public String getCampaignID() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getCampaignID());
            else return (null);
        }
        else return (null);
    }

    /**
     * This method returns the screen density setting.
     * @return - A String representing the screen density for the device. (ldpi,mdpi,hdpi,xhdpi,xxhdpi or xxxhdpi)
     */
    public String getScreenDensity() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getScreenDensity());
            else return (null);
        }
        else return (null);
    }

    /**
     * This method returns the current width of the {@link AppyAdManager} view port.
     * @return - An int value representing the width of the view
     */
    public int getAdViewWidth() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getAdViewWidth());
            else return (0);
        }
        else return (0);
    }

    /**
     * This method returns the current height of the {@link AppyAdManager} view port.
     * @return - An int value representing the height of the view
     */
    public int getAdViewHeight() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getAdViewHeight());
            else return (0);
        }
        else return (0);
    }

    /**
     * This method is called to check whether ad processing is on or off.
     * @return - A boolean value indicating whether or not ad processing is on.
     */
	public boolean adsAreOn() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            return ((toam != null) && (toam.adsAreOn()));
        }
        else return (false);
	}

    /**
     * This method checks to see if it is time to refresh the current ad campaign.
     * @return - A boolean value indicating whether or not it is time to refresh the ad campaign.
     */
    public boolean adsNeedRefreshing() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            return ((toam != null) && (toam.timeToRefresh()));
        }
        else return (false);
    }

    /**
     * This method retrieves the default link set by the {@link AppyAdManager} view object, for taps/clicks on ads which do not set their own link values.
     * @return - A String value representing the default link to use when clicks/taps on ads without link settings occur.
     */
    public String getDefaultLink() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getDefaultLink());
        }
        return (null);
    }

    /**
     * This method returns the default animation for ad views becoming visible.
     * @return - A String value representing the default setting for the animation.
     */
    public String getDefaultInAnimation() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getDefaultInAnimation());
        }
        return (AppyAdStatic.NORMAL_IN_ANIMATION);
    }

    /**
     * This method returns the default animation for ad views becoming invisible.
     * @return - A String value representing the default setting for the animation.
     */
    public String getDefaultOutAnimation() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getDefaultOutAnimation());
        }
        return (AppyAdStatic.NORMAL_OUT_ANIMATION);
    }

    /**
     * This method returns the default duration for animations.
     * @return - An int value in milliseconds for the duration of animations.
     */
    public int getDefaultAnimationDuration() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getDefaultAnimationDuration());
        }
        return (AppyAdStatic.NORMAL_ANIMATION_DURATION);
    }

    /**
     * This method returns the default time for which an ad view is displayed.
     * @return - An int value representing the time in milliseconds for which an ad view is displayed.
     */
    public int getDefaultDisplayInterval() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getDefaultDisplayInterval());
        }
        return (AppyAdStatic.NORMAL_AD_DURATION);
    }

    /**
     * This method returns the custom specification used for a click/tap-through. (Note: tracking must be enabled for click-throughs to be logged)
     * @return - A String value representing the custom setting. (Returns null if not set).
     */
    public String getCustomSpec() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getCustomSpec());
        }
        return (null);
    }

    /**
     * This method returns the default tracking setting.
     * @return - A boolean value representing the default behavior of tracking.
     * If true, ads will be tracked.  If false, ads will not be tracked.
     * Note that this value may be overridden by the setting in an ad package received by the server.
     */
    public boolean getDefaultTracking() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            return ((toam != null) && (toam.getDefaultTracking()));
        }
        else return (false);
    }

    /**
     * This method returns the default sleep interval for the non-UI retriever thread.
     *
     * @return - An int value representing the time in milliseconds for the retriever thread to wait before processing the next loop-cycle.
     */
	public int getDefaultSleepInterval() {
		return (NORMAL_SLEEP_DURATION);
	}

    /**
     * This method returns the interval which delays processing under error conditions.  Retries are
     * governed by this value.
     * @return - An int value defining a time in milliseconds for which to wait before the next retry is attempted.
     */
    public int getErrorWaitInterval() {
        return (NORMAL_SLEEP_DURATION);
    }

    /**
     * This method returns the current sleep interval for the non-UI retriever thread.
     * @return - An int value representing the time in milliseconds for the retriever thread to wait before processing the next loop-cycle.
     */
    public int getCurrentSleepInterval() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getSleepInterval());
            else return (getDefaultSleepInterval());
        }
        else return (getDefaultSleepInterval());
    }

    /**
     * This method calls the currently active {@link AppyAdManager} view object in order to allow it to prepare its
     * next ad.
     * @return - A boolean value indicating whether or not an ad is ready to be displayed.
     */
    public boolean prepareNextAd() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            return ((toam != null) && (toam.prepareNextAd()));
        }
        else return (false);
    }

    /**
     * This method provides a common increment signature for updating the error counter of the
     * currently active {@link AppyAdManager} view object.
     */
    public void checkMaxErrors() {
        checkMaxErrors(1);
    }

    /**
     * This method calls the currently active {@link AppyAdManager} view object and increments its error counter.
     *
     * @param increment - An int value representing the amount by which to increase the error counter.
     */
    public void checkMaxErrors(int increment) {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) toam.checkErrorLimit(increment);
        }
    }

    /**
     * This method returns the maximum errors that is allowed.
     * @return - An int value representing the number of errors that are allowed before a delay kicks in.
     */
    public int maxErrors() {
        return (MAX_ERROR_ALLOWANCE);
    }

    /**
     * This method checks to see if the dalay time has been reached.
     */
    public void checkDelayMaxed() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) toam.processDelay();
        }
    }

    /**
     * This method returns the current error count for the active {@link AppyAdManager} view object.
     * @return - An Integer value representing the number of errors that have occurred for the active {@link AppyAdManager} view object.
     */
    public Integer getErrorCount() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getErrorCounter());
            else return (0);
        }
        else return (0);
    }

    /**
     * This method returns the current delay count for the active {@link AppyAdManager} view object.
     * @return - An Integer value representing the delay counter for the active {@link AppyAdManager} view object.
     */
    public Integer getDelayCount() {
        if (!mgrStack.empty()) {
            AppyAdManager toam = mgrStack.peek();
            if (toam != null) return (toam.getDelayCounter());
            else return (0);
        }
        else return (0);
    }

    /**
     * This method returns the maximum delay setting.
     * @return - An int value representing the number of times to count before attempting a retry after an error condition.
     */
    public int maxDelay() {
        return (ERROR_DELAY_LIMIT);
    }

    /**
     * This method is used to check whether or not the non-UI retriever thread is running.
     * @return - A boolean value indicating whether or not the retriever thread is running.
     */
	public boolean adThreadIsRunning() {
		return (adThreadRunning);
	}

    /**
     * This method is used to set a boolean value indicating the non-UI retriever thread is currently running.
     */
	public void startAdThread() {
		adThreadRunning = true;
	}

    /**
     * This method is used to set a boolean value to indicate that the non-UI retriever thread is either stopping or has stopped.
     */
	public void stopAdThread() {
		adThreadRunning = false;
	}

    /**
     * This method sets a boolean value to allow the non-UI thread to execute normally within its loop.
     */
	public void startService() {
		adThreadLooper = true;
	}

    /**
     * This method sets a boolean value to notify the non-UI thread that it should ignore normal execution within its loop.
     */
	public void stopService() {
		adThreadLooper = false;
        if (adRootDir != null) recurseDeleteFiles(new File(adRootDir));
	}

    /**
     * This method reveals whether the non-UI thread is processing normally or has been temporarily paused.
     * @return - A boolean value indicating whether or not the ad processing is on.
     */
	public boolean adServiceIsOn() {
		return (adThreadLooper);
	}

    /**
     * This method is used to turn debugging on or off.
     * @param onoff - A boolean value. If set to true, logging will be enabled.
     */
	public void setDebug(boolean onoff) {
		debugOn = onoff;
	}

    /**
     * Log a standard informational text message
     * @param from - A String representing the module initiating the log message
     * @param out - A String representing the message
     */
    public void debugOut(String from, String out) {
        if (debugOn) {
            Log.i(from,out);
        }
    }

    /**
     * Log an error text message
     * @param from - A String representing the module initiating the log message
     * @param out - A String representing the message
     */
    public void errorOut(String from, String out) {
        if (debugOn) {
            Log.e(from,out);
        }
    }

	// ********************** Animation Routines ***************************

    /**
     * This method translates a String representation of an animation into an Animation object.
     * @param dir - A String indicating whether the animation is an "in" or "out" animation
     * @param toa - An {@link AppyAd} object containing the specifics of the ad, which includes the animation properties
     * @return - An {@link Animation} object
     */
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
