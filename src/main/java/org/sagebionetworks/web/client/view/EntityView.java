package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.presenter.EntityPresenter;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.binder.EventBinder;

public interface EntityView extends IsWidget {
		
	public interface Presenter {

		/**
		 * refreshes the entity from the service and redraws the view
		 */
		void refresh();
		void clear();
	}
	/**
	 * Set entity to display
	 * @param versionNumber to highlight
	 * @param entity
	 * @param entityMetadata 
	 */
	void setEntityPageTopWidget(IsWidget entityPageTopWidget);
	void setOpenTeamInvitesWidget(IsWidget openTeamInvitesWidgetWidget);
	void setSynAlertWidget(IsWidget synAlert);
	void setAccessDependentMessageVisible(boolean isVisible);
	void clear();
	void showInfo(String message);
	void setLoadingVisible(boolean isVisible);
	void showErrorMessage(String message);
	void setEntityPageTopVisible(boolean isVisible);
	void setOpenTeamInvitesVisible(boolean isVisible);
	EventBinder<EntityPresenter> getEventBinder();
}
