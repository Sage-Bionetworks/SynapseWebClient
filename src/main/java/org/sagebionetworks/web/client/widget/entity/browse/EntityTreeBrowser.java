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
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntitySelectedEvent;
import org.sagebionetworks.web.client.events.EntitySelectedHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.EntityTreeItem;
import org.sagebionetworks.web.shared.EntityType;

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
	
	private String currentSelection;
	
	private final int MAX_FOLDER_LIMIT = 500;
	private String currentFolderChildrenEntityId;
	
	@Inject
	public EntityTreeBrowser(EntityTreeBrowserView view,
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
	public void configure(String entityId) {
		view.clear();
		view.showLoading();
		getFolderChildren(entityId, new AsyncCallback<List<EntityHeader>>() {
			@Override
			public void onSuccess(List<EntityHeader> results) {
				view.setRootEntities(results);
			}
			@Override
			public void onFailure(Throwable caught) {
				DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view);
			}
		});
	}

	
	/**
	 * Configure tree view to be filled initially with the given headers.
	 * @param rootEntities
	 */
	public void configure(List<EntityHeader> rootEntities, boolean sort) {
		if (sort)
			EntityBrowserUtils.sortEntityHeadersByName(rootEntities);
		view.setRootEntities(rootEntities);
	}
	
	@Override
	public Widget asWidget() {
		view.setPresenter(this);		
		return view.asWidget();
	}
	
	@Override
	public void getFolderChildren(final String entityId, final AsyncCallback<List<EntityHeader>> asyncCallback) {
		EntityQuery childrenQuery = createGetChildrenQuery(entityId, org.sagebionetworks.repo.model.entity.query.EntityType.folder);
		//ask for the folder children, then the files
		synapseClient.executeEntityQuery(childrenQuery, new AsyncCallback<EntityQueryResults>() {
			@Override
			public void onSuccess(EntityQueryResults results) {
				getChildrenFiles(entityId, getHeadersFromQueryResults(results), asyncCallback);
			}
			@Override
			public void onFailure(Throwable caught) {
				DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view);				
				asyncCallback.onFailure(caught);
			}
		});
		
	}
	
	public void getChildrenFiles(String entityId, final List<EntityHeader> entityHeaders, final AsyncCallback<List<EntityHeader>> asyncCallback) {
		EntityQuery childrenQuery = createGetChildrenQuery(entityId, org.sagebionetworks.repo.model.entity.query.EntityType.file);
		synapseClient.executeEntityQuery(childrenQuery, new AsyncCallback<EntityQueryResults>() {
			@Override
			public void onSuccess(EntityQueryResults results) {
				entityHeaders.addAll(getHeadersFromQueryResults(results));
				asyncCallback.onSuccess(entityHeaders);
			}
			@Override
			public void onFailure(Throwable caught) {
				DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view);				
				asyncCallback.onFailure(caught);
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
			
			// Change to loading icon.
			target.showLoadingIcon();
			
			getFolderChildren(target.getHeader().getId(), new AsyncCallback<List<EntityHeader>>() {
				
				@Override
				public void onSuccess(List<EntityHeader> result) {
					// We got the children.
					alreadyFetchedEntityChildren.add(target);
					target.asTreeItem().removeItems();	// Remove the dummy item.
					
					// Make a tree item for each child and place them in the tree.
					for (EntityHeader header : result) {
						view.createAndPlaceTreeItem(header, target, false);
					}
					
					// Change back to type icon.
					target.showTypeIcon();
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if (!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)) {                    
						view.showErrorMessage(caught.getMessage());
					}
				}
				
			});
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
