package org.sagebionetworks.web.client.widget.team;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class InviteWidgetViewImpl extends FlowPanel implements InviteWidgetView {

	public interface InviteWidgetViewImplUiBinder extends UiBinder<Widget, InviteWidgetViewImpl> {
	}

	@UiField
	Button sendInviteButton;

	@UiField
	Modal inviteUIModal;

	@UiField
	TextArea inviteTextArea;

	@UiField
	SimplePanel suggestBoxPanel;

	@UiField
	SimplePanel synAlertPanel;

	@UiField
	Button cancelButton;
	@UiField
	Div inviteesContainer;
	@UiField
	LoadingSpinner loadingUI;
	private InviteWidgetView.Presenter presenter;
	private PortalGinInjector ginInjector;
	private Widget widget;

	@Inject
	public InviteWidgetViewImpl(InviteWidgetViewImplUiBinder binder, PortalGinInjector ginInjector) {
		this.widget = binder.createAndBindUi(this);
		this.ginInjector = ginInjector;
		sendInviteButton.addClickHandler(event -> {
			presenter.doSendInvites(inviteTextArea.getValue());
		});
		cancelButton.addClickHandler(event -> {
			inviteUIModal.hide();
		});
	}

	@Override
	public void setSuggestWidget(Widget suggestWidget) {
		suggestBoxPanel.setWidget(suggestWidget);
	}

	@Override
	public void clear() {
		inviteTextArea.setText("");
		inviteesContainer.clear();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setSynAlertWidget(Widget synAlert) {
		synAlertPanel.setWidget(synAlert);
	}

	@Override
	public void show() {
		inviteUIModal.show();
	}

	@Override
	public void hide() {
		inviteUIModal.hide();
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void addEmailToInvite(String emailInvite) {
		Div d = new Div();
		d.addStyleName("margin-top-5");
		d.add(new Span(emailInvite));
		Button button = new Button("", IconType.TIMES, event -> {
			inviteesContainer.remove(d);
			presenter.removeEmailToInvite(emailInvite);
		});
		button.setSize(ButtonSize.EXTRA_SMALL);
		button.setType(ButtonType.DANGER);
		button.addStyleName("displayInline margin-left-5");

		d.add(button);
		inviteesContainer.add(d);
	}

	@Override
	public void addUserToInvite(String userId) {
		Div d = new Div();
		UserBadge badge = ginInjector.getUserBadgeWidget();
		badge.configure(userId);
		badge.setOpenInNewWindow();
		d.add(badge);
		Button button = new Button("", IconType.TIMES, event -> {
			inviteesContainer.remove(d);
			presenter.removeUserToInvite(userId);
		});
		button.setSize(ButtonSize.EXTRA_SMALL);
		button.setType(ButtonType.DANGER);
		button.addStyleName("displayInline margin-left-5");

		d.add(badge);
		d.add(button);
		inviteesContainer.add(d);
	}

	@Override
	public void setLoading(boolean isLoading) {
		loadingUI.setVisible(isLoading);
		sendInviteButton.setEnabled(!isLoading);
	}
}
