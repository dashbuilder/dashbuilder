package org.dashbuilder.common.client.validation.editors;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.ui.client.adapters.HasTextEditor;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.Image;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.base.HasId;
import org.uberfire.ext.widgets.common.client.common.FileUpload;
import org.uberfire.mvp.Command;

/**
 * <p>Editor component that wraps a gwt bootstrap file upload component an additionally provides:</p>
 * <ul>
 * <li>Error messages - Show validation error messages.</li>
 * <li>Dashbuilder File Upload Servlet integration - It uses the UF Dashbuilder servlet for uploading files and provides a listener to obtain the uploaded file path.</li>
 * </ul>
 * <p>NOTE that for uploading the file, this editor encapsulates a FormPanel widget. So do not include it in other forms.</p>
 * <p/>
 * <p>Usage:</p>
 * <code>
 * @UiField <:dash:FileUploadEditor ui:field="fileUpload"/>
 * ...
 * fileUpload.setCallback(new FileUploadEditor.FileUploadEditorCallback() { ... }); # Provide servlet URL and file name to create.
 * fileUpload.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {... }); # Provide the complete handler instance.
 * ...
 * final String vfs_uploaded_file_path = fileUpload.getText();
 * </code>
 * @since 0.3.0
 */
public class FileUploadEditor extends Composite implements
                                                HasId, HasText, IsEditor<HasTextEditor> {

    private static final String LOADING_IMAGE_SIZE[] = new String[]{ "16px", "16px" };

    interface Binder extends UiBinder<Widget, FileUploadEditor> {

        Binder BINDER = GWT.create( Binder.class );
    }

    interface FileUploadEditorStyle extends CssResource {

    }

    @UiField
    FileUploadEditorStyle style;

    @UiField
    FlowPanel mainPanel;

    @UiField
    FormPanel formPanel;

    @UiField
    Tooltip errorTooltip;

    @UiField(provided = true)
    @Editor.Ignore
    FileUpload fileUpload;

    @UiField
    @Editor.Ignore
    Label fileLabel;

    @UiField
    Image loadingImage;

    private String id;
    private String value;
    private HasTextEditor editor;
    private FileUploadEditorCallback callback;
    private SafeUri loadingImageUri;

    public interface FileUploadEditorCallback {

        String getUploadFileName();

        String getUploadFileUrl();
    }

    /**
     * Constructs a FileUploadEditor.
     */
    @UiConstructor
    public FileUploadEditor() {

        fileUpload = createFileUpload();
        
        initWidget( Binder.BINDER.createAndBindUi( this ) );

        loadingImage.setVisible( false );

        formPanel.setEncoding( FormPanel.ENCODING_MULTIPART );
        formPanel.setMethod( FormPanel.METHOD_POST );
        formPanel.setWidget( fileUpload );
        formPanel.addSubmitCompleteHandler( formSubmitCompleteHandler );
    }

    private FileUpload createFileUpload() {
        return new FileUpload( new Command() {
            @Override
            public void execute() {
                final String _f = callback.getUploadFileName();
                final String _a = callback.getUploadFileUrl();
                setText( _f );
                formPanel.setAction( _a );
                if ( loadingImage != null ) {
                    fileUpload.setVisible( false );
                    loadingImage.setVisible( true );
                }
                fileLabel.setVisible(false);
                formPanel.submit();
            }
        }, true );
    }

    private final FormPanel.SubmitCompleteHandler formSubmitCompleteHandler = new FormPanel.SubmitCompleteHandler() {
        @Override
        public void onSubmitComplete( FormPanel.SubmitCompleteEvent event ) {
            if ( loadingImage != null ) {
                fileUpload.setVisible( true );
                loadingImage.setVisible( false );
            }
        }
    };

    public HandlerRegistration addSubmitCompleteHandler( final FormPanel.SubmitCompleteHandler submitCompleteHandler ) {
        return formPanel.addSubmitCompleteHandler( submitCompleteHandler );
    }

    public void setLoadingImageUri( SafeUri loadingImageUri ) {
        this.loadingImageUri = loadingImageUri;
        loadingImage.setUrl( loadingImageUri );
        loadingImage.setSize( LOADING_IMAGE_SIZE[ 0 ], LOADING_IMAGE_SIZE[ 1 ] );
    }

    public void setCallback( final FileUploadEditorCallback callback ) {
        this.callback = callback;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId( String id ) {
        this.id = id;
    }

    @Override
    public String getText() {
        return value;
    }

    @Override
    public void setText( String text ) {
        this.value = text;
        if ( !isEmpty( fileUpload.getFilename() ) ) {
            fileLabel.setVisible( false );
        } else if ( !isEmpty( text ) ) {

            int slash = text.lastIndexOf( "/" ) != -1 ? text.lastIndexOf( "/" ) : text.lastIndexOf( "\\" );

            if ( slash == -1 ) {
                fileLabel.setText( text );
            } else {
                fileLabel.setText( text.substring( slash + 1 ) );
            }
            fileLabel.setVisible( true );
        }
    }

    private boolean isEmpty( final String s ) {
        return s == null || s.trim().length() == 0;
    }

    @Override
    public HasTextEditor asEditor() {
        if ( editor == null ) {
            editor = HasTextEditor.of( this );
        }
        return editor;
    }

    public void clear() {
        formPanel.reset();
        setText( null );
        fileLabel.setVisible( false );
    }
}
