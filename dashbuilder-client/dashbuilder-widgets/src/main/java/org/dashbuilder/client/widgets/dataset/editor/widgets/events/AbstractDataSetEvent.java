package org.dashbuilder.client.widgets.dataset.editor.widgets.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import org.dashbuilder.dataset.def.DataSetDef;

/**
 * @param <H> handler type
 * @since 0.3.0
 */
public abstract class AbstractDataSetEvent<H extends EventHandler> extends GwtEvent<H> {
    private DataSetDef dataSetDef;

    public AbstractDataSetEvent(DataSetDef dataSetDef) {
        this.dataSetDef = dataSetDef;
    }

    public DataSetDef getDataSetDef() {
        return dataSetDef;
    }
}
