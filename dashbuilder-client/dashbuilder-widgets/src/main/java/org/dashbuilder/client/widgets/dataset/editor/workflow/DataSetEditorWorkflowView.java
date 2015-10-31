package org.dashbuilder.client.widgets.dataset.editor.workflow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Popover;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.uberfire.mvp.Command;

import javax.enterprise.context.Dependent;

/**
 * <p>The Data Set workflow editor view.</p>
 *
 * @since 0.4.0
 */
@Dependent
public class DataSetEditorWorkflowView extends Composite implements DataSetEditorWorkflow.View {

    interface Binder extends UiBinder<Widget, DataSetEditorWorkflowView> {
        Binder BINDER = GWT.create(Binder.class);
    }

    @UiField
    Container container;

    @UiField
    Column buttonsPanel;

    DataSetEditorWorkflow presenter;

    @Override
    public void init(final DataSetEditorWorkflow presenter) {
        this.presenter = presenter;
        initWidget(Binder.BINDER.createAndBindUi(this));
    }

    @Override
    public DataSetEditorWorkflow.View add(IsWidget widget) {
        container.add(widget.asWidget());
        return this;
    }

    @Override
    public DataSetEditorWorkflow.View addButton(final String text, final String content, final boolean isPrimary, final Command clickCommand) {
        final Button button = new Button(text);
        if (isPrimary) {
            button.setType(ButtonType.PRIMARY);
        }
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                clickCommand.execute();
            }
        });

        final Popover popover = new Popover(button);
        popover.setTitle(text);
        popover.setContent(content);
        popover.setContainer("body");
        popover.setPlacement(Placement.RIGHT);
        popover.setShowDelayMs(1000);

        buttonsPanel.add(popover);
        return this;
    }

    @Override
    public DataSetEditorWorkflow.View clearButtons() {
        buttonsPanel.clear();
        return this;
    }

    @Override
    public DataSetEditorWorkflow.View clearView() {
        container.clear();
        clearButtons();
        return this;
    }
    
}
