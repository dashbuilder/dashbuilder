package org.dashbuilder.common.client.validation.editors;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.editor.client.HasEditorErrors;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.ui.client.adapters.ValueBoxEditor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.Placement;

/**
 * <p>Editor component decorator that wraps an editor widget and provides displaying of output validation messages.</p>
 * <p>The output validation messages can be displayed as:</p>
 * <ul>
 *     <li>Labels - The validation errors are placed in a label on the RIGHT or LEFT side of the editor widget.</li>
 *     <li>Tooltip - The validation errors are displayed using a tooltip.</li>
 * </ul>
 * @param <T> The type of the value that contains the editor widget.
 */
public class ValueBoxEditorDecorator<T> extends Composite implements
        HasValue<T>, HasEditorErrors<T>, IsEditor<ValueBoxEditor<T>> {

    // The GWT bootstrap styles for error panels.
    private static final String STYLE_ERROR = " control-group has-error ";
    private T value;

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        setValue(value, false);
    }

    @Override
    public void setValue(T value, boolean fireEvents) {
        // Disable current error markers, if present.
        disableError();

        if (value == this.value || (this.value != null && this.value.equals(value))) {
            return;
        }

        T before = this.value;
        this.value = value;

        if (fireEvents) {
            ValueChangeEvent.fireIfNotEqual(this, before, value);
        }
    }

    interface Binder extends UiBinder<Widget, ValueBoxEditorDecorator<?>> {
        Binder BINDER = GWT.create(Binder.class);
    }

    interface ValueBoxEditorDecoratorStyle extends CssResource {
        String contents();
        String errorLabel();
        String errorLabelLeft();
        String errorLabelRight();

    }

    @UiField ValueBoxEditorDecoratorStyle style;

    @UiField
    SimplePanel contents;

    @UiField
    DivElement errorLabel;

    @UiField
    Tooltip errorTooltip;

    public enum ErrorLabelPosition {
        LEFT, RIGHT, TOOLTIP_TOP, TOOLTIP_BOTTOM;
    }

    private ValueBoxEditor<T> editor;
    private ErrorLabelPosition errorLabelPosition;

    /**
     * Constructs a ValueBoxEditorDecorator.
     */
    @UiConstructor
    public ValueBoxEditorDecorator() {
        // By default, show errors using tooltips.
        errorLabelPosition = ErrorLabelPosition.TOOLTIP_TOP;
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
        listenToValueBoxBaseChangeEvent(widget);
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
    public void setValueBox(final ValueBoxBase<T> widget) {
        contents.add(widget);
        listenToValueBoxBaseChangeEvent(widget);
        setEditor(widget.asEditor());
    }

    public void setEnabled(final boolean enabled) {
        ((ValueBoxBase)contents.getWidget()).setEnabled(enabled);
    }

    private void listenToValueBoxBaseChangeEvent(final ValueBoxBase<T> widget) {
        if (widget != null) {
            final T before = widget.getValue();
            widget.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    disableError();
                }
            });
            widget.addValueChangeHandler(new ValueChangeHandler<T>() {
                @Override
                public void onValueChange(ValueChangeEvent<T> event) {
                    disableError();
                    ValueChangeEvent.fireIfNotEqual(ValueBoxEditorDecorator.this, before, event.getValue());
                }
            });
        }
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

        boolean hasErrors = sb.length() > 0;
        if (!hasErrors) {
            disableError();
            return;
        }

        // Show the errors.
        enableError(sb.substring(1));
    }

    private void enableError(String text) {
        contents.addStyleName(STYLE_ERROR);
        if (isUsingErrorLabel()) {
            setErrorLabelText(text);
            setTooltipText(null);
        } else {
            setErrorLabelText(null);
            setTooltipText(text);
        }
    }

    private void disableError() {
        contents.removeStyleName(STYLE_ERROR);
        setErrorLabelText(null);
        setTooltipText(null);
    }

    private void setTooltipText(String text) {
        if (text == null || text.trim().length() == 0) {
            errorTooltip.setTitle( "" );
        } else {
            errorTooltip.setTitle( text );
        }
        reconfigureTooltip();
    }

    private void setErrorLabelText(String text) {
        if (text == null || text.trim().length() == 0) {
            errorLabel.setInnerText("");
            errorLabel.getStyle().setDisplay(Style.Display.NONE);
        } else {
            errorLabel.setInnerText(text);
            errorLabel.getStyle().setDisplay(Style.Display.INLINE);
        }
    }

    private boolean isUsingErrorLabel() {
        return !isUsingErrorTooltip();
    }

    private boolean isUsingErrorTooltip() {
        return ErrorLabelPosition.TOOLTIP_TOP.equals(getErrorLabelPosition()) || ErrorLabelPosition.TOOLTIP_BOTTOM.equals(getErrorLabelPosition());
    }

    public ErrorLabelPosition getErrorLabelPosition() {
        return errorLabelPosition;
    }

    public void setErrorLabelPosition(ErrorLabelPosition errorLabelPosition) {
        this.errorLabelPosition = errorLabelPosition;
        positionErrorLabel();
    }

    private void positionErrorLabel() {
        switch (errorLabelPosition) {
            case TOOLTIP_TOP:
                errorTooltip.setPlacement(Placement.TOP);
                reconfigureTooltip();
                break;
            case TOOLTIP_BOTTOM:
                errorTooltip.setPlacement(Placement.BOTTOM);
                reconfigureTooltip();
                break;
            case LEFT:
                errorLabel.addClassName(style.errorLabelLeft());
                break;
            default:
                errorLabel.addClassName(style.errorLabelRight());
                break;
        }
    }

    // See issue https://github.com/gwtbootstrap/gwt-bootstrap/issues/287
    private void reconfigureTooltip() {
        errorTooltip.reconfigure();
    }

    public void clear() {
        setValue(null);
        disableError();
    }

}
