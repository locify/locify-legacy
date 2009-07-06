/*
 * StoreManagerMapInfo.java
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
package com.locify.client.maps.fileMaps;

/**
 *
 * @author menion
 */
public class StoreManagerMapInfo {
    public String mapName;
    public int mapZoom;

    public StoreManagerMapInfo(String mapName, int mapZoom) {
        this.mapName = mapName;
        this.mapZoom = mapZoom;
    }
}
