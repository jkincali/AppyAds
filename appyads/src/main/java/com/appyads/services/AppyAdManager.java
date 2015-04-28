package com.appyads.services;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import java.util.ArrayList;

/**
 * AppyAdManager
 *
 * This class is used to display advertising campaign components. Note that although this class
 * has public methods available, most interactions should be accomplished using the xml layout
 * file, where values are passed to this class's object via attributes.
 *
 * @author Jon DeWeese
 * @version 1.0
 * @since 1.0
 */
public class AppyAdManager extends ViewFlipper {

    private static final String TAG = "AppyAdManager";
    private ArrayList<AppyAd> tozAdCampaign = new ArrayList<AppyAd>();
    private String tozAdAccountID;
    private String tozAdCampaignID;
    private Integer tozAdCampaignRetrievalInterval;
    private Integer tozAdCampaignRetrievalCounter;
    private Integer nextChangePoint;
    private Integer defaultDisplayInterval;
    private Integer baseViewIndex;
    private Integer finalViewIndex;
    private Integer repeatCycle;
    private boolean defaultTracking;
    private boolean mAdsOn;
    private String toAdDefaultLink;
    private String defaultInAnimation;
    private String defaultOutAnimation;
    private Integer defaultAnimationDuration;
    private String mScreenDensity;
    private String tozCustomSpec;

    private int errorCounter,delayCounter;
    private int sleepInterval;

    private int curAd = 0;
    private int baseAd = 0;
    private int lastAd = 0;
    private int numInternalChildren = 0;
    private int numExternalChildren = 0;
    private int saveCampaignSize = 0;
    private boolean reInitializeCampaign = true;

