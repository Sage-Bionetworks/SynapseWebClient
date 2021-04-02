package org.sagebionetworks.web.client.widget.table.modal.fileview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.EntityTypeUtils;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder.SelectedHandler;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderScope;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityContainerListWidget implements EntityContainerListWidgetView.Presenter, IsWidget {
	EntityFinder finder;
	EntityFinder.Builder entityFinderBuilder;
	EntityContainerListWidgetView view;
	SynapseJavascriptClient jsClient;
	List<String> entityIds;
	SynapseAlert synAlert;
	boolean canEdit = true;
	boolean showVersions = false;
	SelectedHandler<List<Reference>> selectionHandler;

	@Inject
	public EntityContainerListWidget(EntityContainerListWidgetView view, EntityFinder.Builder entityFinderBuilder, SynapseJavascriptClient jsClient, SynapseAlert synAlert) {
		this.view = view;
		this.entityFinderBuilder = entityFinderBuilder;
		this.jsClient = jsClient;
		this.synAlert = synAlert;
		view.setPresenter(this);

		entityIds = new ArrayList<String>();
		selectionHandler = new SelectedHandler<List<Reference>>() {
			@Override
			public void onSelected(List<Reference> selected, EntityFinder finder) {
				for (Reference ref : selected) {
					onAddProject(ref.getTargetId());
				}
			}
		};
	}

	public void configure(List<String> entityContainerIds, boolean canEdit, TableType tableType) {
		view.clear();
		entityIds.clear();
		this.canEdit = canEdit;
		view.setAddButtonVisible(canEdit);
		view.setNoContainers(entityContainerIds.isEmpty());
		synAlert.clear();
		if (!entityContainerIds.isEmpty()) {
			jsClient.getEntityHeaderBatch(entityContainerIds, new AsyncCallback<ArrayList<EntityHeader>>() {
				@Override
				public void onFailure(Throwable caught) {
					synAlert.handleException(caught);
				}

				@Override
				public void onSuccess(ArrayList<EntityHeader> entityHeaders) {
					for (EntityHeader header : entityHeaders) {
						entityIds.add(header.getId());
						view.addEntity(header.getId(), header.getName(), EntityContainerListWidget.this.canEdit);
					}
				}
			});
		}

		if (TableType.projects.equals(tableType)) {
			String friendlyEntityType = "Project View";
			entityFinderBuilder.setInitialScope(EntityFinderScope.ALL_PROJECTS);
			entityFinderBuilder.setSelectableTypes(EntityFilter.PROJECT);
			entityFinderBuilder.setHelpMarkdown("Search or Browse Synapse to find " + EntityTypeUtils.getDisplayName(EntityType.project) + "s to put into this " + friendlyEntityType);
			entityFinderBuilder.setPromptCopy("Find " + EntityTypeUtils.getDisplayName(EntityType.project) + "s for this View");
		} else {
			String friendlyEntityType = "File View";
			entityFinderBuilder.setSelectableTypes(EntityFilter.CONTAINER);
			entityFinderBuilder.setHelpMarkdown("Search or Browse Synapse to find " + EntityTypeUtils.getDisplayName(EntityType.folder) + "s to put into this " + friendlyEntityType);
			entityFinderBuilder.setPromptCopy("Find " + EntityTypeUtils.getDisplayName(EntityType.folder) + "s for this View");
		}
		finder = entityFinderBuilder
				.setModalTitle("Set View Containers")
				.setMultiSelect(true)
				.setSelectedMultiHandler(selectionHandler)
				.setShowVersions(showVersions)
				.build();
	}

	@Override
	public void onAddProject() {
		finder.show();
	}

	/**
	 * Called when a container entity is selected in the entity finder.
	 * 
	 * @param id
	 */
	public void onAddProject(String id) {
		jsClient.getEntityHeaderBatch(Collections.singletonList(id), new AsyncCallback<ArrayList<EntityHeader>>() {
			@Override
			public void onFailure(Throwable caught) {
				finder.showError(caught.getMessage());
			}

			@Override
			public void onSuccess(ArrayList<EntityHeader> entityHeaders) {
				if (entityHeaders.size() == 1) {
					EntityHeader entity = entityHeaders.get(0);
					entityIds.add(entity.getId());
					view.setNoContainers(false);
					view.addEntity(entity.getId(), entity.getName(), canEdit);
					finder.hide();
				} else {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_LOADING));
				}
			}
		});
	}

	@Override
	public void onRemoveProject(String id) {
		entityIds.remove(id);
	}

	public List<String> getEntityIds() {
		return entityIds;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
