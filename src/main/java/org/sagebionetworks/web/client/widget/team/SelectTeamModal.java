package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.search.GroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.search.GroupSuggestionProvider.GroupSuggestion;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SelectTeamModal implements SelectTeamModalView.Presenter {
	private SelectTeamModalView view;
	private CallbackP<String> teamIdSelectedCallback; 
	SynapseSuggestBox teamSuggestBox;
	GroupSuggestionProvider provider;
	
	@Inject
	public SelectTeamModal(SelectTeamModalView view, 
			SynapseSuggestBox teamSuggestBox,
			GroupSuggestionProvider provider) {
		this.view = view;
		this.teamSuggestBox = teamSuggestBox;
		teamSuggestBox.setSuggestionProvider(provider);
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
		GroupSuggestion suggestion = (GroupSuggestion)teamSuggestBox.getSelectedSuggestion();
		if (suggestion != null && teamIdSelectedCallback != null) {
			teamIdSelectedCallback.invoke(suggestion.getId());
		}	
	}
	
	public void setTitle(String title) {
		view.setTitle(title);
	}
}
