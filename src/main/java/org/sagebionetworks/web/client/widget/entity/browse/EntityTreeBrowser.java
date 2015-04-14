package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
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
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntitySelectedEvent;
import org.sagebionetworks.web.client.events.EntitySelectedHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.EntityTreeItem;
import org.sagebionetworks.web.client.widget.entity.MoreTreeItem;
import org.sagebionetworks.web.shared.EntityType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityTreeBrowser implements EntityTreeBrowserView.Presenter, SynapseWidgetPresenter {
	public static final long OFFSET_ZERO = 0;
	
	private EntityTreeBrowserView view;
	private SynapseClientAsync synapseClient;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private HandlerManager handlerManager = new HandlerManager(this);
	private IconsImageBundle iconsImageBundle;
	AdapterFactory adapterFactory;
	EntityTypeProvider entityTypeProvider;
	private Set<EntityTreeItem> alreadyFetchedEntityChildren;
	private PortalGinInjector ginInjector;
	private String currentSelection;
	
	private final int MAX_FOLDER_LIMIT = 3;
	private String currentFolderChildrenEntityId;
	
	@Inject
	public EntityTreeBrowser(PortalGinInjector ginInjector, 
			EntityTreeBrowserView view,
			SynapseClientAsync synapseClient,
			AuthenticationController authenticationController,
			EntityTypeProvider entityTypeProvider,
			GlobalApplicationState globalApplicationState,
			IconsImageBundle iconsImageBundle,
			AdapterFactory adapterFactory) {
		this.view = view;		
		this.synapseClient = synapseClient;
		this.entityTypeProvider = entityTypeProvider;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.iconsImageBundle = iconsImageBundle;
		this.adapterFactory = adapterFactory;
		this.ginInjector = ginInjector;
		alreadyFetchedEntityChildren = new HashSet<EntityTreeItem>();

		view.setPresenter(this);
	}	
	
	public void clearState() {
		view.clear();
		// remove handlers
		handlerManager = new HandlerManager(this);		
	}
	
	public void clear() {
		view.clear();
	}

	/**
	 * Configure tree view with given entityId's children as start set.
	 * Note: Root entities are sorted by default.
	 * @param entityId
	 */
	public void configure(String searchId, EntityTreeItem parent) {
		view.clear();
		view.showLoading();
		// Chains to get also the file children
		long childCount = parent == null ? 0 : parent.asTreeItem().getChildCount();
		getFolderChildren(searchId, parent, childCount);
	}
	
	public void configure(List<EntityHeader> headers) {
		view.clear();
		List<EntityTreeItem> treeItems = getEntityTreeItemsFromHeaders(headers);
		for (EntityTreeItem toAdd: treeItems) {
			view.placeEntityTreeItem(toAdd, null, true);
		}
	}
	
	@Override
	public Widget asWidget() {
		view.setPresenter(this);		
		return view.asWidget();
	}
	
	@Override
	public void getFolderChildren(final String parentId, final EntityTreeItem parent, final long offset) {
		EntityQuery childrenQuery = createGetChildrenQuery(parentId, org.sagebionetworks.repo.model.entity.query.EntityType.folder);
		childrenQuery.setLimit((long) MAX_FOLDER_LIMIT);
		childrenQuery.setOffset(offset);
		GWT.debugger();
		//ask for the folder children, then the files
		synapseClient.executeEntityQuery(childrenQuery, new AsyncCallback<EntityQueryResults>() {
			@Override
			public void onSuccess(EntityQueryResults results) {
				if (!results.getEntities().isEmpty()) {
					addResultsToParent(parent, results);
					if (results.getTotalEntityCount() > offset + results.getEntities().size()) {
						GWT.debugger();
						final MoreTreeItem moreItem = ginInjector.getMoreTreeWidget();
						moreItem.configure(MoreTreeItem.MORE_TYPE.FOLDER);
						view.placeMoreTreeItem(moreItem, parent, parentId, parent == null);
					}
				}
				long childCount = parent == null ? 0 : parent.asTreeItem().getChildCount();
				getChildrenFiles(parentId, parent, childCount);
			}
			@Override
			public void onFailure(Throwable caught) {
				DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view);				
			}
		});
	}
	
	@Override
	public void getChildrenFiles(final String parentId, final EntityTreeItem parent, final long offset) {
		EntityQuery childrenQuery = createGetChildrenQuery(parentId, org.sagebionetworks.repo.model.entity.query.EntityType.file);
		childrenQuery.setLimit((long) MAX_FOLDER_LIMIT);
		childrenQuery.setOffset(offset);
		synapseClient.executeEntityQuery(childrenQuery, new AsyncCallback<EntityQueryResults>() {
			@Override
			public void onSuccess(EntityQueryResults results) {
				if (!results.getEntities().isEmpty()) {
					addResultsToParent(parent, results);
					if (results.getTotalEntityCount() > offset + results.getEntities().size()) {
						final MoreTreeItem moreItem = ginInjector.getMoreTreeWidget();
						moreItem.configure(MoreTreeItem.MORE_TYPE.FILE);
						view.placeMoreTreeItem(moreItem, parent, parentId, false);
					}
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view);				
			}
		});
		
	}

	@Override
	public void setSelection(String id) {
		currentSelection = id;
		fireEntitySelectedEvent();
	}	
	
	public String getSelected() {
		return currentSelection;
	}
		
	@SuppressWarnings("unchecked")
	public void addEntitySelectedHandler(EntitySelectedHandler handler) {
		handlerManager.addHandler(EntitySelectedEvent.getType(), handler);		
	}

	@SuppressWarnings("unchecked")
	public void removeEntitySelectedHandler(EntitySelectedHandler handler) {
		handlerManager.removeHandler(EntitySelectedEvent.getType(), handler);
	}
	
	@SuppressWarnings("unchecked")
	public void addEntityUpdatedHandler(EntityUpdatedHandler handler) {
		handlerManager.addHandler(EntityUpdatedEvent.getType(), handler);		
	}

	@SuppressWarnings("unchecked")
	public void removeEntityUpdatedHandler(EntityUpdatedHandler handler) {
		handlerManager.removeHandler(EntityUpdatedEvent.getType(), handler);
	}
	
	@Override
	public int getMaxLimit() {
		return MAX_FOLDER_LIMIT;
	}

	/**
	 * Rather than linking to the Entity Page, a clicked entity
	 * in the tree will become selected.
	 */
	public void makeSelectable() {
		view.makeSelectable();
	}
	
	/**
	 * When a node is expanded, if its children have not already
	 * been fetched and placed into the tree, it will delete the dummy
	 * child node and fetch the actual children of the expanded node.
	 * During this process, the icon of the expanded node is switched
	 * to a loading indicator.
	 */
	@Override
	public void expandTreeItemOnOpen(final EntityTreeItem target) {
		if (!alreadyFetchedEntityChildren.contains(target)) {
			// We have not already fetched children for this entity.
			alreadyFetchedEntityChildren.add(target);
			// Change to loading icon.
			target.showLoadingIcon();
			long childCount = target == null ? 0 : target.asTreeItem().getChildCount();
			getFolderChildren(target.getHeader().getId(), target, 0);
			target.showTypeIcon();
		}
	}
	
	@Override
	public void clearRecordsFetchedChildren() {
		alreadyFetchedEntityChildren.clear();
	}
	
	/*
	 * Private Methods
	 */
	private void fireEntitySelectedEvent() {
		handlerManager.fireEvent(new EntitySelectedEvent());
	}
	
	public EntityQuery createGetChildrenQuery(String parentId, org.sagebionetworks.repo.model.entity.query.EntityType type) {
		EntityQuery newQuery = new EntityQuery();
		Sort sort = new Sort();
		sort.setColumnName(EntityFieldName.name.name());
		sort.setDirection(SortDirection.ASC);
		newQuery.setSort(sort);
		Condition condition = EntityQueryUtils.buildCondition(EntityFieldName.parentId, Operator.EQUALS, parentId);
		newQuery.setConditions(Arrays.asList(condition));
		newQuery.setFilterByType(type);
		newQuery.setLimit((long) MAX_FOLDER_LIMIT);
		newQuery.setOffset(OFFSET_ZERO);
		return newQuery;
	}
	
	public List<EntityTreeItem> getEntityTreeItemsFromHeaders(List<EntityHeader> results) {
		List<EntityTreeItem> treeItems = new LinkedList<EntityTreeItem>();
		for (EntityHeader header : results) {
			final EntityTreeItem childItem = ginInjector.getEntityTreeItemWidget();
			childItem.configure(header, false);
			treeItems.add(childItem);
		}
		return treeItems;
	}
	
	public List<EntityTreeItem> getEntityTreeItemsFromQueryResults(EntityQueryResults results) {
		return getEntityTreeItemsFromHeaders(getHeadersFromQueryResults(results));
	}
	
	public List<EntityHeader> getHeadersFromQueryResults(EntityQueryResults results) {
		List<EntityHeader> headerList = new LinkedList<EntityHeader>();
		for (EntityQueryResult result : results.getEntities()) {
			EntityHeader header = new EntityHeader();
			header.setId(result.getId());
			header.setName(result.getName());
			header.setType(result.getEntityType());
			header.setVersionNumber(result.getVersionNumber());
			headerList.add(header);
		}
		return headerList;
	}
	
	// Don't always pass false? Because parent can be null?
	public void addResultsToParent(final EntityTreeItem parent, EntityQueryResults results) {
		List<EntityHeader> headers = getHeadersFromQueryResults(results);
		List<EntityTreeItem> treeItems = getEntityTreeItemsFromHeaders(headers);
		for (EntityTreeItem toAdd: treeItems) {
			view.placeEntityTreeItem(toAdd, parent, parent == null);
		}
	}

	@Override
	public ImageResource getIconForType(String type) {
		return getIconForType(type, entityTypeProvider, iconsImageBundle);
	}
	
	public static ImageResource getIconForType(String type, EntityTypeProvider entityTypeProvider, IconsImageBundle iconsImageBundle) {
		if(type == null) return null;
		EntityType entityType;
		if(type.startsWith("org.")) entityType = entityTypeProvider.getEntityTypeForClassName(type); 			
		else entityType = entityTypeProvider.getEntityTypeForString(type);
		if (entityType == null) return null;
		return DisplayUtils.getSynapseIconForEntityClassName(entityType.getClassName(), DisplayUtils.IconSize.PX16, iconsImageBundle);
	}

	/**
	 * For testing purposes only
	 * @param currentFolderChildrenEntityId
	 */
	public void setCurrentFolderChildrenEntityId(String currentFolderChildrenEntityId) {
		this.currentFolderChildrenEntityId = currentFolderChildrenEntityId;
	}

}
