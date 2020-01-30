package org.sagebionetworks.web.client.widget.upload;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.Progress;
import org.gwtbootstrap3.client.ui.ProgressBar;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageUploadViewImpl implements ImageUploadView {

	private static final String PREFIX_FILE_INPUT_WIDGET = "imageInputWidget";

	/**
	 * Used to ensure each new instance of this widget has its own ID. This is important because the ID
	 * is used when interacting with the actual DOM element.
	 */
	private static long ID_SEQUENCE = 0;

	private Widget widget;
	private static boolean isLoaded = false;

	public interface Binder extends UiBinder<Widget, ImageUploadViewImpl> {
	}

	@UiField
	Form form;
	@UiField
	Input fileInput;
	@UiField
	Button uploadbutton;
	@UiField
	Progress progressContainer;
	@UiField
	ProgressBar progressBar;
	@UiField
	Div synAlertContainer;
	@UiField
	Span uploadedFileNameField;
	Presenter presenter;
	@UiField
	CanvasElement originalCanvas;
	@UiField
	CanvasElement resizedCanvas;
	@UiField
	LoadingSpinner loadingUI;

	@Inject
	public ImageUploadViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		// Create a unique for each new instance.
		this.fileInput.getElement().setId(PREFIX_FILE_INPUT_WIDGET + ID_SEQUENCE++);

		// when a file is selected notify the presenter
		this.fileInput.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				presenter.onFileSelected();
			}
		});

		this.uploadbutton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// When they press the button trigger the input box
				fileInput.getElement().<InputElement>cast().click();
			}
		});
		if (!isLoaded) {
			_initResizer();
			isLoaded = true;
		}
	}

	@Override
	public void processFile() {
		// load into image
		loadingUI.setVisible(true);
		_loadImage(fileInput.getElement().getId(), ImageUploadViewImpl.this, originalCanvas, resizedCanvas);
	}

	private static native void _loadImage(String fileFieldId, ImageUploadViewImpl v, CanvasElement originalCanvas, CanvasElement resizedCanvas) /*-{
																																																																							var fileToUploadElement = $doc.getElementById(fileFieldId);
																																																																							var file;
																																																																							var canResize = true;
																																																																							if (fileToUploadElement && 'files' in fileToUploadElement) {
																																																																							file = fileToUploadElement.files[0];
																																																																							var ext = file.name.split('.').pop();
																																																																							// resize if it is a supported file type, and the file size > 3MB
																																																																							canResize = [ 'bmp', 'jpg', 'jpeg', 'png' ].indexOf(ext) > -1 && file.size > 3145728;
																																																																							}
																																																																							
																																																																							var imgElement = $doc.createElement('img');
																																																																							var onImageLoad = function() {
																																																																							// Create an empty canvas element of the same dimensions as the original
																																																																							originalCanvas.width = imgElement.width;
																																																																							originalCanvas.height = imgElement.height;
																																																																							// Copy the image contents to the canvas
																																																																							var ctx = originalCanvas.getContext("2d");
																																																																							ctx.drawImage(imgElement, 0, 0);
																																																																							var maxWidth = 2048;
																																																																							var maxHeight = 2048;
																																																																							if (imgElement.width > maxWidth) {
																																																																							// continue resize based on width
																																																																							var ratio = maxWidth / imgElement.width; // get ratio for scaling image
																																																																							resizedCanvas.width = maxWidth;
																																																																							resizedCanvas.height = imgElement.height * ratio;
																																																																							} else {
																																																																							// continue resize based on height
																																																																							var ratio = maxHeight / imgElement.height; // get ratio for scaling image
																																																																							resizedCanvas.height = maxHeight;
																																																																							resizedCanvas.width = imgElement.width * ratio;
																																																																							}
																																																																							
																																																																							// Resize & convert to blob
																																																																							$wnd.resizer.resize(originalCanvas, resizedCanvas)
																																																																							.then (
																																																																							function(result) {
																																																																							$wnd.resizer.toBlob(result, 'image/jpeg', 90)
																																																																							.then(
																																																																							function(blob) {
																																																																							v.@org.sagebionetworks.web.client.widget.upload.ImageUploadViewImpl::resizeComplete(Lcom/google/gwt/core/client/JavaScriptObject;)(blob);
																																																																							});
																																																																							});
																																																																							};
																																																																							imgElement.addEventListener('load', onImageLoad, false);
																																																																							
																																																																							if (file && canResize) {
																																																																							imgElement.src = $wnd.URL.createObjectURL(file);
																																																																							} else {
																																																																							// send back original content
																																																																							v.@org.sagebionetworks.web.client.widget.upload.ImageUploadViewImpl::noResizeNecessary(Lcom/google/gwt/core/client/JavaScriptObject;)(file);
																																																																							}
																																																																							}-*/;

	private static native void _initResizer() /*-{
		console.log('initializing pica resizer');
		$wnd.resizer = $wnd.pica({
			features : [ 'js', 'wasm', 'ww' ]
		});
		console.log('pica resizer initialized');
	}-*/;

	public void resizeComplete(JavaScriptObject blob) {
		loadingUI.setVisible(false);
		presenter.onFileProcessed(new JavaScriptObjectWrapper(blob), "image/jpeg");
	}

	public void noResizeNecessary(JavaScriptObject blob) {
		loadingUI.setVisible(false);
		presenter.onFileProcessed(new JavaScriptObjectWrapper(blob), null);
	}


	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public HandlerRegistration addAttachHandler(Handler handler) {
		return widget.addAttachHandler(handler);
	}

	@Override
	public boolean isAttached() {
		return widget.isAttached();
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setUploadedFileText(String text) {
		uploadedFileNameField.setText(text);
	}

	@Override
	public String getInputId() {
		return fileInput.getElement().getId();
	}

	@Override
	public void updateProgress(double currentProgress, String progressText) {
		progressBar.setPercent(currentProgress);
		progressBar.setText(progressText);
	}

	@Override
	public void showProgress(boolean visible) {
		progressContainer.setVisible(visible);
	}

	@Override
	public void resetForm() {
		this.form.reset();
		setUploadedFileText("");
	}

	@Override
	public void setInputEnabled(boolean enabled) {
		this.uploadbutton.setEnabled(enabled);
	}

	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		widget.fireEvent(event);
	}

}
