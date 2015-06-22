package org.dashbuilder.client.widgets.dataset.editor.widgets.events;

import org.dashbuilder.dataset.def.DataSetDef;

/**
 * @since 0.3.0
 */
public class SaveDataSetEvent extends AbstractDataSetEvent<SaveDataSetEventHandler> {

    public static Type<SaveDataSetEventHandler> TYPE = new Type<SaveDataSetEventHandler>();

    public SaveDataSetEvent(DataSetDef dataSetDef) {
        super(dataSetDef);
    }

    @Override
    public Type<SaveDataSetEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SaveDataSetEventHandler handler) {
        handler.onSaveDataSet(this);
    }
}
