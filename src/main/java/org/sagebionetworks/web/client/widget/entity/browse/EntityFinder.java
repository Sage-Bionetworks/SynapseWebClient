package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.List;

import org.sagebionetworks.repo.model.Reference;

public interface EntityFinder {

	/**
	 * Designed to provide the same functionality as DisplayUtils.SelectedHandler,
	 * except also providing the finder itself. This allows the caller to access methods
	 * on the EntityFinder in the handler, which is specified prior to calling the Builder.build()
	 * @param <T>
	 */
	@FunctionalInterface
	public interface SelectedHandler<T> {
		public void onSelected(T selected, EntityFinder entityFinder);
	}


	interface Builder {
		EntityFinder build();

		Builder setSelectedHandler(SelectedHandler<Reference> handler);

		Builder setSelectedMultiHandler(SelectedHandler<List<Reference>> handler);

		Builder setMultiSelect(boolean multiSelect);

		Builder setInitialContainerId(String initialContainerId);

		Builder setSelectableTypesInList(EntityFilter selectableFilter);

		Builder setVisibleTypesInList(EntityFilter visibleFilter);

		Builder setVisibleTypesInTree(EntityFilter visibleTypesInTree);

		Builder setShowVersions(boolean showVersions);

		Builder setModalTitle(String modalTitle);

		Builder setPromptCopy(String promptCopy);

		Builder setHelpMarkdown(String helpMarkdown);

		Builder setSelectedCopy(String selectedCopy);

		Builder setInitialScope(EntityFinderScope initialScope);

		Builder setConfirmButtonCopy(String confirmButtonCopy);
	}

	@Deprecated
	/**
	 * Use EntityFinder.Builder
	 */
	void configure(boolean showVersions, SelectedHandler<Reference> handler);

	@Deprecated
	/**
	 * Use EntityFinder.Builder
	 */
	void configure(EntityFilter selectableTypes, boolean showVersions, SelectedHandler<Reference> handler);

	@Deprecated
	/**
	 * Use EntityFinder.Builder
	 */
	void configure(EntityFilter viewable, EntityFilter selectable, boolean showVersions, SelectedHandler<Reference> handler);

	@Deprecated
	/**
	 * Use EntityFinder.Builder
	 */
	void configureMulti(boolean showVersions, SelectedHandler<List<Reference>> handler);

	@Deprecated
	/**
	 * Use EntityFinder.Builder
	 */
	void configureMulti(EntityFilter selectableTypes, boolean showVersions, SelectedHandler<List<Reference>> handler);

	@Deprecated
	/**
	 * Use EntityFinder.Builder
	 */
	void configureMulti(EntityFilter viewable, EntityFilter selectable, boolean showVersions, SelectedHandler<List<Reference>> handler);

	void showError(String errorMessage);

	void show();

	void hide();

	void clearState();
}
