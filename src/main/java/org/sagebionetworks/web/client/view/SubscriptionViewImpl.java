package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Radio;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.header.Header;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubscriptionViewImpl implements SubscriptionView {

	public interface SubscriptionViewImplUiBinder extends UiBinder<Widget, SubscriptionViewImpl> {
	}

	@UiField
	Div synAlertContainer;
	@UiField
	Div topicWidgetContainer;
	@UiField
	Radio followButton;
	@UiField
	Radio unfollowButton;

	private Presenter presenter;
	private Header headerWidget;

	Widget widget;

	@Inject
	public SubscriptionViewImpl(SubscriptionViewImplUiBinder binder, Header headerWidget) {
		widget = binder.createAndBindUi(this);
		this.headerWidget = headerWidget;
		headerWidget.configure();
		followButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSubscribe();
			}
		});
		unfollowButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onUnsubscribe();
			}
		});

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
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setSynAlert(Widget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}

	@Override
	public void setTopicWidget(Widget w) {
		topicWidgetContainer.clear();
		topicWidgetContainer.add(w);
	}

	@Override
	public void selectSubscribedButton() {
		followButton.setValue(true, false);
		unfollowButton.setValue(false, false);
	}

	public void selectUnsubscribedButton() {
		followButton.setValue(false, false);
		unfollowButton.setValue(true, false);
	};
}
