package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.entity.Direction;
import org.sagebionetworks.repo.model.entity.SortBy;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.EntitySelectedEvent;
import org.sagebionetworks.web.client.events.EntitySelectedHandler;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.EntityTreeItem;
import org.sagebionetworks.web.client.widget.entity.MoreTreeItem;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityTreeBrowser implements EntityTreeBrowserView.Presenter,
		SynapseWidgetPresenter {
	private EntityTreeBrowserView view;
	private SynapseJavascriptClient jsClient;
	AdapterFactory adapterFactory;
	private Set<EntityTreeItem> alreadyFetchedEntityChildren;
	private PortalGinInjector ginInjector;
	private String currentSelection;
	EntitySelectedHandler entitySelectedHandler;
	CallbackP<String> entityClickedHandler;
	EntityFilter filter = EntityFilter.ALL;
	SynapseAlert synAlert;
	private SortBy currentSortBy;
	private Direction currentDirection;
	private String rootEntityId;
	public static final SortBy DEFAULT_SORT_BY = SortBy.NAME;
	public static final Direction DEFAULT_DIRECTION = Direction.ASC;
	@Inject
	public EntityTreeBrowser(PortalGinInjector ginInjector,
			EntityTreeBrowserView view, 
			SynapseJavascriptClient synapseClient,
			IconsImageBundle iconsImageBundle, 
			AdapterFactory adapterFactory,
			SynapseAlert synAlert) {
		this.view = view;
		this.jsClient = synapseClient;
		this.adapterFactory = adapterFactory;
		this.ginInjector = ginInjector;
		alreadyFetchedEntityChildren = new HashSet<EntityTreeItem>();
		view.setPresenter(this);
		this.synAlert = synAlert;
		view.setSynAlert(synAlert);
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
	public void configure(String entityId) {
		this.rootEntityId = entityId;
		onSort(currentSortBy, currentDirection);
	}
	
	public void configure(List<EntityHeader> headers) {
		//set existing headers.
		rootEntityId = null;
		view.clear();
		view.setLoadingVisible(true);
		addHeaders(headers);
		view.setLoadingVisible(false);
		// if we are only configuring with a single entity, then we can show the sorting UI for the child queries
	}
	
	@Override
	public void onSort(SortBy sortColumn, Direction sortDirection) {
		currentSortBy = sortColumn;
		currentDirection = sortDirection;
		view.clear();
		view.setLoadingVisible(true);
		getChildren(rootEntityId, null, null);
	}
	
	public void addHeaders(List<EntityHeader> headers) {
		headers = filter.filterForBrowsing(headers);
		for (EntityHeader header : headers) {
			view.appendRootEntityTreeItem(makeTreeItemFromQueryResult(header, true,
					isExpandable(header)));
		}
	}
	
	public void setLoadingVisible(boolean visible) {
		view.setLoadingVisible(visible);
	}
	public EntityQueryResults getEntityQueryResultsFromHeaders(
			List<EntityHeader> headers) {
		EntityQueryResults results = new EntityQueryResults();
		List<EntityQueryResult> resultList = new ArrayList<EntityQueryResult>();
		
		for (EntityHeader header : headers) {
			EntityQueryResult result = new EntityQueryResult();
			result.setId(header.getId());
			result.setName(header.getName());
			result.setEntityType(EntityTypeUtils.getEntityTypeForEntityClassName(header.getType()).name());
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
			final EntityTreeItem parent, String nextPageToken) {
		EntityChildrenRequest request = createGetEntityChildrenRequest(parentId, nextPageToken);
		synAlert.clear();
		// ask for the folder children, then the files
		jsClient.getEntityChildren(request,
				new AsyncCallback<EntityChildrenResponse>() {
					@Override
					public void onSuccess(EntityChildrenResponse results) {
						if (!results.getPage().isEmpty()) {
							addResultsToParent(parent, results.getPage());
							// More total entities than able to be displayed, so
							// must add a "More Folders" button
							if (results.getNextPageToken() != null) {
								final MoreTreeItem moreItem = ginInjector
										.getMoreTreeWidget();
								addMoreButton(moreItem, parentId, parent, results.getNextPageToken());
							}
						}
						if (parent == null) {
							view.setLoadingVisible(false);
							if (view.getRootCount() == 0) {
								view.showEmptyUI();
							} else {
								view.hideEmptyUI();
							}
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						synAlert.handleException(caught);
					}
				});
	}

	/**
	 * Multiplexor to call all the variants of buttons requesting more folders
	 * or more files.
	 */
	@Override
	public void addMoreButton(MoreTreeItem moreItem, String parentId,
			EntityTreeItem parent, String nextPageToken) {
		if (parent == null) {
			view.placeRootMoreTreeItem(moreItem, parentId, nextPageToken);
		} else {
			view.placeChildMoreTreeItem(moreItem, parent, nextPageToken);
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
	
	public EntitySelectedHandler getEntitySelectedHandler() {
		return entitySelectedHandler;
	}

	public void setEntityClickedHandler(CallbackP<String> entityClickedHandler) {
		this.entityClickedHandler = entityClickedHandler;
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
			getChildren(target.getHeader().getId(), target, null);
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

	public EntityChildrenRequest createGetEntityChildrenRequest(String parentId, String nextPageToken) {
		EntityChildrenRequest request = new EntityChildrenRequest();
		request.setNextPageToken(nextPageToken);
		request.setParentId(parentId);
		if (currentSortBy == null) {
			currentSortBy = DEFAULT_SORT_BY;
		}
		if (currentDirection == null) {
			currentDirection = DEFAULT_DIRECTION;
		}
		request.setSortBy(currentSortBy);
		request.setSortDirection(currentDirection);
		request.setIncludeTypes(filter.getEntityQueryValues());
		
		return request;
	}

	public EntityTreeItem makeTreeItemFromQueryResult(EntityHeader header,
			boolean isRootItem, boolean isExpandable) {
		final EntityTreeItem childItem = ginInjector.getEntityTreeItemWidget();
		childItem.configure(header, isRootItem, isExpandable);
		if (entityClickedHandler != null) {
			childItem.setClickHandler(event -> {
				entityClickedHandler.invoke(header.getId());
			});
		}
		return childItem;
	}

	public void addResultsToParent(final EntityTreeItem parent,	List<EntityHeader> results) {
		if (parent == null) {
			for (EntityHeader header : results) {
				boolean isExpandable = isExpandable(header);
				view.appendRootEntityTreeItem(makeTreeItemFromQueryResult(header, true, isExpandable));
			}
		} else {
			for (EntityHeader header : results) {
				boolean isExpandable = isExpandable(header);
				view.appendChildEntityTreeItem(makeTreeItemFromQueryResult(header, false, isExpandable), parent);
			}
		}
	}
	
	public boolean isExpandable(EntityHeader header) {
		if (filter.equals(EntityFilter.PROJECT)) {
			return false;
		}
		String entityType = header.getType();
		return entityType.equals(EntityTypeUtils.getEntityClassNameForEntityType(EntityType.folder.name())) || entityType.equals(EntityTypeUtils.getEntityClassNameForEntityType(EntityType.project.name()));	
	}
	
	public void setEntityFilter(EntityFilter filter) {
		this.filter = filter;
	}
	
	public void clearSelection() {
		currentSelection = null;
		view.clearSelection();
	}
	
	public EntityFilter getEntityFilter() {
		return filter;
	}
}
