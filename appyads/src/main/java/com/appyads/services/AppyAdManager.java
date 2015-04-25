package com.appyads.services;

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
 * AppyAd View Manager
 *
 * This View is used to display advertising campaign components. Note that although this class
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
    private int numInitialChildren = 0;
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

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        numInitialChildren = getChildCount();
        AppyAdService.getInstance().debugOut(TAG, "Number of initial placements is " + numInitialChildren);
        if (numInitialChildren > 0) {
            for (int i=0; i< numInitialChildren; i++) {
                View cv = this.getChildAt(i);
                if (cv != null) {
//                    setAdClickListener(cv);
                    baseAd = i+1;
                    int atype = 0;
                    if (cv instanceof ImageView) atype = AppyAdStatic.TOZIMAGE;
                    tozAdCampaign.add(new AppyAd(atype, toAdDefaultLink, defaultTracking, defaultInAnimation, defaultOutAnimation, defaultAnimationDuration, defaultDisplayInterval));
                }
            }
        }
        else curAd = -1;
        //if (baseViewIndex > baseAd) baseViewIndex = baseAd;
    }

    public void readyNewCampaign() {
        reInitializeCampaign = true;
        saveCampaignSize = tozAdCampaign.size();
    }

    private void clearExternalAds() {
        for (int i=numInitialChildren; i<saveCampaignSize; i++) {
            tozAdCampaign.remove(numInitialChildren);
            removeViewAt(numInitialChildren);
        }
        initializeCounters();
        reInitializeCampaign = false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        AppyAdService.getInstance().unRegisterManager();
        AppyAdService.getInstance().debugOut(TAG,TAG+" is detached, stopping Ad service.");
    }

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

    public void setAdClickListener(View v) {
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAdClicks(view);
            }
        });
    }

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

    public String getCurrentAdID() {
        if (!tozAdCampaign.isEmpty()) {
            AppyAd toa = tozAdCampaign.get(getDisplayedChild());
            if (toa != null) {
                return(toa.mAdID);
            }
        }
        return (null);
    }

    public String getCurrentAdLink() {
        if (!tozAdCampaign.isEmpty()) {
            AppyAd toa = tozAdCampaign.get(getDisplayedChild());
            if (toa != null) {
                return(toa.mLink);
            }
        }
        return (null);
    }

    public void addAdView(AppyAd toa) {
        if (toa != null) {
            if (toa.mAd != null) {
                switch (toa.mType) {
                    case AppyAdStatic.TOZIMAGE:
                        ImageView iv = new ImageView(getContext());
                        iv.setImageBitmap(toa.mAd);
                        addView(iv, getChildCount());
                        toa.mAd = null;  // No sense in wasting space.
                        tozAdCampaign.add(toa);
                        break;
                }
            }
        }
    }

    public void showNextAd() {
        if (reInitializeCampaign) clearExternalAds();
        int viewCount = getChildCount() - 1;
        AppyAdService.getInstance().debugOut(TAG,"Ad Views Count is "+viewCount+". Indexes: current="+curAd+", base="+baseAd);
        if (viewCount < curAd) {
            addAdView(tozAdCampaign.get(curAd));
        }
        setInAnimation(AppyAdService.getInstance().setAnimation("in",tozAdCampaign.get(curAd)));
        setOutAnimation(AppyAdService.getInstance().setAnimation("out",tozAdCampaign.get(curAd)));
        setDisplayedChild(curAd);
        //showNext();
    }

    public void addAd(AppyAd toa) {
        if (toa != null) tozAdCampaign.add(toa);
        AppyAdService.getInstance().debugOut(TAG,"Current number of ads in campaign is "+tozAdCampaign.size());
    }

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
     * @param idx String
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

    public int getRepeatCycle() {
        return (repeatCycle);
    }

    public void declareNoExternalAdSet() {
       // markRefreshed();
    }

    public boolean hasAdCampaign() {
        return (!tozAdCampaign.isEmpty());
    }

    public void initializeCounters() {
        resetCounters();
        lastAd = tozAdCampaign.size() - 1;
        if (baseAd > lastAd) baseAd = lastAd;
        if (baseViewIndex > lastAd) baseViewIndex = lastAd;
        if (baseViewIndex < 0) baseViewIndex = 0;
    }

    public boolean AdsAreOn() {
        return (mAdsOn);
    }

    public void setAdProcessing(boolean sw) {
        mAdsOn = sw;
    }

    public String getAccountID() {
        return (tozAdAccountID);
    }

    public String getCampaignID() {
        return (tozAdCampaignID);
    }

    public String getCustomSpec() {
        return (tozCustomSpec);
    }

    public void setCustomSpec(String s) {
        tozCustomSpec = s;
        AppyAdService.getInstance().debugOut(TAG,"Set custom parameter to "+tozCustomSpec);
    }

    public String getDefaultInAnimation() {
        return (defaultInAnimation);
    }

    public String getDefaultOutAnimation() {
        return (defaultOutAnimation);
    }

    public int getDefaultAnimationDuration() {
        return (defaultAnimationDuration);
    }

    public int getDefaultDisplayInterval() {
        return (defaultDisplayInterval);
    }

    public String getDefaultLink() {
        return (toAdDefaultLink);
    }

    public boolean getDefaultTracking() {
        return (defaultTracking);
    }
    public String getScreenDensity() {
        return (mScreenDensity);
    }

    public int getRefreshInterval() {
        return (tozAdCampaignRetrievalInterval);
    }

    public boolean prepareNextAd() {
        boolean retVal = false;

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

    public int getNextChangePoint() {
        return (nextChangePoint);
    }

    public int getErrorCounter() {
        return errorCounter;
    }

    public void setErrorCounter(int i) {
        errorCounter = i;
    }

    public void incrementErrorCounter(int increment) {
        errorCounter += increment;
    }

    public void checkErrorLimit(int increment) {
        errorCounter += increment;
        if (errorCounter >= AppyAdService.getInstance().maxErrors()) {
            setAdProcessing(false);
            // todo ????
        }
    }

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

    public int getDelayCounter() {
        return delayCounter;
    }

    public void setDelayCounter(int i) {
        delayCounter = i;
    }

    public void incrementDelayCounter(int increment) {
        delayCounter += increment;
    }

    public void resetCounters() {
        setErrorCounter(0);
        setDelayCounter(0);
    }

    public void setSleepInterval(int interval) {
        sleepInterval = interval;
    }

    public int getSleepInterval() {
        if (curAd < 0) return (0);
        else return (sleepInterval);
    }

    public void markRefreshed() {
        tozAdCampaignRetrievalCounter = 0;
    }

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
