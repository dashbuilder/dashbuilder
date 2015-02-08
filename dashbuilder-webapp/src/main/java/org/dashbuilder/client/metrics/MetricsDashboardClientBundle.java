package org.dashbuilder.client.metrics;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface MetricsDashboardClientBundle
        extends
        ClientBundle {

    MetricsDashboardClientBundle INSTANCE = GWT.create( MetricsDashboardClientBundle.class );

    @Source("images/computer-icon.gif")
    ImageResource computerIcon();

    @Source("images/back-icon.png")
    ImageResource backIcon();

    @Source("images/table-icon.png")
    ImageResource tableIcon();

    @Source("images/chart-icon.png")
    ImageResource chartIcon();

}
