package org.dashbuilder.common.client.validation.editors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.editor.client.HasEditorErrors;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.ui.client.adapters.HasTextEditor;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
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

import java.util.List;

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
public class FileUploadEditor extends AbstractEditorDecorator implements
                                                HasId, HasText, IsEditor<HasTextEditor>, HasEditorErrors<String> {

    private static final String LOADING_IMAGE_SIZE[] = new String[]{ "16px", "16px" };

    interface Binder extends UiBinder<Widget, FileUploadEditor> {

        Binder BINDER = GWT.create( Binder.class );
    }

    interface FileUploadEditorStyle extends CssResource {

    }

    @UiField
    @Ignore
    FileUploadEditorStyle style;

    @UiField
    @Ignore
    FlowPanel mainPanel;

    @UiField
    @Ignore
    FormPanel formPanel;

    @UiField
    @Ignore
    Tooltip errorTooltip;

    @UiField(provided = true)
    @Ignore
    FileUpload fileUpload;

    @UiField
    @Ignore
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
        formPanel.addSubmitHandler(new FormPanel.SubmitHandler() {
            @Override
            public void onSubmit( final FormPanel.SubmitEvent event ) {
                final String fileName = fileUpload.getFilename();
                if ( isNullOrEmpty( fileName ) ) {
                    event.cancel();
                } else {
                    if ( loadingImage != null ) {
                        fileUpload.setVisible( false );
                        loadingImage.setVisible( true );
                    }
                }
            }

            private boolean isNullOrEmpty( final String fileName ) {
                return fileName == null || "".equals( fileName );
            }
        });
        formPanel.addSubmitCompleteHandler( new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete( FormPanel.SubmitCompleteEvent event ) {
                disableError();
                if ( loadingImage != null ) {
                    fileUpload.setVisible( true );
                    loadingImage.setVisible( false );
                }
            }
        } );
    }

    private FileUpload createFileUpload() {
        return new FileUpload( new Command() {
            @Override
            public void execute() {
                final String _f = callback.getUploadFileName();
                final String _a = callback.getUploadFileUrl();
                formPanel.setAction( _a );
                setText( _f );
                fileLabel.setVisible(false);
                formPanel.submit();
            }
        }, true );
    }
    
    public void setFileUploadName(final String name) {
        fileUpload.setName(name);
    }

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

    @Override
    public void showErrors(List<EditorError> list) {
        _showErrors(list);
    }

    @Override
    public void setErrorLabelPosition(ErrorLabelPosition errorLabelPosition) {
        super.setErrorLabelPosition(errorLabelPosition);
        doPositionErrorTooltip(errorTooltip);
    }
    
    @Override
    protected void enableError(String message) {
        super.enableError(message);
        setTooltipText(errorTooltip, message);
        markErrorPanel(mainPanel, true);
    }

    protected void disableError() {
        super.disableError();
        setTooltipText(errorTooltip, null);
        markErrorPanel(mainPanel, false);
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
