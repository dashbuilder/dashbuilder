package org.dashbuilder.client.widgets.dataset.editor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.*;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.bean.BeanDataSetDefAttributesEditor;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.csv.CSVDataSetDefAttributesEditor;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.elasticsearch.ELDataSetDefAttributesEditor;
import org.dashbuilder.client.widgets.dataset.editor.widgets.editors.sql.SQLDataSetDefAttributesEditor;
import org.dashbuilder.dataset.def.*;
import org.dashbuilder.dataset.validation.groups.*;
import org.dashbuilder.validations.ValidatorFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.*;

public final class DataSetDefEditWorkflow {

    public DataSetDefEditWorkflow() {
        
    }

    interface BasicAttributesDriver extends SimpleBeanEditorDriver<DataSetDef, DataSetBasicAttributesEditor> {}
    interface ProviderTypeAttributesDriver extends SimpleBeanEditorDriver<DataSetDef, DataSetProviderTypeEditor> {}
    interface AdvancedAttributesDriver extends SimpleBeanEditorDriver<DataSetDef, DataSetAdvancedAttributesEditor> {}
    interface FilterColumnsDriver extends SimpleBeanEditorDriver<DataSetDef, DataSetFilterColumnsEditor> {}
    interface SQLAttributesDriver extends SimpleBeanEditorDriver<SQLDataSetDef, SQLDataSetDefAttributesEditor> {}
    interface CSVAttributesDriver extends SimpleBeanEditorDriver<CSVDataSetDef, CSVDataSetDefAttributesEditor> {}
    interface BeanAttributesDriver extends SimpleBeanEditorDriver<BeanDataSetDef, BeanDataSetDefAttributesEditor> {}
    interface ELAttributesDriver extends SimpleBeanEditorDriver<ElasticSearchDataSetDef, ELDataSetDefAttributesEditor> {}

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
     * <p>Handles initial filter and columns for the data set.</p> 
     */
    public final FilterColumnsDriver filterColumnsDriver = GWT.create(FilterColumnsDriver.class);
    
    /**
     * <p>Handles SQL specific data set definition attributes.</p> 
     */
    public final SQLAttributesDriver sqlAttributesDriver = GWT.create(SQLAttributesDriver.class);

    /**
     * <p>Handles CSV specific data set definition attributes.</p> 
     */
    public final CSVAttributesDriver csvAttributesDriver = GWT.create(CSVAttributesDriver.class);

    /**
     * <p>Handles Bean specific data set definition attributes.</p> 
     */
    public final BeanAttributesDriver beanAttributesDriver = GWT.create(BeanAttributesDriver.class);

    /**
     * <p>Handles ElasticSearch specific data set definition attributes.</p> 
     */
    public final ELAttributesDriver elAttributesDriver = GWT.create(ELAttributesDriver.class);

    private DataSetBasicAttributesEditor basicAttributesEditor = null;
    private DataSetProviderTypeEditor providerTypeAttributeEditor = null;
    private DataSetAdvancedAttributesEditor advancedAttributesEditor = null;
    private DataSetFilterColumnsEditor filterColumnsEditor = null;
    private SQLDataSetDefAttributesEditor sqlAttributesEditor = null;
    private BeanDataSetDefAttributesEditor beanAttributesEditor = null;
    private CSVDataSetDefAttributesEditor csvAttributesEditor = null;
    private ELDataSetDefAttributesEditor elasticSearchAttributesEditor = null;
    
    public DataSetDefEditWorkflow edit(final DataSetBasicAttributesEditor view, final DataSetDef p) {
        basicAttributesDriver.initialize(view);
        basicAttributesDriver.edit(p);
        basicAttributesEditor = view;
        return this;
    }

    public DataSetDefEditWorkflow edit(final DataSetProviderTypeEditor view, final DataSetDef p) {
        providerTypeAttributesDriver.initialize(view);
        providerTypeAttributesDriver.edit(p);
        providerTypeAttributeEditor = view;
        return this;
    }

    public DataSetDefEditWorkflow edit(final DataSetAdvancedAttributesEditor view, final DataSetDef p) {
        advancedAttributesDriver.initialize(view);
        advancedAttributesDriver.edit(p);
        advancedAttributesEditor = view;
        return this;
    }

    public DataSetDefEditWorkflow edit(final DataSetFilterColumnsEditor view, final DataSetDef p) {
        filterColumnsDriver.initialize(view);
        filterColumnsDriver.edit(p);
        filterColumnsEditor = view;
        return this;
    }

    public DataSetDefEditWorkflow edit(final SQLDataSetDefAttributesEditor view, final SQLDataSetDef p) {
        sqlAttributesDriver.initialize(view);
        sqlAttributesDriver.edit(p);
        sqlAttributesEditor = view;
        return this;
    }

    public DataSetDefEditWorkflow edit(final CSVDataSetDefAttributesEditor view, final CSVDataSetDef p) {
        csvAttributesDriver.initialize(view);
        csvAttributesDriver.edit(p);
        csvAttributesEditor = view;
        return this;
    }

    public DataSetDefEditWorkflow edit(final BeanDataSetDefAttributesEditor view, final BeanDataSetDef p) {
        beanAttributesDriver.initialize(view);
        beanAttributesDriver.edit(p);
        beanAttributesEditor = view;
        return this;
    }

    public DataSetDefEditWorkflow edit(final ELDataSetDefAttributesEditor view, final ElasticSearchDataSetDef p) {
        elAttributesDriver.initialize(view);
        elAttributesDriver.edit(p);
        elasticSearchAttributesEditor = view;
        return this;
    }
    
