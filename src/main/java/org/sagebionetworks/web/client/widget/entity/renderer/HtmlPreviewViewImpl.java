package org.sagebionetworks.web.client.widget.entity.renderer;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.entity.PreviewWidgetViewImpl;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
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
	SynapseJSNIUtils jsniUtils;
	Widget w;
	@Inject
	public HtmlPreviewViewImpl(Binder binder,
			SynapseJSNIUtils jsniUtils) {
		w = binder.createAndBindUi(this);
		this.jsniUtils = jsniUtils;
	}
	
	@Override
	public Widget asWidget() {
		return w;
	}
	
	@Override
	public void setHtml(String html) {
		htmlContainer.clear();
		htmlContainer.add(PreviewWidgetViewImpl.getFrame(html, jsniUtils));
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
	public void openHtmlInNewWindow(String html) {
		_openHtmlInNewWindow(html);
	}
	
	private final static native void _openHtmlInNewWindow(String html) /*-{
		var wnd = $wnd.open("about:blank", "");
        wnd.document.write(html);
        // close document, to run scripts inside html string 
        wnd.document.close();
	}-*/;
}
