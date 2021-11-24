package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.List;

import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.jsinterop.EntityFinderProps;
import org.sagebionetworks.web.client.jsinterop.EntityFinderScope;

public interface EntityFinderWidget {

	/**
	 * Invoked when the user confirms their selection. The Entity Finder is also supplied as a parameter, which
	 * allows the caller to access methods on the EntityFinder in the handler
	 * @param <T>
	 */
	@FunctionalInterface
	interface SelectedHandler<T> {
		public void onSelected(T selected, EntityFinderWidget entityFinder);
	}

	enum InitialContainer {
		PROJECT,
		PARENT,
		SCOPE,
		NONE,
	}


	interface Builder {
		EntityFinderWidget build();

		Builder setSelectedHandler(SelectedHandler<Reference> handler);

		Builder setSelectedMultiHandler(SelectedHandler<List<Reference>> handler);

		Builder setMultiSelect(boolean multiSelect);

		Builder setInitialContainer(EntityFinderWidget.InitialContainer initialContainer);

		Builder setSelectableTypes(EntityFilter selectableFilter);

		Builder setMustSelectVersionNumber(boolean mustSelectVersion);

		Builder setVisibleTypesInList(EntityFilter visibleFilter);

		Builder setVisibleTypesInTree(EntityFilter visibleTypesInTree);

		Builder setShowVersions(boolean showVersions);

		Builder setModalTitle(String modalTitle);

		Builder setPromptCopy(String promptCopy);

		Builder setHelpMarkdown(String helpMarkdown);

		Builder setSelectedCopy(EntityFinderProps.SelectedCopyHandler selectedCopy);

		Builder setInitialScope(EntityFinderScope initialScope);

		Builder setConfirmButtonCopy(String confirmButtonCopy);

		Builder setTreeOnly(boolean treeOnly);
	}

	void showError(String errorMessage);

	void show();

	void hide();

	void clearState();
}