    public DataSetDefEditWorkflow save() {
        if (basicAttributesEditor != null) saveBasicAttributes();
        if (providerTypeAttributeEditor != null) saveProviderTypeAttribute();
        if (advancedAttributesEditor != null) saveAdvancedAttributes();
        if (sqlAttributesEditor != null) saveSQLAttributes();
        if (csvAttributesEditor != null) saveCSVAttributes();
        if (filterColumnsEditor != null) saveFilterColumns();
        return this;
    }

    /**
     * <p>Saves <code>UUID</code> and <code>name</code> data set definition attributes.</p> 
     */
    private DataSetDefEditWorkflow saveBasicAttributes() {
        DataSetDef edited = (DataSetDef) basicAttributesDriver.flush();
        return validate(edited, basicAttributesEditor, basicAttributesDriver);
    }

    /**
     * <p>Saves <code>provider</code> data set definition attribute.</p> 
     */
    private DataSetDefEditWorkflow saveProviderTypeAttribute() {
        DataSetDef edited = (DataSetDef) providerTypeAttributesDriver.flush();
        return validate(edited, providerTypeAttributeEditor, providerTypeAttributesDriver);
    }

    /**
     * <p>Saves backend cache, client cache and refresh data set definition related attributes.</p> 
     */
    private DataSetDefEditWorkflow saveAdvancedAttributes() {
        DataSetDef edited = (DataSetDef) advancedAttributesDriver.flush();
        validate(edited, advancedAttributesEditor, advancedAttributesDriver);
        List<Class<?>> groups = new LinkedList<Class<?>>();
        if (edited.isCacheEnabled()) groups.add(DataSetDefCacheRowsValidation.class);
        if (edited.isPushEnabled()) groups.add(DataSetDefPushSizeValidation.class);
        if (edited.isRefreshAlways()) groups.add(DataSetDefRefreshIntervalValidation.class);

        // Validate custom groups, if necessary.
        if (!groups.isEmpty()) {
            validate(edited, advancedAttributesEditor, advancedAttributesDriver, groups.toArray(new Class[groups.size()]));
        }

        return this;
    }

    /**
     * <p>Handles initial filter and columns for the data set.</p> 
     */
    private DataSetDefEditWorkflow saveFilterColumns() {
        DataSetDef edited = (DataSetDef) filterColumnsDriver.flush();
        return validate(edited, filterColumnsEditor, filterColumnsDriver);
    }

    /**
     * <p>Saves sql data set definition attributes.</p> 
     */
    private DataSetDefEditWorkflow saveSQLAttributes() {
        SQLDataSetDef edited = sqlAttributesDriver.flush();
        return validateSQL(edited, sqlAttributesEditor, sqlAttributesDriver);
    }

    /**
     * <p>Saves CSV data set definition attributes.</p> 
     */
    private DataSetDefEditWorkflow saveCSVAttributes() {
        CSVDataSetDef edited = csvAttributesDriver.flush();
        if (csvAttributesEditor.isUsingFilePath()) return validateCSV(edited, csvAttributesEditor, csvAttributesDriver, CSVDataSetDefFilePathValidation.class);
        else if (csvAttributesEditor.isUsingFileURL()) return validateCSV(edited, csvAttributesEditor, csvAttributesDriver, CSVDataSetDefFileURLValidation.class);
        return this;
    }

    private DataSetDefEditWorkflow validateSQL(final SQLDataSetDef def, final AbstractDataSetDefEditor editor, final SimpleBeanEditorDriver driver) {
        final Validator validator = ValidatorFactory.getSQLDataSetDefValidator();
        final Set<ConstraintViolation<SQLDataSetDef>> violations = validator.validate(def);
        final Set<?> test = violations;
        setViolations(editor, driver, (Iterable<ConstraintViolation<?>>) test);
        return this;
    }

    private DataSetDefEditWorkflow validateCSV(final CSVDataSetDef def, final AbstractDataSetDefEditor editor, final SimpleBeanEditorDriver driver, final Class<?>... groups) {
        final Validator validator = ValidatorFactory.getCSVDataSetDefValidator();
        final Set<ConstraintViolation<CSVDataSetDef>> violations = validator.validate(def, groups);
        final Set<?> test = violations;
        setViolations(editor, driver, (Iterable<ConstraintViolation<?>>) test);
        return this;
    }
    
    private DataSetDefEditWorkflow validate(final DataSetDef def, final AbstractDataSetDefEditor editor, final SimpleBeanEditorDriver driver) {
        final Validator validator = ValidatorFactory.getDataSetDefValidator();
        final Set<ConstraintViolation<DataSetDef>> violations = validator.validate(def);
        final Set<?> test = violations;
        setViolations(editor, driver, (Iterable<ConstraintViolation<?>>) test);
        return null;
    }
    
    private DataSetDefEditWorkflow validate(final DataSetDef def, final AbstractDataSetDefEditor editor, final SimpleBeanEditorDriver driver,  final Class<?>... groups) {
        final Validator validator = ValidatorFactory.getDataSetDefValidator();
        final Set<ConstraintViolation<DataSetDef>> violations = validator.validate(def, groups);
        final Set<?> test = violations;
        setViolations(editor, driver, (Iterable<ConstraintViolation<?>>) test);
        return this;
    }
    
    private void setViolations(final AbstractDataSetDefEditor editor, final SimpleBeanEditorDriver driver, final Iterable<ConstraintViolation<?>> violations) {
        driver.setConstraintViolations(violations);
        if (driver.hasErrors()) {
            editor.setViolations(violations);
        } else {
            editor.setViolations(null);
        }
        
    }
    
    public DataSetDefEditWorkflow clear() {
        basicAttributesEditor = null;
        providerTypeAttributeEditor = null;
        advancedAttributesEditor = null;
        sqlAttributesEditor = null;
        csvAttributesEditor = null;
        return this;
    }
}
