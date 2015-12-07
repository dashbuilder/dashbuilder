package org.dashbuilder.common.client.editor.list;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.Image;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.uberfire.mvp.Command;

import javax.enterprise.context.Dependent;

/**
 * <p>The ImageListEditor default view. It places images in an horizontal way.</p>
 *
 * @since 0.4.0
 */
@Dependent
public class HorizImageListEditorView<T> extends Composite implements ImageListEditorView<T> {

    interface Binder extends UiBinder<Widget, HorizImageListEditorView> {
        Binder BINDER = GWT.create(Binder.class);
    }

    interface HorizImageListEditorViewStyle extends CssResource {
        String errorPanel();
        String errorPanelWithError();
        String image();
    }


    @UiField
    HorizImageListEditorViewStyle style;

    @UiField
    @Editor.Ignore
    HTMLPanel errorPanel;

    @UiField
    @Editor.Ignore
    FlowPanel helpPanel;
    
    @UiField
    @Editor.Ignore
    HorizontalPanel mainPanel;

    @UiField
    @Editor.Ignore
    Tooltip errorTooltip;

    ImageListEditor<T> presenter;

    @Override
    public void init(final ImageListEditor<T> presenter) {
        this.presenter = presenter;
    }
    
    @UiConstructor
    public HorizImageListEditorView() {
        initWidget(Binder.BINDER.createAndBindUi(this));
    }

    @Override
    public ImageListEditorView<T> add(final SafeUri uri, final String width, final String height,
                                       final SafeHtml heading, final SafeHtml text, 
                                       final boolean selected, final Command clickCommand) {
        final VerticalPanel panel = new VerticalPanel();
        panel.setVerticalAlignment(HasAlignment.ALIGN_MIDDLE);
        panel.setHeight("100%");
        
        final Image image = new Image(uri);
        image.setWidth(width);
        image.setHeight(height);
        image.addStyleName(style.image());
        final double alpha = selected ? 1 : 0.2;
        image.getElement().setAttribute("style", "filter: alpha(opacity=5);opacity: " + alpha);
        image.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                clickCommand.execute();
            }
        });
        
        final Popover popover = new Popover();
        popover.setTitle( heading.asString() );
        popover.setContent( text.asString() );
        popover.setWidget(image);
        popover.setContainer("body");
        popover.setPlacement(Placement.BOTTOM);
        popover.setShowDelayMs(1000);
        
        final HTML label = new HTML(heading.asString());
        final HorizontalPanel labelPanel = new HorizontalPanel();
        labelPanel.setWidth("100%");
        labelPanel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
        labelPanel.add(label);

        panel.add(popover);
        panel.add(labelPanel);        
        mainPanel.add(panel);
        
        return this;
    }

    @Override
    public ImageListEditorView<T> addHelpContent(final String title, final String content, final Placement placement) {
        final Popover popover = new Popover(mainPanel);
        popover.setContainer("body");
        popover.setShowDelayMs(1000);
        popover.setPlacement(placement);
        popover.setTitle(title);
        popover.setContent(content);
        helpPanel.add(popover);
        return this;
    }

    @Override
    public ImageListEditorView<T> showError(SafeHtml message) {
        errorTooltip.setTitle(message.asString());
        errorTooltip.reconfigure();
        errorPanel.removeStyleName(style.errorPanel());
        errorPanel.addStyleName(style.errorPanelWithError());
        return null;
    }

    @Override
    public ImageListEditorView<T> clearError() {
        errorTooltip.setTitle("");
        errorTooltip.reconfigure();
        errorPanel.removeStyleName(style.errorPanelWithError());
        errorPanel.addStyleName(style.errorPanel());
        return this;
    }

    @Override
    public ImageListEditorView<T> clear() {
        clearError();
        mainPanel.clear();
        return this;
    }
    
}
