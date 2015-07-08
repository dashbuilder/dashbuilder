package org.dashbuilder.common.client.validation.editors;

import com.google.gwt.core.client.GWT;
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
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.LabelType;

import java.util.List;

/**
 * <p>Editor component for boolean values.</p>
 * <p>It displays a label:</p>
 * <ul>
 *     <li>Using the text <code>ON</code> and green background color for <code>true</code> value.</li>     
 *     <li>Using the text <code>OFF</code> and red background color for <code>false</code> value.</li> 
 * </ul>
 * <p>The boolean value that this editor handles is switched when clicking on the label.</p> 
 * <p>If validation fails, the validation error messages are displayed by changing border color to RED and showing the message using a tooltip.</p>
 */
public class BooleanSwitchEditor extends Composite implements
                                                   HasValue<Boolean>, HasEditorErrors<Boolean>, IsEditor<TakesValueEditor<Boolean>> {

    public static final String ON = "ON";
    public static final String OFF = "OFF";

    interface Binder extends UiBinder<Widget, BooleanSwitchEditor> {
        Binder BINDER = GWT.create(Binder.class);
    }

    interface BooleanSwitchEditorStyle extends CssResource {
        String mainPanel();
        String mainPanelError();
        String label();
    }

    @UiField
    BooleanSwitchEditorStyle style;

    @UiField
    HTMLPanel mainPanel;
    
    @UiField
    Tooltip errorTooltip;
    
    @UiField
    @Ignore
    Label label;
    
    private boolean isEditMode = true;
    private TakesValueEditor<Boolean> editor = TakesValueEditor.of(this);
    private Boolean value;

    @UiConstructor
    public BooleanSwitchEditor() {
        initWidget(Binder.BINDER.createAndBindUi(this));
        
        // setAcceptableValues((Collection<T>) Arrays.asList(true, false));
        // The click handler for switching the boolean value.
        if (isEditMode) {
            label.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    labelSwitchValue();
                }
            });
        }
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
    }

    @Override
    public TakesValueEditor<Boolean> asEditor()
    {
        if (editor == null) {
            editor = TakesValueEditor.of(this);
        }
        return editor;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public void setValue(Boolean value) {
        setValue(value, false);
    }

    @Override
    public void setValue(Boolean value, boolean fireEvents) {
        if (value == this.value || (this.value != null && this.value.equals(value))) {
            return;
        }
        
        // Disable current error markers, if present.
        disableError();

        final Boolean before = this.value;
        this.value = value;

        if (this.value) labelON();
        else labelOFF();
        
        if (fireEvents) {
            ValueChangeEvent.fireIfNotEqual(this, before, value);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public void showErrors(List<EditorError> errors) {
        boolean hasErrors = errors != null && !errors.isEmpty();

        String toolTipText = null;
        if (hasErrors) {
            StringBuilder sb = new StringBuilder();
            for (EditorError error : errors) {
                sb.append("\n").append(error.getMessage());
            }
            if (sb.length() > 0) toolTipText = sb.substring(1);
        }

        if (toolTipText != null) {
            enableError(toolTipText);
        } else {
            disableError();
        }
    }

    private void enableError(String text) {
        setTooltipText(text);
        markErrorPanel(true);
    }

    private void disableError() {
        setTooltipText(null);
        markErrorPanel(false);
    }

    private void markErrorPanel(boolean error) {
        if (error) {
            mainPanel.addStyleName(style.mainPanelError());
        } else {
            mainPanel.removeStyleName(style.mainPanelError());
        }
    }

    private void labelSwitchValue() {
        if (isLabelON()) setValue(false, true);
        else setValue(true, true);
    }

    private void labelON() {
        label.setText(ON);
        label.setType(LabelType.SUCCESS);
    }

    private void labelOFF() {
        label.setText(OFF);
        label.setType(LabelType.DEFAULT);
    }

    private boolean isLabelON() {
        return ON.equals(label.getText());
    }

    private void setTooltipText(String text) {
        if (text == null || text.trim().length() == 0) {
            errorTooltip.setTitle( "" );
        } else {
            errorTooltip.setTitle( text );
        }
        // See issue https://github.com/gwtbootstrap/gwt-bootstrap/issues/287
        errorTooltip.reconfigure();
    }
    
    public void clear() {
        setValue(false);
        disableError();
    }
    
}
