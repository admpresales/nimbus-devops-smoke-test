package com.microfocus.nimbus;

import java.io.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;


/**
 * @author Crunchify
 *
 */


public class DevOpsSmokeTest{
    @Test
    public void testingCrunchifyAddition() {
        String s = null;
        String dockerCommand[] = new String[4];
        dockerCommand[0] = "nimbusapp devops:1.1.7.0 -s DEVOPS_TAG=latest up";
        dockerCommand[1] = "nimbusapp leanft-chrome:14.52 up";
        dockerCommand[2] = "nimbusapp octane:12.60.35.186 -s OCTANE_TAG=latest up";
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
}