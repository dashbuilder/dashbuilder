package org.dashbuilder.common.client.validation.editors;

import com.github.gwtbootstrap.client.ui.Image;
import com.google.gwt.core.client.GWT;
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
import com.google.gwt.user.client.ui.HasConstrainedValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.*;

public class ImageListEditorDecorator<T> extends Composite implements
        HasConstrainedValue<T>, IsEditor<TakesValueEditor<T>> {

    
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
    HorizontalPanel mainPanel;
    
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
        mainPanel.clear();
        values.clear();
        images.clear();
        value = null;
    }
    
}
