package com.appyads.services;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

public class AppyAd {

    public String mAdID;
	public String mTitle;
	public String mDescription;
	public String mLink;
    public boolean mTracking;
	public Bitmap mAd;
    public int mType;
    public int mAnimationIn;
    public int mAnimationOut;
    public int mAnimationDuration;
    public int mAdDuration;

    public AppyAd (int atype, String link, boolean track, String animin, String animout, Integer animdur, Integer addur) {
        this(atype,null,"base-layout",null,null,link,track?"true":"false",animin,animout,animdur.toString(),addur.toString());
    }

    public AppyAd (int atype, File adfile, String id, String title, String description, String link, String track, String animin, String animout, String animdur, String addur) {

        int animIn = AppyAdStatic.FADE_IN;
        int animOut = AppyAdStatic.FADE_OUT;
        int animDur = AppyAdStatic.NORMAL_ANIMATION_DURATION;
        int displayDur = AppyAdStatic.NORMAL_AD_DURATION;

        if (addur != null) {
            try {
                displayDur = Integer.parseInt(addur);
                if (displayDur <= 0) displayDur = AppyAdStatic.NORMAL_AD_DURATION;
            } catch (NumberFormatException nfe) {
                displayDur = AppyAdStatic.NORMAL_AD_DURATION;
            }
        }

        if (animdur != null) {
            try {
                animDur = Integer.parseInt(animdur);
                if (animDur <= 0) animDur = AppyAdStatic.NORMAL_ANIMATION_DURATION;
            } catch (NumberFormatException nfe) {
                animDur = AppyAdStatic.NORMAL_ANIMATION_DURATION;
            }
        }

        if (animin != null) {
            animin = animin.toLowerCase();
            if (animin.equals("slide_in_from_left")) animIn = AppyAdStatic.SLIDE_IN_FROM_LEFT;
            else if (animin.equals("slide_in_from_right")) animIn = AppyAdStatic.SLIDE_IN_FROM_RIGHT;
            else if (animin.equals("slide_in_from_top")) animIn = AppyAdStatic.SLIDE_IN_FROM_TOP;
            else if (animin.equals("slide_in_from_bottom")) animIn = AppyAdStatic.SLIDE_IN_FROM_BOTTOM;
            else if (animin.equals("zoom_in_from_left")) animIn = AppyAdStatic.ZOOM_IN_FROM_LEFT;
            else if (animin.equals("zoom_in_from_right")) animIn = AppyAdStatic.ZOOM_IN_FROM_RIGHT;
            else if (animin.equals("zoom_in_from_center")) animIn = AppyAdStatic.ZOOM_IN_FROM_CENTER;
            else if (animin.equals("none")) animIn = AppyAdStatic.NO_ANIMATION;
        }

        if (animout != null) {
            animout = animout.toLowerCase();
            if (animout.equals("slide_out_to_left")) animOut = AppyAdStatic.SLIDE_OUT_TO_LEFT;
            else if (animout.equals("slide_out_to_right")) animOut = AppyAdStatic.SLIDE_OUT_TO_RIGHT;
            else if (animout.equals("slide_out_to_top")) animOut = AppyAdStatic.SLIDE_OUT_TO_TOP;
            else if (animout.equals("slide_out_to_bottom")) animOut = AppyAdStatic.SLIDE_OUT_TO_BOTTOM;
            else if (animout.equals("zoom_out_to_left")) animOut = AppyAdStatic.ZOOM_OUT_TO_LEFT;
            else if (animout.equals("zoom_out_to_right")) animOut = AppyAdStatic.ZOOM_OUT_TO_RIGHT;
            else if (animout.equals("zoom_out_to_center")) animOut = AppyAdStatic.ZOOM_OUT_TO_CENTER;
            else if (animout.equals("none")) animOut = AppyAdStatic.NO_ANIMATION;
        }

        mType = atype;
 		if (adfile != null) mAd = BitmapFactory.decodeFile(adfile.getAbsolutePath(),new BitmapFactory.Options());
        mAdID = id;
		mTitle = title;
		mDescription = description;
		mLink = link;
        mTracking = ((track != null) && (track.toLowerCase().equals("true")));
        mAnimationIn = animIn;
        mAnimationOut = animOut;
        mAnimationDuration = animDur;
        mAdDuration = displayDur;
	}
}