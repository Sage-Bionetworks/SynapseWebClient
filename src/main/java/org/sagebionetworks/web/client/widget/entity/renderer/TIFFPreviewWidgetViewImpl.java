package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

public class TIFFPreviewWidgetViewImpl
  extends FlowPanel
  implements TIFFPreviewWidgetView {

  private Image image;
  private SynapseAlert synAlert;

  @Inject
  public TIFFPreviewWidgetViewImpl(SynapseAlert synAlert) {
    this.synAlert = synAlert;
  }

  @Override
  public void configure(final String url) {
    clear();
    add(synAlert);
    _setImagePresignedUrl(url, this);
  }

  @Override
  public void showError(String error) {
    clear();
    add(synAlert);
    synAlert.showError(error);
  }

  public void setPng(String localPngUrl, int width, int height) {
    image = new Image();
    image.addErrorHandler(
      new ErrorHandler() {
        @Override
        public void onError(ErrorEvent event) {
          synAlert.showError(
            DisplayConstants.IMAGE_FAILED_TO_LOAD + localPngUrl
          );
        }
      }
    );
    image.addLoadHandler(
      new LoadHandler() {
        @Override
        public void onLoad(LoadEvent event) {
          try {
            image.removeStyleName("blur");
            image.setWidth(Integer.toString(width));
            image.setHeight(Integer.toString(height));
          } catch (Throwable e) {
            remove(image);
            synAlert.showError(
              DisplayConstants.IMAGE_FAILED_TO_LOAD +
              localPngUrl +
              ": " +
              e.getMessage()
            );
          }
        }
      }
    );

    add(image);
    image.setUrl(localPngUrl);
    image.addStyleName("blur");
  }

  private static native void _setImagePresignedUrl(
    String url,
    TIFFPreviewWidgetViewImpl view
  ) /*-{
		try {
			var xhr = new XMLHttpRequest();
			xhr.responseType = 'arraybuffer';
			xhr.open('GET', url);
			xhr.onload = function(e) {
			// Decode image into canvas.
			var pages = $wnd.UTIF.decode(e.target.response);
			// console.log('page count: ' + pages.length);
			$wnd.UTIF.decodeImage(e.target.response, pages[0]);
			var rgba = $wnd.UTIF.toRGBA8(pages[0]); // Uint8Array with RGBA pixels
			if (rgba) {
				var canvas = document.createElement('canvas');
				canvas.width = pages[0].width;
				canvas.height = pages[0].height;
				var ctx = canvas.getContext('2d');
				var imageData = ctx.createImageData(pages[0].width, pages[0].height);
				for (var i = 0; i < rgba.length; i++) {
					imageData.data[i] = rgba[i];
				}
				ctx.putImageData(imageData, 0, 0);
				// Convert canvas to img
				var dataUrl = canvas.toDataURL('image/png');
				view.@org.sagebionetworks.web.client.widget.entity.renderer.TIFFPreviewWidgetViewImpl::setPng(Ljava/lang/String;II)(dataUrl, canvas.width, canvas.height);
			}
		};
		xhr.send();
		} catch (err) {
			console.error(err);
		}
	}-*/;

  @Override
  public Widget asWidget() {
    return this;
  }
}
