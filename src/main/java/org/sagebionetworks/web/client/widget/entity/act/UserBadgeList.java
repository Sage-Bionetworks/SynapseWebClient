package org.sagebionetworks.web.client.widget.entity.act;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.sagebionetworks.repo.model.dataaccess.AccessType;
import org.sagebionetworks.repo.model.dataaccess.AccessorChange;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;


public class UserBadgeList implements UserBadgeListView.Presenter, IsWidget {

	UserBadgeListView view;
	PortalGinInjector ginInjector;
	boolean isToolbarVisible;
	List<UserBadgeItem> users;
	Callback selectionChangedCallback;
	Set<String> userIds;

	@Inject
	public UserBadgeList(UserBadgeListView view, PortalGinInjector ginInjector) {
		super();
		this.view = view;
		this.ginInjector = ginInjector;
		this.view.setPresenter(this);
		users = new ArrayList<UserBadgeItem>();
		userIds = new HashSet<>();
		selectionChangedCallback = new Callback() {
			@Override
			public void invoke() {
				refreshListUI();
			}
		};
	}

	public UserBadgeList configure() {
		this.isToolbarVisible = false;
		view.setSelectionOptionsVisible(false);
		return this;
	};

	public void addAccessorChange(AccessorChange change) {
		addAccessorChange(change, true);
	}

	public void addSubmitterAccessorChange(AccessorChange change) {
		addAccessorChange(change, false);
	}

	private void addAccessorChange(AccessorChange change, boolean enableSelect) {
		if (!userIds.contains(change.getUserId())) {
			UserBadgeItem item = ginInjector.getUserBadgeItem();
			item.configure(change);
			item.setSelectionChangedCallback(selectionChangedCallback);
			users.add(item);
			view.addUserBadge(item.asWidget());
			// selection is enabled as soon as we have one item of type "GAIN_ACCESS"
			isToolbarVisible = isToolbarVisible || AccessType.GAIN_ACCESS.equals(change.getType());
			boolean toolbarVisible = isToolbarVisible && users.size() > 0;
			view.setSelectionOptionsVisible(toolbarVisible);
			item.setSelectEnabled(enableSelect);
			userIds.add(change.getUserId());
		}
	}

	public void refreshListUI() {
		view.clearUserBadges();
		for (UserBadgeItem item : users) {
			view.addUserBadge(item.asWidget());
			item.reconfigure();
		}

		boolean toolbarVisible = isToolbarVisible && users.size() > 0;
		view.setSelectionOptionsVisible(toolbarVisible);
	}

	@Override
	public void deleteSelected() {
		// remove all selected users
		Iterator<UserBadgeItem> it = users.iterator();
		while (it.hasNext()) {
			UserBadgeItem row = it.next();
			if (row.isSelected()) {
				userIds.remove(row.getUserId());
				it.remove();
			}
		}
		refreshListUI();
	}

	/**
	 * Change the selection state of all rows to the passed value.
	 * 
	 * @param select
	 */
	private void changeAllSelection(boolean select) {
		// Select all
		for (UserBadgeItem item : users) {
			if (item.isSelectEnabled()) {
				item.setSelected(select);	
			}
		}
	}

	public void clear() {
		changeAllSelection(true);
		deleteSelected();
	}

	@Override
	public void selectAll() {
		changeAllSelection(true);
	}

	@Override
	public void selectNone() {
		changeAllSelection(false);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public List<AccessorChange> getAccessorChanges() {
		List<AccessorChange> accessorChanges = new ArrayList<AccessorChange>();
		for (UserBadgeItem item : users) {
			AccessorChange change = new AccessorChange();
			change.setUserId(item.getUserId());
			change.setType(item.getAccessType());
			accessorChanges.add(change);
		}
		return accessorChanges;
	}
}
