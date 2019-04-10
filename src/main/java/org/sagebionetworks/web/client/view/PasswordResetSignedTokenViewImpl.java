package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Quiz;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PasswordResetSignedTokenViewImpl implements PasswordResetSignedTokenView {

	public interface PasswordResetSignedTokenViewImplUiBinder extends UiBinder<Widget, PasswordResetSignedTokenViewImpl> {}
	@UiField
	PasswordTextBox password1Field;
	@UiField
	PasswordTextBox password2Field;
	@UiField
	Row password1;
	@UiField
	Row password2;
	@UiField
	SimplePanel passwordSynAlertPanel;
	@UiField
	Button changePasswordBtn;
	private Presenter presenter;
	Widget w;
	
	@Inject
	public PasswordResetSignedTokenViewImpl(PasswordResetSignedTokenViewImplUiBinder binder, final SynapseJSNIUtils jsniUtils) {
		w = binder.createAndBindUi(this);
		password1Field.getElement().setAttribute("placeholder", "Enter new password");
		password2Field.getElement().setAttribute("placeholder", "Confirm new password");
		
		changePasswordBtn.addClickHandler(event -> {
			presenter.changePassword();
		});
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;		
		Window.scrollTo(0, 0); // scroll user to top of page
	}
	
	@Override
	public void showPasswordChangeSuccess() {
		clear();
		DisplayUtils.showInfo("Password has been successfully changed");
	}
	
	@Override
	public String getPassword1Field() {
		return password1Field.getText();
	}
		
	@Override
	public void setPassword1InError(boolean inError) {
		if (inError) {
			password1.addStyleName("has-error");
		} else {
			password1.removeStyleName("has-error");
		}
	}
	
	@Override
	public String getPassword2Field() {
		return password2Field.getText();
	}
	
	@Override
	public void setPassword2InError(boolean inError) {
		if (inError) {
			password2.addStyleName("has-error");
		} else {
			password2.removeStyleName("has-error");
		}
	}
	
	@Override
	public void clear() {
		password1Field.setValue("");
		password2Field.setValue("");
		changePasswordBtn.setEnabled(true);
		setPassword1InError(false);
		setPassword2InError(false);
	}
	
	@Override
	public void setChangePasswordEnabled(boolean isEnabled) {
		changePasswordBtn.setEnabled(isEnabled);
	}

	@Override
	public void setSynAlertWidget(IsWidget synAlert) {
		passwordSynAlertPanel.setWidget(synAlert);
	}
	@Override
	public Widget asWidget() {
		return w;
	}
}