    public AppyAdManager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initManager(context,attrs);
    }

    public AppyAdManager(Context context) {
        super(context);
        initManager(context,null);
    }

    private void initManager(Context context, AttributeSet attrs) {

        errorCounter = 0;
        delayCounter = 0;
        sleepInterval = AppyAdService.getInstance().getDefaultSleepInterval();

        if (attrs != null) {
            tozAdAccountID = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "accountID");

            tozAdCampaignID = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "campaignID");

            tozCustomSpec = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "customSpec");

            toAdDefaultLink = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "defaultLink");

            defaultInAnimation = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "defaultInAnimation");
            if (defaultInAnimation == null) defaultInAnimation = AppyAdStatic.NORMAL_IN_ANIMATION;
            defaultOutAnimation = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "defaultOutAnimation");
            if (defaultOutAnimation == null) defaultOutAnimation = AppyAdStatic.NORMAL_OUT_ANIMATION;

            String temp = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "refreshInterval");
            if (temp != null) {
                try {
                    tozAdCampaignRetrievalInterval = Integer.parseInt(temp);
                }
                catch (NumberFormatException nfe) {
                    tozAdCampaignRetrievalInterval = 0;
                }
            }

            temp = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "defaultDisplayInterval");
            if (temp != null) {
                try {
                    defaultDisplayInterval = Integer.parseInt(temp);
                }
                catch (NumberFormatException nfe) {
                    defaultDisplayInterval = sleepInterval;
                }
            }

            temp = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "defaultAnimationDuration");
            if (temp != null) {
                try {
                    defaultAnimationDuration = Integer.parseInt(temp);
                }
                catch (NumberFormatException nfe) {
                    defaultAnimationDuration = AppyAdStatic.NORMAL_ANIMATION_DURATION;
                }
            }

            temp = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "baseViewIndex");
            if (temp != null) {
                try {
                    baseViewIndex = Integer.parseInt(temp);
                }
                catch (NumberFormatException nfe) {
                    baseViewIndex = 0;
                }
            }

            temp = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "finalViewIndex");
            if (temp != null) {
                try {
                    finalViewIndex = Integer.parseInt(temp);
                }
                catch (NumberFormatException nfe) {
                    // leave null
                }
            }

            temp = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "repeatCycle");
            if (temp != null) {
                try {
                    repeatCycle = Integer.parseInt(temp);
                }
                catch (NumberFormatException nfe) {
                    // leave null
                }
            }

            temp = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "defaultTracking");
            defaultTracking = false;
            if (temp != null) if (temp.toLowerCase().equals("true")) defaultTracking = true;

            temp = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "adProcessing");
            mAdsOn = true;
            if (temp != null) if (temp.toLowerCase().equals("false")) mAdsOn = false;

            temp = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "debug");
            if (temp != null) if (temp.toLowerCase().equals("true")) AppyAdService.getInstance().setDebug(true);

        }

        if (tozAdAccountID == null) tozAdAccountID = "undefined";
        if (tozAdCampaignID == null) tozAdCampaignID = "default";
        if (baseViewIndex == null) baseViewIndex = 0;
        if (tozAdCampaignRetrievalInterval == null) tozAdCampaignRetrievalInterval = 0;
        if (defaultDisplayInterval == null) defaultDisplayInterval = sleepInterval;
        if (defaultAnimationDuration == null) defaultAnimationDuration = AppyAdStatic.NORMAL_ANIMATION_DURATION;
        nextChangePoint = defaultDisplayInterval;

        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        switch (metrics.densityDpi) {
            case 120: mScreenDensity = "ldpi"; break;
            case 160: mScreenDensity = "mdpi"; break;
            case 240: mScreenDensity = "hdpi"; break;
            case 320: mScreenDensity = "xhdpi"; break;
            case 480: mScreenDensity = "xxhdpi"; break;
            case 640: mScreenDensity = "xxxhdpi"; break;
            default:  mScreenDensity = "xhdpi"; break;
        }

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAdClicks(view);
            }
         });

        PackageManager pm = getContext().getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo( getContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            ai = null;
        }
        String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
        String android_id = Settings.Secure.getString(getContext().getContentResolver(),Settings.Secure.ANDROID_ID);
        AppyAdService.getInstance().registerManager(android_id,applicationName,this);

        AppyAdService.getInstance().debugOut(TAG, "Initialized state ....."+
            "\n - Account: " + tozAdAccountID+
            "\n - Ad Campaign: " + tozAdCampaignID+
            "\n - Custom Parameter: " + tozCustomSpec+
            "\n - Campaign retrieval interval is " + tozAdCampaignRetrievalInterval+
            "\n - Default link " + toAdDefaultLink+
            "\n - Default In Animation is " + defaultInAnimation+
            "\n - Default Out Animation is " + defaultOutAnimation+
            "\n - Default Animation duration " + defaultAnimationDuration+
            "\n - Default Tracking is " + defaultTracking+
            "\n - Ad processing is " + mAdsOn+
            "\n - Default Display Time is "+defaultDisplayInterval+
            "\n - Campaign Repeat Cycle "+repeatCycle+
            "\n - Base View Index is "+baseViewIndex+
            "\n - Final View Index is "+finalViewIndex+
            "\n - Client Screen Density is "+metrics.densityDpi+" ("+mScreenDensity+")");
    }

    /**
     * SDK's method is overridden to register this AppyAdManager module with the AppyAdService module
     * and ensure a background AppyAdRetriever thread is launched to handle ad campaign retrievals
     * and transition handling.
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        numInternalChildren = getChildCount();
        AppyAdService.getInstance().debugOut(TAG, "Number of initial placements is " + numInternalChildren);
        if (numInternalChildren > 0) {
            for (int i=0; i< numInternalChildren; i++) {
                View cv = this.getChildAt(i);
                if (cv != null) {
//                    setAdClickListener(cv);
                    baseAd = i+1;
                    int atype = 0;
                    if (cv instanceof ImageView) atype = AppyAdStatic.TOZIMAGE;
                    tozAdCampaign.add(new AppyAd(atype, toAdDefaultLink, defaultTracking, defaultInAnimation, defaultOutAnimation, defaultAnimationDuration, defaultDisplayInterval));
                    AppyAdService.getInstance().debugOut(TAG, "Added internal ad slot at index " + i);
                }
            }
        }
        else curAd = -1;
        //if (baseViewIndex > baseAd) baseViewIndex = baseAd;
    }

    /**
     * SDK's method overridden to ensure that the retriever thread is properly shut down when
     * this view is destroyed.
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Object host = (Object) getContext();
        if (host instanceof Activity) {
            if (((Activity)host).isFinishing()) {
                AppyAdService.getInstance().unRegisterManager(true);
                AppyAdService.getInstance().debugOut(TAG, TAG + " is detached, stopping Ad service thread.");
            }
            else {
                AppyAdService.getInstance().unRegisterManager(false);
                AppyAdService.getInstance().debugOut(TAG, TAG + " is detached, removing from stack, but not stopping Ad service thread.");

            }
        }
    }

    /**
     * SDK's method overridden, but currently not altered. (For future use.)
     *
     * @param visibility - An integer representing the View's current visibility setting
     */
    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        switch (visibility) {
            case View.GONE:
                break;
            case View.INVISIBLE:
                break;
            case View.VISIBLE:
                break;
        }
    }

    /**
     * This method is called just before the ad campaign is initialized.  It tells the system
     * that a new ad campaign is coming.  The existing ad campaign parameters must be saved for the
     * UI thread to tear the old campaign down, while the background thread builds the new campaign.
     */
    public void readyNewCampaign() {
        reInitializeCampaign = true;
        numExternalChildren = tozAdCampaign.size() - numInternalChildren;
        saveCampaignSize = tozAdCampaign.size();
    }

    /**
     * This method is used to remove entries in the ad campaign's ArrayList and the corresponding
     * views.  Called every time there has been a new ad campaign retrieved from the server.
     */
    private void clearExternalAds() {
        AppyAdService.getInstance().debugOut(TAG,"Removing previous campaign: "+numExternalChildren+" slots starting at index "+numInternalChildren);
        removeViews(numInternalChildren, numExternalChildren);
        for (int i= numInternalChildren; i< saveCampaignSize; i++) {
            AppyAdService.getInstance().debugOut(TAG,"Removing previous ad at index "+i);
            tozAdCampaign.remove(numInternalChildren);
        }
        curAd = 0;
    }

    /**
     * This method is called when the user taps/clicks the ad view.
     *
     * @param v - The View which was clicked/tapped
     */
    public void handleAdClicks(View v) {
        if (!tozAdCampaign.isEmpty()) {
            AppyAd toa = tozAdCampaign.get(getDisplayedChild());
            if (toa != null) {
                String link = toa.mLink;
                if (link == null) link = getDefaultLink();
                if (link != null) {
                    if (!link.toLowerCase().equals("none")) {
                        if (toa.mTracking) AppyAdService.getInstance().trackAdCampaign(this, toa);
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                        getContext().startActivity(browserIntent);
                    }
                }
            }
        }
    }

    /**
     * This method adds a new ad view, which correlates to a specific entry in the ad campaign's ArrayList.
     *
     * @param toa - The AppyAd object to use to get the information from in order to ad the new ad View.
     */
    public void addAdView(AppyAd toa) {
        if (toa != null) {
            if (toa.mAd != null) {
                switch (toa.mType) {
                    case AppyAdStatic.TOZIMAGE:
                        ImageView iv = new ImageView(getContext());
                        iv.setImageBitmap(toa.mAd);
                        addView(iv, getChildCount());
                        toa.mAd = null;  // No sense in wasting space.
                        //tozAdCampaign.add(toa);
                        AppyAdService.getInstance().debugOut(TAG, "Added a view for external ad. Current view count is " + getChildCount());
                        break;
                }
            }
        }
    }

    /**
     * This method is called when the handler receives a message from the retriever thread indicating
     * it is time to switch to the next ad view.  Note that when an ad campaign has just been recieved,
     * extra measures are taken to re-initialize the existing views, counters, pointers appropriately.
     */
    public void showNextAd() {
        if (reInitializeCampaign) clearExternalAds();
        int viewCount = getChildCount() - (1 + numInternalChildren);
        AppyAdService.getInstance().debugOut(TAG,"Ad Views Count is "+viewCount+". Indexes: current="+curAd+", base="+baseAd);
        if ((viewCount < curAd) && (curAd > (numInternalChildren-1))) {
            addAdView(tozAdCampaign.get(curAd));
        }
        if (reInitializeCampaign && (curAd == 0) && (numInternalChildren > 0) && (getDisplayedChild() == 0)) {
            AppyAdService.getInstance().debugOut(TAG,"Leaving root internal ad (index=0) displayed (first pass with new ad campaign)");
        }
        else {
            setInAnimation(AppyAdService.getInstance().setAnimation("in", tozAdCampaign.get(curAd)));
            setOutAnimation(AppyAdService.getInstance().setAnimation("out", tozAdCampaign.get(curAd)));
            setDisplayedChild(curAd);
        }
        if (reInitializeCampaign) initializeCounters();
    }

    /**
     * This method is called by the AppyAdConfig module when a new ad campaign is being retrieved.
     * New ads are implemented into the ad campaign's ArrayList.
     *
     * @param toa - The new AppyAd object to add to the current campaign ArrayList
     */
    public void addAd(AppyAd toa) {
        if (toa != null) tozAdCampaign.add(toa);
        AppyAdService.getInstance().debugOut(TAG,"Current number of ads in campaign is "+tozAdCampaign.size());
    }

    /**
     * This method is called to set the base view index.  The base view index is the index of the
     * ad view that will be used to begin the next cycle within the ad campaign.  The input value
     * is of type String, which represents an integer because it is either set by this module at
     * initialization via a parameter within the xml layout, or it is dyanmically set via the AppyAdConfig
     * module when a new ad campaign has been retrieved.
     *
     * @param idx - A String value representing the integer to be used as the index. Example "2" will be converted to 2.
     */
    public void setBaseViewIndex(String idx) {
        if (idx != null) {
            try {
                int cidx = Integer.parseInt(idx);
                if (cidx >= 0) {
                    baseViewIndex = cidx;
                   // if (baseViewIndex > lastAd) baseViewIndex = lastAd;
                    AppyAdService.getInstance().debugOut(TAG,"Set Base View Index to "+baseViewIndex);
                }
            } catch (NumberFormatException nfe) {
                // Ignore
            }
        }
    }

    /**
     * This method defines the index of the advertising element to land on when the campaign
     * completes its cycling. The view will stay on this index for the remainder of the
     * activity's lifecycle.
     *
     * @param idx - A String value representing the integer to be used as the index. Example "2" will be converted to 2.
     */
    public void setFinalViewIndex(String idx) {
        if (idx != null) {
            try {
                int cidx = Integer.parseInt(idx);
                if (cidx >= 0) {
                    finalViewIndex = cidx;
                    AppyAdService.getInstance().debugOut(TAG,"Set Final View Index to "+finalViewIndex);
                }
            } catch (NumberFormatException nfe) {
                // Ignore
            }
        }
    }

    /**
     * This method sets the repeat cycle for the existing ad campaign.  This input is a string
     * representing the desired integer value.  This method is either called by this object upon initialization
     * when the value was specified in the layout xml file, or called by the AppyAdConfig module
     * when set or overridden by a new ad campaign retrieved from the server.
     *
     * @param rc - A String value representing the integer to be used as the number of times the campaign will cycle through its ad elements. Example "2" will be converted to 2.
     */
    public void setRepeatCycle(String rc) {
        if (rc != null) {
            try {
                int rci = Integer.parseInt(rc);
                if (rci >= 0) {
                    repeatCycle = rci;
                    AppyAdService.getInstance().debugOut(TAG,"Set Repeat Cycle to "+repeatCycle);
                }
            } catch (NumberFormatException nfe) {
                // Ignore
            }
        }
    }

    /**
     * This method is used to check to see if an ad campaign exists. (Either by including static
     * views within the AppyAdManager component in the layout xml file, or with the retrieval of
     * dynamic ads within an AppyAd campaign from the server.
     *
     * @return exists - A boolean value indicating whether or not a campaign has been set.
     */
    public boolean hasAdCampaign() {
        return (!tozAdCampaign.isEmpty());
    }

    /**
     * This method initializes all counters and sets the initial pointer values to their respective
     * values.  Called each time a new ad campaign has been retrieved from the server.
     */
    public void initializeCounters() {
        resetCounters();
        lastAd = tozAdCampaign.size() - 1;
        if (baseAd > lastAd) baseAd = lastAd;
        if (baseViewIndex > lastAd) baseViewIndex = lastAd;
        if (baseViewIndex < 0) baseViewIndex = 0;
        reInitializeCampaign = false;
    }

    /**
     * This method returns a boolean value indicating whether or not ads are currently being processed.
     *
     * @return on - A boolean value indicating whether or not the ad campaign system is on or off.
     */
    public boolean AdsAreOn() {
        return (mAdsOn);
    }

    /**
     * This method allows ad processing to be switched on or off, depending on the boolean value
     * of sw, which is passed into the method.  A value of true turns ad processing on, while false
     * turns it off.
     *
     * @param sw - A boolean value to turn ad processing on or off.
     */
    public void setAdProcessing(boolean sw) {
        mAdsOn = sw;
    }

    /**
     * This method returns the current AppyAds account id, which was originally set in the layout xml file.
     *
     * @return value - A String representing the account id.
     */
    public String getAccountID() {
        return (tozAdAccountID);
    }

    /**
     * This method returns the current campaign id, which was originally set in the layout xml file.
     *
     * @return value - A String representing the campaign id.
     */
    public String getCampaignID() {
        return (tozAdCampaignID);
    }

    /**
     * When tracking is enabled, analytics reporting will show historical trends on user
     * interactions with ad campaign click-throughs.  It is possible for an extra String
     * identifier to be included in the tracking information.  Use this method to retrieve
     * the current identifier in String format (will return null if not set).
     *
     * @return value - A String representing the custom value to use for tracking. (Returns null if not set.)
     */
    public String getCustomSpec() {
        return (tozCustomSpec);
    }

    /**
     * When tracking is enabled, analytics reporting will show historical trends on user
     * interactions with ad campaign click-throughs.  It is possible for an extra String
     * identifier to be included in the tracking information.  Use this method to specify
     * that extra String identifier (s).
     *
     * @param s - A String representing the custom specification to set for tracking/recording click-throughs.
     */
    public void setCustomSpec(String s) {
        tozCustomSpec = s;
        AppyAdService.getInstance().debugOut(TAG,"Set custom parameter to "+tozCustomSpec);
    }

    /**
     * This method returns the default "in" animation setting, which was originally set via the layout
     * xml file.  This value will be overridden if the campaign package from AppyAds specifies a new
     * value.
     *
     * @return inAnimation - A String representing the animation used to introduce new views.
     */
    public String getDefaultInAnimation() {
        return (defaultInAnimation);
    }

    /**
     * This method returns the default "out" animation setting, which was originally set via the layout
     * xml file.  This value will be overridden if the campaign package from AppyAds specifies a new
     * value.
     *
     * @return outAnimation - A String representing the animation used to remove a view.
     */
    public String getDefaultOutAnimation() {
        return (defaultOutAnimation);
    }

    /**
     * This method returns the default time in milliseconds for the duration of animations used to segue
     * between ad views.  This value will be overridden if the campaign package from AppyAds specifies a new
     * value.
     *
     * @return duration - An int value specifying the number of milliseconds animations will take to process.
     */
    public int getDefaultAnimationDuration() {
        return (defaultAnimationDuration);
    }

    /**
     * This method returns the default time in milliseconds that ad views within a campaign
     * will be displayed.  This value will be overridden if the campaign package from AppyAds specifies a new
     * value.
     *
     * @return interval - An int value specifying the number of milliseconds a view will be displayed.
     */
    public int getDefaultDisplayInterval() {
        return (defaultDisplayInterval);
    }

    /**
     * This method returns the default link used to direct taps/clicks for the ads.  This value
     * will be overridden if the campaign package from AppyAds specifies a new value.
     *
     * @return link - A String value specifying the link pointing to the URL the user will be taken to when the view is tapped/clicked on.
     */
    public String getDefaultLink() {
        return (toAdDefaultLink);
    }

    /**
     * This value returns the default tracking setting (whether or not ad taps/clicks are tracked
     * and recorded).  This value will be overridden if the campaign package from AppyAds specifies a new
     * value.
     *
     * @return tracking - A boolean value revealing whether or not tracking is on or off.
     */
    public boolean getDefaultTracking() {
        return (defaultTracking);
    }

    /**
     * This method returns the screen density of the current device.
     *
     * @return screenDensity - A String value representing the screen density. (ldpi,mdpi,hdpi,xdpi,xxdpi or xxxdpi)
     */
    public String getScreenDensity() {
        return (mScreenDensity);
    }

    /**
     * This method returns the time in milliseconds that the AppyAds service waits before trying
     * to download a new ad campaign.  This value is initially set via the layout xml file.
     *
     * @return refreshInterval - An int value representing the number of milliseconds the system will wait before going to the server for a new ad campaign.
     */
    public int getRefreshInterval() {
        return (tozAdCampaignRetrievalInterval);
    }

    /**
     * This value is called by the retriever thread to prepare the next ad view to be shown.
     *
     * @return nextAdPreparedOK - A boolean value indicating whether or not a new ad is ready to be shown.
     */
    public boolean prepareNextAd() {
        boolean retVal = false;

        if ((tozAdCampaign.size() - numInternalChildren) <= 0) return (retVal);

        AppyAdService.getInstance().debugOut(TAG,"Preparing next Ad index: current="+curAd+", base="+baseAd+", last="+lastAd);

        if ((repeatCycle != null) && (repeatCycle < 0)) return (false);

        if (curAd < lastAd) {
            curAd++;
            retVal = true;
        }
        else if ((curAd == lastAd) && (curAd != baseViewIndex)) {
            curAd = baseViewIndex;
            retVal = true;
            if (repeatCycle != null) {
                repeatCycle--;
                if (repeatCycle < 0) {
                    if (finalViewIndex != null) {
                        if (finalViewIndex > lastAd) finalViewIndex = lastAd;
                        if (finalViewIndex < 0) finalViewIndex = 0;
                        curAd = finalViewIndex;
                    }
                }
            }
        }

        if (retVal) {
            setSleepInterval(tozAdCampaign.get(curAd).mAdDuration);
            AppyAdService.getInstance().debugOut(TAG,"Next Ad index is "+curAd+", which will display for "+tozAdCampaign.get(curAd).mAdDuration+" milliseconds.");
        }
        return (retVal);
    }

    /**
     * This method returns the current error counter.
     *
     * @return errors - An int value representing the current error count.
     */
    public int getErrorCounter() {
        return errorCounter;
    }

    /**
     * This method sets the error counter to the specified value.
     *
     * @param value - An int value to be used to set the new value of the error counter.
     */
    public void setErrorCounter(int value) {
        errorCounter = value;
    }

    /**
     * This method increments the error counter by the specified value.
     *
     * @param increment - An int value to be used to increment the error counter.
     */
    public void incrementErrorCounter(int increment) {
        errorCounter += increment;
    }

    /**
     * This method increments the error counter and checks to see if the maximum number of errors
     * has been reached.  If so, it will turn off ad processing.
     *
     * @param increment - An int value specifying the amount to increment the error counter by prior to checking whether or not it has surpassed the limit.
     */
    public void checkErrorLimit(int increment) {
        errorCounter += increment;
        if (errorCounter >= AppyAdService.getInstance().maxErrors()) {
            setAdProcessing(false);
            // todo ????
        }
    }

    /**
     * This method increments the delay counter until the maximum delay time has been reached.
     * Once it has been reached, it will reset the error counter to just under the error limit
     * and turn ad processing back on to try one more time.
     */
    public void processDelay() {
        if (errorCounter > 0) {
            if (delayCounter >= AppyAdService.getInstance().maxDelay()) {
                setAdProcessing(true);
                errorCounter = AppyAdService.getInstance().maxErrors() - 1;
                delayCounter = 0;
            }
            else delayCounter++;
        }
    }

    /**
     * This method returns the delay counter.
     *
     * @return delayCounter - An int value representing the current value of the delay counter.
     */
    public int getDelayCounter() {
        return delayCounter;
    }

    /**
     * This method sets the delay counter to the specified value.
     *
     * @param value - An int value representing the number to set the delay counter.
     */
    public void setDelayCounter(int value) {
        delayCounter = value;
    }

    /**
     * This method increments the delay counter by the specified value.
     *
     * @param increment - An int value used to increment the current delay counter.
     */
    public void incrementDelayCounter(int increment) {
        delayCounter += increment;
    }

    /**
     * This method resets the error and delay counters back to zero.
     */
    public void resetCounters() {
        setErrorCounter(0);
        setDelayCounter(0);
    }

    /**
     * This method sets the sleep interval time for the retriever thread to wait before flagging
     * the next ad to be shown.
     *
     * @param interval - An int value representing the number of milliseconds to use for the sleep interval.
     */
    public void setSleepInterval(int interval) {
        sleepInterval = interval;
    }

    /**
     * This method is called by the retriever thread to determine the time to wait before flagging
     * the next ad to be shown.
     *
     * @return interval - An int value representing the number of milliseconds to wait for the next ad to be shown.
     */
    public int getSleepInterval() {
        if (curAd < 0) return (0);
        else return (sleepInterval);
    }

    /**
     * This method is called after a new campaign has been retrieved and prepared.
     */
    public void markRefreshed() {
        tozAdCampaignRetrievalCounter = 0;
    }

    /**
     * This method is called by the retriever thread to determine if a new ad campaign package
     * should be retrieved from the server.  It increments a retriever counter to keep track of
     * the time that has passed since the last ad campaign refresh.
     *
     * @return refresh - A boolean value indicating whether or not the time has been reached for a new campaign to be fetched.
     */
    public boolean timeToRefresh() {
        if (tozAdCampaignRetrievalCounter == null) {
            return (true);
        }
        else if (tozAdCampaignRetrievalInterval != 0) {
            tozAdCampaignRetrievalCounter += sleepInterval;

            AppyAdService.getInstance().debugOut(TAG,"Ad Campaign refresh counter="+tozAdCampaignRetrievalCounter);
            if (tozAdCampaignRetrievalCounter >= tozAdCampaignRetrievalInterval) {
                tozAdCampaignRetrievalCounter = 0;
                return (true);
            }
        }
        return (false);
    }
}
