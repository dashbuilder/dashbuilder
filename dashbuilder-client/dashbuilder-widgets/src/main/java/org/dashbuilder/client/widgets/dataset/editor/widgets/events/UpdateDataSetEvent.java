package org.dashbuilder.client.widgets.dataset.editor.widgets.events;

import org.dashbuilder.dataset.def.DataSetDef;
import org.uberfire.workbench.events.UberFireEvent;

/**
 * @since 0.3.0
 */
public class UpdateDataSetEvent extends ContextualEvent implements UberFireEvent {

    private final String uuid;
    private final DataSetDef def;

    public UpdateDataSetEvent(Object context, String uuid, DataSetDef def) {
        super(context);
        this.uuid = uuid;
        this.def = def;
    }

    public DataSetDef getDef() {
        return def;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public String toString() {
        return "UpdateDataSetEvent [UUID=" + uuid + "]";
    }
}
