package org.dashbuilder.client.widgets.dataset.editor.widgets.events;

/**
 * <p>Base class for any event that fires in a given context.</p>
 */
public abstract class ContextualEvent {
    private final Object context;

    public ContextualEvent(Object context) {
        this.context = context;
    }

    public Object getContext() {
        return context;
    }
}