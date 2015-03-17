package org.dashbuilder.dataset.client.validation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.validation.client.impl.Validation;
import org.dashbuilder.dataset.client.widgets.editors.DataSetAdvancedAttributesEditor;
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
    interface AdvancedAttributesDriver extends SimpleBeanEditorDriver<DataSetDef, DataSetAdvancedAttributesEditor> {}

    // Create the drivers.
    /**
     * <p>Handles <code>UUID</code> and <code>name</code> data set definition attributes.</p> 
     */
    public final BasicAttributesDriver basicAttributesDriver = GWT.create(BasicAttributesDriver.class);
    /**
     * <p>Handles <code>provider</code> data set definition attribute.</p> 
     */
    public final ProviderTypeAttributesDriver providerTypeAttributesDriver = GWT.create(ProviderTypeAttributesDriver.class);
    /**
     * <p>Handles backend cache, client cache and refresh data set definition related attributes.</p> 
     */
    public final AdvancedAttributesDriver advancedAttributesDriver = GWT.create(AdvancedAttributesDriver.class);

    public void edit(DataSetBasicAttributesEditor view, DataSetDef p) {
        basicAttributesDriver.initialize(view);
        basicAttributesDriver.edit(p);
    }

    public void edit(DataSetProviderTypeEditor view, DataSetDef p) {
        providerTypeAttributesDriver.initialize(view);
        providerTypeAttributesDriver.edit(p);
    }

    public void edit(DataSetAdvancedAttributesEditor view, DataSetDef p) {
        advancedAttributesDriver.initialize(view);
        advancedAttributesDriver.edit(p);
    }

    /**
     * <p>Saves <code>UUID</code> and <code>name</code> data set definition attributes.</p> 
     */
    public Set<ConstraintViolation<DataSetDef>> saveBasicAttributes() {
        return save(basicAttributesDriver);
    }

    /**
     * <p>Saves <code>provider</code> data set definition attribute.</p> 
     */
    public Set<ConstraintViolation<DataSetDef>> saveProviderTypeAttribute() {
        return save(providerTypeAttributesDriver);
    }

    /**
     * <p>Saves backend cache, client cache and refresh data set definition related attributes.</p> 
     */
    public Set<ConstraintViolation<DataSetDef>> saveAdvancedAttributes() {
        return save(advancedAttributesDriver);
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
        Set<ConstraintViolation<DataSetDef>> advancedAttributesViolations = saveAdvancedAttributes();
        if (advancedAttributesViolations != null) violations.addAll(advancedAttributesViolations);
        
        if (violations.isEmpty()) return null;
        return violations;
    }
    
}
