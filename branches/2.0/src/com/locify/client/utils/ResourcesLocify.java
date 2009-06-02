/*
 * ResourcesLocify.java
 * This file is part of Locify.
 *
 * Locify is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * (read more at: http://www.gnu.org/licenses/gpl.html)
 *
 * Commercial licenses are also available, please
 * refer http://code.google.com/p/locify/ for details.
 */
package com.locify.client.utils;

import com.sun.lwuit.Font;
import com.sun.lwuit.Image;
import com.sun.lwuit.plaf.UIManager;
import com.sun.lwuit.util.Resources;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

/**
 *
 * @author menion
 */
public class ResourcesLocify {

    private static boolean localResources = true;

    private static Resources fonts;

//    public static Image getImage(String image) {
//        try {
//            if (images == null) {
//                images = getResource(RESOURCE_NAMES[0]);
//            }
//            if (!image.endsWith(".png"))
//                image += ".png";
//            return images.getImage(image);
//        } catch (IOException ex) {
//            return null;
//        }
//    }

    public static Font getFont(String fontName) {
        try {
            if (fonts == null) {
                // do selection of fonts due to device
                fonts = getResource("fonts");
            }
            return fonts.getFont(fontName);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public static void setTheme() {
        try {
            Resources res = getResource("locify");
            String[] resName = res.getThemeResourceNames();
            if (resName.length > 0)
                UIManager.getInstance().setThemeProps(res.getTheme(resName[0]));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

////    public static void loadMainResource() {
////        try {
////            InputStream stream = Midlet.class().getResourceAsStream("/resource_main.res");
////            Resources r2;
////            if (stream == null) {
////                localResources = false;
////
////                try {
////                    RecordStore.openRecordStore(RESOURCE_NAMES[0], false).closeRecordStore();
////                    Resources r1 = getResource("javaTheme");
//////                    UIManager.getInstance().setThemeProps(r1.getTheme(r1.getThemeResourceNames()[0]));
////
////                    setMainForm(getResource("resources"));
////                    return;
////                } catch (Exception ignor) {
////                    // this exception is expected the first time the application is executed
////                }
////                downloadResources();
////                return;
////            } else {
////                localResources = true;
////                r2 = Resources.open(stream);
////                stream.close();
////            }
////        } catch (Throwable ex) {
////            ex.printStackTrace();
////            Dialog.show("Exception", ex.getMessage(), "OK", null);
////        }
////    }
////
////    /**
////     * Downloads resources for the very first activation of the demo if it is running
////     * with light deployment where the resources are not packaged in the JAR itself
////     */
////    private void downloadResources() {
////        // download resources from the internet and install them in the RMS
////        // while showing a progress indicator
////        Form pleaseWait = new Form("Download");
////        pleaseWait.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
////        pleaseWait.addComponent(new Label("Downloading Resources"));
////        final Label progressInfo = new Label("Starting");
////        progressInfo.setAlignment(Component.CENTER);
////        final Progress prog = new Progress();
////        pleaseWait.addComponent(progressInfo);
////        pleaseWait.addComponent(prog);
////        pleaseWait.show();
////
////        new Thread() {
////            public void run() {
////                try {
////                    byte[] buffer = new byte[4096];
////                    for (int iter = 0; iter < RESOURCE_NAMES.length; iter++) {
////                        progressInfo.setText("Downloading: " + RESOURCE_NAMES[iter]);
////                        byte percent = (byte) (100.0f * ((iter + 0.25f) / ((float) RESOURCE_NAMES.length)));
////                        prog.setProgress(percent);
////                        String url = getAppProperty(RESOURCE_NAMES[iter] + "-url");
////                        InputConnection inputCon = (InputConnection) Connector.open(url, Connector.READ);
////                        InputStream stream = inputCon.openInputStream();
////                        ByteArrayOutputStream output = new ByteArrayOutputStream();
////                        int size = stream.read(buffer);
////                        while (size > -1) {
////                            output.write(buffer, 0, size);
////                            size = stream.read(buffer);
////                        }
////                        stream.close();
////                        inputCon.close();
////                        progressInfo.setText("Storing: " + RESOURCE_NAMES[iter]);
////                        RecordStore store = RecordStore.openRecordStore(RESOURCE_NAMES[iter], true);
////                        byte[] array = output.toByteArray();
////                        store.addRecord(array, 0, array.length);
////                        store.closeRecordStore();
////                    }
////                    progressInfo.setText("Done!");
////                    prog.setProgress((byte) 100);
////                    Resources r1 = getResource("javaTheme");
////                    UIManager.getInstance().setThemeProps(r1.getTheme(r1.getThemeResourceNames()[0]));
////
////                    setMainForm(UIDemoMIDlet.getResource("resources"));
////                } catch (Exception ex) {
////                    ex.printStackTrace();
////                    Dialog.show("Exception", ex.getMessage(), "OK", null);
////                }
////            }
////        }.start();
////    }

    /**
     * Used instead of using the Resources API to allow us to fetch locally downloaded
     * resources
     *
     * @param name the name of the resource
     * @return a resources object
     */
    public static Resources getResource(String name) throws IOException {
        if (localResources) {
            return Resources.open("/" + name + ".res");
        }

        byte[] resourceData = null;
        try {
            resourceData = RecordStore.openRecordStore(name, false).enumerateRecords(null, null, false).nextRecord();
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
            throw new IOException(ex.getMessage());
        }
        return Resources.open(new ByteArrayInputStream(resourceData));
    }
}
