package org.sagebionetworks.web.client.widget.entity.renderer;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.utils.CajaHtmlSanitizer;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HtmlViewImpl implements HtmlView {

	public interface Binder extends UiBinder<Widget, HtmlViewImpl> {}
	
	@UiField
	Div htmlContainer;
	@UiField
	Div synAlertContainer;
	@UiField
	Div loadingUI;
	
	Widget w;
	@Inject
	public HtmlViewImpl(Binder binder,
			CajaHtmlSanitizer sanitizer) {
		w = binder.createAndBindUi(this);
	}
	
	@Override
	public Widget asWidget() {
		return w;
	}
	
	@Override
	public void setHtml(String html) {
		htmlContainer.clear();
		htmlContainer.add(new HTML(html));
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
}
