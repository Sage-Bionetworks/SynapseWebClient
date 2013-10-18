package org.sagebionetworks.web.client.view;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface HomeView extends IsWidget, SynapseView {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
		
	public void refresh();
	
	public void showNews(String html);
	
	public interface Presenter extends SynapsePresenter {

		boolean showLoggedInDetails();	
		void createProject(String name);
		void createTeam(String teamName);
	}

	public void setMyProjects(List<EntityHeader> result);

	public void setMyProjectsError(String string);

	public void setFavorites(List<EntityHeader> result);

	public void setFavoritesError(String string);
	
	public void setMyEvaluationList(List<EntityHeader> myEvaluationEntities);
	public void setMyEvaluationsError(String string);
	
	public void refreshMyTeams(String userId);
}
