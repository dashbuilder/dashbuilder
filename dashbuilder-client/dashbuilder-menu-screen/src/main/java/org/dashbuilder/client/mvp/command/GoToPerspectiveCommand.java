package org.dashbuilder.client.mvp.command;

import com.google.gwt.core.client.GWT;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mvp.Command;

import java.util.Collection;

public class GoToPerspectiveCommand implements Command{
    private String activityId;

    public GoToPerspectiveCommand(String activityId) {
        this.activityId = activityId;
    }

    private static PlaceManager getPlaceManager() {
        Collection<IOCBeanDef<PlaceManager>> beans = IOC.getBeanManager().lookupBeans(PlaceManager.class);
        IOCBeanDef<PlaceManager> beanDef = beans.iterator().next();
        return beanDef.getInstance();
    }
    
    @Override
    public void execute() {
        if (!isEmpty(activityId)) {
            final PlaceManager placeManager = getPlaceManager();
            placeManager.goTo(activityId);
        } else {
            GWT.log("ERROR [GoToPerspectiveCommand]: No activity id specified to navigate.");
        }
    }

    public String getActivityId() {
        return activityId;
    }

    private static boolean isEmpty(final String str) {
        return str == null || str.trim().length() == 0;
    }
}
