package com.locify.client.gui;

import android.test.ActivityInstrumentationTestCase;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.locify.client.gui.MidletTest \
 * com.locify.client.gui.tests/android.test.InstrumentationTestRunner
 */
public class MidletTest extends ActivityInstrumentationTestCase<Midlet> {

    public MidletTest() {
        super("com.locify.client.gui", Midlet.class);
    }

}
