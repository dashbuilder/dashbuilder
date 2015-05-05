package org.dashbuilder.client.widgets.dataset.editor.widgets.editors.datacolumn;

import com.github.gwtbootstrap.client.ui.Image;
import java.util.EnumMap;
import org.dashbuilder.client.widgets.resources.i18n.DataSetEditorConstants;
import org.dashbuilder.common.client.validation.editors.DropDownImageListEditor;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.client.resources.bundles.DataSetClientResources;

import java.util.Map;

/**
 * <p>Drop down column types editor specific for data columns. Provider the acceptable values for each column type.</p>
 *
 * @since 0.3.0 
 */
public class DataColumnTypeEditor extends DropDownImageListEditor<ColumnType> {

    public DataColumnTypeEditor() {
        super();
        fireEvents = true;
        setAcceptableValues(buildAcceptableValues(null));
    }

    @Override
    public void setValue(ColumnType value, boolean fireEvents) {
        // Build available column type selector widgets based on current column type.
        final Map<ColumnType, Image> acceptableValues = buildAcceptableValues(value);
        setAcceptableValues(acceptableValues);
        
        // Set editor's value.
        super.setValue(value, fireEvents);
    }

    private Map<ColumnType, Image> buildAcceptableValues(final ColumnType type) {
        final Map<ColumnType, Image> providerEditorValues = new EnumMap<ColumnType, Image>(ColumnType.class);
        if (type != null) {
            if (ColumnType.DATE.equals(type)) {
                final Image dateImage = buildTypeSelectorWidget(ColumnType.DATE);
                final Image labelImage = buildTypeSelectorWidget(ColumnType.LABEL);
                providerEditorValues.put(ColumnType.LABEL, labelImage);
                providerEditorValues.put(ColumnType.DATE, dateImage);
            } else if (ColumnType.LABEL.equals(type)) {
                final Image textImage = buildTypeSelectorWidget(ColumnType.TEXT);
                final Image labelImage = buildTypeSelectorWidget(ColumnType.LABEL);
                providerEditorValues.put(ColumnType.TEXT, textImage);
                providerEditorValues.put(ColumnType.LABEL, labelImage);
            } else if (ColumnType.TEXT.equals(type)) {
                final Image labelImage = buildTypeSelectorWidget(ColumnType.LABEL);
                final Image textImage = buildTypeSelectorWidget(ColumnType.TEXT);
                providerEditorValues.put(ColumnType.LABEL, labelImage);
                providerEditorValues.put(ColumnType.TEXT, textImage);
            } else if (ColumnType.NUMBER.equals(type)) {
                final Image numberImage = buildTypeSelectorWidget(ColumnType.NUMBER);
                providerEditorValues.put(ColumnType.NUMBER, numberImage);
            }
        } 
        return providerEditorValues;
    }

    public static Image buildTypeSelectorWidget(ColumnType type) {
        Image typeIcon = null;
        switch (type) {
            case LABEL:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().labelIcon32().getSafeUri());
                typeIcon.setAltText(DataSetEditorConstants.INSTANCE.label());
                typeIcon.setTitle(DataSetEditorConstants.INSTANCE.label());
                break;
            case TEXT:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().textIcon32().getSafeUri());
                typeIcon.setAltText(DataSetEditorConstants.INSTANCE.text());
                typeIcon.setTitle(DataSetEditorConstants.INSTANCE.text());
                break;
            case NUMBER:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().numberIcon32V3().getSafeUri());
                typeIcon.setAltText(DataSetEditorConstants.INSTANCE.number());
                typeIcon.setTitle(DataSetEditorConstants.INSTANCE.number());
                break;
            case DATE:
                typeIcon = new Image(DataSetClientResources.INSTANCE.images().dateIcon32().getSafeUri());
                typeIcon.setAltText(DataSetEditorConstants.INSTANCE.date());
                typeIcon.setTitle(DataSetEditorConstants.INSTANCE.date());
                break;
        }
        return typeIcon;
    }
}
