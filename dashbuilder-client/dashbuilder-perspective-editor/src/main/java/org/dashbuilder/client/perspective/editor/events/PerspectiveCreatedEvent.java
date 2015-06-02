package org.dashbuilder.client.perspective.editor.events;

public class PerspectiveCreatedEvent {

    protected String perspectiveId;
    protected String perspectiveName;

    public PerspectiveCreatedEvent(String perspectiveId, String name) {
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
