package org.dashbuilder.common.client.validation.editors;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.DataGrid;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.editor.client.HasEditorErrors;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

import java.util.*;

public class MapEditor<T, K> extends Composite implements
        HasValue<Map<T,K>>, HasEditorErrors<Map<T,K>>, IsEditor<TakesValueEditor<Map<T,K>>> {

    interface MapEditorStyle extends CssResource {
        String errorPanelError();
    }

    interface Binder extends UiBinder<Widget, MapEditor> {
        Binder BINDER = GWT.create(Binder.class);
    }
    
    private Map<T, K> value;
    private TakesValueEditor<Map<T, K>> editor;

    @UiField
    MapEditorStyle style;
    
    @UiField
    HTMLPanel errorPanel;
    
    @UiField
    FlowPanel gridPanel;
    
    @UiField
    DataGrid<Map.Entry<T, K>> grid;
    
    @UiField
    Tooltip errorTooltip;
    
    @UiField
    FlowPanel mainPanel;

    public MapEditor() {
        initWidget(Binder.BINDER.createAndBindUi(this));
        createGrid();
        createMainPanel();
    }

    @Override
    public void showErrors(List<EditorError> errors) {
        // TODO
    }

    @Override
    public Map<T, K> getValue() {
        return value;
    }

    @Override
    public void setValue(Map<T, K> value) {
        setValue(value, false);
    }

    @Override
    public void setValue(Map<T, K> value, boolean fireEvents) {
        /*if (value == this.value || (this.value != null && this.value.equals(value))) {
            return;
        }*/

        // Disable current error markers, if present.
        disableError();

        final Map<T, K> before = this.value;
        this.value = value;

        // Fill grid values.
        fillGrid();

        // Fire events, if necessary.
        if (fireEvents) {
            ValueChangeEvent.fireIfNotEqual(this, before, value);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Map<T, K>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public TakesValueEditor<Map<T, K>> asEditor() {
        if (editor == null) {
            editor = TakesValueEditor.of(this);
        }
        return editor;
    }

    private void createGrid() {

        grid.setEmptyTableWidget(new Label("No value pairs."));
        
        // Create KEY column.
        com.google.gwt.user.cellview.client.Column<Map.Entry<T, K>, String> keyColumn =
                new com.google.gwt.user.cellview.client.Column<Map.Entry<T, K>, String>(new EditTextCell()) {
                    @Override
                    public String getValue(Map.Entry<T, K> object) {
                        return object.getKey().toString();
                    }
                };
        keyColumn.setSortable(false);
        grid.addColumn(keyColumn, "Key");
        grid.setColumnWidth(keyColumn, 20, Style.Unit.PCT);
        
        /*firstNameColumn.setFieldUpdater(new FieldUpdater<ContactInfo, String>() {
            @Override
            public void update(int index, ContactInfo object, String value) {
                // Called when the user changes the value.
                object.setFirstName(value);
                ContactDatabase.get().refreshDisplays();
            }
        });*/

        // Create Value column.
        com.google.gwt.user.cellview.client.Column<Map.Entry<T, K>, String> valueColumn =
                new com.google.gwt.user.cellview.client.Column<Map.Entry<T, K>, String>(new EditTextCell()) {
                    @Override
                    public String getValue(Map.Entry<T, K> object) {
                        return object.getValue().toString();
                    }
                };
        valueColumn.setSortable(false);
        grid.addColumn(valueColumn, "Value");
        grid.setColumnWidth(valueColumn, 20, Style.Unit.PCT);

        // Remove entry button.
        final com.github.gwtbootstrap.client.ui.Button removeButton = new Button("Remove", IconType.MINUS, new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // TODO
            }
        });
        // TODO: add remove button as a grid column.
    }
    
    private void fillGrid() {
        grid.setRowCount(value != null ? value.size() : 0);
        grid.setRowData(value != null ? new LinkedList<Map.Entry<T, K>>(value.entrySet()) : new LinkedList<Map.Entry<T, K>>());
    }
    
    private void createMainPanel() {

        // Clear current widgets.
        mainPanel.clear();;
        
        final Panel newPairPanel = createValuePairEditors();
        mainPanel.add(newPairPanel);
        
        // Add entry button.
        final com.github.gwtbootstrap.client.ui.Button addButton = new Button("Add", IconType.PLUS, new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // TODO
            }
        });
        mainPanel.add(addButton);
    }
    
    private Panel createValuePairEditors() {
        final FlowPanel result = new FlowPanel();
        final TextBox keyBox = new TextBox();
        final TextBox valueBox = new TextBox();
        
        result.add(keyBox);
        result.add(valueBox);
        return result;
    }

    private void enableError(String text) {
        setTooltipText(text);
        markErrorPanel(true);
    }

    private void disableError() {
        setTooltipText(null);
        markErrorPanel(false);
    }

    public void clear() {
        setValue(null);
    }

    private void markErrorPanel(boolean error) {
        if (error) {
            errorPanel.addStyleName(style.errorPanelError());
        } else {
            errorPanel.removeStyleName(style.errorPanelError());
        }

    }

    private void setTooltipText(String text) {
        if (text == null || text.trim().length() == 0) {
            errorTooltip.setText("");
        } else {
            errorTooltip.setText(text);
        }
        // See issue https://github.com/gwtbootstrap/gwt-bootstrap/issues/287
        errorTooltip.reconfigure();
    }
        
}
