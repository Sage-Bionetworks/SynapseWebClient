package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ShinySiteWidgetViewImpl extends LayoutContainer implements ShinySiteWidgetView {

	private Presenter presenter;
	
	@Inject
	public ShinySiteWidgetViewImpl() {
	}
	
	@Override
	public void configure(String siteUrl, int width, int height) {
		this.removeAll();
		add(new HTMLPanel(getShinySiteHTML(siteUrl, width, height)));
	}	
	
	public static String getShinySiteHTML(String siteUrl, int width, int height){
		
		StringBuilder sb = new StringBuilder();
		sb.append("<iframe width=\"" + width + "\" height=\""+ height +"\" src=\"");
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
		this.removeAll();
		add(new HTMLPanel("<div class=\"alert alert-block\"><strong>"+ DisplayConstants.MARKDOWN_WIDGET_WARNING + "</strong><br/> " + siteUrl + DisplayConstants.INVALID_SHINY_SITE + "</div>"));
	}		
	
	/*
	 * Private Methods
	 */

}
