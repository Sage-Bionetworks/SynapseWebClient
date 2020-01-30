package org.sagebionetworks.web.client.widget.team;

import org.gwtbootstrap3.client.ui.Button;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenUserInvitationWidget implements IsWidget {
	public interface Binder extends UiBinder<Widget, OpenUserInvitationWidget> {
	}

	@UiField
	TableData badgeTableData;
	@UiField
	TableData inviteeEmailTableData;
	@UiField
	TableData messageTableData;
	@UiField
	TableData createdOnTableData;
	@UiField
	Button resendButton;
	@UiField
	Button cancelButton;

	private Widget widget;

	@Inject
	public OpenUserInvitationWidget(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
}
