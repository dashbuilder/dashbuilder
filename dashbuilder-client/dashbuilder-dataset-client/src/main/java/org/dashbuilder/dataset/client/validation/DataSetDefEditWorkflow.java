package org.dashbuilder.dataset.client.validation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.validation.client.impl.Validation;
import org.dashbuilder.dataset.client.widgets.editors.DataSetAdvancedAttributesEditor;
import org.dashbuilder.dataset.client.widgets.editors.DataSetBasicAttributesEditor;
import org.dashbuilder.dataset.client.widgets.editors.DataSetProviderTypeEditor;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.validation.groups.DataSetDefCacheRowsValidation;
import org.dashbuilder.dataset.validation.groups.DataSetDefPushSizeValidation;
import org.dashbuilder.dataset.validation.groups.DataSetDefRefreshIntervalValidation;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
        DataSetDef edited = (DataSetDef) basicAttributesDriver.flush();
        return validate(edited, basicAttributesDriver);
    }

    /**
     * <p>Saves <code>provider</code> data set definition attribute.</p> 
     */
    public Set<ConstraintViolation<DataSetDef>> saveProviderTypeAttribute() {
        DataSetDef edited = (DataSetDef) providerTypeAttributesDriver.flush();
        return validate(edited, providerTypeAttributesDriver);
    }

    /**
     * <p>Saves backend cache, client cache and refresh data set definition related attributes.</p> 
     */
    public Set<ConstraintViolation<DataSetDef>> saveAdvancedAttributes() {
        DataSetDef edited = (DataSetDef) advancedAttributesDriver.flush();
        Set<ConstraintViolation<DataSetDef>> violations =  validate(edited, advancedAttributesDriver);
        if (!existViolations(violations)) {
            // If cache enabled, validate backend max rows value.
            if (edited.isCacheEnabled()) {
                Set<ConstraintViolation<DataSetDef>> cacheViolations =  validate(edited, advancedAttributesDriver, DataSetDefCacheRowsValidation.class);
                if (cacheViolations != null) return cacheViolations;
            }
            // If push enabled, validate push max bytes value.
            if (edited.isPushEnabled()) {
                Set<ConstraintViolation<DataSetDef>> pushViolations =  validate(edited, advancedAttributesDriver, DataSetDefPushSizeValidation.class);
                if (pushViolations != null) return pushViolations;
            }
            // If refresh enabled, validate refresh interval value.
            if (edited.isRefreshAlways()) {
                Set<ConstraintViolation<DataSetDef>> refreshViolations =  validate(edited, advancedAttributesDriver, DataSetDefRefreshIntervalValidation.class);
                if (refreshViolations != null) return refreshViolations;
            }
        }
        return violations;
    }

    private Set<ConstraintViolation<DataSetDef>> validate(DataSetDef def, SimpleBeanEditorDriver driver) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<DataSetDef>> violations = validator.validate(def);
        Set<?> test = violations;
        driver.setConstraintViolations(test);
        if (driver.hasErrors()) {
            return violations;
        }
        return null;
    }
    
    private Set<ConstraintViolation<DataSetDef>> validate(DataSetDef def, SimpleBeanEditorDriver driver,  Class<?>... groups) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<DataSetDef>> violations = validator.validate(def, groups);
        Set<?> test = violations;
        driver.setConstraintViolations(test);
        if (driver.hasErrors()) {
            return violations;
        }
        return null;
    }
    
    private boolean existViolations(Set<ConstraintViolation<DataSetDef>> violations) {
        return violations != null && !violations.isEmpty();
    }

}
