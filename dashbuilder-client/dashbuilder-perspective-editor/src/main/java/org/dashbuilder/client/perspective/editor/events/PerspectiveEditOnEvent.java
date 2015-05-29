package org.dashbuilder.client.perspective.editor.events;

import org.uberfire.client.mvp.PerspectiveActivity;

public class PerspectiveEditOnEvent {

    private PerspectiveActivity currentPerspective;

    public PerspectiveEditOnEvent(PerspectiveActivity currentPerspective) {
        this.currentPerspective = currentPerspective;
    }

    public PerspectiveActivity getCurrentPerspective() {
        return currentPerspective;
    }
}
