package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.Map;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.extjs.gxt.ui.client.widget.Dialog;
public class UserTeamConfigEditor implements UserTeamConfigView.Presenter, WidgetEditorPresenter {
	
	private UserTeamConfigView view;
	private Map<String, String> descriptor;
	@Inject
	public UserTeamConfigEditor(UserTeamConfigView view) {
		this.view = view;
		view.setPresenter(this);
		view.initView();
	}
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Dialog window) {
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
		//update widget descriptor from the view
		view.checkParams();
		descriptor.put(WidgetConstants.USER_TEAM_BADGE_WIDGET_IS_INDIVIDUAL_KEY, view.isIndividual());
		descriptor.put(WidgetConstants.USER_TEAM_BADGE_WIDGET_ID_KEY, view.getId());
	}
	
	@Override
	public int getDisplayHeight() {
		return view.getDisplayHeight();
	}
	
	@Override
	public int getAdditionalWidth() {
		return view.getAdditionalWidth();
	}
	
	@Override
	public String getTextToInsert() {
		return null;
	}
	
	/*
	 * Private Methods
	 */
}
