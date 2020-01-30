package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.Team;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface InviteWidgetView extends IsWidget {

	/**
	 * Set this view's presenter
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public interface Presenter {
		void doSendInvites(String invitationMessage);

		void configure(Team team);

		void show();

		void hide();

		void removeEmailToInvite(String email);

		void removeUserToInvite(String userId);
	}

	void addEmailToInvite(String emailInvite);

	void addUserToInvite(String userId);

	public void setSynAlertWidget(Widget asWidget);

	void setSuggestWidget(Widget suggestWidget);

	public void clear();

	public void showInfo(String string);

	void show();

	void hide();

	void setLoading(boolean isLoading);
}
