package org.dashbuilder.client.widgets.dataset.editor.widgets.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * @since 0.3.0
 */
public interface SaveDataSetEventHandler extends EventHandler
{
    void onSaveDataSet(SaveDataSetEvent event);
}