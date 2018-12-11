package org.sagebionetworks.web.client.widget.upload;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.Image;
import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.Progress;
import org.gwtbootstrap3.client.ui.ProgressBar;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.widget.LoadingSpinner;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
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

public class CroppedImageUploadViewImpl implements ImageUploadView {

	private static final String PREFIX_FILE_INPUT_WIDGET = "croppedImageInputWidget";

	/**
	 * Used to ensure each new instance of this widget has its own ID. This is
	 * important because the ID is used when interacting with the actual DOM
	 * element.
	 */
	private static long ID_SEQUENCE = 0;

	private Widget widget;
	public interface Binder extends UiBinder<Widget, CroppedImageUploadViewImpl> {
	}
	@UiField
	Modal previewModal;
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
	LoadingSpinner loadingUI;
	@UiField
	Button cancelCropButton;
	@UiField
	Button saveCropButton;
	@UiField
	ModalBody previewModalBody;
	@Inject
	public CroppedImageUploadViewImpl(Binder binder) {
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
				fileInput.getElement().<InputElement> cast().click();
			}
		});
		
		cancelCropButton.addClickHandler(event -> {
			cancelCropImage();
		});
		
		saveCropButton.addClickHandler(event -> {
			saveCroppedImage();
		});
		previewModal.getElement().setAttribute("style", "z-index: 3000; height: 700px;");
		previewModal.addShownHandler(event -> {
			previewModalBody.clear();
			Image image = new Image();
			previewModalBody.add(image);
			_loadImage(fileInput.getElement().getId(), image.getElement(), CroppedImageUploadViewImpl.this);	
		});
	}

	@Override
	public void processFile() {
		// load into image
		loadingUI.setVisible(true);
		previewModal.show();
		loadingUI.setVisible(false);
	}

	private static native void _loadImage(
			String fileFieldId, 
			Element imagePreviewEl,
			CroppedImageUploadViewImpl v) /*-{
		var fileToUploadElement = $doc.getElementById(fileFieldId);
		var file;
		var canProcess = true;
		if (fileToUploadElement && 'files' in fileToUploadElement) {
			file = fileToUploadElement.files[0];
			canProcess = file.type.startsWith('image/');
		}

		if (file && canProcess) {
			var fileUrl = $wnd.URL.createObjectURL(file);
			if ($wnd.cropping) {
				$wnd.cropping.destroy();
			}
			$wnd.cropping = new $wnd.Croppie(imagePreviewEl, {
				enableExif: true,
				enableOrientation: true,
			    viewport: { width: 200, height: 200 },
			    boundary: { width: 400, height: 400 }
			});
			$wnd.cropping.bind({
			    url: fileUrl
			});
		} else {
			// send back original content
			v.@org.sagebionetworks.web.client.widget.upload.CroppedImageUploadViewImpl::cancelCropImage()();
		}
		
		
	}-*/;

	private static native void _getCroppedImageBlob(CroppedImageUploadViewImpl v) /*-{
		try {
			$wnd.cropping.result({type: 'blob', format: 'jpeg', quality: 1, circle: false}).then(function(blob) {
				v.@org.sagebionetworks.web.client.widget.upload.CroppedImageUploadViewImpl::saveCroppedImage(Lcom/google/gwt/core/client/JavaScriptObject;)(blob);
			});
		} catch (err) {
			console.error(err);
		}
	}-*/;
	
	public void saveCroppedImage() {
		// get the blob from the cropper
		_getCroppedImageBlob(this);
	}
	
	public void saveCroppedImage(JavaScriptObject blob) {
		// get the blob from the cropper
		loadingUI.setVisible(false);
		previewModal.hide();
		presenter.onFileProcessed(new JavaScriptObjectWrapper(blob), "image/jpeg");
	}
	
	public void cancelCropImage() {
		loadingUI.setVisible(false);
		resetForm();
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
		previewModal.hide();
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
