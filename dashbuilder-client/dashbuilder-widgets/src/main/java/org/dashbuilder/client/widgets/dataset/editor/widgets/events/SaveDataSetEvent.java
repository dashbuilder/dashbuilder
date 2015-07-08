package org.dashbuilder.client.widgets.dataset.editor.widgets.events;

import org.dashbuilder.dataset.def.DataSetDef;
import org.uberfire.workbench.events.UberFireEvent;

/**
 * @since 0.3.0
 */
public class SaveDataSetEvent extends ContextualEvent implements UberFireEvent {

    private final DataSetDef def;

    public SaveDataSetEvent(Object context, DataSetDef def) {
        super(context);
        this.def = def;
    }

    public DataSetDef getDef() {
        return def;
    }

    @Override
    public String toString() {
        return "SaveDataSetEvent [UUID=" + def.getUUID() + "]";
    }
}
