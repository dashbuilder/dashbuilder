package org.dashbuilder.client.widgets.dataset.editor.widgets.editors;

import com.google.gwt.editor.client.EditorError;
import com.google.gwt.user.client.ui.Composite;

import javax.validation.ConstraintViolation;
import java.util.List;

/**
 * <p>This is the default view implementation for Editor widgets .</p>
 *
 * @since 0.3.0 
 */
public abstract class AbstractEditor extends Composite {

    protected Iterable<ConstraintViolation<?>> violations = null;
    
    /**
     * <p>Helper method to consume validation errors that do not belong to this editor.</p>
     * <p>Provided for using it in editors that implement <code>HasEditorErrors</code> interface. It consumes
     *      the unconsumed errors collected by children editors producing that this editor's driver 
     *      will not handle errors not validated by this editor's properties.</p> 
     *
     * @param errors The list or editor's errors to consume.
     */
    protected void consumeErrors(List<EditorError> errors) {
        for (EditorError error : errors) {
            if (error.getEditor().equals(this)) {
                error.setConsumed(true);
            }
        }
    }

    public Iterable<ConstraintViolation<?>> getViolations() {
        return violations;
    }

    public void setViolations(Iterable<ConstraintViolation<?>> violations) {
        this.violations = violations;
    }

    /**
     * <p>Clears the state and the errors for the editor instance.</p>
     */
    public void clear() {
        violations = null;
    }
}
