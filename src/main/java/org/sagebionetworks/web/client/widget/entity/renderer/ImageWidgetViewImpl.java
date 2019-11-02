package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.shared.WebConstants;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageWidgetViewImpl extends FlowPanel implements ImageWidgetView {

	private GlobalApplicationState globalApplicationState;
	private ClientCache clientCache;
	private static final int MAX_IMAGE_WIDTH = 940;
	// if image fails to load from the given source, it will try to load from the cache (this is for the
	// case when the image has been uploaded, but the wiki has not yet been saved)
	private boolean hasTriedCache;
	private Image image;
	IsWidget synAlert;
	Presenter p;
	String styles = "";

	@Inject
	public ImageWidgetViewImpl(GlobalApplicationState globalApplicationState, ClientCache clientCache) {
		this.globalApplicationState = globalApplicationState;
		this.clientCache = clientCache;
	}

	@Override
	public void setSynAlert(IsWidget w) {
		synAlert = w;
		add(synAlert);
	}

	@Override
	public void setPresenter(Presenter p) {
		this.p = p;
	}

	@Override
	public void configure(final String url, final String fileName, final String scale, String alignment, String altText, final String synapseId, final boolean isLoggedIn) {
		clear();
		add(synAlert);
		hasTriedCache = false;
		// Add a html panel that contains the image src from the attachments server (to pull asynchronously)

		image = new Image();
		if (synapseId != null) {
			image.addStyleName("imageButton");
			image.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					// go to the relevant Synapse page
					globalApplicationState.getPlaceChanger().goTo(new Synapse(synapseId));
				}
			});
		}
		String imageStyleNames = ImageWidget.getAlignmentStyleNames(alignment);
		if (imageStyleNames != null && imageStyleNames.length() > 0) {
			image.addStyleName(imageStyleNames);
		}
		if (altText == null) {
			altText = "";
		}
		image.getElement().setAttribute("alt", altText);

		// don't show until we have the correct size (otherwise it's initially shown at 100%, then scaled
		// down!).
		image.addErrorHandler(new ErrorHandler() {
			@Override
			public void onError(ErrorEvent event) {
				if (!hasTriedCache) {
					hasTriedCache = true;
					String newUrl = clientCache.get(fileName + WebConstants.TEMP_IMAGE_ATTACHMENT_SUFFIX);
					if (newUrl != null && newUrl.length() > 0) {
						image.setUrl(newUrl);
						return;
					}
				}
				if (synapseId != null) {
					if (!isLoggedIn)
						p.handleLoadingError(DisplayConstants.IMAGE_FAILED_TO_LOAD + "You may need to log in to gain access to this image content (" + synapseId + ")");
					else
						p.handleLoadingError(DisplayConstants.IMAGE_FAILED_TO_LOAD + "Unable to view image " + synapseId);
				}

				else if (fileName != null)
					p.handleLoadingError(DisplayConstants.IMAGE_FAILED_TO_LOAD + fileName);
				else
					p.handleLoadingError(DisplayConstants.IMAGE_FAILED_TO_LOAD + url);
			}
		});
		image.addLoadHandler(new LoadHandler() {
			@Override
			public void onLoad(LoadEvent event) {
				try {
					image.removeStyleName("blur");
					float imageHeight = image.getHeight();
					float imageWidth = image.getWidth();
					if (scale != null && !"100".equals(scale) && imageWidth > 0 && imageHeight > 0) {
						// scale is specified
						final float scaleFloat = Float.parseFloat(scale) * .01f;
						if (scaleFloat < 0) {
							throw new IllegalArgumentException("Image scale must be positive.");
						}
						// scale image
						float scaledImageWidth = (imageWidth * scaleFloat);
						// if the scaled width is too wide for the screen, then render the max width that we can
						if (scaledImageWidth > MAX_IMAGE_WIDTH) {
							setImageToMaxSize(imageWidth, imageHeight);
						} else {
							image.setWidth(scaledImageWidth + "px");
							float scaledImageHeight = (imageHeight * scaleFloat);
							image.setHeight(scaledImageHeight + "px");
						}
					}
					updateImageStyles();
				} catch (Throwable e) {
					remove(image);
					p.handleLoadingError(DisplayConstants.IMAGE_FAILED_TO_LOAD + e.getMessage());
				}
			}

			private void setImageToMaxSize(float imageWidth, float imageHeight) {
				image.setWidth(MAX_IMAGE_WIDTH + "px");
				image.setHeight(imageHeight * MAX_IMAGE_WIDTH / imageWidth + "px");
			}
		});
		add(image);
		image.setUrl(url);
		image.addStyleName("blur");
	}

	public void addStyleName(String style) {
		styles += " " + style;
		updateImageStyles();
	}

	private void updateImageStyles() {
		if (image != null && !styles.isEmpty()) {
			image.addStyleName(styles);
		}
	}

	@Override
	public Widget asWidget() {
		return this;
	}
}
