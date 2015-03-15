package org.dashbuilder.common.client.validation.editors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.editor.client.HasEditorErrors;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.ui.client.adapters.ValueBoxEditor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

public class ValueBoxEditorDecorator<T> extends Composite implements
        HasEditorErrors<T>, IsEditor<ValueBoxEditor<T>> {
    interface Binder extends UiBinder<Widget, ValueBoxEditorDecorator<?>> {
        Binder BINDER = GWT.create(Binder.class);
    }

    @UiField
    SimplePanel contents;

    @UiField
    DivElement errorLabel;

    private ValueBoxEditor<T> editor;

    /**
     * Constructs a ValueBoxEditorDecorator.
     */
    @UiConstructor
    public ValueBoxEditorDecorator() {
        initWidget(Binder.BINDER.createAndBindUi(this));
    }

    /**
     * Constructs a ValueBoxEditorDecorator using a {@link com.google.gwt.user.client.ui.ValueBoxBase}
     * widget and a {@link com.google.gwt.editor.ui.client.adapters.ValueBoxEditor} editor.
     *
     * @param widget the widget
     * @param editor the editor
     */
    public ValueBoxEditorDecorator(ValueBoxBase<T> widget,
                                   ValueBoxEditor<T> editor) {
        this();
        contents.add(widget);
        this.editor = editor;
    }

    /**
     * Returns the associated {@link com.google.gwt.editor.ui.client.adapters.ValueBoxEditor}.
     *
     * @return a {@link com.google.gwt.editor.ui.client.adapters.ValueBoxEditor} instance
     * @see #setEditor(com.google.gwt.editor.ui.client.adapters.ValueBoxEditor)
     */
    public ValueBoxEditor<T> asEditor() {
        return editor;
    }

    /**
     * Sets the associated {@link com.google.gwt.editor.ui.client.adapters.ValueBoxEditor}.
     *
     * @param editor a {@link com.google.gwt.editor.ui.client.adapters.ValueBoxEditor} instance
     * @see #asEditor()
     */
    public void setEditor(ValueBoxEditor<T> editor) {
        this.editor = editor;
    }

    /**
     * Set the widget that the EditorPanel will display. This method will
     * automatically call {@link #setEditor}.
     *
     * @param widget a {@link com.google.gwt.user.client.ui.ValueBoxBase} widget
     */
    @UiChild(limit = 1, tagname = "valuebox")
    public void setValueBox(ValueBoxBase<T> widget) {
        contents.add(widget);
        setEditor(widget.asEditor());
    }

    /**
     * The default implementation will display, but not consume, received errors
     * whose {@link com.google.gwt.editor.client.EditorError#getEditor() getEditor()} method returns the Editor
     * passed into {@link #setEditor}.
     *
     * @param errors a List of {@link com.google.gwt.editor.client.EditorError} instances
     */
    public void showErrors(List<EditorError> errors) {
        StringBuilder sb = new StringBuilder();
        for (EditorError error : errors) {
            if (error.getEditor().equals(editor)) {
                sb.append("\n").append(error.getMessage());
            }
        }

        if (sb.length() == 0) {
            errorLabel.setInnerText("");
            errorLabel.getStyle().setDisplay(Style.Display.NONE);
            return;
        }

        errorLabel.setInnerText(sb.substring(1));
        errorLabel.getStyle().setDisplay(Style.Display.INLINE_BLOCK);
    }
}
