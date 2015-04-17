package org.dashbuilder.client.widgets.dataset.editor.widgets.events;

/**
 * @since 0.3.0
 */
public class NewDataSetEvent  {

    private String uuid;
    
    public NewDataSetEvent(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }
}