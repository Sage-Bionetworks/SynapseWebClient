package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class YouTubeWidgetViewImpl extends FlowPanel implements YouTubeWidgetView {

	private Presenter presenter;
	
	@Inject
	public YouTubeWidgetViewImpl() {
	}
	
	@Override
	public void configure(String videoId) {
		this.clear();
		add(new HTMLPanel(getYouTubeHTML(videoId)));
	}	
	
	public static String getYouTubeHTML(String videoId){
		
		StringBuilder sb = new StringBuilder();
		sb.append("<iframe width=\"560\" height=\"315\" src=\"https://www.youtube.com/embed/");
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
