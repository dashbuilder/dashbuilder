package org.dashbuilder.common.client.validation.editors;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.Image;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
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
import com.google.gwt.user.client.ui.*;

import java.util.*;

/**
 * <p>Editor component that accepts multiple values and display each one using a given button in a dropdown button grup.</p>
 * <p>The validation error messages are displayed by changing border color to RED and showing the message using a tooltip.</p>
 * <p>This component is ideal for handling enums.</p>
 *  
 * @param <T> The type of the value that contains the editor widget
 */
public class DropDownImageListEditor<T> extends Composite implements
        HasConstrainedValue<T>, HasEditorErrors<T>, IsEditor<TakesValueEditor<T>> {

    interface Binder extends UiBinder<Widget, DropDownImageListEditor> {
        Binder BINDER = GWT.create(Binder.class);
    }

    interface DropDownImageListEditorStyle extends CssResource {
        String errorPanel();
        String errorPanelError();
        String imagePointer();
    }
    
    private final List<T> values = new ArrayList<T>();
    private TakesValueEditor<T> editor;
    private final Map<T ,Image> images = new LinkedHashMap<T, Image>();
    private T value;
    private  boolean isEditMode;
    private int width = -1;
    private int height = -1;
    protected boolean fireEvents = false;

    @UiField
    DropDownImageListEditorStyle style;

    @UiField
    HTMLPanel errorPanel;
    
    @UiField
    Image currentTypeImage;
    
    @UiField(provided = true)
    DropdownButton dropDownButton;

    @UiField
    Tooltip errorTooltip;

    private IconAnchor trigger;
    
    @UiConstructor
    public DropDownImageListEditor() {

        // Create and configure the dropdown button.
        dropDownButton = new DropdownButton() {
            @Override
            protected IconAnchor createTrigger() {
                DropDownImageListEditor.this.trigger =  super.createTrigger();
                return DropDownImageListEditor.this.trigger;
            }
        };
        dropDownButton.setType(ButtonType.LINK);
        
        // UI binding.
        initWidget(Binder.BINDER.createAndBindUi(this));
        
    }

    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<T> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Returns a {@link com.google.gwt.editor.client.adapters.TakesValueEditor} backed by the ValueListBox.
     */
    public TakesValueEditor<T> asEditor() {
        if (editor == null) {
            editor = TakesValueEditor.of(this);
        }
        return editor;
    }

    public T getValue() {
        return value;
    }

    public void setAcceptableValues(Map<T, Image> newValues) {
        values.clear();
        images.clear();
        
        if (newValues != null && !newValues.isEmpty()) {
            for (Map.Entry<T, Image> entry : newValues.entrySet()) {
                final T _value = entry.getKey();
                final Image _image = entry.getValue();

                if (width > 0 && height > 0) _image.setSize(width+"px", height+"px");
                _image.addStyleName(style.imagePointer());
                _image.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (isEditMode) setValue(_value, fireEvents);
                    }
                });
                values.add(_value);
                images.put(_value, _image);
            }
        }
        
        // Configure drop down button trigger.
        if (values.size() == 1) {
            trigger.setActive(false);
            trigger.setCaret(false);
        }
        
    }
    
    private void buildUIDropDown() {
        dropDownButton.clear();
        dropDownButton.addCustomTrigger(currentTypeImage);
        dropDownButton.getMenuWiget().setVisible(false);   
        
        if (images!= null && images.size() > 1) {
            dropDownButton.getMenuWiget().setVisible(true);
            for (Map.Entry<T, Image> entry : images.entrySet()) {
                if (this.value != null && !this.value.equals(entry.getKey())) dropDownButton.add(entry.getValue());
            }
        }
        
    }
    
    public void setAcceptableValues(final Collection<T> newValues) {
        values.clear();

        if (newValues != null) {
            for (T nextNewValue : newValues) {
                values.add(nextNewValue);
            }
        }
    }

    /**
     * Set the value and display it in the select element. Add the value to the
     * acceptable set if it is not already there.
     */
    public void setValue(final T value) {
        setValue(value, false);
    }

    public void setValue(final T value, final boolean fireEvents) {

        // Disable current error markers, if present.
        disableError();

        if (value == this.value || (this.value != null && this.value.equals(value))) {
            return;
        }
        
        T before = this.value;
        this.value = value;

        for (T entry : values) {
            final Image image = images.get(entry);
            // if (entry.equals(value)) dropDownButton.setText(value.toString()); 
            if (entry.equals(value)) {
                currentTypeImage.setUrl(image.getUrl());
                currentTypeImage.setSize("16px", "16px");
                currentTypeImage.setAltText(image.getAltText()); //discriminator for selenium
            }
        }

        // Build the drop down button.
        buildUIDropDown();

        if (fireEvents) {
            ValueChangeEvent.fireIfNotEqual(this, before, value);
        }
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

    public void setEditMode(final boolean isEditMode) {
        this.isEditMode = isEditMode;
    }
    
    public void setSize(final int w, final int h) {
        this.width = w;
        this.height = h;
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
