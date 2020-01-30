package org.sagebionetworks.web.client.widget.accessrequirements;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ToggleACTActionsButton implements IsWidget {
	Button button;
	IsACTMemberAsyncHandler isACTMemberAsyncHandler;
	GlobalApplicationState globalAppState;
	public static final String HIDE_ACT_UI = "Hide ACT UI";
	public static final String SHOW_ACT_UI = "Show ACT UI";

	@Inject
	public ToggleACTActionsButton(Button button, IsACTMemberAsyncHandler isACTMemberAsyncHandler, GlobalApplicationState globalAppState) {
		this.button = button;
		this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
		this.globalAppState = globalAppState;

		button.setVisible(false);
		button.setSize(ButtonSize.EXTRA_SMALL);
		button.setType(ButtonType.LINK);
		button.addStyleName("color-white margin-left-10 margin-right-10");
		button.setText(HIDE_ACT_UI);
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				toggle();
			}
		});
		showIfACTMember();
	}

	public void refresh() {
		showIfACTMember();
	}

	public void toggle() {
		boolean visible = !isACTMemberAsyncHandler.isACTActionVisible();
		button.setText(visible ? HIDE_ACT_UI : SHOW_ACT_UI);
		isACTMemberAsyncHandler.setACTActionVisible(visible);
		globalAppState.refreshPage();
	}

	private void showIfACTMember() {
		isACTMemberAsyncHandler.isACTMember(new CallbackP<Boolean>() {
			@Override
			public void invoke(Boolean isACTMember) {
				button.setVisible(isACTMember);
			}
		});
	}

	public Widget asWidget() {
		return button.asWidget();
	}
}
