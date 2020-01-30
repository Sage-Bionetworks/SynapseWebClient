package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SynapseWikiViewImpl extends Composite implements SynapseWikiView {

	public interface WikiViewImplUiBinder extends UiBinder<Widget, SynapseWikiViewImpl> {
	}

	@UiField
	FlowPanel mainContainer;
	private Presenter presenter;
	private Header headerWidget;
	private WikiPageWidget wikiPage;

	@Inject
	public SynapseWikiViewImpl(WikiViewImplUiBinder binder, Header headerWidget, WikiPageWidget wikiPage) {
		initWidget(binder.createAndBindUi(this));

		this.headerWidget = headerWidget;
		this.wikiPage = wikiPage;
		headerWidget.configure();
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		headerWidget.configure();
		headerWidget.refresh();
		Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void clear() {}

	@Override
	public void showPage(final WikiPageKey wikiKey, final boolean canEdit) {
		mainContainer.clear();

		mainContainer.add(wikiPage.asWidget());
		wikiPage.configure(wikiKey, canEdit, new WikiPageWidget.Callback() {
			@Override
			public void pageUpdated() {
				presenter.configure(wikiKey);
			}

			@Override
			public void noWikiFound() {}
		});
		wikiPage.showSubpages(null);
	}
}
