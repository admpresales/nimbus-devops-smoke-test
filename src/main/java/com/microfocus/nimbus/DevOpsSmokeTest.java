package com.microfocus.nimbus;

import com.google.gson.Gson;
import org.apache.http.auth.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.*;
import org.junit.runner.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import static org.junit.Assert.fail;
import java.util.concurrent.TimeUnit;
import org.json.*;


/**
 * @author Jason Hrabi
 *
 */


public class DevOpsSmokeTest{

    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            System.out.println("Starting test: " + description.getMethodName());
        }
    };

//    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        String s = null;
        String[] dockerCommand = new String[4];
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
    public void AOSWebRootSmokeTest() {
        try {
            HttpClient httpclient = HttpClientBuilder.create().build();
            URL jenkinsUrl = new URL("http://nimbusserver.aos.com:8090/");
            String buildStatus;

            buildStatus = executeJenkinsPipeline(jenkinsUrl,"AOS_Web_Root_Module_Pipeline");

            Assert.assertEquals("SUCCESS", buildStatus);

        } catch(Exception e) {
            e.printStackTrace();
            fail();
        }

    }

    @Test
    public void AOSDAWebRootSmokeTest() {
        try {
            HttpClient httpclient = HttpClientBuilder.create().build();
            URL jenkinsUrl = new URL("http://nimbusserver.aos.com:8090/");
            String buildStatus;

            buildStatus = executeJenkinsPipeline(jenkinsUrl,"AOS_Android_Core_Pipeline");

            Assert.assertEquals("SUCCESS", buildStatus);

        } catch(Exception e) {
            e.printStackTrace();
            fail();
        }

    }

    @Test
    public void AOSAndroidMCSmokeTest() {
        try {
            HttpClient httpclient = HttpClientBuilder.create().build();
            URL jenkinsUrl = new URL("http://nimbusserver.aos.com:8090/");
            String buildStatus;

            buildStatus = executeJenkinsPipeline(jenkinsUrl,"aos-web-da-root-dev-pipeline");

            Assert.assertEquals("SUCCESS", buildStatus);

        } catch(Exception e) {
            e.printStackTrace();
            fail();
        }

    }

    // helper construct to deserialize crumb json into
    public static class CrumbJson {
        public String crumb;
        public String crumbRequestField;
    }

    private static String executeRequest(HttpClient client,
                                   HttpRequestBase request) throws Exception {
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseBody = client.execute(request, responseHandler);
        return responseBody;
    }

    private static CrumbJson getCrumb(URL url) throws Exception {
        HttpClient httpclient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url + "crumbIssuer/api/json");
        String crumbResponse = executeRequest(httpclient, httpGet);
        CrumbJson crumbJson = new Gson().fromJson(crumbResponse, CrumbJson.class);
        return crumbJson;
    }

    private static String executeJenkinsPipeline(URL jenkinsUrl, String pipelineName) throws Exception
    {
        URL jobUrl = new URL(jenkinsUrl + "/job/" + pipelineName + "/");
        HttpClient httpclient = HttpClientBuilder.create().build();
        HttpGet httpGet;
        URL url;
        HttpPost httpost;
        JSONObject obj;
        String buildStatus = "IN_PROGRESS";

        CrumbJson crumbJson = getCrumb (jenkinsUrl);

        url = new URL (jobUrl + "build?delay=0sec");
        httpost = new HttpPost(url.toString());
        httpost.addHeader(crumbJson.crumbRequestField, crumbJson.crumb);
        executeRequest(httpclient, httpost);

        TimeUnit.SECONDS.sleep(5);

        url = new URL (jobUrl + "lastBuild/api/json");
        httpost = new HttpPost(url.toString());
        httpost.addHeader(crumbJson.crumbRequestField, crumbJson.crumb);
        obj = new JSONObject(executeRequest(httpclient, httpost));
        String buildNumber = Integer.toString(obj.getInt("number"));

        url = new URL (jobUrl + buildNumber + "/wfapi/");
        System.out.println("Contacting " + url.toString());
        httpost = new HttpPost(url.toString());
        httpost.addHeader(crumbJson.crumbRequestField, crumbJson.crumb);
        obj = new JSONObject(executeRequest(httpclient, httpost));
        buildStatus = obj.getString("status");

        System.out.println("Build Status: " + buildStatus);

        while (buildStatus.equals("IN_PROGRESS")) {
            TimeUnit.SECONDS.sleep(30);
            System.out.println("Pipeline is still running.");
            obj = new JSONObject(executeRequest(httpclient, httpost));
            buildStatus = obj.getString("status");
        }

        return buildStatus;

    }
}