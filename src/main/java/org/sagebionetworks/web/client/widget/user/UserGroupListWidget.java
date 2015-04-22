package org.sagebionetworks.web.client.widget.user;

import java.util.LinkedList;
import java.util.List;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.team.BigTeamBadge;
import org.sagebionetworks.web.client.widget.team.TeamBadge;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Simple container for holding and displaying a list of users.
 */
public class UserGroupListWidget implements UserGroupListWidgetView.Presenter {
	
	private PortalGinInjector portalGinInjector;
	private UserGroupListWidgetView view;
	private GlobalApplicationState globalApplicationState;
	
	private List<UserGroupHeader> users;
	private boolean isBig;
	
	@Inject
	public UserGroupListWidget(
			UserGroupListWidgetView view, 
			GlobalApplicationState globalApplicationState,
			PortalGinInjector portalGinInjector) { 
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.portalGinInjector = portalGinInjector;
		view.setPresenter(this);
	}
	
	public void configure(List<UserGroupHeader> users, boolean isBig) {
		this.users = users;
		this.isBig = isBig;
		view.configure(users);
	}
	
	public void configure(List<UserGroupHeader> users) {
		configure(users, true);
	}
	
	public List<UserGroupHeader> getUsers() {
		return new LinkedList<UserGroupHeader>(users);
	}
	
	public boolean getIsBig() {
		return isBig;
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
	
	public Widget getBadgeWidget(String ownerId, boolean isIndividual, String userName) {
		if (isBig) {
			if (isIndividual) {
				UserBadge userBadge = portalGinInjector.getUserBadgeWidget();
				userBadge.configure(ownerId, true);
				userBadge.setSize(BadgeSize.LARGE);
				return userBadge.asWidget();
			} else {
				BigTeamBadge teamBadge = portalGinInjector.getBigTeamBadgeWidget();
				teamBadge.configure(ownerId, userName);
				return teamBadge.asWidget();
			}
		} else {
			if (isIndividual) {
				UserBadge userBadge = portalGinInjector.getUserBadgeWidget();
				userBadge.configure(ownerId);
				return userBadge.asWidget();
			} else {
				TeamBadge teamBadge = portalGinInjector.getTeamBadgeWidget();
				teamBadge.configure(ownerId);
				return teamBadge.asWidget();
			}
		}
	}
	
}
