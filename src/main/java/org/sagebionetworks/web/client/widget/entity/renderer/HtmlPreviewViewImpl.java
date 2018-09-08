package org.sagebionetworks.web.client.widget.entity.renderer;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtilsImpl;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HtmlPreviewViewImpl implements HtmlPreviewView {

	public interface Binder extends UiBinder<Widget, HtmlPreviewViewImpl> {}
	
	@UiField
	Div htmlContainer;
	@UiField
	Div synAlertContainer;
	@UiField
	Div loadingUI;
	@UiField
	Div htmlSanitizedWarning;
	@UiField
	Anchor showContentLink;
	@UiField
	Span storeRawHtmlSpan;
	Presenter p;
	SynapseJSNIUtils jsniUtils;
	Widget w;
	@Inject
	public HtmlPreviewViewImpl(Binder binder,
			SynapseJSNIUtils jsniUtils) {
		w = binder.createAndBindUi(this);
		this.jsniUtils = jsniUtils;
		showContentLink.addClickHandler(event->{
			p.onShowFullContent();
		});
	}
	
	@Override
	public Widget asWidget() {
		return w;
	}
	
	@Override
	public void setHtml(String html) {
		htmlContainer.clear();
		htmlContainer.add(getFrame(html, jsniUtils));
	}

	@Override
	public void setLoadingVisible(boolean visible) {
		loadingUI.setVisible(visible);
	}
	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
	@Override
	public void openRawHtmlInNewWindow() {
		String html = storeRawHtmlSpan.getText();
		_openHtmlInNewWindow(html);
	}
	
	private final static native void _openHtmlInNewWindow(String html) /*-{
		var wnd = $wnd.open("", "");
        wnd.document.write(html);
        // close document, to run scripts inside html string 
        wnd.document.close();
	}-*/;
	

	public static Frame getFrame(final String htmlContent, SynapseJSNIUtils jsniUtils) {
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
				}, 200);
			}
		});
		
		frame.addAttachHandler(new AttachEvent.Handler() {
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if (event.isAttached()) {
					// use html5 srcdoc if available
					if (jsniUtils.elementSupportsAttribute(frame.getElement(), "srcdoc")) {
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
			if (!frameHeight || (Math.abs(newHeightPx - frameHeight) > 70)) {
				iframe.height = "";
				iframe.height = (newHeightPx + 50) + "px";
				if (frameHeight) {
					SynapseJSNIUtilsImpl._scrollIntoView(iframe);
				}
			}
		}
	}-*/;
	
	public static native void _setFrameContent(Element iframe, String htmlContent) /*-{
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
	public void setPresenter(Presenter p) {
		this.p = p;
	}
	@Override
	public void setSanitizedWarningVisible(boolean visible) {
		htmlSanitizedWarning.setVisible(visible);
	}
	@Override
	public void setRawHtml(String rawHtml) {
		storeRawHtmlSpan.setText(rawHtml);
	}
	@Override
	public void openInNewWindow(String url) {
		Window.open(url, "", "");
	}
	@Override
	public void setShowContentLinkText(String text) {
		showContentLink.setText(text);
	}
}
