package org.sagebionetworks.web.client.widget.accessrequirements.approval;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.widget.TextBoxWithCopyToClipboardWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AccessorGroupViewImpl implements AccessorGroupView {

	@UiField
	Div synAlertContainer;
	@UiField
	Div accessorsContainer;
	@UiField
	Div submittedByContainer;
	@UiField
	Button showAccessRequirementButton;
	@UiField
	Div emailsContainer;
	@UiField
	Button revokeAccessButton;
	@UiField
	Label expiresOnField;
	@UiField
	Button closeButton;

	@UiField
	Modal dialog;
	@UiField
	Div accessRequirementWidgetContainer;

	Presenter presenter;

	public interface Binder extends UiBinder<Widget, AccessorGroupViewImpl> {
	}

	Widget w;

	@Inject
	public AccessorGroupViewImpl(Binder binder) {
		this.w = binder.createAndBindUi(this);
		showAccessRequirementButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onShowAccessRequirement();
			}
		});
		revokeAccessButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onRevoke();
			}
		});
		closeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				dialog.hide();
			}
		});
	}

	@Override
	public void addStyleNames(String styleNames) {
		w.addStyleName(styleNames);
	}

	@Override
	public Widget asWidget() {
		return w;
	}

	@Override
	public void setVisible(boolean visible) {
		w.setVisible(visible);
	}

	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}

	@Override
	public void addAccessor(IsWidget w) {
		accessorsContainer.add(w);
	}

	@Override
	public void clearAccessors() {
		accessorsContainer.clear();
	}

	@Override
	public void setSubmittedBy(IsWidget w) {
		submittedByContainer.clear();
		submittedByContainer.add(w);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void showAccessRequirementDialog() {
		accessRequirementWidgetContainer.setVisible(true);
		dialog.show();
	}

	@Override
	public void setAccessRequirementWidget(IsWidget w) {
		accessRequirementWidgetContainer.clear();
		accessRequirementWidgetContainer.add(w);
	}

	@Override
	public void setExpiresOn(String expiresOnString) {
		expiresOnField.setText(expiresOnString);
	}

	@Override
	public void clearEmails() {
		emailsContainer.clear();
	}

	@Override
	public void addEmail(String username) {
		TextBoxWithCopyToClipboardWidget emailTextBox = new TextBoxWithCopyToClipboardWidget();
		emailTextBox.setText(username + "@synapse.org");
		emailTextBox.setAddStyleNames("displayBlock");
		emailsContainer.add(emailTextBox);
	}
}
