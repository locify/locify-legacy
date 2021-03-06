/*
 * Sms.java
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
package com.locify.client.net;

import com.locify.client.utils.Locale;
import com.locify.client.utils.R;
import com.sun.lwuit.Dialog;
import javax.wireless.messaging.TextMessage;
import javax.wireless.messaging.MessageConnection;
import javax.microedition.io.Connector;

/**
 * Provides sending of SMS messages
 * @author Destil
 */
public class Sms {


    /**
     * Sends and SMS
     * @param number number - has to contain +
     * @param text text to send - maximum 160 chars
     */
    public static void send(String number, String text) {
        try {
            String addr = "sms://"+number;
            MessageConnection conn = (MessageConnection) Connector.open(addr);
            TextMessage msg =(TextMessage) conn.newMessage(MessageConnection.TEXT_MESSAGE);
            msg.setPayloadText(text);
            conn.send(msg);
            R.getCustomAlert().quickView(Locale.get("Sms_sent"), Dialog.TYPE_INFO, "locify://back");
        }catch (SecurityException e)
        {
           R.getBack().goBack();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "Sms.send", number+", "+text);
        }
    }
}
