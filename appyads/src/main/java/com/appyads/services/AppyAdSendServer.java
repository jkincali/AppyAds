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

public class AppyAdSendServer {

    private static final String TAG = "AppyAdSendServer";
    private String mServer;
    private int mPort;
    private String mSendString;
    private int mOp = 0;
    private String[] specError = new String[] {"",""};
    private int specErrorNum = 0;
    public boolean mStatus = false;

    public AppyAdSendServer(int op, String track, String accID, String appID, String campID, String custom, String uspec, String screen) {
        mOp = op;
        mSendString = "|"+op+"|track="+track+"\\account="+accID+"\\app="+appID+"\\campaign="+campID+"\\custom="+custom+"\\user="+uspec+"\\screen="+screen+"\\";
        initVars();
    }

    public AppyAdSendServer(int op, String sendstr) {
        mOp = op;
        mSendString = sendstr;
        initVars();
    }

    private void initVars() {
        mServer = AppyAdService.getHostIP();
        mPort = AppyAdService.getHostPort();
    }

    private void setSpecError(int e, String a, String b) {
        specErrorNum = e;
        specError[0] = a;
        specError[1] = b;
        mStatus = false;
    }

    public String[] getSpecError() {
        return(specError);
    }

    public String getSpecErrorStr() {
        if (specError != null) {
            if (specError.length > 1) return (specError[0]+" - "+specError[1]);
            if (specError.length > 0) return (specError[1]);
        }
        return(null);
    }

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
