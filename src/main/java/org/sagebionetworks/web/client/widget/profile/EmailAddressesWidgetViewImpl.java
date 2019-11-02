package org.sagebionetworks.web.client.widget.profile;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Strong;
import org.sagebionetworks.web.client.widget.HelpWidget;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EmailAddressesWidgetViewImpl implements EmailAddressesWidgetView {

	public interface Binder extends UiBinder<Widget, EmailAddressesWidgetViewImpl> {
	}

	Widget widget;
	Presenter presenter;
	@UiField
	Button addEmailButton;
	@UiField
	TextBox newEmailTextBox;
	@UiField
	LoadingSpinner loadingUI;
	@UiField
	Div synapseAlertContainer;
	@UiField
	Div emailsPanel;

	@Inject
	public EmailAddressesWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		newEmailTextBox.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					addEmailButton.click();
				}
			}
		});

		addEmailButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onAddEmail(newEmailTextBox.getValue());
				newEmailTextBox.setValue("");
			}
		});
		newEmailTextBox.getElement().setAttribute("placeholder", "New email address");
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setVisible(boolean visible) {
		widget.setVisible(visible);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setLoadingVisible(boolean visible) {
		loadingUI.setVisible(visible);
	}

	@Override
	public void setSynAlert(IsWidget w) {
		synapseAlertContainer.add(w);
	}

	@Override
	public void addPrimaryEmail(String email, boolean isQuarantined) {
		Div emailDiv = new Div();
		emailDiv.add(new Strong(email + " (Primary)"));
		if (isQuarantined) {
			HelpWidget helpWidget = new HelpWidget();
			helpWidget.setIconType(IconType.EXCLAMATION_CIRCLE);
			helpWidget.setHelpMarkdown("#### Your email address may be invalid\n\nWe have been unable to reach you at this email address.  Please update your primary email address.");
			helpWidget.setIconStyles("text-danger");
			emailDiv.add(helpWidget);
		}
		emailsPanel.add(emailDiv);
	}

	@Override
	public void addSecondaryEmail(final String email) {
		Div emailDiv = new Div();
		emailDiv.addStyleName("margin-top-5");
		Span emailSpan = new Span();
		emailSpan.setText(email);
		emailDiv.add(emailSpan);
		Button makePrimaryButton = new Button("Make primary", IconType.ENVELOPE, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onMakePrimary(email);
			}
		});
		makePrimaryButton.addStyleName("margin-left-5");
		makePrimaryButton.setSize(ButtonSize.EXTRA_SMALL);
		emailDiv.add(makePrimaryButton);
		Button deleteButton = new Button("", IconType.TIMES, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onRemoveEmail(email);
			}
		});
		deleteButton.addStyleName("margin-left-5");
		deleteButton.setHeight("21px");
		deleteButton.setType(ButtonType.DANGER);
		deleteButton.setSize(ButtonSize.EXTRA_SMALL);
		emailDiv.add(deleteButton);
		emailsPanel.add(emailDiv);
	}

	@Override
	public void clearEmails() {
		emailsPanel.clear();
	}
}
