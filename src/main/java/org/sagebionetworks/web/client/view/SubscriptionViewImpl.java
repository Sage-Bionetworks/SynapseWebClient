package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Radio;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubscriptionViewImpl implements SubscriptionView {

	public interface SubscriptionViewImplUiBinder extends UiBinder<Widget, SubscriptionViewImpl> {}

	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	
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
	private Footer footerWidget;
	
	Widget widget;
	@Inject
	public SubscriptionViewImpl(SubscriptionViewImplUiBinder binder,
			Header headerWidget, 
			Footer footerWidget
			) {
		widget = binder.createAndBindUi(this);
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		followButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onFollow();
			}
		});
		unfollowButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onUnfollow();
			}
		});

	}
	
	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		header.clear();
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		headerWidget.refresh();	
		Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {		
	}
	
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
	public void selectFollow() {
		followButton.setValue(true, false);
		unfollowButton.setValue(false, false);
	}
	public void selectUnfollow() {
		followButton.setValue(false, false);
		unfollowButton.setValue(true, false);
	};
}
