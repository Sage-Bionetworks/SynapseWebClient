package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class VideoWidgetViewImpl extends FlowPanel implements VideoWidgetView {

	private Presenter presenter;
	private SynapseJSNIUtils synapseJsniUtils;
	private GlobalApplicationState globalApplicationState;
	
	@Inject
	public VideoWidgetViewImpl(SynapseJSNIUtils synapseJsniUtils, GlobalApplicationState globalApplicationState) {
		this.synapseJsniUtils = synapseJsniUtils;
		this.globalApplicationState = globalApplicationState;
	}

	@Override
	public void configure(WikiPageKey wikiKey, String mp4SynapseId, String oggSynapseId, String webmSynapseId, String width, String height, final boolean isLoggedIn, Long wikiVersion) {
		this.clear();
		
		StringBuilder builder = new StringBuilder();
		
		builder.append("<video width=\"");
		if (width != null)
			builder.append(SafeHtmlUtils.htmlEscape(width));
		else builder.append("640");
		
		builder.append("\" height=\"");
		if (height != null)
			builder.append(SafeHtmlUtils.htmlEscape(height));
		else builder.append("480");
		
		builder.append("\" controls>");
		if (mp4SynapseId != null) {
			builder.append("<source src=\"");
			builder.append(DisplayUtils.createFileEntityUrl(synapseJsniUtils.getBaseFileHandleUrl(), mp4SynapseId, null, false));
			builder.append("\" type=\"video/mp4\">");
		}
		
		if (oggSynapseId != null) {
			builder.append("<source src=\"");
			builder.append(DisplayUtils.createFileEntityUrl(synapseJsniUtils.getBaseFileHandleUrl(), oggSynapseId, null, false));
			builder.append("\" type=\"video/ogg\">");
		}
		
		if (webmSynapseId != null) {
			builder.append("<source src=\"");
			builder.append(DisplayUtils.createFileEntityUrl(synapseJsniUtils.getBaseFileHandleUrl(), webmSynapseId, null, false));
			builder.append("\" type=\"video/webm\">");
		}
		
		//and finally, alt text if the browser does not support
		builder.append("Your browser does not support the video tag. </video>");
		
		add(new HTML(builder.toString()));
	}

	public void showError(String error) {
		clear();
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