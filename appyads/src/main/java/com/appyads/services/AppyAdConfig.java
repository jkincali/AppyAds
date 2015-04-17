package com.appyads.services;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class AppyAdConfig {

    private static final String TAG = "AppyAdConfig";
    public AppyAdConfig(AppyAdManager toam, String adDir, String xmlfilename) {
        File file = new File(adDir+xmlfilename);
        if ((file.exists()) && (toam != null)) {
            try {
                String defaultTrack = AppyAdService.getInstance().getDefaultTracking() ? "true" : "false";
                String defaultInAnimation = AppyAdService.getInstance().getDefaultInAnimation();
                String defaultOutAnimation = AppyAdService.getInstance().getDefaultOutAnimation();
                String defaultAnimationDuration = String.valueOf(AppyAdService.getInstance().getDefaultAnimationDuration());
                String defaultAdDuration = String.valueOf(AppyAdService.getInstance().getDefaultDisplayInterval());
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(file);
                doc.getDocumentElement().normalize();
                AppyAdService.getInstance().debugOut(TAG,"Root element " + doc.getDocumentElement().getNodeName());

                NodeList nodeLst = doc.getElementsByTagName("AppyAd");
                AppyAdService.getInstance().debugOut(TAG,"Information of all ads.....");

                for (int s = 0; s < nodeLst.getLength(); s++) {

                    Node adNode = nodeLst.item(s);

                    if (adNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element adElmnt = (Element) adNode;

                        String imgFileName = getAdElementValue(adElmnt,"image");
                        AppyAdService.getInstance().debugOut(TAG,"Image : "+imgFileName);

                        String title = getAdElementValue(adElmnt,"title");
                        AppyAdService.getInstance().debugOut(TAG,"Title : "+title);

                        String descr = getAdElementValue(adElmnt,"description");
                        AppyAdService.getInstance().debugOut(TAG,"Descr : "+descr);

                        String link = getAdElementValue(adElmnt,"link");
                        AppyAdService.getInstance().debugOut(TAG,"Link : "+link);

                        String id = getAdAttribute(adNode, "id", null);
                        AppyAdService.getInstance().debugOut(TAG,"id="+id);

                        String adType = getAdAttribute(adNode, "type", null);
                        AppyAdService.getInstance().debugOut(TAG,"type="+adType);

                        String dd = getAdAttribute(adNode, "displayDuration", defaultAdDuration);
                        AppyAdService.getInstance().debugOut(TAG,"displayDuration="+dd);

                        String inA = getAdAttribute(adNode, "inAnimation", defaultInAnimation);
                        AppyAdService.getInstance().debugOut(TAG,"inAnimation="+inA);

                        String outA = getAdAttribute(adNode, "outAnimation", defaultOutAnimation);
                        AppyAdService.getInstance().debugOut(TAG,"outAnimation="+outA);

                        String ad = getAdAttribute(adNode, "animationDuration", defaultAnimationDuration);
                        AppyAdService.getInstance().debugOut(TAG,"animationDuration="+ad);

                        String tracking = getAdAttribute(adNode, "tracking", defaultTrack);
                        AppyAdService.getInstance().debugOut(TAG,"tracking="+tracking);

                        if (adType != null) {
                            if (adType.toLowerCase().equals("image")) {
                                AppyAdService.getInstance().debugOut(TAG,"Ad type is image...file name is "+imgFileName);
                                File imgFile = new File(adDir+"/"+imgFileName);
                                if (imgFile.exists()) {
                                    AppyAdService.getInstance().debugOut(TAG,"Found Ad file "+imgFileName);
                                    toam.addAd(new AppyAd(AppyAdStatic.TOZIMAGE,imgFile,id,title,descr,link,tracking,inA,outA,ad,dd));
                                }
                            }
                        }

                    }

                }

                NodeList campLst = doc.getElementsByTagName("AppyAdCampaign");
                Node cNode = campLst.item(0);
                if (cNode.getNodeType() == Node.ELEMENT_NODE) {
                    String baseIdx = getAdAttribute(cNode, "baseViewIndex", null);
                    AppyAdService.getInstance().debugOut(TAG,"baseViewIndex="+baseIdx);
                    if (baseIdx != null) toam.setBaseViewIndex(baseIdx);

                    String finIdx = getAdAttribute(cNode, "finalViewIndex", null);
                    AppyAdService.getInstance().debugOut(TAG,"finalViewIndex="+finIdx);
                    if (finIdx != null) toam.setFinalViewIndex(finIdx);

                    String repCy = getAdAttribute(cNode, "repeatCycle", null);
                    AppyAdService.getInstance().debugOut(TAG,"repeatCycle="+repCy);
                    if (repCy != null) toam.setRepeatCycle(repCy);
                }

            } catch (Exception e) {
                AppyAdService.getInstance().errorOut(TAG,"Exception reading campaign package xml file. \n - "+e.getMessage());
            }
        }
    }

    public String getAdElementValue(Element adElmnt, String piece) {
        NodeList pLst = adElmnt.getElementsByTagName(piece);
        if (pLst != null) {
            Element pEle = (Element) pLst.item(0);
            if (pEle != null) {
                NodeList pNdLst = pEle.getChildNodes();
                if (pNdLst != null) {
                    return (((Node) pNdLst.item(0)).getNodeValue());
                }
            }
        }
        return (null);
    }

    public String getAdAttribute(Node adNode, String attr, String def) {
        NamedNodeMap nm = adNode.getAttributes();
        if (nm != null) {
            for (int i = 0; i < nm.getLength(); i++) {
                if (nm.item(i).getNodeName().equals(attr)) return (nm.item(i).getNodeValue());
            }
        }
        return (def);
    }
}