package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.List;

import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;

public interface EntityFinder {

	interface Builder {
		EntityFinder build();

		Builder setSelectedHandler(DisplayUtils.SelectedHandler<Reference> handler);

		Builder setSelectedMultiHandler(DisplayUtils.SelectedHandler<List<Reference>> handler);

		Builder setMultiSelect(boolean multiSelect);

		Builder setInitialContainerId(String initialContainerId);

		Builder setSelectableFilter(EntityFilter selectableFilter);

		Builder setVisibleFilter(EntityFilter visibleFilter);

		Builder setShowVersions(boolean showVersions);

		Builder setModalTitle(String modalTitle);

		Builder setPromptCopy(String promptCopy);

		Builder setSelectedCopy(String selectedCopy);

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
