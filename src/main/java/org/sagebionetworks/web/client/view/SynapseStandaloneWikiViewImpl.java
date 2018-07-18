package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SynapseStandaloneWikiViewImpl implements SynapseStandaloneWikiView {

	public interface SynapseStandaloneWikiViewImplUiBinder extends UiBinder<Widget, SynapseStandaloneWikiViewImpl> {}
	@UiField
	Div markdownContainer;
	@UiField
	Div synAlertContainer;
	Widget widget;
	
	private Header headerWidget;
	private MarkdownWidget markdownWidget;
	
	@Inject
	public SynapseStandaloneWikiViewImpl(SynapseStandaloneWikiViewImplUiBinder binder, MarkdownWidget markdownWidget,
			Header headerWidget) {		
		widget = binder.createAndBindUi(this);
		
		this.headerWidget = headerWidget;
		this.markdownWidget = markdownWidget;
		headerWidget.configure();
		markdownContainer.add(markdownWidget.asWidget());
	}

	@Override
	public void configure(String markdown, WikiPageKey wikiKey) {
		markdownWidget.configure(markdown, wikiKey, null);
		markdownContainer.setVisible(true);
		headerWidget.configure();
		headerWidget.refresh();
		Window.scrollTo(0, 0); // scroll user to top of page
	}
	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
	@Override
	public Widget asWidget() {
		return widget;
	}
}
