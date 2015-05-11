package org.dashbuilder.client.widgets.dataset.editor.widgets.events;

/**
 * @since 0.3.0
 */
public class DeleteDataSetEvent extends AbstractDataSetEvent<DeleteDataSetEventHandler> {

    public static Type<DeleteDataSetEventHandler> TYPE = new Type<DeleteDataSetEventHandler>();

    public DeleteDataSetEvent(String uuid) {
        super(uuid);
    }

    @Override
    public Type<DeleteDataSetEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(DeleteDataSetEventHandler handler) {
        handler.onDeleteDataSet(this);
    }
}
