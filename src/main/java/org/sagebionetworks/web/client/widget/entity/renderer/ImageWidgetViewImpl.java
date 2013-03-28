package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageWidgetViewImpl extends LayoutContainer implements ImageWidgetView {

	private Presenter presenter;
	private SynapseJSNIUtils synapseJsniUtils;
	private static final int MAX_IMAGE_WIDTH = 940;
	@Inject
	public ImageWidgetViewImpl(SynapseJSNIUtils synapseJsniUtils) {
		this.synapseJsniUtils = synapseJsniUtils;
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, String fileName,
			final String scale, String alignment) {
		this.removeAll();
		//add a html panel that contains the image src from the attachments server (to pull asynchronously)
		//create img
		final Image image = new Image(DisplayUtils.createWikiAttachmentUrl(synapseJsniUtils.getBaseFileHandleUrl(), wikiKey, fileName,false));
		
		if (alignment != null) {
			String trimmedAlignment = alignment.trim();
			if ("left".equalsIgnoreCase(trimmedAlignment)) {
				image.addStyleName("floatleft");
				image.addStyleName("margin-right-10");
			} else if ("right".equalsIgnoreCase(trimmedAlignment)) {
				image.addStyleName("floatright");
				image.addStyleName("margin-left-10");
			}
		}
		//don't show until we have the correct size (otherwise it's initially shown at 100%, then scaled down!).
		image.setVisible(false);
		image.addLoadHandler(new LoadHandler() {
			@Override
			public void onLoad(LoadEvent event) {
				float imageHeight = image.getHeight();
				float imageWidth = image.getWidth();
				if (scale != null && !"100".equals(scale)) {
					//scale is specified
					final float scaleFloat = Float.parseFloat(scale) * .01f;
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
			}
			
			private void setImageToMaxSize(float imageWidth, float imageHeight) {
				image.setWidth(MAX_IMAGE_WIDTH + "px");	
				image.setHeight(imageHeight * MAX_IMAGE_WIDTH / imageWidth + "px");
			}
		});
		
		add(image);
		this.layout(true);
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
