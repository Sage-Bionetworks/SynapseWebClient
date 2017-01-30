package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubscribersWidgetViewImpl implements SubscribersWidgetView {

	public interface Binder extends UiBinder<Widget, SubscribersWidgetViewImpl> {}

	@UiField
	Div synAlertContainer;
	@UiField
	Div userListContainer;
	@UiField
	Modal modal;
	@UiField
	Button okButton;
	@UiField
	Anchor followersLink;
	
	public static final String FOLLOWERS = "Followers";
	
	private Widget widget;
	private SubscribersWidget presenter;
	
	@Inject
	public SubscribersWidgetViewImpl (Binder binder) {
		widget = binder.createAndBindUi(this);
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				modal.hide();
			}
		});
		followersLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onClickSubscribersLink();
			}
		});
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(SubscribersWidget presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setSynapseAlert(Widget w) {
		synAlertContainer.add(w);
	}
	
	@Override
	public void clearSubscriberCount() {
		followersLink.setText(FOLLOWERS);
	}
	
	@Override
	public void setSubscribersLinkVisible(boolean visible) {
		followersLink.setVisible(visible);
	}
	
	@Override
	public void setSubscriberCount(Long count) {
		followersLink.setText(FOLLOWERS + "(" + count + ")");
	}
	
	@Override
	public void setUserListContainer(Widget w) {
		userListContainer.clear();
		userListContainer.add(w);
	}
	
	@Override
	public void showDialog() {
		modal.show();
	}
}
