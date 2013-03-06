package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;

import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PreviewWidgetViewImpl extends SimplePanel implements PreviewWidgetView, IsWidget{
	private Presenter presenter;
	private SynapseJSNIUtils synapseJSNIUtils;
	
	@Inject
	public PreviewWidgetViewImpl(SynapseJSNIUtils synapseJSNIUtils) {
		this.synapseJSNIUtils = synapseJSNIUtils;
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}
	
	@Override
	public void setImagePreview(String fullFileUrl, String previewUrl) {
		clear();
		StringBuilder sb = new StringBuilder();
		sb.append("<a href=\"");
		sb.append(fullFileUrl);
		sb.append("\"><img class=\"imageDescriptor\" ");
		sb.append(" src=\"");
		sb.append(previewUrl);
		sb.append("\"></img></a>");
		add(new HTMLPanel(sb.toString()));
	}
	
	@Override
	public void setCodePreview(String code) {
		clear();
		setStylePrimaryName("markdown");
		StringBuilder sb = new StringBuilder();
		sb.append("<pre><code>");
		sb.append(code);
		sb.append("</code></pre>");
		add(new HTMLPanel(sb.toString()));
		synapseJSNIUtils.highlightCodeBlocks();
	}
	
	@Override
	public void setBlockQuotePreview(String text) {
		clear();
		setStylePrimaryName("markdown");
		StringBuilder sb = new StringBuilder();
		sb.append("<blockquote>");
		sb.append(text);
		sb.append("</blockquote>");
		add(new HTMLPanel(sb.toString()));
	}
	
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
}
