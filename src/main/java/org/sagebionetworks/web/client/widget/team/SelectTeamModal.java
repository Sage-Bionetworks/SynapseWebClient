package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SelectTeamModal implements SelectTeamModalView.Presenter {
	private SelectTeamModalView view;
	private CallbackP<String> teamIdSelectedCallback;
	SynapseSuggestBox teamSuggestBox;
	UserGroupSuggestionProvider provider;

	@Inject
	public SelectTeamModal(SelectTeamModalView view, SynapseSuggestBox teamSuggestBox, UserGroupSuggestionProvider provider) {
		this.view = view;
		this.teamSuggestBox = teamSuggestBox;
		teamSuggestBox.setSuggestionProvider(provider);
		teamSuggestBox.setTypeFilter(TypeFilter.TEAMS_ONLY);
		view.setSuggestWidget(teamSuggestBox.asWidget());
		view.setPresenter(this);
	}

	public void configure(CallbackP<String> teamIdSelectedCallback) {
		this.teamIdSelectedCallback = teamIdSelectedCallback;
	}

	public Widget asWidget() {
		return view.asWidget();
	}

	public void show() {
		teamSuggestBox.clear();
		view.show();
	}

	public void hide() {
		view.hide();
	}

	@Override
	public void onSelectTeam() {
		UserGroupSuggestion suggestion = (UserGroupSuggestion) teamSuggestBox.getSelectedSuggestion();
		if (suggestion != null && teamIdSelectedCallback != null) {
			teamIdSelectedCallback.invoke(suggestion.getId());
		}
	}

	public void setTitle(String title) {
		view.setTitle(title);
	}
}
