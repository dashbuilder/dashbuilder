package org.dashbuilder.dataset.client.widgets.editors;

import com.google.gwt.editor.client.EditorError;
import com.google.gwt.user.client.ui.Composite;
import java.util.List;

/**
 * <p>This is the default view implementation for Data Set definition Editor widgets .</p>
 */
public class AbstractDataSetDefEditor extends Composite {

    /**
     * <p>Helper method to consume validation errors that do not belong to this editor.</p>
     * <p>Provided for using it in editors that implement <code>HasEditorErrors</code> interface. It consumes
     *      the unconsumed errors collected by children editors producing that this editor's driver 
     *      will not handle errors not validated by this editor's properties.</p> 
     *
     * @param errors The list or editor's errors to consume.
     */
    protected void consumeErrors(List<EditorError> errors) {
        StringBuilder sb = new StringBuilder();
        for (EditorError error : errors) {
            if (error.getEditor().equals(this)) {
                error.setConsumed(true);
            }
        }
    }
}
