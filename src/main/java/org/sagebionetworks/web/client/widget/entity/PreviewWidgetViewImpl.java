package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.ModalSize;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.modal.Dialog;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;

import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PreviewWidgetViewImpl extends FlowPanel implements PreviewWidgetView, IsWidget{
	private Presenter presenter;
	private SynapseJSNIUtils synapseJSNIUtils;
	private String previewHtml;
	private Anchor fullScreenAnchor; 
	private Dialog previewDialog;
	private boolean isCode;
	
	@Inject
	public PreviewWidgetViewImpl(SynapseJSNIUtils synapseJsniUtils, IconsImageBundle iconsImageBundle, Dialog dialog) {
		this.synapseJSNIUtils = synapseJsniUtils;
		this.previewDialog = dialog;
		dialog.setSize(ModalSize.LARGE);
		fullScreenAnchor = new Anchor(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(iconsImageBundle.fullScreen16())));
		fullScreenAnchor.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (previewHtml != null) {
					previewDialog.configure("Preview", new HTMLPanel(previewHtml), DisplayConstants.OK, null, null, true);
					previewDialog.show();
					if (isCode) {
						synapseJSNIUtils.highlightCodeBlocks();
					}
				}
			}
		});
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
	public void setImagePreview(final String fullFileUrl, String previewUrl) {
		clear();
	
		final Image image = new Image();
		image.addStyleName("imageButton imageDescriptor");
		image.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow(fullFileUrl, "", "");
			}
		});
		image.addErrorHandler(new ErrorHandler() {
			@Override
		    public void onError(ErrorEvent event) {
				presenter.imagePreviewLoadFailed(event);
		    }
		});
		
		add(image);
		image.setUrl(previewUrl);
	}
	
	@Override
	public void setCodePreview(String code) {
		clear();
		StringBuilder sb = new StringBuilder();
		sb.append("<pre style=\"overflow:auto;white-space:pre;\"><code style=\"background-color:white;\">");
		sb.append(code);
		sb.append("</code></pre>");
		setPreview(sb.toString());
		synapseJSNIUtils.highlightCodeBlocks();
		isCode = true;
	}
	
	@Override
	public void setTextPreview(String text) {
		clear();
		StringBuilder sb = new StringBuilder();
		sb.append("<pre style=\"overflow:auto;white-space:pre;\">");
		sb.append(text);
		sb.append("</pre>");
		setPreview(sb.toString());
	}
	
	@Override
	public void setTablePreview(String text, String delimiter) {
		clear();
		StringBuilder sb = new StringBuilder();
		sb.append("<table class=\"previewtable\" style=\"overflow:auto;display:block\">");
		String[] lines = text.split("[\r\n]");
		for (int i = 0; i < lines.length; i++) {
			sb.append("<tr>");
			String[] cells = lines[i].split(delimiter + "(?=(?:[^\"]*\"[^\"]*\")*(?![^\"]*\"))");
			for (int j = 0; j < cells.length; j++) {
				sb.append("<td>");
				sb.append(SafeHtmlUtils.htmlEscapeAllowEntities(cells[j]));
				sb.append("</td>");
			}
			sb.append("</tr>");
		}
		sb.append("</table>");
		setPreview(sb.toString());
	}
	
	@Override
	public void clear() {
		previewHtml = null;
		isCode = false;
		super.clear();
	}
	
	private void setPreview(String html) {
		previewHtml = html;
		add(fullScreenAnchor);
		ScrollPanel wrapper= new ScrollPanel(new HTMLPanel(html));
		wrapper.setHeight("205px");
		add(wrapper);
	}
	@Override
	public void addSynapseAlertWidget(Widget w) {
		clear();
		add(w);
	}
	
}
