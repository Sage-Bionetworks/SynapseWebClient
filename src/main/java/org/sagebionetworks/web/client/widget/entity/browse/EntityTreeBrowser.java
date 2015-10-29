package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.entity.query.Condition;
import org.sagebionetworks.repo.model.entity.query.EntityFieldName;
import org.sagebionetworks.repo.model.entity.query.EntityQuery;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
import org.sagebionetworks.repo.model.entity.query.EntityQueryUtils;
import org.sagebionetworks.repo.model.entity.query.Operator;
import org.sagebionetworks.repo.model.entity.query.Sort;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntitySelectedEvent;
import org.sagebionetworks.web.client.events.EntitySelectedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.EntityTreeItem;
import org.sagebionetworks.web.client.widget.entity.MoreTreeItem;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityTreeBrowser implements EntityTreeBrowserView.Presenter,
		SynapseWidgetPresenter {
	public static final long OFFSET_ZERO = 0;

	private EntityTreeBrowserView view;
	private SynapseClientAsync synapseClient;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	AdapterFactory adapterFactory;
	private Set<EntityTreeItem> alreadyFetchedEntityChildren;
	private PortalGinInjector ginInjector;
	private String currentSelection;
	private final int MAX_FOLDER_LIMIT = 100;
	EntitySelectedHandler entitySelectedHandler;
	CallbackP<String> entityClickedHandler;
	@Inject
	public EntityTreeBrowser(PortalGinInjector ginInjector,
			EntityTreeBrowserView view, SynapseClientAsync synapseClient,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			IconsImageBundle iconsImageBundle, AdapterFactory adapterFactory) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.adapterFactory = adapterFactory;
		this.ginInjector = ginInjector;
		alreadyFetchedEntityChildren = new HashSet<EntityTreeItem>();
		view.setPresenter(this);
	}

	public void clearState() {
		view.clear();
		// remove handlers
		entitySelectedHandler = null;
		entityClickedHandler = null;
	}

	public void clear() {
		view.clear();
	}

	/**
	 * Configure tree view with given entityId's children as start set. Note:
	 * Root entities are sorted by default.
	 * 
	 * @param entityId
	 */
	public void configure(String searchId) {
		view.clear();
		view.setLoadingVisible(true);
		getChildren(searchId, null, 0);
	}

	public void configure(List<EntityHeader> headers) {
		view.clear();
		view.setLoadingVisible(true);
		EntityQueryResults results = getEntityQueryResultsFromHeaders(headers);
		for (EntityQueryResult wrappedHeader : results.getEntities()) {
			view.appendRootEntityTreeItem(makeTreeItemFromQueryResult(wrappedHeader, true,
					false));
		}
		view.setLoadingVisible(false);
	}

	public EntityQueryResults getEntityQueryResultsFromHeaders(
			List<EntityHeader> headers) {
		EntityQueryResults results = new EntityQueryResults();
		List<EntityQueryResult> resultList = new ArrayList<EntityQueryResult>();
		
		for (EntityHeader header : headers) {
			EntityQueryResult result = new EntityQueryResult();
			result.setId(header.getId());
			result.setName(header.getName());
			result.setEntityType(header.getType());
			result.setVersionNumber(header.getVersionNumber());
			resultList.add(result);
		}
		
		results.setEntities(resultList);
		results.setTotalEntityCount((long)headers.size());
		
		return results;
	}
	
	@Override
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}

	@Override
	public void getChildren(final String parentId,
			final EntityTreeItem parent, final long offset) {
		EntityQuery childrenQuery = createGetChildrenQuery(parentId, offset);
		childrenQuery.setLimit((long) MAX_FOLDER_LIMIT);
		// ask for the folder children, then the files
		synapseClient.executeEntityQuery(childrenQuery,
				new AsyncCallback<EntityQueryResults>() {
					@Override
					public void onSuccess(EntityQueryResults results) {
						if (!results.getEntities().isEmpty()) {
							addResultsToParent(parent, results,	offset, true);
							// More total entities than able to be displayed, so
							// must add a "More Folders" button
							if (results.getTotalEntityCount() > offset
									+ results.getEntities().size()) {
								final MoreTreeItem moreItem = ginInjector
										.getMoreTreeWidget();
								addMoreButton(moreItem, parentId, parent, offset);
							}
						}
						if (parent == null) {
							view.setLoadingVisible(false);
							if (view.getRootCount() == 0) {
								view.showEmptyUI();
							} else {
								view.hideEmptyUI();
							}
						} else {
							parent.showTypeIcon();
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						DisplayUtils.handleServiceException(caught,
								globalApplicationState,
								authenticationController.isLoggedIn(), view);
					}
				});
	}

	/**
	 * Multiplexor to call all the variants of buttons requesting more folders
	 * or more files.
	 */
	@Override
	public void addMoreButton(MoreTreeItem moreItem, String parentId,
			EntityTreeItem parent, long offset) {
		if (parent == null) {
			view.placeRootMoreTreeItem(moreItem, parentId, offset + MAX_FOLDER_LIMIT);
		} else {
			view.placeChildMoreTreeItem(moreItem, parent, offset + MAX_FOLDER_LIMIT);
		}
	}

	@Override
	public void setSelection(String id) {
		currentSelection = id;
		fireEntitySelectedEvent();
	}

	public String getSelected() {
		return currentSelection;
	}

	public void setEntitySelectedHandler(EntitySelectedHandler handler) {
		entitySelectedHandler = handler;
		//if adding a selection handler, then the component user want to make this selectable
		makeSelectable();
	}
	
	public void setEntityClickedHandler(CallbackP<String> callback) {
		entityClickedHandler = callback;
	}
	
	public EntitySelectedHandler getEntitySelectedHandler() {
		return entitySelectedHandler;
	}

	@Override
	public int getMaxLimit() {
		return MAX_FOLDER_LIMIT;
	}

	/**
	 * Rather than linking to the Entity Page, a clicked entity in the tree will
	 * become selected.
	 */
	public void makeSelectable() {
		view.makeSelectable();
	}

	/**
	 * When a node is expanded, if its children have not already been fetched
	 * and placed into the tree, it will delete the dummy child node and fetch
	 * the actual children of the expanded node. During this process, a loading
	 * icon is appended below the folder.
	 */
	@Override
	public void expandTreeItemOnOpen(final EntityTreeItem target) {
		if (!alreadyFetchedEntityChildren.contains(target)) {
			// We have not already fetched children for this entity.
			alreadyFetchedEntityChildren.add(target);
			target.asTreeItem().removeItems();
			target.showLoadingIcon();
			getChildren(target.getHeader().getId(), target, 0);
		}
	}

	@Override
	public void clearRecordsFetchedChildren() {
		alreadyFetchedEntityChildren.clear();
	}
  
	public void fireEntitySelectedEvent() {
		if (entitySelectedHandler != null) {
			entitySelectedHandler.onSelection(new EntitySelectedEvent(getSelected()));
		}
	}

	public EntityQuery createGetChildrenQuery(String parentId, long offset) {
		EntityQuery newQuery = new EntityQuery();
		Sort sort = new Sort();
		sort.setColumnName(EntityFieldName.name.name());
		sort.setDirection(SortDirection.ASC);
		newQuery.setSort(sort);
		Condition parentCondition = EntityQueryUtils.buildCondition(
				EntityFieldName.parentId, Operator.EQUALS, parentId);
		Condition typeCondition = EntityQueryUtils.buildCondition(
				EntityFieldName.nodeType, Operator.IN, new String[]{"folder", "file", "link"});
		newQuery.setConditions(Arrays.asList(parentCondition, typeCondition));
		newQuery.setLimit((long) MAX_FOLDER_LIMIT);
		newQuery.setOffset(offset);
		return newQuery;
	}

	public EntityTreeItem makeTreeItemFromQueryResult(EntityQueryResult header,
			boolean isRootItem, boolean isExpandable) {
		final EntityTreeItem childItem = ginInjector.getEntityTreeItemWidget();
		childItem.configure(header, isRootItem, isExpandable);
		if (entityClickedHandler != null) {
			childItem.setEntityClickedHandler(entityClickedHandler);
		}
		return childItem;
	}

	public void addResultsToParent(final EntityTreeItem parent,	EntityQueryResults results, long offset, boolean isExpandable) {
		if (parent == null) {
			for (EntityQueryResult header : results.getEntities()) {
				String entityType = header.getEntityType();
				if (entityType.equals("folder")) {
					view.appendRootEntityTreeItem(makeTreeItemFromQueryResult(header, true, true));
				} else {
					view.appendRootEntityTreeItem(makeTreeItemFromQueryResult(header, true, false));
				}
			}
		} else {
			for (EntityQueryResult header : results.getEntities()) {
				String entityType = header.getEntityType();
				if (entityType.equals("folder")) {
					view.appendChildEntityTreeItem(makeTreeItemFromQueryResult(header, false, true), parent);
				} else {
					view.appendChildEntityTreeItem(makeTreeItemFromQueryResult(header, false, false), parent);
				}
			}

		}
	}
}
