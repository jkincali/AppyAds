package com.appyads.services;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import java.util.ArrayList;

/**
 * This class is used to display advertising campaign components. Note that although this class
 * has public methods available, most interactions should be accomplished using the xml layout
 * file, where values are passed to this class's object via attributes.
 *
 * <p><strong>Attributes</strong> (for use in layout xml file):</p>
 *  <ul><li>appyadmgr:accountID="myaccount"
 *      <ul><li>Mandatory. Specifies the AppyAds account id.</li></ul></li></ul>
 *
 *  <ul><li>appyadmgr:campaignID="mycampaign"
 *      <ul><li>Mandatory. Specifies the AppyAds ad campaign id.</li></ul></li></ul>
 *
 *  <ul><li>appyadmgr:adProcessing="true"
 *      <ul><li>Optional. Specifies whether or not ads should be processed.  Default is true.  Setting adProcessing to "false" will turn off ad processing. To start ad processing the application will need to programmatically call the method {@link #setAdProcessing(boolean) setAdProcessing(true)}</li></ul></li></ul>
 *
 *  <ul><li>appyadmgr:baseViewIndex="1"
 *      <ul><li>Optional. Specifies the index of the view to use to start each cycle (after the first cycle). If not specified, the default is index 0. Note that this value will most likely be overridden by ad campaigns retrieved by the server.</li></ul></li></ul>
 *
 *  <ul><li>appyadmgr:customSpec="myspecialpurpose:-)"
 *      <ul><li>Optional. Specifies a user-defined string value to be recorded with the tracking information.</li></ul></li></ul>
 *
 *  <ul><li>appyadmgr:debug="true"
 *      <ul><li>Optional. Specifies whether or not logging informational/error messages should be on. Default is off.</li></ul></li></ul>
 *
 *  <ul><li>appyadmgr:defaultAnimationDuration="500"
 *      <ul><li>Optional. Specifies the default animation duration in milliseconds (when ads transition to the next one).  Default is 500 (half a second). Note that this value will likely be overridden by ad campaigns retrieved from the server.</li></ul></li></ul>
 *
 *  <ul><li>appyadmgr:defaultDisplayInterval="5000"
 *      <ul><li>Optional. Specifies the default time in milliseconds to keep an ad view visible before switching to the next ad. The default is 5000 (5 seconds).  Note that this value will most likely be overriden by ad campaigns retrieved from the server.</li></ul></li></ul>
 *
 *  <ul><li>appyadmgr:defaultInAnimation="zoom_in_from_center"
 *      <ul><li>Optional. Specifies the default animation to be used for ad views becoming visible. Default is "fade_in".  Note that this value will most likely be overridden by ad campaigns retrieved from the server.
 *          <ul><li><strong>In animation values:</strong>
 *              <ul><li>fade_in</li></ul>
 *              <ul><li>slide_in_from_left</li></ul>
 *              <ul><li>slide_in_from_right</li></ul>
 *              <ul><li>slide_in_from_top</li></ul>
 *              <ul><li>slide_in_from_bottom</li></ul>
 *              <ul><li>zoom_in_from_left</li></ul>
 *              <ul><li>zoom_in_from_right</li></ul>
 *              <ul><li>zoom_in_from_center</li></ul>
 *              <ul><li>none</li></ul>
 *          </li></ul>
 *      </li></ul>
 *  </li></ul>
 *
 *  <ul><li>appyadmgr:defaultLink="http://www.appyads.com"
 *      <ul><li>Optional. Specifies the default link to use for directing the user to when they tap/click on the ad.  Note that this value will most likely be overridden by ad campaigns retrieved from the server.</li></ul></li></ul>
 *
 *  <ul><li>appyadmgr:defaultOutAnimation="zoom_out_to_right"
 *      <ul><li>Optional. Specifies the default animation to be used for ad views that are becoming invisible.  Default is "fade_out".  Note that this value will most likely be overridden by ad campaigns retrieved from the server.
 *          <ul><li><strong>Out animation values:</strong>
 *              <ul><li>fade_out</li></ul>
 *              <ul><li>slide_out_to_left</li></ul>
 *              <ul><li>slide_out_to_right</li></ul>
 *              <ul><li>slide_out_to_top</li></ul>
 *              <ul><li>slide_out_to_bottom</li></ul>
 *              <ul><li>zoom_out_to_left</li></ul>
 *              <ul><li>zoom_out_to_right</li></ul>
 *              <ul><li>zoom_out_to_center</li></ul>
 *              <ul><li>none</li></ul>
 *          </li></ul>
 *      </li></ul>
 *  </li></ul>
 *
 *  <ul><li>appyadmgr:defaultTracking="true"
 *      <ul><li>Optional. Specifies the default tracking setting.  Default is false.  Note, however that ad campaign packages retrieved may override this setting.</li></ul></li></ul>
 *
 *  <ul><li>appyadmgr:finalViewIndex="0"
 *      <ul><li>Optional. Specifies the index of the view on which to stay when the ad campaign cycles have completed. If not specified, the default is the index specified by <strong>baseViewIndex</strong> Note that this value may be overridden by ad campaigns retrieved from the server.</li></ul></li></ul>
 *
 *  <ul><li>appyadmgr:refreshInterval="60000"
 *      <ul><li>Optional. Specifies the interval in milliseconds to wait before going back to the AppyAds server to retrieve another ad campaign.  The default is only retrieve once, when the view is created, but never again.  Note that this value may be overridden by an ad campaign package received from the server.</li></ul></li></ul>
 *
 *  <ul><li>appyadmgr:repeatCycle="3"
 *      <ul><li>Optional. Specifies the number of cycles the ad campaign should be repeated after the initial round. Default value is to continuously cycle.  A value of "0" will force the cycle to stop after the first round.  Note that this value will most likely be overridden by ad campaigns retrieved from the server.</li></ul></li></ul>
 *
 *  <br /><br />
 *  At the top of the layout xml file, be sure and include a definition for the appyads schema name space:<br />
 *  <strong>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;xmlns:appyadmgr="http://schemas.appyads.com/attributes"</strong><br /><br />
 *
 *  An example inclusion of the AppyAdManager view object in the xml layout:<br /><br />
 *  <pre>
 *      {@code
 *      ...
 *          <com.appyads.services.AppyAdManager
 *              android:layout_width="200dp"
 *              android:layout_height="200dp"
 *              android:layout_gravity="center"
 *              android:background="#000000"
 *              android:orientation="horizontal"
 *              appyadmgr:refreshInterval="600000"
 *              appyadmgr:accountID="myaccount"
 *              appyadmgr:campaignID="mycampaign"
 *              appyadmgr:defaultTracking="true"
 *              appyadmgr:defaultInAnimation="zoom_in_from_center"
 *              appyadmgr:defaultOutAnimation="zoom_out_to_right"
 *              appyadmgr:defaultLink="http://www.mysponsor.com"
 *              appyadmgr:baseViewIndex="0" >
 *
 *              <ImageView
 *                  android:id="@+id/my_default_image_view"
 *                  android:layout_width="match_parent"
 *                  android:layout_height="wrap_content"
 *                  android:layout_gravity="center"
 *                  android:adjustViewBounds="true"
 *                  android:contentDescription="someinformation"
 *                  android:scaleType="centerCrop"
 *                  android:src="@drawable/my_image_file" />
 *
 *          </com.appyads.services.AppyAdManager>
 *      ...
 *      }
 *  </pre>
 *  <p>From the xml layout snippet given above, notice that an {@link ImageView} object is embedded inside the AppyAdManager object.
 *  There is no limit to the number of view objects that can be embedded. These objects will be included in the ad cycle and will be combined with ad packages retrieved from the server.
 *  However, note that an ad campaign package retrieved from the server may also override the <strong>baseViewIndex</strong>, which may cause the embedded objects to be bypassed in subsequent ad cycles.</p>
 *
 * @author Jon DeWeese
 * @version 1.0.20
 * @since 1.0
 */
