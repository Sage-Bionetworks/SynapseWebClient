package org.sagebionetworks.web.client.widget.subscription;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubscribeButtonWidgetViewImpl implements SubscribeButtonWidgetView{

	@UiField
	Button followButton;
	@UiField
	Button unfollowButton;
	@UiField
	Icon followIcon;
	@UiField
	Icon unfollowIcon;
	@UiField
	Tooltip followIconTooltip;
	@UiField
	Tooltip unfollowIconTooltip;
	
	@UiField
	Div synAlertContainer;
	public interface Binder extends UiBinder<Widget, SubscribeButtonWidgetViewImpl> {
	}
	
	Widget w;
	Presenter presenter;
	
	@Inject
	public SubscribeButtonWidgetViewImpl(Binder binder){
		this.w = binder.createAndBindUi(this);
		ClickHandler followClickHandler = event -> {
			presenter.onSubscribe();
			followIconTooltip.hide();
		};
		followIcon.addClickHandler(followClickHandler);
		followButton.addClickHandler(followClickHandler);
		
		ClickHandler unfollowClickHandler = event -> {
			presenter.onUnsubscribe();
			unfollowIconTooltip.hide();
		};
		unfollowIcon.addClickHandler(unfollowClickHandler);
		unfollowButton.addClickHandler(unfollowClickHandler);
	}
	@Override
	public void addStyleNames(String styleNames) {
		w.addStyleName(styleNames);
	}
	
	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public Widget asWidget() {
		return w;
	}

	@Override
	public void showLoading() {
		followButton.state().loading();
		unfollowButton.state().loading();
	}

	@Override
	public void clear() {
		followButton.setVisible(false);
		unfollowButton.setVisible(false);
		followIcon.setVisible(false);
		unfollowIcon.setVisible(false);
		hideLoading();
	}
	
	@Override
	public void showUnfollowButton() {
		clear();
		unfollowButton.setVisible(true);
	}
	@Override
	public void showFollowButton() {
		clear();
		followButton.setVisible(true);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	@Override
	public void setSynAlert(Widget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
	
	@Override
	public void hideLoading() {
		followButton.state().reset();
		unfollowButton.state().reset();
	}
	
	@Override
	public void showFollowIcon() {
		clear();
		followIcon.setVisible(true);
	}
	
	@Override
	public void showUnfollowIcon() {
		clear();
		unfollowIcon.setVisible(true);
	}
	@Override
	public void setButtonSize(ButtonSize size) {
		followButton.setSize(size);
		unfollowButton.setSize(size);
	}
}
