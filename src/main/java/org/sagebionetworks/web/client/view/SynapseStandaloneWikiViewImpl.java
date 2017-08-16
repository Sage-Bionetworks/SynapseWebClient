package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Strong;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SynapseStandaloneWikiViewImpl implements SynapseStandaloneWikiView {

	public interface SynapseStandaloneWikiViewImplUiBinder extends UiBinder<Widget, SynapseStandaloneWikiViewImpl> {}
	@UiField
	Div markdownContainer;
	
	@UiField
	Row loadingUI;
	@UiField
	Row errorUI;
	@UiField
	Strong errorText;
	
	Widget widget;
	
	private Presenter presenter;
	private Header headerWidget;
	
	private MarkdownWidget markdownWidget;
	
	@Inject
	public SynapseStandaloneWikiViewImpl(SynapseStandaloneWikiViewImplUiBinder binder, MarkdownWidget markdownWidget,
			Header headerWidget) {		
		widget = binder.createAndBindUi(this);
		
		this.headerWidget = headerWidget;
		this.markdownWidget = markdownWidget;
		headerWidget.configure(false);
		markdownContainer.add(markdownWidget.asWidget());
	}

	@Override
	public void configure(String markdown, WikiPageKey wikiKey) {
		clear();
		markdownWidget.configure(markdown, wikiKey, null);
		markdownContainer.setVisible(true);
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		headerWidget.configure(false);
		headerWidget.refresh();
		Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public void showErrorMessage(String message) {
		clear();
		errorText.setText(message);
		errorUI.setVisible(true);
	}

	@Override
	public void showLoading() {
		clear();
		loadingUI.setVisible(true);
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
		loadingUI.setVisible(false);
		errorUI.setVisible(false);
		markdownContainer.setVisible(false);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
}
