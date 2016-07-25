package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageWidgetViewImpl extends FlowPanel implements ImageWidgetView {

	private Presenter presenter;
	private SynapseJSNIUtils synapseJsniUtils;
	private GlobalApplicationState globalApplicationState;
	private ClientCache clientCache;
	private static final int MAX_IMAGE_WIDTH = 940;
	//if image fails to load from the given source, it will try to load from the cache (this is for the case when the image has been uploaded, but the wiki has not yet been saved)
	private boolean hasTriedCache;
	private Image image;
	@Inject
	public ImageWidgetViewImpl(SynapseJSNIUtils synapseJsniUtils, GlobalApplicationState globalApplicationState, ClientCache clientCache) {
		this.synapseJsniUtils = synapseJsniUtils;
		this.globalApplicationState = globalApplicationState;
		this.clientCache = clientCache;
	}

	@Override
	public void configure(WikiPageKey wikiKey, final String fileName,
			final String scale, String alignment, final String synapseId, final boolean isLoggedIn, Long wikiVersion, String xsrfToken) {
		this.clear();
		hasTriedCache = false;
		// Add a html panel that contains the image src from the attachments server (to pull asynchronously)
		
		final String url;
		// If the wiki page is showing a different/old version, we need to get the URL to that version's attachments
		if(wikiVersion != null) {
			url = synapseId != null ? DisplayUtils.createFileEntityUrl(synapseJsniUtils.getBaseFileHandleUrl(), synapseId, null, false, xsrfToken) :
				DisplayUtils.createVersionOfWikiAttachmentUrl(synapseJsniUtils.getBaseFileHandleUrl(), wikiKey, fileName,false, wikiVersion);
		} else {
			url = synapseId != null ? DisplayUtils.createFileEntityUrl(synapseJsniUtils.getBaseFileHandleUrl(), synapseId, null, false, xsrfToken) :
				DisplayUtils.createWikiAttachmentUrl(synapseJsniUtils.getBaseFileHandleUrl(), wikiKey, fileName,false);
		}
		
		image = new Image();
		if (synapseId != null) {
			image.addStyleName("imageButton");
			image.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					//go to the relevant Synapse page
					globalApplicationState.getPlaceChanger().goTo(new Synapse(synapseId));
				}
			});
		}

		if (alignment != null) {
			String trimmedAlignment = alignment.trim();
			if (WidgetConstants.FLOAT_LEFT.equalsIgnoreCase(trimmedAlignment)) {
				image.addStyleName("floatleft");
				image.addStyleName("margin-right-10");
			} else if (WidgetConstants.FLOAT_RIGHT.equalsIgnoreCase(trimmedAlignment)) {
				image.addStyleName("floatright");
				image.addStyleName("margin-left-10");
			}else if (WidgetConstants.FLOAT_CENTER.equalsIgnoreCase(trimmedAlignment)) {
				image.addStyleName("align-center");
			} else if (!WidgetConstants.FLOAT_NONE.equalsIgnoreCase(trimmedAlignment)) {
				showError("Alignment value not recognized: " + alignment);
				return;
			}
		}
		//don't show until we have the correct size (otherwise it's initially shown at 100%, then scaled down!).
		image.addErrorHandler(new ErrorHandler() {
			@Override
		    public void onError(ErrorEvent event) {
				if (!hasTriedCache) {
					hasTriedCache = true;
					String newUrl = clientCache.get(fileName+WebConstants.TEMP_IMAGE_ATTACHMENT_SUFFIX);
					if (newUrl != null && newUrl.length() > 0) {
						image.setUrl(newUrl);
						return;
					}
				}
				if (synapseId != null) {
					if (!isLoggedIn) 
						showError(DisplayConstants.IMAGE_FAILED_TO_LOAD + "You may need to log in to gain access to this image content (" + synapseId+")");
					else 
						showError(DisplayConstants.IMAGE_FAILED_TO_LOAD + "Unable to view image " + synapseId);
				}

				else if (fileName != null)
					showError(DisplayConstants.IMAGE_FAILED_TO_LOAD + fileName);
				else
					showError(DisplayConstants.IMAGE_FAILED_TO_LOAD + url);
		    }
		});
		image.addLoadHandler(new LoadHandler() {
			@Override
			public void onLoad(LoadEvent event) {
				try {
					float imageHeight = image.getHeight();
					float imageWidth = image.getWidth();
					if (scale != null && !"100".equals(scale) && imageWidth > 0 && imageHeight > 0) {
						//scale is specified
						final float scaleFloat = Float.parseFloat(scale) * .01f;
						if (scaleFloat < 0) {
							throw new IllegalArgumentException("Image scale must be positive.");
						}
						// scale image
						float scaledImageWidth = (imageWidth * scaleFloat);
						//if the scaled width is too wide for the screen, then render the max width that we can
						if (scaledImageWidth > MAX_IMAGE_WIDTH) {
							setImageToMaxSize(imageWidth, imageHeight);
						}
						else {
							image.setWidth(scaledImageWidth + "px");
							float scaledImageHeight = (imageHeight * scaleFloat);
							image.setHeight(scaledImageHeight + "px");
						}
					}
					image.getElement().getStyle().setVisibility(Visibility.VISIBLE);
				} catch (Throwable e) {
					remove(image);
					showError(DisplayConstants.IMAGE_FAILED_TO_LOAD + e.getMessage());
				}
			}

			private void setImageToMaxSize(float imageWidth, float imageHeight) {
				image.setWidth(MAX_IMAGE_WIDTH + "px");	
				image.setHeight(imageHeight * MAX_IMAGE_WIDTH / imageWidth + "px");
			}
		});
		image.getElement().getStyle().setVisibility(Visibility.HIDDEN);
		add(image);
		image.setUrl(url);
	}
	
	public void addStyleName(String style) {
		if (image != null) {
			image.addStyleName(style);
		}
	}

	public void showError(String error) {
		add(new HTMLPanel(DisplayUtils.getMarkdownWidgetWarningHtml(error)));
	}

	@Override
	public Widget asWidget() {
		return this;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}


	/*
	 * Private Methods
	 */

}