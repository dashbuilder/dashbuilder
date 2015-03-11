package org.dashbuilder.dataset.client.widgets.events;

public class NewDataSetEvent  {

    private String uuid;
    
    public NewDataSetEvent(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }
}