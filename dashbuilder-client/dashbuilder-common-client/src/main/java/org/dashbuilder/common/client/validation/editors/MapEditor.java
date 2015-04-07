package org.dashbuilder.common.client.validation.editors;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
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
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Label;
import org.dashbuilder.common.client.resources.i18n.DashbuilderCommonConstants;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    HTMLPanel mainPanel;
    
    @UiField
    ScrollPanel gridPanel;
    
    @UiField
    DataGrid<Map.Entry<T, K>> grid;
    
    @UiField
    Tooltip keyErrorTooltip;
    
    @UiField
    FlowPanel keyPanel;
    
    @UiField
    @Ignore
    com.github.gwtbootstrap.client.ui.TextBox keyBox;

    @UiField
    Tooltip valueErrorTooltip;

    @UiField
    FlowPanel valuePanel;

    @UiField
    @Ignore
    com.github.gwtbootstrap.client.ui.TextBox valueBox;
    
    @UiField
    @Ignore
    Button addButton;
    
    public MapEditor() {
        initWidget(Binder.BINDER.createAndBindUi(this));
        createGrid();
        addButton.addClickHandler(addClickHandler);
    }
    
    private final ClickHandler addClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            String key = keyBox.getValue();
            String value = valueBox.getValue();
            fireEvent(new ValueAddEvent(key, value));
        }
    };
    
    private final FieldUpdater removeButtonHandler = new FieldUpdater<Map.Entry<T, K>, String>() {
        @Override
        public void update(int index, Map.Entry<T, K> object, String value) {
            removeEntry(object.getKey());
            redraw();
        }
    };

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
        redraw();

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

        grid.setEmptyTableWidget(new Label(DashbuilderCommonConstants.INSTANCE.noData()));
        
        // Create KEY column.
        final com.google.gwt.user.cellview.client.Column<Map.Entry<T, K>, String> keyColumn =
                new com.google.gwt.user.cellview.client.Column<Map.Entry<T, K>, String>(new EditTextCell()) {
                    @Override
                    public String getValue(Map.Entry<T, K> object) {
                        return object.getKey().toString();
                    }
                };
        keyColumn.setSortable(false);
        keyColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        grid.addColumn(keyColumn, DashbuilderCommonConstants.INSTANCE.key());
        grid.setColumnWidth(keyColumn, 20, Style.Unit.PCT);
        
        // TODO: Column update handlers.
        /*firstNameColumn.setFieldUpdater(new FieldUpdater<ContactInfo, String>() {
            @Override
            public void update(int index, ContactInfo object, String value) {
                // Called when the user changes the value.
                object.setFirstName(value);
                ContactDatabase.get().refreshDisplays();
            }
        });*/

        // Create Value column.
        final com.google.gwt.user.cellview.client.Column<Map.Entry<T, K>, String> valueColumn =
                new com.google.gwt.user.cellview.client.Column<Map.Entry<T, K>, String>(new EditTextCell()) {
                    @Override
                    public String getValue(Map.Entry<T, K> object) {
                        return object.getValue().toString();
                    }
                };
        valueColumn.setSortable(false);
        valueColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        grid.addColumn(valueColumn, DashbuilderCommonConstants.INSTANCE.value());
        grid.setColumnWidth(valueColumn, 20, Style.Unit.PCT);
        // TODO: Column update handlers.

        // Create remove button column.
        final com.google.gwt.user.cellview.client.Column<Map.Entry<T, K>, String> removeColumn =
                new com.google.gwt.user.cellview.client.Column<Map.Entry<T, K>, String>(new ButtonCell(IconType.MINUS, ButtonSize.MINI)) {

                    @Override
                    public String getValue(Map.Entry<T, K> object) {
                        // return DashbuilderCommonConstants.INSTANCE.remove();
                        return null;
                    }
                };
        
        /*final com.google.gwt.user.cellview.client.Column<Map.Entry<T, K>, Void> removeColumn =
            new com.google.gwt.user.cellview.client.Column<Map.Entry<T, K>, Void>(new IconCell(IconType.MINUS)) {
    
                @Override
                public Void getValue(Map.Entry<T, K> object) {
                    return null;
                }
            };*/
        removeColumn.setFieldUpdater(removeButtonHandler);
        removeColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        grid.addColumn(removeColumn, DashbuilderCommonConstants.INSTANCE.actions());
        grid.setColumnWidth(removeColumn, 20, Style.Unit.PCT);
    }
    
    private K removeEntry(T key) {
        return this.value.remove(key);
    }
    
    public void redraw() {
        grid.setRowCount(value != null ? value.size() : 0);
        grid.setRowData(value != null ? new LinkedList<Map.Entry<T, K>>(value.entrySet()) : new LinkedList<Map.Entry<T, K>>());
        grid.setVisible(true);
        grid.redraw();
    }
    
    private void enableError(final Tooltip tooltip, final Panel panel, String text) {
        setTooltipText(tooltip, text);
        markErrorPanel(panel, true);
    }

    private void disableError() {
        disableError(keyErrorTooltip, keyPanel);
        disableError(valueErrorTooltip, valuePanel);
    }
    
    private void disableError(final Tooltip tooltip, final Panel panel) {
        setTooltipText(tooltip, null);
        markErrorPanel(panel, false);
    }

    public void clear() {
        setValue(null);
    }

    private void markErrorPanel(final Panel panel, boolean error) {
        if (error) {
            panel.addStyleName(style.errorPanelError());
        } else {
            panel.removeStyleName(style.errorPanelError());
        }

    }

    private void setTooltipText(final Tooltip tooltip, final String text) {
        if (text == null || text.trim().length() == 0) {
            tooltip.setText("");
        } else {
            tooltip.setText(text);
        }
        // See issue https://github.com/gwtbootstrap/gwt-bootstrap/issues/287
        tooltip.reconfigure();
    }

    /*
        EVENTS
     */
    
    public HandlerRegistration addValueAddEventHandler(ValueAddEventHandler handler)
    {
        return this.addHandler(handler, ValueAddEvent.TYPE);
    }
    
    public static class ValueAddEvent extends GwtEvent<ValueAddEventHandler> {

        public static GwtEvent.Type<ValueAddEventHandler> TYPE = new GwtEvent.Type<ValueAddEventHandler>();
        
        private String key;
        private String value;

        public ValueAddEvent(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }


        @Override
        public Type<ValueAddEventHandler> getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(ValueAddEventHandler handler) {
            handler.onValueAdd(this);
        }
    }

    public interface ValueAddEventHandler extends EventHandler
    {
        void onValueAdd(ValueAddEvent event);
    }
}
