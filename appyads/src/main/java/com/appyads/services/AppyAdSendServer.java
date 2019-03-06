package com.appyads.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;

/**
 * This class is used for all network communcation to/from the AppyAds server.
 */
public class AppyAdSendServer {

    private static final String TAG = "AppyAdSendServer";
    private AppyAdRequest mAppyAdRequest;
    private String[] specError = new String[] {"",""};
    private int specErrorNum = 0;
    public boolean mStatus = false;

    /**
     * This constructor is for initializing the parameters needed to send a query to the AppyAds server.
     * @param aaRequest - An AppyAdRequest object representing the entire request string to send to the AppyAds server.
     */
    public AppyAdSendServer(AppyAdRequest aaRequest) {
        mAppyAdRequest = aaRequest;
    }

    /**
     * This method sets parameters based on an error that has occurred.
     * @param e - An int value representing the error situation.
     * @param a - A String value representing part of the textual error message.
     * @param b - A String value representing part of the textual error message.
     */
    private void setSpecError(int e, String a, String b) {
        specErrorNum = e;
        specError[0] = a;
        specError[1] = b;
        mStatus = false;
    }

    /**
     * This method returns the error text of the sequence of errors that have occurred.
     * @return - An array of String values representing the textual error messages.
     */
    public String[] getSpecError() {
        return(specError);
    }

    /**
     * This method returns a textual representation of an error that has occurred.
     * @return - A String representing the textual value of the last error
     */
    public String getSpecErrorStr() {
        if (specError != null) {
            if (specError.length > 1) return (specError[0]+" - "+specError[1]);
            if (specError.length > 0) return (specError[1]);
        }
        return(null);
    }

    /**
     * This method prepares query parameters to send to the AppyAds server
     * @return - A ByteBuffer object which is created with a call to the queryServer() method.
     */
    private String prepExQuery() {
        StringBuilder pars = new StringBuilder("");
        try {
            if (mAppyAdRequest.track != null) pars.append("&tracking=").append(URLEncoder.encode(mAppyAdRequest.track, "UTF-8"));
            if (mAppyAdRequest.accId != null) pars.append("&account_id=").append(URLEncoder.encode(mAppyAdRequest.accId, "UTF-8"));
            if (mAppyAdRequest.appId != null) pars.append("&app_id=").append(URLEncoder.encode(mAppyAdRequest.appId, "UTF-8"));
            if (mAppyAdRequest.campAcct != null) pars.append("&cacct=").append(URLEncoder.encode(mAppyAdRequest.campAcct, "UTF-8"));
            if (mAppyAdRequest.campId != null) pars.append("&campaign_id=").append(URLEncoder.encode(mAppyAdRequest.campId, "UTF-8"));
            if (mAppyAdRequest.campSize != null) pars.append("&campaign_size=").append(URLEncoder.encode(mAppyAdRequest.campSize, "UTF-8"));
            if (mAppyAdRequest.adId != null) pars.append("&ad_id=").append(URLEncoder.encode(mAppyAdRequest.adId, "UTF-8"));
            if (mAppyAdRequest.adLink != null) pars.append("&ad_link=").append(URLEncoder.encode(mAppyAdRequest.adLink, "UTF-8"));
            if (mAppyAdRequest.custom != null) pars.append("&custom=").append(URLEncoder.encode(mAppyAdRequest.custom, "UTF-8"));
            if (mAppyAdRequest.uSpec != null) pars.append("&user_id=").append(URLEncoder.encode(mAppyAdRequest.uSpec, "UTF-8"));
            if (mAppyAdRequest.screen != null) pars.append("&screen=").append(URLEncoder.encode(mAppyAdRequest.screen, "UTF-8"));
            if (mAppyAdRequest.width > 0) pars.append("&width=").append(String.valueOf(mAppyAdRequest.width));
            if (mAppyAdRequest.height > 0) pars.append("&height=").append(String.valueOf(mAppyAdRequest.height));
            if (pars.length() > 1) pars = pars.deleteCharAt(0);
        }
        catch (Exception e) {
            AppyAdService.getInstance().errorOut(TAG,"Exception while building query parameters. \n - "+e.getMessage());
        }
        return(pars.toString());
    }

    /**
     * This method prepares the request string and sends it to the AppyAds server.  Note that If the request
     * is not a tracking request, processing will be blocked by the TCP/IP socket call while
     * waiting for a response from the server.
     * @return - A String value containing the response from the server.
     */
    public String queryServer() {
        specErrorNum = 0;
        mStatus = false;
        StringBuilder svrResp = new StringBuilder();
        BufferedReader input = null;
        OutputStreamWriter output = null;
        HttpURLConnection client = null;
        try {
            String sPars = prepExQuery();

            // Define http connection
            URL url = new URL(AppyAdService.getAppyAdsServerUrl(mAppyAdRequest.operation));
            client = (HttpURLConnection) url.openConnection();

            // Setup for writing/reading
            client.setDoOutput(true);
            client.setFixedLengthStreamingMode(sPars.length());
            output = new OutputStreamWriter(client.getOutputStream());
            output.write(sPars);
            output.flush();

            if (mAppyAdRequest.operation == AppyAdStatic.GETADSET) {
                input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                // Now wait for response

                String line = null;
                while ((line = input.readLine()) != null) {
                    svrResp.append(line).append("\n");
                }
                AppyAdService.getInstance().debugOut(TAG, "Got in ad package with length " + svrResp.length());
            }
            else {
                svrResp.append("ok");
            }
            // Set status to ok
            mStatus = true;
            AppyAdService.getInstance().debugOut(TAG,"Network server connection operation successful.");

        }
        catch (UnknownHostException e1) {
            setSpecError(1,"Unable to connect to server.","Possible network DNS system error.");
        }
        catch (ConnectException e1) {
            setSpecError(1,"Unable to connect to server.","Please check network status.");
        }
        catch (IOException e1) {
            setSpecError(1,"Unable to connect to server.","Please check network status.");
        }
        catch (Exception e1) {
            setSpecError(1,"Unable to connect to server.","Error during network connection.");
        }
        finally {
            AppyAdService.getInstance().debugOut(TAG,"Closing network connections.");
            // Close the http connection
            try { if (input != null) input.close(); } catch (Exception el) { }
            try { if (output != null) output.close(); } catch (Exception el) { }
            try { if (client != null) client.disconnect(); } catch (Exception el) { }
        }

        if (svrResp.length() == 0) {
            svrResp.append(' '); // Failure
            if (specErrorNum == 0) setSpecError(1,"No response from network server.","Please try again later.");
        }

        return (svrResp.toString());
    }

}
