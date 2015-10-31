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
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Image;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.uberfire.mvp.Command;

import javax.enterprise.context.Dependent;

/**
 * <p>The ImageListEditor view that uses a drop down as selector.</p>
 *
 * @since 0.4.0
 */
@Dependent
public class DropDownImageListEditorView<T> extends Composite implements DropDownImageListEditor.View<T> {

    interface Binder extends UiBinder<Widget, DropDownImageListEditorView> {
        Binder BINDER = GWT.create(Binder.class);
    }

    interface DropDownImageListEditorViewStyle extends CssResource {
        String errorPanel();
        String errorPanelWithError();
        String image();
    }

    @UiField
    DropDownImageListEditorViewStyle style;

    @UiField
    @Editor.Ignore
    HTMLPanel errorPanel;

    @UiField
    @Editor.Ignore
    FlowPanel helpPanel;
    
    @UiField
    @Editor.Ignore
    DropDown dropDown;

    @UiField
    @Editor.Ignore
    Anchor dropDownAnchor;

    @UiField
    @Editor.Ignore
    DropDownMenu dropDownMenu;

    @UiField
    @Editor.Ignore
    Tooltip errorTooltip;

    @Editor.Ignore
    Image currentTypeImage;

    @Editor.Ignore
    InlineLabel caret;

    ImageListEditor<T> presenter;

    @Override
    public void init(final ImageListEditor<T> presenter) {
        this.presenter = presenter;
    }
    
    @UiConstructor
    public DropDownImageListEditorView() {
        initWidget(Binder.BINDER.createAndBindUi(this));
        currentTypeImage = new Image();
        caret = new InlineLabel();
        caret.addStyleName( "caret" );
        caret.setVisible( true);

        dropDownAnchor.add( currentTypeImage );
        dropDownAnchor.add( caret );
        dropDownAnchor.setEnabled( true );
    }

    @Override
    public ImageListEditorView<T> add(final SafeUri uri, final String width, final String height,
                                       final SafeHtml heading, final SafeHtml text, 
                                       final boolean selected, final Command clickCommand) {
        final Image image = new Image(uri);
        image.setWidth(width);
        image.setHeight(height);
        image.addStyleName(style.image());
        image.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                clickCommand.execute();
            }
        });
        
        if (selected) {
            currentTypeImage.setUrl( uri );
            currentTypeImage.setSize( width, height );
            currentTypeImage.setAltText( heading.asString() );
        } else {
            dropDownMenu.add(image);
        }

        return this;
    }

    @Override
    public ImageListEditorView<T> addHelpContent(String title, String content, Placement placement) {
        final Popover popover = new Popover(dropDown);
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
        return this;
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
        dropDownMenu.clear();
        return this;
    }

    @Override
    public void setDropDown(boolean isDropDown) {
        dropDownAnchor.setEnabled( isDropDown );
        caret.setVisible( isDropDown );
    }
    
}
