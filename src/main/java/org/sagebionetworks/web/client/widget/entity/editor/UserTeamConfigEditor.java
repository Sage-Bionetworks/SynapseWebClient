package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import java.util.Map;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserTeamConfigEditor implements UserTeamConfigView.Presenter, WidgetEditorPresenter {

	private UserTeamConfigView view;
	private Map<String, String> descriptor;

	@Inject
	public UserTeamConfigEditor(UserTeamConfigView view) {
		this.view = view;
		view.initView();
	}

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		descriptor = widgetDescriptor;
		String id = descriptor.get(WidgetConstants.USER_TEAM_BADGE_WIDGET_ID_KEY);
		if (id != null)
			view.setId(id);
	}

	@SuppressWarnings("unchecked")
	public void clearState() {
		view.clear();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void updateDescriptorFromView() {
		// update widget descriptor from the view
		view.checkParams();
		descriptor.put(WidgetConstants.USER_TEAM_BADGE_WIDGET_IS_INDIVIDUAL_KEY, view.isIndividual());
		descriptor.put(WidgetConstants.USER_TEAM_BADGE_WIDGET_ID_KEY, view.getId());
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
