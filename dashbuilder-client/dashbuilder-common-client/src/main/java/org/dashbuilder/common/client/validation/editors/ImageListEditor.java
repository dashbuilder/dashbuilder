package org.dashbuilder.common.client.validation.editors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import com.google.gwt.user.client.ui.HasConstrainedValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.Popover;
import org.gwtbootstrap3.client.ui.Tooltip;

/**
 * <p>Editor component that accepts multiple values and display each one using a given image.</p>
 * <p>The validation error messages are displayed by changing border color to RED and showing the message using a tooltip.</p>
 * <p>This component is ideal for handling enums.</p>
 *  
 * @param <T> The type of the value that contains the editor widget
 */
public class ImageListEditor<T> extends Composite implements
        HasConstrainedValue<T>, HasEditorErrors<T>, IsEditor<TakesValueEditor<T>> {

    public static final int POPOVER_SHOW_DELAY = 1000;

    interface Binder extends UiBinder<Widget, ImageListEditor> {
        Binder BINDER = GWT.create(Binder.class);
    }

    interface ImageListEditorStyle extends CssResource {
        String errorPanel();
        String errorPanelError();
        String imagePointer();
    }
    
    private static final double ALPHA_ICON_NOT_SELECTED = 0.2;
    private final List<T> values = new ArrayList<T>();
    private TakesValueEditor<T> editor;
    private final Map<T ,Entry> entries = new LinkedHashMap<T, Entry>();
    private T value;
    private  boolean isEditMode;
    private int width = -1;
    private int height = -1;
    private String imageStyle = null;

    @UiField
    ImageListEditorStyle style;

    @UiField
    HTMLPanel errorPanel;
    
    @UiField
    HorizontalPanel mainPanel;

    @UiField
    Tooltip errorTooltip;
    
    public static final class Entry {
        Image image;
        String heading;
        String text;

        public Entry(Image image) {
            this.image = image;
        }
        
        public Entry(Image image, String heading, String text) {
            this.image = image;
            this.heading = heading;
            this.text = text;
        }
        
    }
    @UiConstructor
    public ImageListEditor() {
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

    public void setAcceptableValues(Map<T, Entry> newValues) {
        values.clear();
        entries.clear();

        if (newValues != null) {
            for (Map.Entry<T, Entry> entry : newValues.entrySet()) {
                final T _value = entry.getKey();
                final Entry _entry = entry.getValue();
                final Image _image = _entry.image;
                
                if (width > 0 && height > 0) _image.setSize(width + "px", height + "px");
                _image.addStyleName(style.imagePointer());
                if (imageStyle != null) _image.addStyleName(imageStyle);
                _image.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (isEditMode) setValue(_value);
                    }
                });
                values.add(_value);
                
                final String heading = _entry.heading;
                final String text = _entry.text;
                if (heading != null && text != null) {
                    final Popover popover = new Popover();
                    popover.setTitle( heading );
                    popover.setContent( text );
                    popover.setWidget(_image);
                    popover.setShowDelayMs( POPOVER_SHOW_DELAY );
                    mainPanel.add(popover);
                } else {
                    mainPanel.add(_image);
                }
                entries.put(_value, _entry);
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
        if (value == this.value) {
            for (T entry : values) {
                final Image image = entries.get(entry).image;
                applyAlpha(image, 1);
            }
            return;
        }
        
        if (this.value != null && this.value.equals(value)) return;

        // Disable current error markers, if present.
        disableError();
        
        T before = this.value;
        this.value = value;

        
        for (T entry : values) {
            final Image image = entries.get(entry).image;
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
        this.isEditMode = isEditMode;
    }
    
    public void setSize(final int w, final int h) {
        this.width = w;
        this.height = h;
    }

    public void setImageStyle(final String imageStyle) {
        this.imageStyle = imageStyle;
    }

    private void applyAlpha(final Image image, final double alpha) {
        image.getElement().setAttribute("style", "filter: alpha(opacity=5);opacity: " + alpha);
        if (width > 0 && height > 0) image.setSize(width+"px", height+"px");
    }

    public void clear() {
        setValue(null);
        disableError();
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
            errorTooltip.setTitle( "" );
        } else {
            errorTooltip.setTitle(text);
        }
        // See issue https://github.com/gwtbootstrap/gwt-bootstrap/issues/287
        errorTooltip.reconfigure();
    }
    
}
