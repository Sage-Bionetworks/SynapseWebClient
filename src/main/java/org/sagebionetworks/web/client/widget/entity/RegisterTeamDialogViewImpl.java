package org.sagebionetworks.web.client.widget.entity;

import java.util.Map;

import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.presenter.LoginPresenter;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RegisterTeamDialogViewImpl implements RegisterTeamDialogView {
	private Presenter presenter;
	public interface RegisterTeamDialogViewImplUiBinder extends UiBinder<Widget, RegisterTeamDialogViewImpl> {}
	@UiField
	TextBox recruitmentMessageField;
	@UiField
	SimplePanel teamSelectionPanel;
	Modal modal;
	
	@Inject
	public RegisterTeamDialogViewImpl(RegisterTeamDialogViewImplUiBinder binder) {
		modal = (Modal)binder.createAndBindUi(this);
	}
	
	@Override
	public Widget asWidget() {
		return modal;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	/*
	 * Private Methods
	 */

}
