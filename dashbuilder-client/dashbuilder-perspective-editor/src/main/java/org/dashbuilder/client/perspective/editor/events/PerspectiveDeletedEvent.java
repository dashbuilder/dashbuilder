package org.dashbuilder.client.perspective.editor.events;

public class PerspectiveDeletedEvent {

    protected String perspectiveId;
    protected String perspectiveName;

    public PerspectiveDeletedEvent() {
    }

    public PerspectiveDeletedEvent(String perspectiveId, String name) {
        this.perspectiveId = perspectiveId;
        this.perspectiveName = name;
    }

    public String getPerspectiveId() {
        return perspectiveId;
    }

    public String getPerspectiveName() {
        return perspectiveName;
    }
}
