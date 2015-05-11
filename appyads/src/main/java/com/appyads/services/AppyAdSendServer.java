package com.appyads.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import troyozezstr.TroyOzEZStrOut;

/**
 * This class is used for all network communcation to/from the AppyAds server.
 */
public class AppyAdSendServer {

    private static final String TAG = "AppyAdSendServer";
    private String mServer;
    private int mPort;
    private String mSendString;
    private int mOp = 0;
    private String[] specError = new String[] {"",""};
    private int specErrorNum = 0;
    public boolean mStatus = false;

    /**
     * This constructor defines the necessary parameters for a client request to the server. This
     * particular signature is used for the retrieval of an ad campaign package from the server.
     * @param op - An int value representing the request code.
     * @param track - A String value representing whether or not tracking is currently on or off.
     * @param accID - A String value representing the AppyAds account id.
     * @param appID - A String value representing the application id.
     * @param campID - A String value representing the AppyAds ad campaign id.
     * @param custom - A String representing the custom setting (reserved for the application developer/ownder).
     * @param uspec - A String representing the unique user/device id.
     * @param screen - A String representing device's screen density.
     * @param width - An int value representing the view's width.
     * @param height - An int value representing the view's height.
     */
    public AppyAdSendServer(int op, String track, String accID, String appID, String campID, String custom, String uspec, String screen, int width, int height) {
        mOp = op;
        mSendString = "|"+op+"|track="+track+"\\account="+accID+"\\app="+appID+"\\campaign="+campID+"\\custom="+custom+"\\user="+uspec+"\\screen="+screen+"\\width="+width+"\\height="+height+"\\";
        initVars();
    }

    /**
     * This constructor is specifically used only by the {@link AppyAdQuickThread} module for tracking
     * click/tap-through actions.
     * @param op - An int value representing the request code.
     * @param sendstr - A String representing the entire request string to send to the AppyAds server.
     */
    public AppyAdSendServer(int op, String sendstr) {
        mOp = op;
        mSendString = sendstr;
        initVars();
    }

    /**
     * This method initializes the AppyAds server IP and port.
     */
    private void initVars() {
        mServer = AppyAdService.getHostIP();
        mPort = AppyAdService.getHostPort();
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
     * This method prepares the request string and sends it to the AppyAds server.  Note that If the request
     * is not a tracking request, processing will be blocked by the TCP/IP socket call while
     * waiting for a response from the server.
     * @return - A {@link ByteBuffer} containing the response from the server.
     */
    public ByteBuffer queryServer() {
        specErrorNum = 0;
        mStatus = false;
        ByteBuffer retBuf = null;
        TroyOzEZStrOut ts = new TroyOzEZStrOut();
        String str = ts.tozPrepStr(mSendString);
        Socket socket = null;
        try {
            // Create socket
            InetAddress serverAddr = InetAddress.getByName(mServer);

            socket = new Socket(serverAddr, mPort);

            // Set up input and output buffers
            PrintWriter output = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(),"UTF8")),
                    true);

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send query to server
            output.println(str);

            if (mOp == AppyAdStatic.GETADSET) {
                // Now wait for response
                byte[] b = new byte[2048];
                retBuf = ByteBuffer.allocate(150000);
                int length;
                while ((length = socket.getInputStream().read(b)) != -1) {
                    retBuf.put(b, 0, length);
                }
                AppyAdService.getInstance().debugOut(TAG, "Got in ad package with length " + retBuf.position());
            }
            else {
                retBuf = ByteBuffer.allocate(2);
                retBuf.put(new byte[]{'o','k'});
            }
            // Set status to ok
            mStatus = true;

            input.close();
            output.close();
            // Close the socket connection
            AppyAdService.getInstance().debugOut(TAG,"Network socket read successfully. Closed channels.");
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
            try {
                AppyAdService.getInstance().debugOut(TAG,"Closing network socket.");
                if (socket != null) socket.close();
            }
            catch (IOException el) {
                // ignore, we're done anyway.
            }
        }

        if (retBuf == null) {
            if (specErrorNum == 0) setSpecError(1,"No response from network server.","Please try again later.");
        }
        else if (retBuf.position() == 0) {
            if (specErrorNum == 0) setSpecError(1,"Unable to reach network server.","Please try again later.");
        }
        else {
            return (retBuf);
        }
        return (ByteBuffer.allocate(1));//("Fail"));
    }

}
