package org.sagebionetworks.web.client.widget.entity.act;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ApproveUserAccessModalView extends IsWidget {

	void setPresenter(Presenter presenter);
	void setSynAlert(Widget asWidget);
	void setStates(List<String> states);
	void setUserPickerWidget(Widget w);
	void setLoadingEmailWidget(Widget w);
	String getAccessRequirement();
	String getEmailMessage();
	Widget getEmailBodyWidget(String html);
	void setAccessRequirement(String num, String html);
	void setApproveProcessing(boolean processing);
	void setRevokeProcessing(boolean processing);
	void setDatasetTitle(String text);
	void setMessageBody(String html);
	void setMessageEditArea(String html);
	void startLoadingEmail();
	void finishLoadingEmail();
	void showInfo(String message);
	void setLoadingEmailVisible(boolean visible);
	void show();
	void hide();
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onSubmit();
		void onStateSelected(String state);
		void onRevoke();
	}

}
