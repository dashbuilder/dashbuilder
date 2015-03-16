package org.dashbuilder.dataset.client.validation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.validation.client.impl.Validation;
import org.dashbuilder.dataset.client.widgets.editors.DataSetBasicAttributesEditor;
import org.dashbuilder.dataset.client.widgets.editors.DataSetProviderTypeEditor;
import org.dashbuilder.dataset.def.DataSetDef;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.HashSet;
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

    public Set<ConstraintViolation<DataSetDef>> saveBasicAttributes() {
        return save(basicAttributesDriver);
    }

    public Set<ConstraintViolation<DataSetDef>> saveProviderTypeAttribute() {
        return save(providerTypeAttributesDriver);
    }

    private Set<ConstraintViolation<DataSetDef>> save(SimpleBeanEditorDriver driver) {
        DataSetDef edited = (DataSetDef) driver.flush();
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<DataSetDef>> violations = validator.validate(edited);
        Set<?> test = violations;
        driver.setConstraintViolations(test);
        if (driver.hasErrors()) {
            return violations;
        }
        return null;
    }

    public Set<ConstraintViolation<DataSetDef>> saveAll() {
        Set<ConstraintViolation<DataSetDef>> violations = new HashSet<ConstraintViolation<DataSetDef>>();
        Set<ConstraintViolation<DataSetDef>> basicAttributesViolations = saveBasicAttributes();
        if (basicAttributesViolations != null) violations.addAll(basicAttributesViolations);
        Set<ConstraintViolation<DataSetDef>> providerTypeAttributeViolations = saveProviderTypeAttribute();
        if (providerTypeAttributeViolations != null) violations.addAll(providerTypeAttributeViolations);
        
        if (violations.isEmpty()) return null;
        return violations;
    }
    
}
