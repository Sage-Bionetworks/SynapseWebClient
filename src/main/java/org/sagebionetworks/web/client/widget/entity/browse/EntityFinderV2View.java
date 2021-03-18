package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.gwt.user.client.ui.Widget;

public interface EntityFinderV2View extends SynapseView {

	/**
	 * Set the presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	boolean isShowing();

	void show();

	void hide();

	void renderComponent(String initialContainerId, EntityFinderScope initialScope, boolean showVersions, boolean multiSelect, EntityFilter visible, EntityFilter selectable, EntityFilter visibleTypesInTree, String selectedCopy);

	void setSynAlert(Widget w);

    void setModalTitle(String modalTitle);

	void setPromptCopy(String promptCopy);

	void setHelpMarkdown(String helpMarkdown);

	void setConfirmButtonCopy(String confirmButtonCopy);


	/**
	 * Presenter interface
	 */
	public interface Presenter {

		void setSelectedEntity(Reference selected);

		void okClicked();

		void show();

		void hide();

		Widget asWidget();

		void setSelectedEntities(List<Reference> selected);

		void clearSelectedEntities();

		void renderComponent();
	}

	Widget asWidget();
}
