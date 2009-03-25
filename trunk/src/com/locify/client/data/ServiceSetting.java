/*
 * ServiceSetting.java
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

package com.locify.client.data;

/**
 * Object for storing service settings
 *
 * @author Destil
 */
public class ServiceSetting {
    private String name;
    private String value;
    private String service;

    public ServiceSetting(String name, String value, String service) {
        this.name = name;
        this.value = value;
        this.service = service;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getService() {
        return service;
    }
}