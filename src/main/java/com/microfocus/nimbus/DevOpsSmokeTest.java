package com.microfocus.nimbus;

import java.io.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * @author Crunchify
 *
 */



public class DevOpsSmokeTest{

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        String s = null;
        String dockerCommand[] = new String[4];
        dockerCommand[0] = "nimbusapp devops:1.1.7.0 -s DEVOPS_TAG=latest up";
        dockerCommand[1] = "nimbusapp intellij:1.1.7.0 -s INTELLIJ_TAG=1.1.7.1 up --force-recreate";
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
            URL url = new URL ("http://nimbusserver.aos.com:8090/job/AOS_Web_Root_Module_Pipeline/build"); // Jenkins URL nimbusserver.aos.com:80900, job named 'nimbus-smoke-test'
            String user = "developer"; // username
            String pass = "developer"; // password or API token
            String authStr = user +":"+  pass;
            String encoding = DatatypeConverter.printBase64Binary(authStr.getBytes("utf-8"));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
//            connection.setRequestProperty("Authorization", "Basic " + encoding);
            InputStream content = connection.getInputStream();
            BufferedReader in   =
                    new BufferedReader (new InputStreamReader (content));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}