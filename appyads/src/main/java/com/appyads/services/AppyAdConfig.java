package com.appyads.services;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * This class parses an AppyAds campaign package (xml format) and creates the individual {@link AppyAd}
 * objects within the {@link AppyAdManager} as well as the campaign's base settings.
 *
 */
public class AppyAdConfig {

    private static final String TAG = "AppyAdConfig";

    /**
     * This method is the main constructor and handles the parsing of the xml campaign package file
     * and sets the properties in the {@link AppyAd} objecs within the {@link AppyAdManager}.
     *
     * @param toam - The {@link AppyAdManager} object which will receive the ad campaign information
     * @param adXmlDriver - A String containing an AppyAds ad campaign details in XML format
     *
     */
    public AppyAdConfig(AppyAdManager toam, String adXmlDriver) {
        String campAcct = null;
        if ((adXmlDriver.length() > 50) && (toam != null)) {
            try {
                String defaultTrack = AppyAdService.getInstance().getDefaultTracking() ? "true" : "false";
                String defaultInAnimation = AppyAdService.getInstance().getDefaultInAnimation();
                String defaultOutAnimation = AppyAdService.getInstance().getDefaultOutAnimation();
                String defaultAnimationDuration = String.valueOf(AppyAdService.getInstance().getDefaultAnimationDuration());
                String defaultAdDuration = String.valueOf(AppyAdService.getInstance().getDefaultDisplayInterval());
                //String defaultLink = AppyAdService.getInstance().getDefaultLink();
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(adXmlDriver));
                Document doc = db.parse(is);
                doc.getDocumentElement().normalize();
                AppyAdService.getInstance().debugOut(TAG,"Root element " + doc.getDocumentElement().getNodeName());

                NodeList nodeLst = doc.getElementsByTagName("AppyAdsElement");
                AppyAdService.getInstance().debugOut(TAG,"Information for all ads.....");

                int nodeLength = nodeLst.getLength();
                if (nodeLength > 0) toam.initCampaignData();

                for (int s = 0; s < nodeLength; s++) {

                    Node adNode = nodeLst.item(s);

                    if (adNode.getNodeType() == Node.ELEMENT_NODE) {

                        String imgSrc = getAdAttribute(adNode, "src", null);
                        AppyAdService.getInstance().debugOut(TAG,"src="+imgSrc);

                        if (campAcct == null && imgSrc != null && imgSrc.contains("/resources/?rc=")) {
                            campAcct = imgSrc.substring(imgSrc.indexOf("/resources/?rc=")+15);
                            if (campAcct.indexOf('/') > 0) campAcct = campAcct.substring(0,campAcct.indexOf('/'));
                            else campAcct = null;
                        }

                        String title = getAdAttribute(adNode, "title", null);
                        AppyAdService.getInstance().debugOut(TAG,"title="+title);

                        String link = getAdAttribute(adNode, "link", null);
                        AppyAdService.getInstance().debugOut(TAG,"link="+link);

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
                                toam.addAd(new AppyAd(AppyAdStatic.TOZIMAGE,imgSrc,id,title,link,tracking,inA,outA,ad,dd));
                            }
                        }

                    }

                }

                NodeList campLst = doc.getElementsByTagName("AppyAdsCampaign");
                Node cNode = campLst.item(0);
                if (cNode.getNodeType() == Node.ELEMENT_NODE) {
                    toam.setCampaignAccount(campAcct);

                    String campId = getAdAttribute(cNode, "id", null);
                    AppyAdService.getInstance().debugOut(TAG,"campaignID="+campId);
                    toam.setCampaignId(campId);

                    String baseIdx = getAdAttribute(cNode, "baseViewIndex", null);
                    AppyAdService.getInstance().debugOut(TAG,"baseViewIndex="+baseIdx);
                    if (baseIdx != null) toam.setBaseViewIndex(baseIdx);

                    String finIdx = getAdAttribute(cNode, "finalViewIndex", null);
                    AppyAdService.getInstance().debugOut(TAG,"finalViewIndex="+finIdx);
                    if (finIdx != null) toam.setFinalViewIndex(finIdx);

                    String repCy = getAdAttribute(cNode, "repeatCycle", null);
                    AppyAdService.getInstance().debugOut(TAG,"repeatCycle="+repCy);
                    if (repCy != null) toam.setRepeatCycle(repCy);

                    String refInt = getAdAttribute(cNode, "refreshInterval", null);
                    AppyAdService.getInstance().debugOut(TAG,"refreshInterval="+refInt);
                    if (refInt != null) toam.setRefreshInterval(refInt);
                }

            } catch (Exception e) {
                AppyAdService.getInstance().errorOut(TAG,"Exception reading campaign package xml file. \n - "+e.getMessage());
            }
        }
    }

    /**
     * This is a utility method to help the constructor parse a certain value from the xml file
     *
     * @param adElmnt - The {@link Element} of an xml entity
     * @param piece - A String representing the name of an attribute of the xml entity
     * @return - Returns a value representing the value of the named attribute
     *
     */
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

    /**
     * This is a utility method that helps the constructor determine the values of {@link Node} attributes
     * within the xml file.
     *
     * @param adNode - The {@link Node} object being parsed
     * @param attr - A String representing the name of the attribute of the Node
     * @param def - A String defining the default value (in case the attribute was not specified).
     *
     * @return - A String representing the value of the attribute (or the default value supplied by def if the attribute was not specified in the xml)
     */
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