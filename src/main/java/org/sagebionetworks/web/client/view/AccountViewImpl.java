package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.header.Header;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AccountViewImpl extends Composite implements AccountView {

	public interface AccountViewImplUiBinder extends UiBinder<Widget, AccountViewImpl> {
	}

	@UiField
	FlowPanel mainContainer;

	private Presenter presenter;
	private Header headerWidget;

	@Inject
	public AccountViewImpl(AccountViewImplUiBinder binder, Header headerWidget) {
		initWidget(binder.createAndBindUi(this));

		this.headerWidget = headerWidget;
		headerWidget.configure();
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		mainContainer.clear();
		headerWidget.configure();
		headerWidget.refresh();
		Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showErrorInPage(String title, String message) {
		clear();
		mainContainer.add(new HTML(DisplayUtils.getWarningHtml(title, message)));
	}

	@Override
	public void showLoading() {}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void clear() {
		mainContainer.clear();
	}


}
