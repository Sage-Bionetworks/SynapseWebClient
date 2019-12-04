package org.sagebionetworks.web.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.Image;

/**
 * Image that you can set the max size in the constructor
 * 
 * @author jayhodgson
 *
 */
public class FitImage extends Image {

	public FitImage(String url, final int maxWidth, final int maxHeight) {
		setVisible(false);
		this.addLoadHandler(new LoadHandler() {
			@Override
			public void onLoad(LoadEvent event) {
				Element element = event.getRelativeElement();
				if (element == getElement()) {
					int originalHeight = getHeight();
					int originalWidth = getWidth();
					if (originalHeight > originalWidth) {
						setHeight(maxHeight + "px");
						addStyleName("autowidth");
					} else {
						setWidth(maxWidth + "px");
						addStyleName("autoheight");
					}
					setVisible(true);
				}
			}
		});
		setUrl(url);
	}
}
