package org.sagebionetworks.web.client.widget.entity.editor;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

public class TeamSelectEditor implements WidgetEditorPresenter {

  private Map<String, String> descriptor;
  TeamSelectEditorView view;
  SynapseSuggestBox teamSuggestBox;
  UserGroupSuggestionProvider provider;

  @Inject
  public TeamSelectEditor(
    TeamSelectEditorView view,
    SynapseSuggestBox teamSuggestBox,
    UserGroupSuggestionProvider provider
  ) {
    this.view = view;
    this.teamSuggestBox = teamSuggestBox;
    teamSuggestBox.setSuggestionProvider(provider);
    teamSuggestBox.setTypeFilter(TypeFilter.TEAMS_ONLY);
    view.setTeamSuggestBox(teamSuggestBox);
  }

  @Override
  public void configure(
    WikiPageKey wikiKey,
    Map<String, String> widgetDescriptor,
    DialogCallback dialogCallback
  ) {
    descriptor = widgetDescriptor;
  }

  @SuppressWarnings("unchecked")
  public void clearState() {
    teamSuggestBox.clear();
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  @Override
  public void updateDescriptorFromView() {
    if (teamSuggestBox.getSelectedSuggestion() == null) {
      throw new IllegalArgumentException("Please select a Team and try again.");
    }
    descriptor.put(
      WidgetConstants.TEAM_ID_KEY,
      teamSuggestBox.getSelectedSuggestion().getId()
    );
  }

  @Override
  public String getTextToInsert() {
    return null;
  }

  @Override
  public List<String> getNewFileHandleIds() {
    return null;
  }

  @Override
  public List<String> getDeletedFileHandleIds() {
    return null;
  }
}