public class AppyAdManager extends ViewFlipper {

    private static final String TAG = "AppyAdManager";
    private ArrayList<AppyAd> tozAdCampaign = new ArrayList<AppyAd>();
    private String tozAdAccountID;
    private String tozAdCampaignID;
    private Integer tozAdCampaignRetrievalInterval;
    private Integer tozAdCampaignRetrievalCounter;
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
    private String tozApplicationName;
    private String tozAndroidId;
    private int tozAdViewWidth = 0;
    private int tozAdViewHeight = 0;

    private int errorCounter,delayCounter;

    private int curAd = 0;
    private int nextAd = 0;
    private int baseAd = 0;
    private int lastAd = 0;
    private int numInternalChildren = 0;
    private int saveCampaignSize = 0;
    private boolean reInitializeCampaign = true;

    private Handler parentHandler = null;
    private Integer parentControl = null;

    public AppyAdManager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initManager(context,attrs);
    }

    public AppyAdManager(Context context) {
        super(context);
        AttributeSet attrs = new AttributeSet() {
            @Override
            public int getAttributeCount() {
                return 0;
            }

            @Override
            public String getAttributeName(int i) {
                return null;
            }

            @Override
            public String getAttributeValue(int i) {
                return null;
            }

            @Override
            public String getAttributeValue(String s, String s2) {
                return null;
            }

            @Override
            public String getPositionDescription() {
                return null;
            }

            @Override
            public int getAttributeNameResource(int i) {
                return 0;
            }

            @Override
            public int getAttributeListValue(String s, String s2, String[] strings, int i) {
                return 0;
            }

            @Override
            public boolean getAttributeBooleanValue(String s, String s2, boolean b) {
                return false;
            }

            @Override
            public int getAttributeResourceValue(String s, String s2, int i) {
                return 0;
            }

            @Override
            public int getAttributeIntValue(String s, String s2, int i) {
                return 0;
            }

            @Override
            public int getAttributeUnsignedIntValue(String s, String s2, int i) {
                return 0;
            }

            @Override
            public float getAttributeFloatValue(String s, String s2, float v) {
                return 0;
            }

            @Override
            public int getAttributeListValue(int i, String[] strings, int i2) {
                return 0;
            }

            @Override
            public boolean getAttributeBooleanValue(int i, boolean b) {
                return false;
            }

            @Override
            public int getAttributeResourceValue(int i, int i2) {
                return 0;
            }

            @Override
            public int getAttributeIntValue(int i, int i2) {
                return 0;
            }

            @Override
            public int getAttributeUnsignedIntValue(int i, int i2) {
                return 0;
            }

            @Override
            public float getAttributeFloatValue(int i, float v) {
                return 0;
            }

            @Override
            public String getIdAttribute() {
                return null;
            }

            @Override
            public String getClassAttribute() {
                return null;
            }

            @Override
            public int getIdAttributeResourceValue(int i) {
                return 0;
            }

            @Override
            public int getStyleAttribute() {
                return 0;
            }
        };

        initManager(context, attrs);
    }

    private void initManager(Context context, AttributeSet attrs) {

        errorCounter = 0;
        delayCounter = 0;

        if (attrs != null) {
            tozAdAccountID = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "accountID");
            if (tozAdAccountID == null) tozAdAccountID = "undefined";

            String temp = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "campaignID");
            setCampaignId(temp);

            tozCustomSpec = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "customSpec");

            toAdDefaultLink = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "defaultLink");

            defaultInAnimation = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "defaultInAnimation");
            if (defaultInAnimation == null) defaultInAnimation = AppyAdStatic.NORMAL_IN_ANIMATION;
            defaultOutAnimation = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "defaultOutAnimation");
            if (defaultOutAnimation == null) defaultOutAnimation = AppyAdStatic.NORMAL_OUT_ANIMATION;

            temp = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "refreshInterval");
            setRefreshInterval(temp);

            temp = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "defaultDisplayInterval");
            defaultDisplayInterval = parseIntegerValue(temp,AppyAdService.getInstance().getDefaultSleepInterval());

            temp = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "defaultAnimationDuration");
            defaultAnimationDuration = parseIntegerValue(temp,AppyAdStatic.NORMAL_ANIMATION_DURATION);

            temp = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "baseViewIndex");
            setBaseViewIndex(temp);

            temp = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "finalViewIndex");
            setFinalViewIndex(temp);

            temp = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "repeatCycle");
            setRepeatCycle(temp);

            temp = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "defaultTracking");
            defaultTracking = false;
            if (temp != null) if (temp.toLowerCase().equals("true")) defaultTracking = true;

            temp = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "adProcessing");
            mAdsOn = true;
            if (temp != null) if (temp.toLowerCase().equals("false")) mAdsOn = false;

            temp = attrs.getAttributeValue(AppyAdService.getInstance().TROYOZ_NAME_SPACE, "debug");
            if (temp != null) if (temp.toLowerCase().equals("true")) AppyAdService.getInstance().setDebug(true);

        }

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
        tozApplicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
        tozAndroidId = Settings.Secure.getString(getContext().getContentResolver(),Settings.Secure.ANDROID_ID);

        AppyAdService.getInstance().debugOut(TAG, "Initialized state ....." +
                "\n - Account: " + tozAdAccountID +
                "\n - Ad Campaign: " + tozAdCampaignID +
                "\n - Custom Parameter: " + tozCustomSpec +
                "\n - Campaign retrieval interval is " + tozAdCampaignRetrievalInterval +
                "\n - Default link " + toAdDefaultLink +
                "\n - Default In Animation is " + defaultInAnimation +
                "\n - Default Out Animation is " + defaultOutAnimation +
                "\n - Default Animation duration " + defaultAnimationDuration +
                "\n - Default Tracking is " + defaultTracking +
                "\n - Ad processing is " + mAdsOn +
                "\n - Default Display Time is " + defaultDisplayInterval +
                "\n - Campaign Repeat Cycle " + repeatCycle +
                "\n - Base View Index is " + baseViewIndex +
                "\n - Final View Index is " + finalViewIndex +
                "\n - Client Screen Density is " + metrics.densityDpi + " (" + mScreenDensity + ")");
    }

    /**
     * SDK's method is overridden to register this AppyAdManager object with the {@link AppyAdService} module
     * and ensure a background {@link AppyAdRetriever} thread is launched to handle ad campaign retrievals
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
                    baseAd = i+1;
                    int atype = 0;
                    if (cv instanceof ImageView) atype = AppyAdStatic.TOZIMAGE;
                    tozAdCampaign.add(new AppyAd(atype, toAdDefaultLink, defaultTracking, defaultInAnimation, defaultOutAnimation, defaultAnimationDuration, defaultDisplayInterval));
                    AppyAdService.getInstance().debugOut(TAG, "Added internal ad slot at index " + i);
                }
            }
        }
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
     * This method overrides the View's onSizeChanged method.  Added functionality is to save the
     * view's dimensions and register this AppyAdManager view with the {@link AppyAdService} object.
     *
     * @param w - int representing the new width
     * @param h - int representing the new height
     * @param oldw - int representing the old width
     * @param oldh - int representing the old height
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if ((tozAdViewHeight == 0) && (tozAdViewWidth == 0)) {
            tozAdViewHeight = h;
            tozAdViewWidth = w;

            AppyAdService.getInstance().debugOut(TAG, "Ad view port width is "+tozAdViewWidth+" and height is "+ tozAdViewHeight);
            AppyAdService.getInstance().debugOut(TAG, "Registering this manager view...");
            AppyAdService.getInstance().registerManager(tozAndroidId, tozApplicationName, this);
        }
    }

    /**
     * This method is called just before the ad campaign is initialized.  It tells the system
     * that a new ad campaign is coming.  The existing ad campaign parameters must be saved for the
     * UI thread to tear the old campaign down, while the background thread builds the new campaign.
     */
    public void readyNewCampaign() {
        reInitializeCampaign = true;
        saveCampaignSize = tozAdCampaign.size();
    }

    /**
     * This method is used to remove entries in the ad campaign's {@link ArrayList} and the corresponding
     * views.  Called every time there has been a new ad campaign retrieved from the server.
     */
    private void clearExternalAds() {
        int numberOfExternalChildrenViews = getChildCount() - numInternalChildren;
        curAd = 0;
        nextAd = 0;
        if (numInternalChildren < getChildCount()) {
            AppyAdService.getInstance().debugOut(TAG, "Removing previous campaign: " + numberOfExternalChildrenViews + " slots starting at index " + numInternalChildren);
            setInAnimation(AppyAdService.getInstance().setAnimation("in", tozAdCampaign.get(nextAd)));
            removeViews(numInternalChildren, numberOfExternalChildrenViews);
        }
        for (int i= numInternalChildren; i< saveCampaignSize; i++) {
            AppyAdService.getInstance().debugOut(TAG,"Removing previous ad at index "+i);
            tozAdCampaign.remove(numInternalChildren);
        }
    }

    /**
     * This method is called when the user taps/clicks the ad view.
     * @param v - The View which was clicked/tapped
     */
    public void handleAdClicks(View v) {
        if (!tozAdCampaign.isEmpty()) {
            AppyAd toa = tozAdCampaign.get(getDisplayedChild());
            if (toa != null) {
                if (toa.mTracking) AppyAdService.getInstance().trackAdCampaign(this, toa);
                String link = toa.mLink;
                if (link != null) {
                    if ((link.startsWith("app/")) && (link.length() > 4)) {
                        String param = link.substring(4);
                        if ((parentHandler != null) && (parentControl != null)) {
                            Message message = parentHandler.obtainMessage();
                            message.obj = param;
                            message.what = parentControl;
                            parentHandler.sendMessage(message);
                        }
                    }
                    else if (!link.toLowerCase().equals("none")) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                        getContext().startActivity(browserIntent);
                    }
                }
            }
        }
    }

    /**
     * This method adds a new ad view, which correlates to a specific entry in the ad campaign's {@link ArrayList}.
     * @param toa - The {@link AppyAd} object to use to get the information from in order to ad the new ad View.
     */
    public void addAdView(AppyAd toa) {
        if (toa != null) {
            if (toa.mAd != null) {
                switch (toa.mType) {
                    case AppyAdStatic.TOZIMAGE:
                        ImageView iv = new ImageView(getContext());
                        iv.setImageBitmap(toa.mAd);
                        if ((getChildCount() == 0) && (!tozAdCampaign.isEmpty())) setInAnimation(AppyAdService.getInstance().setAnimation("in", tozAdCampaign.get(nextAd)));
                        addView(iv, getChildCount());
                        toa.mAd = null;  // No sense in wasting space.
                        AppyAdService.getInstance().debugOut(TAG, "Added a view for external ad. Current view count is " + getChildCount());
                        break;
                }
            }
        }
    }

    /**
     * This method is called when the handler receives a message from the retriever thread indicating
     * it is time to switch to the next ad view.  Note that when an ad campaign has just been received,
     * extra measures are taken to re-initialize the existing views, counters, pointers appropriately.
     */
    public void showNextAd() {
        if (reInitializeCampaign) clearExternalAds();
        int extViewIndicator = getChildCount() - (1 + numInternalChildren);
        AppyAdService.getInstance().debugOut(TAG,"External view indicator is "+extViewIndicator+". Indexes: current="+curAd+", next="+nextAd+", base="+baseAd);
        if ((extViewIndicator < nextAd) && (nextAd > (numInternalChildren-1))) {
            addAdView(tozAdCampaign.get(nextAd));
        }
        if (reInitializeCampaign && (nextAd == 0) && (numInternalChildren > 0) && (getDisplayedChild() == 0)) {
            AppyAdService.getInstance().debugOut(TAG,"Leaving root internal ad (index=0) displayed (first pass with new ad campaign)");
        }
        else {
            if (nextAd != getDisplayedChild()) {
                setInAnimation(AppyAdService.getInstance().setAnimation("in", tozAdCampaign.get(nextAd)));
                setOutAnimation(AppyAdService.getInstance().setAnimation("out", tozAdCampaign.get(curAd)));
                setDisplayedChild(nextAd);
            }
            curAd = nextAd;
        }
        if (reInitializeCampaign) initializeCounters();
    }

    /**
     * This method is called by the {@link AppyAdConfig} object when a new ad campaign is being retrieved.
     * New ads are implemented into the ad campaign's {@link ArrayList}.
     *
     * @param toa - The new {@link AppyAd} object to add to the current campaign {@link ArrayList}
     */
    public void addAd(AppyAd toa) {
        if (toa != null) tozAdCampaign.add(toa);
        AppyAdService.getInstance().debugOut(TAG,"Current number of ads in campaign is "+tozAdCampaign.size());
    }

    /**
     * This method sets the campaignID string.
     * @param newCampaignId - A String value representing a unique identifier for the current ad campaign.
     */
    public void setCampaignId(String newCampaignId) {
        if ((newCampaignId != null) && !newCampaignId.isEmpty()) {
            tozAdCampaignID = newCampaignId;
        }
        else tozAdCampaignID = "default";
    }

    /**
     * This method returns the Integer value of a String.
     * @param value - A String representing the integer to parse
     * @param defaultValue - An Integer value used if the String value cannot be parsed as an integer
     * @return - An Integer value representing the String, or the defaultValue if it could not be parsed.
     */
    private Integer parseIntegerValue(String value, Integer defaultValue) {
        Integer ival = defaultValue;
        if (value != null) {
            try {
                ival = Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
                ival = defaultValue;
            }
        }
        return (ival);
    }

    /**
     * This method is called to set the base view index.  The base view index is the index of the
     * ad view that will be used to begin the next cycle within the ad campaign.  The input value
     * is of type String, which represents an integer because it is either set by this module at
     * initialization via a parameter within the xml layout, or it is dyanmically set via the {@link AppyAdConfig}
     * object when a new ad campaign has been retrieved.
     *
     * @param idx - A String value representing the integer to be used as the index. Example "2" will be converted to 2.
     */
    public void setBaseViewIndex(String idx) {
        int internalBuffer = 0;
        if ((idx != null) && (idx.startsWith("+"))) {
            idx = idx.substring(1);
            internalBuffer = numInternalChildren;
        }
        baseViewIndex = parseIntegerValue(idx,0) + internalBuffer;
        AppyAdService.getInstance().debugOut(TAG,"Set Base View Index to "+baseViewIndex);
    }

    /**
     * This method defines the index of the advertising element to land on when the campaign
     * completes its cycling. The view will stay on this index for the remainder of the
     * activity's lifecycle.
     *
     * @param idx - A String value representing the integer to be used as the index. Example "2" will be converted to 2.
     */
    public void setFinalViewIndex(String idx) {
        int internalBuffer = 0;
        if ((idx != null) && (idx.startsWith("+"))) {
            idx = idx.substring(1);
            internalBuffer = numInternalChildren;
        }
        finalViewIndex = parseIntegerValue(idx,null);
        if (finalViewIndex != null) finalViewIndex += internalBuffer;
        AppyAdService.getInstance().debugOut(TAG,"Set Final View Index to "+finalViewIndex);
    }

    /**
     * This method sets the repeat cycle for the existing ad campaign.  This input is a string
     * representing the desired integer value.  This method is either called by this object upon initialization
     * when the value was specified in the layout xml file, or called by the {@link AppyAdConfig} object
     * when set or overridden by a new ad campaign retrieved from the server.
     *
     * @param rc - A String value representing the integer to be used as the number of times the campaign will cycle through its ad elements. Example "2" will be converted to 2.
     */
    public void setRepeatCycle(String rc) {
        repeatCycle = parseIntegerValue(rc,null);
        AppyAdService.getInstance().debugOut(TAG,"Set Repeat Cycle to "+repeatCycle);
    }

    /**
     * This method is used to check to see if an ad campaign exists. (Either by including static
     * views within the AppyAdManager component in the layout xml file, or with the retrieval of
     * dynamic ads within an AppyAds campaign from the server.
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
        alignRefreshInterval();
        reInitializeCampaign = false;
    }

    /**
     * This method returns a boolean value indicating whether or not ads are currently being processed.
     * @return on - A boolean value indicating whether or not the ad campaign system is on or off.
     */
    public boolean adsAreOn() {
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
     * @return value - A String representing the account id.
     */
    public String getAccountID() {
        return (tozAdAccountID);
    }

    /**
     * This method returns the current campaign id, which was originally set in the layout xml file.
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
     * @param s - A String representing the custom specification to set for tracking/recording click-throughs. (Returns null if not set.)
     */
    public void setCustomSpec(String s) {
        tozCustomSpec = s;
        AppyAdService.getInstance().debugOut(TAG,"Set custom parameter to "+tozCustomSpec);
    }

    /**
     * This method allows the parent application to define a {@link Handler} that will get notified when
     * custom links are used for the click/tap-throughs on ads.  Custom links start with a "app/"
     * followed by any pattern of a String value.  When these links are clicked/tapped by a user
     * the handler will be sent the String pattern in a message sent to the handler.  The 'what'
     * field will be marked with the controlValue, which will enable better processing by the parent
     * {@link Handler} object.
     * @param mHandle - A {@link Handler} object which must exist in the the parent {@link Activity}.
     * @param controlValue - An Integer value specifying the control code to pass with messages sent to the {@link Handler} object.
     */
    public void setParentCallback(Handler mHandle, Integer controlValue) {
        if ((mHandle != null) && (controlValue != null)) {
            parentHandler = mHandle;
            parentControl = controlValue;
            AppyAdService.getInstance().debugOut(TAG, "Set Parent callback parameters with flag " + parentControl);
        }
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
     * @return screenDensity - A String value representing the screen density. (ldpi,mdpi,hdpi,xhdpi,xxhdpi or xxxhdpi)
     */
    public String getScreenDensity() {
        return (mScreenDensity);
    }

    /**
     * This method returns the width of this AppyAdManager view object.
     * @return - int value defining the width in pixels of the AppyAdManager view object.
     */
    public int getAdViewWidth() {
        return (tozAdViewWidth);
    }

    /**
     * This method returns the height of this AppyAdManager view object.
     * @return - int value defining the height in pixels of the AppyAdManager view object.
     */
    public int getAdViewHeight() {
        return (tozAdViewHeight);
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
     * This method ensures that the refresh interval is neither too small or too large.
     * <ul><li>It must be less than the maximum allowed refresh time (24 hours). If the value is found to be greater than that, it will be re-adjusted to the maximum value.</li></ul>
     * <ul><li>It must be greater than the minimum allowed refresh time (30 seconds). If the current ad campaign duration is less than the minimum refresh interval time, then the refresh interval is set to that minimum value.</li></ul>
     * <ul><li>It must not be less than the total number of time it takes for one cycle of the current ad campaign.  If the value is found to be less than than, it will be re-adjusted to match the current ad campaign duration.</li></ul>
     */
    private void alignRefreshInterval() {
        int totalCampaignDuration = 0;
        int externalCampaignDuration = 0;
        for (int i=0; i<tozAdCampaign.size(); i++) {
            totalCampaignDuration += tozAdCampaign.get(i).mAdDuration;
            if (i >= numInternalChildren) externalCampaignDuration += tozAdCampaign.get(i).mAdDuration;
        }
        AppyAdService.getInstance().debugOut(TAG,"Current ad campaign has external ad duration of "+externalCampaignDuration+". Total total campaign duration is "+totalCampaignDuration);

        if (tozAdCampaignRetrievalInterval != null) {
            int minRefresh = Math.max(totalCampaignDuration,AppyAdService.getInstance().MINIMUM_REFRESH_TIME);
            if (tozAdCampaignRetrievalInterval < minRefresh) {
                AppyAdService.getInstance().debugOut(TAG,"Adjusting refresh interval upward to "+minRefresh);
                tozAdCampaignRetrievalInterval = minRefresh;
            }
            if (tozAdCampaignRetrievalInterval > AppyAdService.getInstance().MAXIMUM_REFRESH_TIME) {
                AppyAdService.getInstance().debugOut(TAG,"Adjusting refresh interval downward to "+AppyAdService.getInstance().MAXIMUM_REFRESH_TIME);
                tozAdCampaignRetrievalInterval = AppyAdService.getInstance().MAXIMUM_REFRESH_TIME;
            }
        }
    }

    /**
     * This method sets the time in milliseconds that the system will wait before going to the server for another ad campaign package.
     * This value can be initialized by the <strong>refreshInterval</strong> attribute for the AppyAdManager object in the layout xml file, but
     * note that this value can also be overridden by a subsequent ad campaign package retrieved from the server.
     *
     * @param interval - A String value representing the time in milliseconds to wait before going to the server for another ad campaign package.
     */
    public void setRefreshInterval(String interval) {
        tozAdCampaignRetrievalInterval = parseIntegerValue(interval,0);
        AppyAdService.getInstance().debugOut(TAG,"Set refresh interval to "+tozAdCampaignRetrievalInterval);
    }

    /**
     * This value is called by the retriever thread to prepare the next ad view to be shown.
     * @return nextAdPreparedOK - A boolean value indicating whether or not a new ad is ready to be shown.
     */
    public boolean prepareNextAd() {
        boolean retVal = false;

        if ((tozAdCampaign.size() - numInternalChildren) <= 0) return (retVal);

        AppyAdService.getInstance().debugOut(TAG,"Preparing next Ad... indexes: next="+nextAd+", current="+curAd+", base="+baseAd+", last="+lastAd+", repeatCycle="+repeatCycle);

        if ((repeatCycle != null) && (repeatCycle < 0)) return (false);

        if (curAd < lastAd) {
            nextAd = curAd + 1;
            retVal = true;
        }
        else if ((curAd == lastAd) && (curAd != baseViewIndex)) {
            nextAd = baseViewIndex;
            retVal = true;
            if (repeatCycle != null) {
                repeatCycle--;
                if (repeatCycle < 0) {
                    if (finalViewIndex != null) {
                        if (finalViewIndex > lastAd) finalViewIndex = lastAd;
                        if (finalViewIndex < 0) finalViewIndex = 0;
                        nextAd = finalViewIndex;
                    }
                }
            }
        }

        return (retVal);
    }

    /**
     * This method returns the current error counter.
     * @return errors - An int value representing the current error count.
     */
    public int getErrorCounter() {
        return errorCounter;
    }

    /**
     * This method sets the error counter to the specified value.
     * @param value - An int value to be used to set the new value of the error counter.
     */
    public void setErrorCounter(int value) {
        errorCounter = value;
    }

    /**
     * This method increments the error counter by the specified value.
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
     * @param value - An int value representing the number to set the delay counter.
     */
    public void setDelayCounter(int value) {
        delayCounter = value;
    }

    /**
     * This method increments the delay counter by the specified value.
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
     * This method is called by the retriever thread to determine the time to wait before flagging
     * the next ad to be shown.
     *
     * @return interval - An int value representing the number of milliseconds to wait for the next ad to be shown.
     */
    public int getSleepInterval() {
        if ((curAd < 0) || (tozAdCampaign.isEmpty())) {
            AppyAdService.getInstance().debugOut(TAG, "Sleep interval defaulting to " + AppyAdService.getInstance().getDefaultSleepInterval());
            return (AppyAdService.getInstance().getDefaultSleepInterval());
        }
        else {
            AppyAdService.getInstance().debugOut(TAG,"Sleep interval for current ad is "+tozAdCampaign.get(curAd).mAdDuration);
            return (tozAdCampaign.get(curAd).mAdDuration);
        }
        //else return (sleepInterval);
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
            tozAdCampaignRetrievalCounter += getSleepInterval();

            AppyAdService.getInstance().debugOut(TAG,"Ad Campaign refresh counter="+tozAdCampaignRetrievalCounter);
            if (tozAdCampaignRetrievalCounter >= tozAdCampaignRetrievalInterval) {
                tozAdCampaignRetrievalCounter = 0;
                return (true);
            }
        }
        return (false);
    }
}
