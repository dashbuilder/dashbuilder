package org.dashbuilder.common.client.validation.editors;

import com.github.gwtbootstrap.client.ui.Image;
import com.github.gwtbootstrap.client.ui.Tooltip;
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
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import java.util.*;

/**
 * <p>Editor component decorator that accepts multiple values and display each one using a given image.</p>
 * <p>The validation error messages are displayed by changing border color to RED and showing the message using a tooltip.</p>
 * <p>This component is ideal for handling enums.</p>
 *  
 * @param <T> The type of the value that contains the editor widget
 */
public class ImageListEditorDecorator<T> extends Composite implements
        HasConstrainedValue<T>, HasEditorErrors<T>, IsEditor<TakesValueEditor<T>> {

    private static final String COLOR_RED = "#FF0000";
    private static final String COLOR_BLACK = "#FFFFFF";

    interface Binder extends UiBinder<Widget, ImageListEditorDecorator> {
        Binder BINDER = GWT.create(Binder.class);
    }

    interface ImageListEditorDecoratorStyle extends CssResource {
        String mainPanel();
        String imagePointer();
    }
    
    private static final double ALPHA_ICON_NOT_SELECTED = 0.2;
    private final List<T> values = new ArrayList<T>();
    private TakesValueEditor<T> editor;
    private final Map<T ,Image> images = new LinkedHashMap<T, Image>();
    private T value;
    private  boolean isEditMode;
    private int width = -1;
    private int height = -1;

    @UiField ImageListEditorDecoratorStyle style;

    @UiField
    HTMLPanel errorPanel;
    
    @UiField
    HorizontalPanel mainPanel;

    @UiField
    Tooltip errorTooltip;
    
    @UiConstructor
    public ImageListEditorDecorator() {
        initWidget(Binder.BINDER.createAndBindUi(this));
    }

    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<T> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Returns a {@link TakesValueEditor} backed by the ValueListBox.
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

        if (newValues != null) {
            for (Map.Entry<T, Image> entry : newValues.entrySet()) {
                final T _value = entry.getKey();
                final Image _image = entry.getValue();

                if (width > 0 && height > 0) _image.setSize(width+"px", height+"px");
                _image.addStyleName(style.imagePointer());
                _image.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        setValue(_value);
                    }
                });
                
                values.add(_value);
                images.put(_value, _image);
                mainPanel.add(_image);
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
        if (value == this.value || (this.value != null && this.value.equals(value))) {
            return;
        }

        // Disable current error markers, if present.
        disableError();
        
        T before = this.value;
        this.value = value;

        
        for (T entry : values) {
            final Image image = images.get(entry);
            if (entry.equals(value)) applyAlpha(image, 1);
            else applyAlpha(image, ALPHA_ICON_NOT_SELECTED);
        }

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
        // TODO
        this.isEditMode = isEditMode;
    }
    
    public void setSize(final int w, final int h) {
        this.width = w;
        this.height = h;
    }

    private void applyAlpha(final Image image, final double alpha) {
        image.getElement().setAttribute("style", "filter: alpha(opacity=5);opacity: " + alpha);
        if (width > 0 && height > 0) image.setSize(width+"px", height+"px");
    }

    public void clear() {
        setValue(null);
    }

    private void markErrorPanel(boolean error) {
        if (error) {
            errorPanel.getElement().getStyle().setBorderWidth(1, Style.Unit.PX);
            errorPanel.getElement().getStyle().setBorderStyle(Style.BorderStyle.SOLID);
            errorPanel.getElement().getStyle().setBorderColor(COLOR_RED);
        } else {
            errorPanel.getElement().getStyle().setBorderWidth(0, Style.Unit.PX);
            errorPanel.getElement().getStyle().setBorderColor(COLOR_BLACK);
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
