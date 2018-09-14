package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Lead;
import org.sagebionetworks.repo.model.LogEntry;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ErrorViewImpl implements ErrorView {
	@UiField
	SimplePanel synAlertContainer;
	@UiField
	Heading title;
	@UiField
	Lead message;

	private Presenter presenter;
	private Header headerWidget;
	
	public interface Binder extends UiBinder<Widget, ErrorViewImpl> {}
	public Widget widget;
	
	@Inject
	public ErrorViewImpl(Binder uiBinder,
			Header headerWidget) {
		widget = uiBinder.createAndBindUi(this);
		this.headerWidget = headerWidget;
		headerWidget.configure();
	}

	@Override
	public void setPresenter(Presenter loginPresenter) {
		this.presenter = loginPresenter;
		headerWidget.configure();
		headerWidget.refresh();
		com.google.gwt.user.client.Window.scrollTo(0, 0); // scroll user to top of page
	}
	
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}


	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}


	@Override
	public void clear() {
		title.setText("");
		message.setText("");
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	@Override
	public void setSynAlertWidget(Widget w) {
		synAlertContainer.setWidget(w);
	}
	
	@Override
	public void setEntry(LogEntry entry) {
		title.setText(entry.getLabel());
		message.setText(entry.getMessage());
	}
}
