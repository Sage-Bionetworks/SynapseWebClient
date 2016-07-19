package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.Team;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface InviteWidgetView extends IsWidget {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	public interface Presenter {
		void validateAndSendInvite(String invitationMessage);
		void configure(Team team);
		void show();
		void hide();
	}
	public void setSynAlertWidget(Widget asWidget);
	void setSuggestWidget(Widget suggestWidget);
	public void clear();
	public void showInfo(String string, String string2);
	void show();
	void hide();
}
