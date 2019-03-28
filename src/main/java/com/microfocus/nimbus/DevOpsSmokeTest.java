package com.microfocus.nimbus;

import com.google.gson.Gson;
import org.apache.http.auth.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.fail;


/**
 * @author Crunchify
 *
 */

public class DevOpsSmokeTest{

//    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        String s = null;
        String dockerCommand[] = new String[4];
        dockerCommand[0] = "nimbusapp devops:1.1.7.0 -s DEVOPS_TAG=latest up";
        dockerCommand[1] = "nimbusapp intellij:1.1.7.0 -s INTELLIJ_TAG=1.1.7.1 up";
        dockerCommand[2] = "nimbusapp octane:12.60.35.186 up";
        dockerCommand[3] = "nimbusapp aos:1.1.7 up";

        Process p;
        BufferedReader stdInput;
        BufferedReader stdError;

        for (int i = 0; i < dockerCommand.length; i++) {
            try {
                System.out.println(dockerCommand[i]);
                p = Runtime.getRuntime().exec(dockerCommand[i]);

                stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                // read the output from the command
                while ((s = stdInput.readLine()) != null) {
                    System.out.println(s);
                }

                // read any errors from the attempted command
                while ((s = stdError.readLine()) != null) {
                    System.out.println(s);
                }

            } catch (IOException e) {
                e.printStackTrace();
                fail();
            }
        }
    }

    @Test
    public void DevOpsSmokeTest() {
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            String jenkinsUrl = "http://nimbusserver.aos.com:8090/";
            String jobUrl = jenkinsUrl + "job/AOS_Web_Root_Module_Pipeline/";

            HttpGet httpGet;
            URL url;
            HttpPost httpost;
            httpGet = new HttpGet(jenkinsUrl + "crumbIssuer/api/json");
            String crumbResponse = toString(httpclient, httpGet);
            CrumbJson crumbJson = new Gson().fromJson(crumbResponse, CrumbJson.class);

            url = new URL (jobUrl + "build?delay=0sec");

            httpost = new HttpPost(url.toString());
            httpost.addHeader(crumbJson.crumbRequestField, crumbJson.crumb);
            toString(httpclient, httpost);

            url = new URL (jobUrl + "lastBuild/api/json");

            httpost = new HttpPost(url.toString());
            httpost.addHeader(crumbJson.crumbRequestField, crumbJson.crumb);
            toString(httpclient, httpost);


        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    // helper construct to deserialize crumb json into
    public static class CrumbJson {
        public String crumb;
        public String crumbRequestField;
    }

    private static String toString(DefaultHttpClient client,
                                   HttpRequestBase request) throws Exception {
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseBody = client.execute(request, responseHandler);
        System.out.println(responseBody + "\n");
        return responseBody;
    }
}