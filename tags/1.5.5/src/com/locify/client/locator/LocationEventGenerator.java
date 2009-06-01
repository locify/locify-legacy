/*
 * LocationEventGenerator.java
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
 * Interface for generators of location
 * @author Jiri Stepan
 */
public interface LocationEventGenerator {

    /**
     * register new liseter. This object shoul be notified about any
     * change of location or state of thos provider
     **/
    public abstract void addLocationChangeListener(LocationEventListener listener);

    /** remove listener **/
    public abstract void removeLocationChangeListener(LocationEventListener listener);

    /** sets new state to all listeners */
    public void notifyChangeStateToListeners();

    /** sets new location to all listeners */
    public void notifyNewLocationToListeners();

    /** notifying standard messages to all listeners*/
    public void notifyMessageToListener(String message);

    /** notifying error messagens to Listeners */
    public void notifyErrorMessageToListeners(String message);
}
