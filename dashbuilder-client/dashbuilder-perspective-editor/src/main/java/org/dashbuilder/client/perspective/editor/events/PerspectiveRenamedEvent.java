package org.dashbuilder.client.perspective.editor.events;

public class PerspectiveRenamedEvent {

    protected String perspectiveOldId;
    protected String perspectiveNewId;
    protected String perspectiveName;

    public PerspectiveRenamedEvent(String perspectiveOldId, String perspectiveNewId, String perspectiveName) {
        this.perspectiveOldId = perspectiveOldId;
        this.perspectiveNewId = perspectiveNewId;
        this.perspectiveName = perspectiveName;
    }

    public String getPerspectiveOldId() {
        return perspectiveOldId;
    }

    public String getPerspectiveNewId() {
        return perspectiveNewId;
    }

    public String getPerspectiveName() {
        return perspectiveName;
    }
}
