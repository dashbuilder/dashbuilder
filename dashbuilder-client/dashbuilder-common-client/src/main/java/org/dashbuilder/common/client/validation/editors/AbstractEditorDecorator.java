package org.dashbuilder.common.client.validation.editors;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.Placement;

import java.util.List;

/**
 * <p>Subclasses for this class are intended to be gwt editor decorators that provides an error widget, usually a label or tooltip.</p>
 * <p>This class provides common error handling methods for all subclasses.</p>
 * 
 * @since 0.4.0
 */
public abstract class AbstractEditorDecorator<T> extends Composite {

    public enum ErrorLabelPosition {
        LEFT, RIGHT, TOOLTIP_TOP, TOOLTIP_BOTTOM;
    }

    @Editor.Ignore
    protected ErrorLabelPosition errorLabelPosition;

    @UiConstructor
    public AbstractEditorDecorator() {
        // By default, show errors using tooltips.
        errorLabelPosition = ErrorLabelPosition.TOOLTIP_TOP;
    }

    public void setErrorLabelPosition(ErrorLabelPosition errorLabelPosition) {
        this.errorLabelPosition = errorLabelPosition;
    }

    public ErrorLabelPosition getErrorLabelPosition() {
        return errorLabelPosition;
    }

    protected void _showErrors(List<EditorError> errors) {
        StringBuilder sb = new StringBuilder();
        for (EditorError error : errors) {

            // error.getEditor().equals(getEditor())
            if (error.getEditor() == this) {
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

    protected void disableError() {
        // Subclass impl.
    }

    protected void enableError(final String message) {
        // Subclass impl.
    }

    protected void doPositionErrorTooltip(final Tooltip tooltip) {
        switch (errorLabelPosition) {
            case TOOLTIP_TOP:
                tooltip.setPlacement(Placement.TOP);
                reconfigureTooltip(tooltip);
                break;
            case TOOLTIP_BOTTOM:
                tooltip.setPlacement(Placement.BOTTOM);
                reconfigureTooltip(tooltip);
                break;
        }
    }

    protected void doPositionErrorElement(final Element errorElement) {
        switch (errorLabelPosition) {
            case LEFT:
                errorElement.getStyle().setFloat(Style.Float.LEFT);
                break;
            default:
                errorElement.getStyle().setFloat(Style.Float.RIGHT);
                break;
        }
    }

    protected void setErrorElementText(final Element errorElement, final String text) {
        if (text == null || text.trim().length() == 0) {
            errorElement.setInnerText("");
            errorElement.getStyle().setDisplay(Style.Display.NONE);
        } else {
            errorElement.setInnerText(text);
            errorElement.getStyle().setDisplay(Style.Display.INLINE);
        }
    }

    protected void setTooltipText(final Tooltip tooltip, final String text) {
        if (text == null || text.trim().length() == 0) {
            tooltip.setTitle( "" );
        } else {
            tooltip.setTitle(text);
        }
        reconfigureTooltip(tooltip);
    }

    // See issue https://github.com/gwtbootstrap/gwt-bootstrap/issues/287
    protected void reconfigureTooltip(final Tooltip tooltip) {
        tooltip.reconfigure();
    }

    protected  void markErrorPanel(final Panel errorPanel, final boolean error) {
        if (error) {
            errorPanel.getElement().getStyle().setBorderColor("red");
            errorPanel.getElement().getStyle().setBorderStyle(Style.BorderStyle.SOLID);
            errorPanel.getElement().getStyle().setBorderWidth(1, Style.Unit.PX);
        } else {
            errorPanel.getElement().getStyle().setBorderWidth(0, Style.Unit.PX);
        }

    }


}
