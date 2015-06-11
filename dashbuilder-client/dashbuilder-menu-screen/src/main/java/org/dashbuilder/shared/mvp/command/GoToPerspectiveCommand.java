package org.dashbuilder.shared.mvp.command;

import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mvp.Command;

public class GoToPerspectiveCommand implements Command{
    private String activityId;
    private PlaceManager placeManager;

    public GoToPerspectiveCommand(final PlaceManager placeManager, String activityId) {
        this.placeManager = placeManager;
        this.activityId = activityId;
    }

    @Override
    public void execute() {
        if (!isEmpty(activityId)) {
            placeManager.goTo(activityId);
        } 
    }

    public String getActivityId() {
        return activityId;
    }

    private static boolean isEmpty(final String str) {
        return str == null || str.trim().length() == 0;
    }
}
