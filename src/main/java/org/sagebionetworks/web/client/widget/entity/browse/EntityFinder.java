package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.List;

import org.sagebionetworks.repo.model.Reference;

public interface EntityFinder {

	/**
	 * Designed to provide the same functionality as DisplayUtils.SelectedHandler,
	 * except also providing the finder itself. This allows the caller to access methods
	 * on the EntityFinder in the handler, which is specified prior to calling Builder.build()
	 * @param <T>
	 */
	@FunctionalInterface
	public interface SelectedHandler<T> {
		public void onSelected(T selected, EntityFinder entityFinder);
	}

	enum InitialContainer {
		PROJECT,
		PARENT,
		SCOPE,
		NONE,
	}


	interface Builder {
		EntityFinder build();

		Builder setSelectedHandler(SelectedHandler<Reference> handler);

		Builder setSelectedMultiHandler(SelectedHandler<List<Reference>> handler);

		Builder setMultiSelect(boolean multiSelect);

		/**
		 * Initial container ID will dictate the current project, and will also be
		 * automatically opened if the initial scope is Current Project
		 *
		 *
		 * @param initialContainerId
		 * @return
		 */
		Builder setInitialContainer(InitialContainer initialContainer);

		Builder setSelectableTypes(EntityFilter selectableFilter);

		Builder setVisibleTypesInList(EntityFilter visibleFilter);

		Builder setVisibleTypesInTree(EntityFilter visibleTypesInTree);

		Builder setShowVersions(boolean showVersions);

		Builder setModalTitle(String modalTitle);

		Builder setPromptCopy(String promptCopy);

		Builder setHelpMarkdown(String helpMarkdown);

		Builder setSelectedCopy(String selectedCopy);

		Builder setInitialScope(EntityFinderScope initialScope);

		Builder setConfirmButtonCopy(String confirmButtonCopy);

		Builder setTreeOnly(boolean treeOnly);
	}

	void showError(String errorMessage);

	void show();

	void hide();

	void clearState();
}
