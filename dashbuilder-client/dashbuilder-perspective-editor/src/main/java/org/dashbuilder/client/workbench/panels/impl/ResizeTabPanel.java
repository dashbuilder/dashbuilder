package org.dashbuilder.client.workbench.panels.impl;

import com.github.gwtbootstrap.client.ui.TabPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import org.uberfire.client.util.Layouts;

public class ResizeTabPanel extends TabPanel implements RequiresResize, ProvidesResize {

    @Override
    public void onResize() {
        // there are two layers of children in a TabPanel: the TabContent and the TabPane.
        // TabContent is just a container for all the TabPane divs, one of which is made visible at a time.
        // For compatibility with GWT LayoutPanel, we have to set both layers of children to fill their parents.
        // We do it in onResize() to get to the TabPanes no matter how they were added.
        for ( Widget child : getChildren() ) {
            Layouts.setToFillParent(child);
            if ( child instanceof RequiresResize ) {
                ((RequiresResize) child).onResize();
            }
            for( Widget grandChild : (HasWidgets) child ) {
                Layouts.setToFillParent( grandChild );
            }
        }
    }
}
