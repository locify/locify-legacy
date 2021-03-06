/*
 * GeoFileStyleMap.java
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
package com.locify.client.data.items;

/**
 *
 * @author menion
 */
public class GeoFileStyleMap {

    protected String name;
    protected String styleNormalUrl;
    protected GeoFileStyle styleNormal;
    protected String styleHighLightUrl;
    protected GeoFileStyle styleHighLight;

    public GeoFileStyleMap(String name) {
        this.name = name;
    }

    public void setPair(String actualKey, String actualStyleUrl) {
        if (actualKey.equalsIgnoreCase("normal")) {
            styleNormalUrl = actualStyleUrl;
        } else if (actualKey.equalsIgnoreCase("highlight")) {
            styleHighLightUrl = actualStyleUrl;
        }
    }

}
