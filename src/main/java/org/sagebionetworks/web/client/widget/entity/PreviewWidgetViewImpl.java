package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.SynapseJSNIUtils;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
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
		sb.append("<p  class=\"file-preview\">");
		sb.append("<a href=\"");
		sb.append(fullFileUrl);
		sb.append("\"><img class=\"imageDescriptor\" ");
		sb.append(" src=\"");
		sb.append(previewUrl);
		sb.append("\"></img></a>");
		sb.append("</p>");
		add(new HTMLPanel(sb.toString()));
	}
	
	@Override
	public void setCodePreview(String code) {
		clear();
		StringBuilder sb = new StringBuilder();
		sb.append("<pre class=\"file-preview\" style=\"overflow:auto;white-space:pre\"><code style=\"background-color:white;\">");
		sb.append(code);
		sb.append("</code></pre>");
		add(new HTMLPanel(sb.toString()));
		synapseJSNIUtils.highlightCodeBlocks();
	}
	
	@Override
	public void setTextPreview(String text) {
		clear();
		StringBuilder sb = new StringBuilder();
		sb.append("<pre class=\"file-preview\" style=\"overflow:auto;white-space:pre\">");
		sb.append(text);
		sb.append("</pre>");
		add(new HTMLPanel(sb.toString()));
	}
	
	@Override
	public void setTablePreview(String text, String delimiter) {
		clear();
		StringBuilder sb = new StringBuilder();
		sb.append("<table class=\"file-preview previewtable\" style=\"overflow:auto;display:block;max-height:200px\">");
		String[] lines = text.split("[\r\n]");
		for (int i = 0; i < lines.length; i++) {
			sb.append("<tr>");
			String[] cells = lines[i].split(delimiter + "(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
			for (int j = 0; j < cells.length; j++) {
				sb.append("<td style=\"border-top:0px\">");
				sb.append(SafeHtmlUtils.htmlEscapeAllowEntities(cells[j]));
				sb.append("</td>");
			}
			sb.append("</tr>");
		}
		sb.append("</table>");
		add(new HTMLPanel(sb.toString()));
	}
	
	@Override
	public void showErrorMessage(String message) {
		clear();
		add(new HTMLPanel(message));
	}
	
}
