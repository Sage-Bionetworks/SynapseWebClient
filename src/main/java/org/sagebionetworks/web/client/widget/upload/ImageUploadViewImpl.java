package org.sagebionetworks.web.client.widget.upload;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.Progress;
import org.gwtbootstrap3.client.ui.ProgressBar;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageUploadViewImpl implements ImageUploadView {

	private static final String PREFIX_FILE_INPUT_WIDGET = "imageInputWidget";

	/**
	 * Used to ensure each new instance of this widget has its own ID. This is
	 * important because the ID is used when interacting with the actual DOM
	 * element.
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
	Image image;
	@UiField
	CanvasElement originalCanvas;
	@UiField
	CanvasElement resizedCanvas;
	@UiField
	HTMLPanel loadingUI;
	
	@Inject
	public ImageUploadViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		// Create a unique for each new instance.
		this.fileInput.getElement().setId(PREFIX_FILE_INPUT_WIDGET + ID_SEQUENCE++);

		// when a file is selected notify the presenter
		this.fileInput.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				// load into image
				loadingUI.setVisible(true);
				_loadImage(image.getElement(), fileInput.getElement().getId());
			}
		});
		
		image.addLoadHandler(new LoadHandler() {
			@Override
			public void onLoad(LoadEvent event) {
				_resize(ImageUploadViewImpl.this, image.getElement(), originalCanvas, resizedCanvas);
			}
		});

		this.uploadbutton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// When they press the button trigger the input box
				fileInput.getElement().<InputElement> cast().click();
			}
		});
		if (!isLoaded) {
			_initResizer();
			isLoaded = true;
		}
	}

	private static native void _loadImage(Element imgElement, String fileFieldId) /*-{
		var fileToUploadElement = $doc.getElementById(fileFieldId);
		if (fileToUploadElement && 'files' in fileToUploadElement) {
			var fr = new FileReader();
			fr.onload = function() {
				imgElement.src = fr.result;
			}
			fr.readAsDataURL(fileToUploadElement.files[0]);
		}
	}-*/;
	
	private static native void _initResizer() /*-{
		$wnd.resizer = $wnd.pica({ features: ['all'] });	
	}-*/;
	
	private static native void _resize(ImageUploadViewImpl v, Element imgElement, CanvasElement originalCanvas,
			CanvasElement resizedCanvas) /*-{
		// Create an empty canvas element of the same dimensions as the original
		originalCanvas.width = imgElement.width;
		originalCanvas.height = imgElement.height;
		// Copy the image contents to the canvas
		var ctx = originalCanvas.getContext("2d");
		ctx.drawImage(imgElement, 0, 0);
		if (imgElement.width > 1600 || imgElement.height > 2200) {
			if (imgElement.width > 1600) {
				// continue resize based on width
				var ratio = 1600 / imgElement.width; // get ratio for scaling image
				resizedCanvas.width = 1600;
				resizedCanvas.height = imgElement.height * ratio;
			} else {
				// continue resize based on height
				var ratio = 2200 / imgElement.height; // get ratio for scaling image
				resizedCanvas.height = 2200;
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
		} else {
			// small enough, send back original content
			originalCanvas.toBlob(function(blob) {
				v.@org.sagebionetworks.web.client.widget.upload.ImageUploadViewImpl::resizeComplete(Lcom/google/gwt/core/client/JavaScriptObject;)(blob);
			});
		}
	}-*/;

	public void resizeComplete(JavaScriptObject blob) {
		loadingUI.setVisible(false);
		presenter.resizeComplete(blob);
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
