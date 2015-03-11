package org.dashbuilder.dataset.client.widgets.events;

public class DeleteDataSetEvent extends AbstractDataSetEvent<DeleteDataSetEventHandler> {

    public static Type<DeleteDataSetEventHandler> TYPE = new Type<DeleteDataSetEventHandler>();
    
    public DeleteDataSetEvent(String uuid) {
        super(uuid);
    }

    @Override
    public Type getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(DeleteDataSetEventHandler handler) {
        handler.onDeleteDataSet(this);
    }

}