package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityType;
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
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.EntityTreeItem;
import org.sagebionetworks.web.client.widget.entity.MoreTreeItem;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.resources.client.ImageResource;
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
	private HandlerManager handlerManager = new HandlerManager(this);
	AdapterFactory adapterFactory;
	private Set<EntityTreeItem> alreadyFetchedEntityChildren;
	private PortalGinInjector ginInjector;
	private String currentSelection;
	private final int MAX_FOLDER_LIMIT = 100;

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
		handlerManager = new HandlerManager(this);
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
		getFolderChildren(searchId, null, 0);
	}

	public void configure(List<EntityHeader> headers) {
		view.clear();
		view.setLoadingVisible(true);
		for (EntityHeader header : headers) {
			view.appendRootEntityTreeItem(makeTreeItemFromHeader(header, true,
					false));
		}
		view.setLoadingVisible(false);
	}

	@Override
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}

	@Override
	public void getFolderChildren(final String parentId,
			final EntityTreeItem parent, final long offset) {
		EntityQuery childrenQuery = createGetChildrenQuery(parentId, offset,
				org.sagebionetworks.repo.model.entity.query.EntityType.folder);
		childrenQuery.setLimit((long) MAX_FOLDER_LIMIT);
		// ask for the folder children, then the files
		synapseClient.executeEntityQuery(childrenQuery,
				new AsyncCallback<EntityQueryResults>() {
					@Override
					public void onSuccess(EntityQueryResults results) {
						if (!results.getEntities().isEmpty()) {
							addResultsToParent(
									parent,
									results,
									org.sagebionetworks.repo.model.entity.query.EntityType.folder,
									offset, true);
							// More total entities than able to be displayed, so
							// must add a "More Folders" button
							if (results.getTotalEntityCount() > offset
									+ results.getEntities().size()) {
								final MoreTreeItem moreItem = ginInjector
										.getMoreTreeWidget();
								moreItem.configure(MoreTreeItem.MORE_TYPE.FOLDER);
								addMoreButton(moreItem, parentId, parent,
										offset);
							}
						}
						if (offset == 0) {
							getChildrenFiles(parentId, parent, 0);
						} else {
							if (parent == null)
								view.setLoadingVisible(false);
							else
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
			if (moreItem.type == MoreTreeItem.MORE_TYPE.FOLDER)
				view.placeRootMoreFoldersTreeItem(moreItem, parentId, offset
						+ MAX_FOLDER_LIMIT);
			else
				view.placeRootMoreFilesTreeItem(moreItem, parentId, offset
						+ MAX_FOLDER_LIMIT);
		} else {
			if (moreItem.type == MoreTreeItem.MORE_TYPE.FOLDER)
				view.placeChildMoreFoldersTreeItem(moreItem, parent, offset
						+ MAX_FOLDER_LIMIT);
			else
				view.placeChildMoreFilesTreeItem(moreItem, parent, offset
						+ MAX_FOLDER_LIMIT);
		}
	}

	@Override
	public void getChildrenFiles(final String parentId,
			final EntityTreeItem parent, final long offset) {
		EntityQuery childrenQuery = createGetChildrenQuery(parentId, offset,
				org.sagebionetworks.repo.model.entity.query.EntityType.file);
		synapseClient.executeEntityQuery(childrenQuery,
				new AsyncCallback<EntityQueryResults>() {
					@Override
					public void onSuccess(EntityQueryResults results) {
						if (!results.getEntities().isEmpty()) {
							addResultsToParent(
									parent,
									results,
									org.sagebionetworks.repo.model.entity.query.EntityType.file,
									offset, false);
							// More total entities than able to be displayed, so
							// must add a "More Files" button
							if (results.getTotalEntityCount() > offset
									+ results.getEntities().size()) {
								final MoreTreeItem moreItem = ginInjector
										.getMoreTreeWidget();
								moreItem.configure(MoreTreeItem.MORE_TYPE.FILE);
								addMoreButton(moreItem, parentId, parent,
										offset);
							}
						}
						if (parent == null) {
							view.setLoadingVisible(false);
						} else {
							parent.showTypeIcon();

						}
						// Root should only have the now hidden loading UI,
						// which amounts to a single child.
						if (parent == null && view.getRootCount() == 0) {
							view.showEmptyUI();
						} else {
							view.hideEmptyUI();
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
			getFolderChildren(target.getHeader().getId(), target, 0);
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

	public EntityQuery createGetChildrenQuery(String parentId, long offset,
			org.sagebionetworks.repo.model.entity.query.EntityType type) {
		EntityQuery newQuery = new EntityQuery();
		Sort sort = new Sort();
		sort.setColumnName(EntityFieldName.name.name());
		sort.setDirection(SortDirection.ASC);
		newQuery.setSort(sort);
		Condition condition = EntityQueryUtils.buildCondition(
				EntityFieldName.parentId, Operator.EQUALS, parentId);
		newQuery.setConditions(Arrays.asList(condition));
		newQuery.setFilterByType(type);
		newQuery.setLimit((long) MAX_FOLDER_LIMIT);
		newQuery.setOffset(offset);
		return newQuery;
	}

	public List<EntityHeader> getHeadersFromQueryResults(
			EntityQueryResults results) {
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

	public EntityTreeItem makeTreeItemFromHeader(EntityHeader header,
			boolean isRootItem, boolean isExpandable) {
		final EntityTreeItem childItem = ginInjector.getEntityTreeItemWidget();
		childItem.configure(header, isRootItem, isExpandable);
		return childItem;
	}

	public void addResultsToParent(final EntityTreeItem parent,
			EntityQueryResults results,
			org.sagebionetworks.repo.model.entity.query.EntityType type,
			long offset, boolean isExpandable) {
		List<EntityHeader> headers = getHeadersFromQueryResults(results);
		if (parent == null) {
			if (type == org.sagebionetworks.repo.model.entity.query.EntityType.file) {
				for (EntityHeader header : headers) {
					view.appendRootEntityTreeItem(makeTreeItemFromHeader(
							header, true, false));
				}
			} else {
				for (EntityHeader header : headers) {
					view.insertRootEntityTreeItem(
							makeTreeItemFromHeader(header, true, true),
							offset++);
				}
			}
		} else {
			if (type == org.sagebionetworks.repo.model.entity.query.EntityType.file) {
				for (EntityHeader header : headers) {
					view.appendChildEntityTreeItem(
							makeTreeItemFromHeader(header, false, false),
							parent);
				}
			} else {
				for (EntityHeader header : headers) {
					view.insertChildEntityTreeItem(
							makeTreeItemFromHeader(header, false, true),
							parent, offset++);
				}
			}
		}
	}

	public static ImageResource getIconForType(String type,
			IconsImageBundle iconsImageBundle) {
		if (type == null)
			return null;
		EntityType entityType;
		if (type.startsWith("org."))
			entityType = EntityType.getEntityTypeForClassName(type);
		else
			entityType = EntityType.valueOf(type.toLowerCase());
		if (entityType == null)
			return null;
		return DisplayUtils.getSynapseIconForEntityClassName(
				entityType.getClassForType().getName(), DisplayUtils.IconSize.PX16,
				iconsImageBundle);
	}
}
