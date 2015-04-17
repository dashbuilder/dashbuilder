package org.dashbuilder.client.widgets.dataset.editor.widgets.editors;

import com.google.gwt.editor.client.EditorError;
import com.google.gwt.user.client.ui.Composite;
import org.dashbuilder.dataset.def.DataSetDef;

import javax.validation.ConstraintViolation;
import java.util.List;

/**
 * <p>This is the default view implementation for Data Set definition Editor widgets .</p>
 *
 * @since 0.3.0 
 */
public class AbstractDataSetDefEditor extends AbstractEditor {

    protected DataSetDef dataSetDef;
    
    public void set(DataSetDef dataSetDef) {
        this.dataSetDef = dataSetDef;
    }
    
}
