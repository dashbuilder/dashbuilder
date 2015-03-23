package org.dashbuilder.dataset.client.widgets.editors;

import com.google.gwt.editor.client.EditorError;
import com.google.gwt.user.client.ui.Composite;

import javax.enterprise.context.Dependent;
import java.util.List;

/**
 * <p>This is the default view implementation for Data Set definition Editor widgets .</p>
 */
@Dependent
public class AbstractDataSetDefEditor extends Composite {

    protected void consumeErrors(List<EditorError> errors) {
        StringBuilder sb = new StringBuilder();
        for (EditorError error : errors) {
            if (error.getEditor().equals(this)) {
                // sb.append("\n").append(error.getMessage());
                error.setConsumed(true);
            }
        }

        /*if (sb.length() == 0) {
            GWT.log("showErrors - No error");
            return;
        }
        GWT.log("showErrors - Error: " + sb.substring(1));*/

    }
}
