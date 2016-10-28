package org.sagebionetworks.web.client.widget.entity.act;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ApproveUserAccessModalView extends IsWidget {

	void setPresenter(Presenter presenter);
	void setSynAlert(Widget asWidget);
	void setStates(List<String> states);
	void setUserPickerWidget(Widget w);
	String getAccessRequirement();
	void setAccessRequirement(String num, String text);
	void setApproveProcessing(boolean processing);
	void setDatasetTitle(String string);
	void startLoadingEmail(Widget asWidget);
	void finishLoadingEmail();
	void showInfo(String string);
	void show();
	void hide();
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onSubmit();
		void onStateSelected(String state);
		void showPreview();
	}


}
