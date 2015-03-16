package org.dashbuilder.dataset.client.validation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.validation.client.impl.Validation;
import org.dashbuilder.dataset.client.widgets.editors.DataSetBasicAttributesEditor;
import org.dashbuilder.dataset.client.widgets.editors.DataSetProviderTypeEditor;
import org.dashbuilder.dataset.def.DataSetDef;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

public class DataSetDefEditWorkflow {

    interface BasicAttributesDriver extends SimpleBeanEditorDriver<DataSetDef, DataSetBasicAttributesEditor> {}
    interface ProviderTypeAttributesDriver extends SimpleBeanEditorDriver<DataSetDef, DataSetProviderTypeEditor> {}

    // Create the drivers.
    public final BasicAttributesDriver basicAttributesDriver = GWT.create(BasicAttributesDriver.class);
    public final ProviderTypeAttributesDriver providerTypeAttributesDriver = GWT.create(ProviderTypeAttributesDriver.class);

    public void edit(DataSetBasicAttributesEditor view, DataSetDef p) {
        basicAttributesDriver.initialize(view);
        basicAttributesDriver.edit(p);
    }

    public void edit(DataSetProviderTypeEditor view, DataSetDef p) {
        providerTypeAttributesDriver.initialize(view);
        providerTypeAttributesDriver.edit(p);
    }

    // Called by some UI action
    public Set<ConstraintViolation<DataSetDef>> save() {
        DataSetDef edited = basicAttributesDriver.flush();
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<DataSetDef>> violations = validator.validate(edited);
        Set<?> test = violations;
        basicAttributesDriver.setConstraintViolations((Set<ConstraintViolation<?>>) test);
        if (basicAttributesDriver.hasErrors()) {
            return violations;
        }
        return null;
    }
    
}
