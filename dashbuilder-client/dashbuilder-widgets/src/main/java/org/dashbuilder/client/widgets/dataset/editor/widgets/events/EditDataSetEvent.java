package org.dashbuilder.client.widgets.dataset.editor.widgets.events;

import org.dashbuilder.dataset.def.DataSetDef;
import org.jboss.errai.security.shared.api.identity.User;
import org.uberfire.workbench.events.UberFireEvent;

public class EditDataSetEvent implements UberFireEvent {

    private final DataSetDef def;

    public EditDataSetEvent(final DataSetDef def) {
        this.def = def;
    }

    public DataSetDef getDef() {
        return def;
    }

    @Override
    public String toString() {
        return "EditDataSetEvent [UUID=" + def.getUUID() + "]";
    }

}
