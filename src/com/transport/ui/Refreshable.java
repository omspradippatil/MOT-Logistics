package com.transport.ui;

/**
 * Refreshable.java - Interface for Dynamic UI Auto-Updating
 *
 * Any panel that implements this interface will automatically
 * reload its database records and dropdown selections when its tab
 * is activated in the Dashboard tabbed pane.
 */
public interface Refreshable {
    /**
     * Reload data, dropdown lists, and tables from the database.
     */
    void refreshData();
}
