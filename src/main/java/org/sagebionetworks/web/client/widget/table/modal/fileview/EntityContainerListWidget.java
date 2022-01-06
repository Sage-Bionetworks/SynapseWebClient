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
import org.sagebionetworks.web.client.jsinterop.EntityFinderScope;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderWidget;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderWidget.SelectedHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityContainerListWidget implements EntityContainerListWidgetView.Presenter, IsWidget {
	EntityFinderWidget finder;
	EntityFinderWidget.Builder entityFinderBuilder;
	EntityContainerListWidgetView view;
	SynapseJavascriptClient jsClient;
	List<Reference> references;
	SynapseAlert synAlert;
	boolean canEdit = true;
	SelectedHandler<List<Reference>> selectionHandler;

	@Inject
	public EntityContainerListWidget(EntityContainerListWidgetView view, EntityFinderWidget.Builder entityFinderBuilder, SynapseJavascriptClient jsClient, SynapseAlert synAlert) {
		this.view = view;
		this.entityFinderBuilder = entityFinderBuilder;
		this.jsClient = jsClient;
		this.synAlert = synAlert;
		view.setPresenter(this);

		references = new ArrayList<Reference>();
		selectionHandler = new SelectedHandler<List<Reference>>() {
			@Override
			public void onSelected(List<Reference> selected, EntityFinderWidget finder) {
				for (Reference ref : selected) {
					onAddEntity(ref.getTargetId());
				}
			}
		};
	}

	public void configure(List<Reference> entityContainerIds, boolean canEditScope, TableType tableType) {
		view.clear();
		references.clear();
		this.canEdit = canEditScope;
		view.setAddButtonVisible(canEditScope);
		view.setNoContainers(entityContainerIds.isEmpty());
		synAlert.clear();
		if (!entityContainerIds.isEmpty()) {
			jsClient.getEntityHeaderBatchFromReferences(entityContainerIds, new AsyncCallback<ArrayList<EntityHeader>>() {
				@Override
				public void onFailure(Throwable caught) {
					synAlert.handleException(caught);
				}

				@Override
				public void onSuccess(ArrayList<EntityHeader> entityHeaders) {
					for (EntityHeader header : entityHeaders) {
						Reference reference = new Reference();
						reference.setTargetId(header.getId());
						reference.setTargetVersionNumber(header.getVersionNumber());
						references.add(reference);
						view.addEntity(header.getId(), header.getName(), EntityContainerListWidget.this.canEdit);
					}
				}
			});
		}

		if (TableType.project_view.equals(tableType)) {
			entityFinderBuilder
					.setModalTitle("Set " + tableType.getDisplayName() + " Containers")
					.setInitialScope(EntityFinderScope.ALL_PROJECTS)
					.setInitialContainer(EntityFinderWidget.InitialContainer.SCOPE)
					.setSelectableTypes(EntityFilter.PROJECT)
					.setShowVersions(false)
					.setHelpMarkdown("Search or Browse Synapse to find " + EntityTypeUtils.getDisplayName(EntityType.project) + "s to put into this " + tableType.getDisplayName())
					.setPromptCopy("Find " + EntityTypeUtils.getDisplayName(EntityType.project) + "s for this View");
		} else if (TableType.dataset.equals(tableType)) {
			entityFinderBuilder
					.setModalTitle("Add Files to " + tableType.getDisplayName())
					.setInitialScope(EntityFinderScope.CURRENT_PROJECT)
					.setInitialContainer(EntityFinderWidget.InitialContainer.PROJECT)
					.setSelectableTypes(EntityFilter.FILE)
					.setShowVersions(true)
					.setMustSelectVersionNumber(true)
					.setSelectedCopy((count) -> count + " File" + ((count > 1) ? "s" : "") + " Selected")
					.setHelpMarkdown("Search or Browse Synapse to find " + EntityTypeUtils.getDisplayName(EntityType.file) + "s to add to this " + tableType.getDisplayName())
					.setPromptCopy("Find and select " + EntityTypeUtils.getDisplayName(EntityType.file) + "s to add to the " + tableType.getDisplayName());
		} else {
			entityFinderBuilder
					.setModalTitle("Set " + tableType.getDisplayName() + " Containers")
					.setInitialScope(EntityFinderScope.CURRENT_PROJECT)
					.setInitialContainer(EntityFinderWidget.InitialContainer.PROJECT)
					.setSelectableTypes(EntityFilter.CONTAINER)
					.setShowVersions(false)
					.setHelpMarkdown("Search or Browse Synapse to find " + EntityTypeUtils.getDisplayName(EntityType.folder) + "s containing items for this " + tableType.getDisplayName())
					.setPromptCopy("Find and select " + EntityTypeUtils.getDisplayName(EntityType.folder) + "s to add their contents");
		}
		finder = entityFinderBuilder
				.setMultiSelect(true)
				.setSelectedMultiHandler(selectionHandler)
				.build();
	}

	@Override
	public void onAddEntity() {
		finder.show();
	}

	/**
	 * Called when a container entity is selected in the entity finder.
	 * 
	 * @param id
	 */
	public void onAddEntity(String id) {
		// TODO: Support versions
		jsClient.getEntityHeaderBatch(Collections.singletonList(id), new AsyncCallback<ArrayList<EntityHeader>>() {
			@Override
			public void onFailure(Throwable caught) {
				finder.showError(caught.getMessage());
			}

			@Override
			public void onSuccess(ArrayList<EntityHeader> entityHeaders) {
				if (entityHeaders.size() == 1) {
					EntityHeader entity = entityHeaders.get(0);
					Reference reference = new Reference();
					reference.setTargetId(entity.getId());
					reference.setTargetVersionNumber(entity.getVersionNumber());
					references.add(reference);
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
	public void onRemoveEntity(String id) {
		for (int i = references.size() - 1; i >= 0; i--) {
			if (references.get(i).getTargetId().equals(id)) {
				references.remove(i);
			}
		}
	}

	public List<String> getEntityIds() {
		List<String> entityIds = new ArrayList<>(references.size());
		for (Reference reference : references) {
			entityIds.add(reference.getTargetId());
		}
		return entityIds;
	}

	public List<Reference> getReferences() {
		return references;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
