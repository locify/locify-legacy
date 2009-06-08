/*
 * LocationEventListener.java
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
package com.locify.client.locator;

/**
 * Interface for location changes
 * @author jiris
 */
public interface LocationEventListener {

    void locationChanged(LocationEventGenerator sender, Location4D location);

    void stateChanged(LocationEventGenerator sender, int state);

    void errorMessage(LocationEventGenerator sender, String message);

    void message(LocationEventGenerator sender, String message);
}
