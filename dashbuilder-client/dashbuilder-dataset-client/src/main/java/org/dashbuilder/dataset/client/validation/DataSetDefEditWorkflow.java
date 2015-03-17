package org.dashbuilder.dataset.client.validation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import org.dashbuilder.dataset.client.widgets.editors.DataSetAdvancedAttributesEditor;
import org.dashbuilder.dataset.client.widgets.editors.DataSetBasicAttributesEditor;
import org.dashbuilder.dataset.client.widgets.editors.DataSetProviderTypeEditor;
import org.dashbuilder.dataset.client.widgets.editors.sql.SQLDataSetDefAttributesEditor;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.SQLDataSetDef;
import org.dashbuilder.dataset.validation.groups.DataSetDefCacheRowsValidation;
import org.dashbuilder.dataset.validation.groups.DataSetDefPushSizeValidation;
import org.dashbuilder.dataset.validation.groups.DataSetDefRefreshIntervalValidation;
import org.dashbuilder.validations.ValidatorFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.LinkedHashSet;
import java.util.Set;

public class DataSetDefEditWorkflow {

    interface BasicAttributesDriver extends SimpleBeanEditorDriver<DataSetDef, DataSetBasicAttributesEditor> {}
    interface ProviderTypeAttributesDriver extends SimpleBeanEditorDriver<DataSetDef, DataSetProviderTypeEditor> {}
    interface AdvancedAttributesDriver extends SimpleBeanEditorDriver<DataSetDef, DataSetAdvancedAttributesEditor> {}
    interface SQLAttributesDriver extends SimpleBeanEditorDriver<SQLDataSetDef, SQLDataSetDefAttributesEditor> {}

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

    /**
     * <p>Handles SQL specific data set definition attributes.</p> 
     */
    public final SQLAttributesDriver sqlAttributesDriver = GWT.create(SQLAttributesDriver.class);
    
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

    public void edit(SQLDataSetDefAttributesEditor view, SQLDataSetDef p) {
        sqlAttributesDriver.initialize(view);
        sqlAttributesDriver.edit(p);
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
        Set<ConstraintViolation<DataSetDef>> violations =  new LinkedHashSet<ConstraintViolation<DataSetDef>>();
        Set<ConstraintViolation<DataSetDef>> advViolations = validate(edited, advancedAttributesDriver);
        if (advViolations != null) violations.addAll(advViolations);
        // If cache enabled, validate backend max rows value.
        if (edited.isCacheEnabled()) {
            Set<ConstraintViolation<DataSetDef>> cacheViolations =  validate(edited, advancedAttributesDriver, DataSetDefCacheRowsValidation.class);
            if (cacheViolations != null) violations.addAll(cacheViolations);
        }
        // If push enabled, validate push max bytes value.
        if (edited.isPushEnabled()) {
            Set<ConstraintViolation<DataSetDef>> pushViolations =  validate(edited, advancedAttributesDriver, DataSetDefPushSizeValidation.class);
            if (pushViolations != null) violations.addAll(pushViolations);
        }
        // If refresh enabled, validate refresh interval value.
        if (edited.isRefreshAlways()) {
            Set<ConstraintViolation<DataSetDef>> refreshViolations =  validate(edited, advancedAttributesDriver, DataSetDefRefreshIntervalValidation.class);
            if (refreshViolations != null) violations.addAll(refreshViolations);
        }
        return violations;
    }

    /**
     * <p>Saves sql data set definition attributes.</p> 
     */
    public Set<ConstraintViolation<SQLDataSetDef>> saveSQLAttributes() {
        SQLDataSetDef edited = sqlAttributesDriver.flush();
        return validateSQL(edited, sqlAttributesDriver);
    }

    private Set<ConstraintViolation<SQLDataSetDef>> validateSQL(SQLDataSetDef def, SimpleBeanEditorDriver driver) {
        Validator validator = ValidatorFactory.getSQLDataSetDefValidator();
        Set<ConstraintViolation<SQLDataSetDef>> violations = validator.validate(def);
        Set<?> test = violations;
        driver.setConstraintViolations(test);
        if (driver.hasErrors()) {
            return violations;
        }
        return null;
    }
    
    private Set<ConstraintViolation<DataSetDef>> validate(DataSetDef def, SimpleBeanEditorDriver driver) {
        Validator validator = ValidatorFactory.getDataSetDefValidator();
        Set<ConstraintViolation<DataSetDef>> violations = validator.validate(def);
        Set<?> test = violations;
        driver.setConstraintViolations(test);
        if (driver.hasErrors()) {
            return violations;
        }
        return null;
    }
    
    private Set<ConstraintViolation<DataSetDef>> validate(DataSetDef def, SimpleBeanEditorDriver driver,  Class<?>... groups) {
        Validator validator = ValidatorFactory.getDataSetDefValidator();
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
