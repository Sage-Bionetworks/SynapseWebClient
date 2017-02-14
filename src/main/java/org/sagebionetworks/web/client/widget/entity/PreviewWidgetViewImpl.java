package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.modal.Dialog;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PreviewWidgetViewImpl extends FlowPanel implements PreviewWidgetView, IsWidget{
	private Presenter presenter;
	private SynapseJSNIUtils synapseJSNIUtils;
	private Anchor fullScreenAnchor; 
	private Dialog previewDialog;
	private boolean isCode;
	private Widget currentPopupPreviewWidget;
	private SageImageBundle sageImageBundle;
	
	@Inject
	public PreviewWidgetViewImpl(SynapseJSNIUtils synapseJsniUtils, IconsImageBundle iconsImageBundle, SageImageBundle sageImageBundle, Dialog dialog) {
		this.synapseJSNIUtils = synapseJsniUtils;
		this.previewDialog = dialog;
		this.sageImageBundle = sageImageBundle;
		dialog.addStyleName("modal-fullscreen");
		fullScreenAnchor = new Anchor(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(iconsImageBundle.fullScreen16())));
		fullScreenAnchor.addStyleName("position-absolute");
		fullScreenAnchor.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showPopup();
			}
		});
	}
	
	private void showPopup() {
		if (currentPopupPreviewWidget != null) {
			previewDialog.configure("Preview", currentPopupPreviewWidget, DisplayConstants.OK, null, null, true);
			previewDialog.show();
			if (isCode) {
				synapseJSNIUtils.highlightCodeBlocks();
			}
		}
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
		add(fullScreenAnchor);
		final Image image = new Image();
		image.addStyleName("imageButton maxWidth100 maxHeight100 margin-left-20");
		image.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showPopup();
			}
		});
		image.addErrorHandler(new ErrorHandler() {
			@Override
		    public void onError(ErrorEvent event) {
				presenter.imagePreviewLoadFailed(event);
		    }
		});
		Div div = new Div();
		div.setHeight("200px");
		div.add(image);
		add(div);
		image.setUrl(previewUrl);
		
		currentPopupPreviewWidget = new Image(previewUrl);
		currentPopupPreviewWidget.addStyleName("maxWidth100 maxHeight100");
	}
	
	@Override
	public void showLoading() {
		clear();
		add(new HTMLPanel(DisplayUtils.getLoadingHtml(sageImageBundle)));
	}
	@Override
	public void setPreviewWidget(Widget w) {
		clear();
		add(w.asWidget());
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
		currentPopupPreviewWidget = null;
		isCode = false;
		super.clear();
	}
	
	private void setPreview(String html) {
		add(fullScreenAnchor);
		currentPopupPreviewWidget = new HTMLPanel(html);
		ScrollPanel wrapper= new ScrollPanel(new HTMLPanel(html));
		wrapper.setHeight("200px");
		wrapper.addStyleName("margin-left-20");
		add(wrapper);
	}
	
	@Override
	public void setHTML(String htmlContent) {
		clear();
		final Frame frame = new Frame("about:blank");
		frame.getElement().setAttribute("frameborder", "0");
		frame.setWidth("100%");
		frame.addLoadHandler(new LoadHandler() {
			@Override
			public void onLoad(LoadEvent event) {
				_autoAdjustFrameHeight(frame.getElement());
				Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
					@Override
					public boolean execute() {
						_autoAdjustFrameHeight(frame.getElement());
						// keep executing as long as frame is attached
						return frame.isAttached();
					}
				}, 100);
			}
		});
		
		add(frame);
		
		// use html5 srcdoc if available
		if (synapseJSNIUtils.elementSupportsAttribute(frame.getElement(), "srcdoc")) {
			frame.getElement().setAttribute("srcdoc", htmlContent);	
		} else {
			_setFrameContent(frame.getElement(), htmlContent);	
		}
	}
	
	private static native void _autoAdjustFrameHeight(Element iframe) /*-{
		if(iframe && iframe.contentWindow) {
			var newHeightPx = iframe.contentWindow.document.body.scrollHeight;
			if (newHeightPx < 450) {
				newHeightPx = 450;
			}
			var newHeightValue = newHeightPx + "px";
			if (newHeightValue != iframe.height) {
				iframe.height = "";
				iframe.height = (newHeightPx + 50) + "px";
			}
		}
	}-*/;
	
	private static native void _setFrameContent(Element iframe, String htmlContent) /*-{
		if(iframe) {
			try {
				iframe.contentWindow.document.open('text/html', 'replace'); 
				iframe.contentWindow.document.write(htmlContent);
				iframe.contentWindow.document.close();	
			} catch (err) {
				console.error(err);
			}
		}
	}-*/;

  
	@Override
	public void addSynapseAlertWidget(Widget w) {
		clear();
		add(w);
	}
}
