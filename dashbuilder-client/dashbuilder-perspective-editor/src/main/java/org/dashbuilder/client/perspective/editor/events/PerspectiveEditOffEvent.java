package org.dashbuilder.client.perspective.editor.events;

import org.uberfire.client.mvp.PerspectiveActivity;

public class PerspectiveEditOffEvent {

    private PerspectiveActivity currentPerspective;

    public PerspectiveEditOffEvent(PerspectiveActivity currentPerspective) {
        this.currentPerspective = currentPerspective;
    }

    public PerspectiveActivity getCurrentPerspective() {
        return currentPerspective;
    }
}
