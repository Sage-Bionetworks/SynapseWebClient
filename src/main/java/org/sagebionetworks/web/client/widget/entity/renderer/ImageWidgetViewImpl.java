package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageWidgetViewImpl extends LayoutContainer implements ImageWidgetView {

	private Presenter presenter;
	private SynapseJSNIUtils synapseJsniUtils;
	private GlobalApplicationState globalApplicationState;
	private static final int MAX_IMAGE_WIDTH = 940;
	@Inject
	public ImageWidgetViewImpl(SynapseJSNIUtils synapseJsniUtils, GlobalApplicationState globalApplicationState) {
		this.synapseJsniUtils = synapseJsniUtils;
		this.globalApplicationState = globalApplicationState;
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, final String fileName,
			final String scale, String alignment, final String synapseId, final boolean isLoggedIn) {
		this.removeAll();
		//add a html panel that contains the image src from the attachments server (to pull asynchronously)
		//create img
		final String url = synapseId != null ? DisplayUtils.createFileEntityUrl(synapseJsniUtils.getBaseFileHandleUrl(), synapseId, null, false) :
			DisplayUtils.createWikiAttachmentUrl(synapseJsniUtils.getBaseFileHandleUrl(), wikiKey, fileName,false);

		final Image image = new Image(url);
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
		image.setVisible(false);
		image.addErrorHandler(new ErrorHandler() {
			@Override
		    public void onError(ErrorEvent event) {
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
					if (scale != null && !"100".equals(scale)) {
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
					else if (imageWidth > MAX_IMAGE_WIDTH){
						//if scale is not specified (or if 100%), then only scale this image if it's too wide to fit in the screen
						setImageToMaxSize(imageWidth, imageHeight);
					}
					image.setVisible(true);
				} catch (Exception e) {
					remove(image);
					showError(DisplayConstants.IMAGE_FAILED_TO_LOAD + e.getMessage());
				}
			}
			
			private void setImageToMaxSize(float imageWidth, float imageHeight) {
				image.setWidth(MAX_IMAGE_WIDTH + "px");	
				image.setHeight(imageHeight * MAX_IMAGE_WIDTH / imageWidth + "px");
			}
		});
		
		add(image);
		this.layout(true);
	}
	
	public void showError(String error) {
		add(new HTMLPanel(DisplayUtils.getMarkdownWidgetWarningHtml(error)));
		layout(true);
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
