package com.example.a4agora2.sample;

import android.util.Log;

import com.example.a4agora2.R;
import com.example.a4agora2.media.RtcTokenBuilder;
import com.example.a4agora2.media.RtcTokenBuilder.Role;

import static com.example.a4agora2.Constants.USER_UID;

public class RtcTokenBuilderSample {



    static String appId = "40d6c74319624236bb6a5b05449b2606";
    static String appCertificate = "10d6ea8fbe4d4cc083e0749f0a30bbc6";
    static String channelName = "test-channel";

//    static String userAccount = "2082341273";
//    static int uid = 2082341273;
//    static String userAccount = "0";
//    static int uid = 0;
    static String userAccount = String.valueOf(USER_UID);
    static int uid = USER_UID;
    static int expirationTimeInSeconds = 3600; 

    public static String generateNewAccessToken() {
        RtcTokenBuilder token = new RtcTokenBuilder();
        int timestamp = (int)(System.currentTimeMillis() / 1000 + expirationTimeInSeconds);
        String result = token.buildTokenWithUserAccount(appId, appCertificate,  
        		 channelName, userAccount, Role.Role_Publisher, timestamp);
        System.out.println(result);
        
        result = token.buildTokenWithUid(appId, appCertificate,  
       		 channelName, uid, Role.Role_Publisher, timestamp);
        System.out.println(result);

        Log.d("asmi: new access token",result);
        return result;
    }
}
