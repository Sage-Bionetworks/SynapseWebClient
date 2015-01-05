package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ShinySiteWidgetViewImpl extends FlowPanel implements ShinySiteWidgetView {

	private Presenter presenter;
	
	@Inject
	public ShinySiteWidgetViewImpl() {
	}
	
	@Override
	public void configure(String siteUrl, int height) {
		this.clear();
		add(new HTMLPanel(getShinySiteHTML(siteUrl, height)));
	}	
	
	public static String getShinySiteHTML(String siteUrl, int height){
		
		StringBuilder sb = new StringBuilder();
		sb.append("<iframe width=\"100%\" height=\""+ height +"\" src=\"");
		sb.append(siteUrl);
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

	@Override
	public void showInvalidSiteUrl(String siteUrl) {
		this.clear();
		add(new HTMLPanel(DisplayUtils.getMarkdownWidgetWarningHtml(siteUrl + DisplayConstants.INVALID_SHINY_SITE)));
	}		
	
	/*
	 * Private Methods
	 */

}
