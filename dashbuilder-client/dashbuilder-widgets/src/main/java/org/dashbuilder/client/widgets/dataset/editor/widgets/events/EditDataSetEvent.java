package org.dashbuilder.client.widgets.dataset.editor.widgets.events;

import org.dashbuilder.dataset.def.DataSetDef;

/**
 * @since 0.3.0
 */
public class EditDataSetEvent extends AbstractDataSetEvent<EditDataSetEventHandler> {

    public static Type<EditDataSetEventHandler> TYPE = new Type<EditDataSetEventHandler>();

    public EditDataSetEvent(DataSetDef dataSetDef) {
        super(dataSetDef);
    }

    @Override
    public Type<EditDataSetEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(EditDataSetEventHandler handler) {
        handler.onEditDataSet(this);
    }
}
