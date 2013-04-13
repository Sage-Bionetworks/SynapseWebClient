package org.sagebionetworks.web.client.widget.entity.renderer;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class YouTubeWidgetViewImpl extends LayoutContainer implements YouTubeWidgetView {

	private Presenter presenter;
	
	@Inject
	public YouTubeWidgetViewImpl() {
	}
	
	@Override
	public void configure(String videoId) {
		this.removeAll();
		add(new HTMLPanel(getYouTubeHTML(videoId)));
	}	
	
	public static String getYouTubeHTML(String videoId){
		
		StringBuilder sb = new StringBuilder();
		sb.append("<iframe width=\"560\" height=\"315\" src=\"http://www.youtube.com/embed/");
		sb.append(videoId);
		sb.append("\" frameborder=\"0\" allowfullscreen></iframe>");
	    return sb.toString();
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
