package org.dashbuilder.common.client.validation.editors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.editor.client.HasEditorErrors;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.extras.toggleswitch.client.ui.ToggleSwitch;

import java.util.List;

/**
 * <p>Editor component for boolean values.</p>
 * <p>The boolean value that this editor handles is switched when the user clicks on component.</p> 
 * <p>If validation fails, the validation error messages are displayed by changing border color to RED and showing the message using a tooltip.</p>
 * <p>It uses the ToggleSwitch component from GWT Bootstrap 3 extras.</p>
 * @see <a href="https://gwtbootstrap3.github.io/gwtbootstrap3-demo/#toggleswitch">Toggle Switch offical doc</a>
 * 
 * @since 0.3.0
 */
public class BooleanSwitchEditor extends AbstractEditorDecorator implements
                                                   HasValue<Boolean>, HasEditorErrors<Boolean>, IsEditor<TakesValueEditor<Boolean>> {

    public static final String ON = "ON";
    public static final String OFF = "OFF";

    interface Binder extends UiBinder<Widget, BooleanSwitchEditor> {
        Binder BINDER = GWT.create(Binder.class);
    }

    @UiField
    @Ignore
    HTMLPanel mainPanel;
    
    @UiField
    @Ignore
    Tooltip errorTooltip;
    
    @UiField
    @Ignore
    ToggleSwitch toggleSwitch;
    
    private boolean isEditMode = true;
    private TakesValueEditor<Boolean> editor = TakesValueEditor.of(this);
    private Boolean value;

    @UiConstructor
    public BooleanSwitchEditor() {
        initWidget(Binder.BINDER.createAndBindUi(this));
        
        // The click handler for switching the boolean value.
        if (isEditMode) {
            toggleSwitch.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> valueChangeEvent) {
                    Boolean isOn = valueChangeEvent.getValue();
                    setValue(isOn, true);
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

        if (this.value) {
            toggleSwitch.setValue(true);
        } else {
            toggleSwitch.setValue(false);
        }
        
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
        _showErrors(errors);
    }

    @Override
    public void setErrorLabelPosition(ErrorLabelPosition errorLabelPosition) {
        super.setErrorLabelPosition(errorLabelPosition);
        doPositionErrorTooltip(errorTooltip);
    }

    protected void enableError(String text) {
        setTooltipText(errorTooltip, text);
        markErrorPanel(mainPanel, true);
    }

    protected void disableError() {
        setTooltipText(errorTooltip, null);
        markErrorPanel(mainPanel, false);
    }

    public void clear() {
        setValue(false);
        disableError();
    }
    
}
