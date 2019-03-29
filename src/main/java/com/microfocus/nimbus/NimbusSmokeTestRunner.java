package com.microfocus.nimbus;

import org.junit.runner.*;
import org.junit.runner.notification.*;


public class NimbusSmokeTestRunner {

    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(DevOpsSmokeTest.class);
        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }
        if (result.wasSuccessful()) {
            System.out.println("Test run complete");
        }
    }

}
