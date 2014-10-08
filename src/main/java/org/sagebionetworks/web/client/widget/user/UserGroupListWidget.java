package org.sagebionetworks.web.client.widget.user;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMember;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;
import org.sagebionetworks.web.client.widget.search.PaginationUtil;
import org.sagebionetworks.web.client.widget.team.MemberListWidgetView;
import org.sagebionetworks.web.client.widget.team.TeamListWidgetView;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Simple container for holding and displaying a list of users.
 */
public class UserGroupListWidget implements UserGroupListWidgetView.Presenter {

	private UserGroupListWidgetView view;
	private GlobalApplicationState globalApplicationState;
	
	public static int MEMBER_LIMIT = 100;
	private int offset;
	private String searchTerm;
	private boolean isAdmin;
	
	private List<UserGroupHeader> users;
	
	@Inject
	public UserGroupListWidget(
			UserGroupListWidgetView view, 
			GlobalApplicationState globalApplicationState) { 
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		view.setPresenter(this);
	}
	
	public void configure(List<UserGroupHeader> users, boolean isBig) {
		this.users = users;
		view.configure(users, isBig);
	}
	
	public void configure(List<UserGroupHeader> users) {
		configure(users, false);
	}
	
	public List<UserGroupHeader> getUsers() {
		return users;
	}
	
	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}

	public void clear() {
		view.clear();
	}
}
