package org.dashbuilder.client.widgets.dataset.editor.widgets.events;

public class EditDataSetEvent extends AbstractDataSetEvent<EditDataSetEventHandler> {

    public static Type<EditDataSetEventHandler> TYPE = new Type<EditDataSetEventHandler>();
    
    public EditDataSetEvent(String uuid) {
        super(uuid);
    }

    @Override
    public Type getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(EditDataSetEventHandler handler) {
        handler.onEditDataSet(this);
    }

}