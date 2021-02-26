package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.List;

import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;

public interface EntityFinder {

	void configure(boolean showVersions, SelectedHandler<Reference> handler);

	void configure(EntityFilter selectableTypes, boolean showVersions, SelectedHandler<Reference> handler);

	void configure(EntityFilter viewable, EntityFilter selectable, boolean showVersions, SelectedHandler<Reference> handler);

	void configureMulti(boolean showVersions, SelectedHandler<List<Reference>> handler);

	void configureMulti(EntityFilter selectableTypes, boolean showVersions, SelectedHandler<List<Reference>> handler);

	void configureMulti(EntityFilter viewable, EntityFilter selectable, boolean showVersions, SelectedHandler<List<Reference>> handler);

	void showError(String errorMessage);

	void show();

	void hide();

	void clearState();
}
