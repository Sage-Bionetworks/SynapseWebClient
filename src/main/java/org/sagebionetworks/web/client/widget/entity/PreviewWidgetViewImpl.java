package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.modal.Dialog;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PreviewWidgetViewImpl extends FlowPanel implements PreviewWidgetView, IsWidget{
	private Presenter presenter;
	private SynapseJSNIUtils synapseJSNIUtils;
	private boolean isCode;
	private SageImageBundle sageImageBundle;
	private Dialog previewDialog;
	private Widget dialogContent;
	@Inject
	public PreviewWidgetViewImpl(SynapseJSNIUtils synapseJsniUtils, 
			SageImageBundle sageImageBundle, 
			final Dialog previewDialog) {
		this.synapseJSNIUtils = synapseJsniUtils;
		this.sageImageBundle = sageImageBundle;
		this.previewDialog = previewDialog;
		previewDialog.configure("", null, "Close", new Dialog.Callback() {
			@Override
			public void onDefault() {
				hideFullscreenPreview();
			}
			
			@Override
			public void onPrimary() {
			}
		}, false);
		previewDialog.addStyleName("modal-fullscreen");
	}
	
	private void showFullscreenPreview() {
		dialogContent = getWidget(0);
		dialogContent.removeFromParent();
		FocusPanel panel = new FocusPanel();
		panel.addStyleName("margin-right-20");
		panel.add(dialogContent);
		panel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hideFullscreenPreview();
			}
		});
		previewDialog.add(panel);
		previewDialog.show();
	}
	private void hideFullscreenPreview() {
		dialogContent.removeFromParent();
		add(dialogContent);
		previewDialog.hide();
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
	public void setImagePreview(final String fullFileUrl) {
		clear();
		Image fullImage = new Image();
		fullImage.addStyleName("imageButton maxWidth100 maxHeight100 margin-left-20");
		fullImage.addErrorHandler(new ErrorHandler() {
			@Override
		    public void onError(ErrorEvent event) {
				presenter.imagePreviewLoadFailed(event);
		    }
		});
		add(fullImage);
		fullImage.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showFullscreenPreview();
			}
		});
		fullImage.setUrl(fullFileUrl);
	}
	
	@Override
	public void showLoading() {
		clear();
		add(new HTMLPanel(DisplayUtils.getLoadingHtml(sageImageBundle)));
	}
	@Override
	public void setPreviewWidget(IsWidget w) {
		clear();
		add(w.asWidget());
	}
	
	@Override
	public void setCodePreview(String code) {
		clear();
		add(new HTMLPanel(getCodeHtml(code)));
		synapseJSNIUtils.highlightCodeBlocks();
		isCode = true;
	}
	
	private String getCodeHtml(String code) {
		StringBuilder sb = new StringBuilder();
		sb.append("<pre style=\"overflow:auto;white-space:pre;\"><code style=\"background-color:white;\">");
		sb.append(code);
		sb.append("</code></pre>");
		return sb.toString();
	}
	
	@Override
	public void setTextPreview(String text) {
		clear();
		add(new HTMLPanel(getTextPreviewHtml(text)));
	}
	
	private String getTextPreviewHtml(String text) {
		StringBuilder sb = new StringBuilder();
		sb.append("<pre style=\"overflow:auto;white-space:pre;\">");
		sb.append(text);
		sb.append("</pre>");
		return sb.toString();
	}
	
	@Override
	public void setTablePreview(String text, String delimiter) {
		clear();
		add(new HTMLPanel(getTableHtml(text, delimiter)));
	}
	
	private String getTableHtml(String text, String delimiter) {
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
		return sb.toString();
	}
	
	@Override
	public void clear() {
		isCode = false;
		super.clear();
	}
	
	@Override
	public void setHTML(final String htmlContent) {
		clear();
		add(getFrame(htmlContent));
	}
	
	private Frame getFrame(final String htmlContent) {
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
		
		frame.addAttachHandler(new AttachEvent.Handler() {
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if (event.isAttached()) {
					// use html5 srcdoc if available
					if (synapseJSNIUtils.elementSupportsAttribute(frame.getElement(), "srcdoc")) {
						frame.getElement().setAttribute("srcdoc", htmlContent);	
					} else {
						_setFrameContent(frame.getElement(), htmlContent);	
					}
				}
			}
		});
		return frame;
	}
	
	public static native void _autoAdjustFrameHeight(Element iframe) /*-{
		if(iframe && iframe.contentWindow && iframe.contentWindow.document.body) {
			var newHeightPx = iframe.contentWindow.document.body.scrollHeight;
			if (newHeightPx < 450) {
				newHeightPx = 450;
			}
			var frameHeight = parseInt(iframe.height);
			if (!frameHeight || (newHeightPx != frameHeight && newHeightPx > frameHeight)) {
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
	public void addSynapseAlertWidget(IsWidget w) {
		clear();
		add(w);
	}
}
