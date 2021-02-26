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

	void renderComponent(String initialContainerId, boolean showVersions, boolean multiSelect, EntityFilter visible, EntityFilter selectable);

	void setSynAlert(Widget w);


	/**
	 * Presenter interface
	 */
	public interface Presenter {

		void setSelectedEntity(Reference selected);

		void lookupEntity(String entityId, CallbackP<List<EntityHeader>> callback);

		void lookupEntity(ReferenceList rl, CallbackP<List<EntityHeader>> callback);

		boolean showVersions();

		void okClicked();

		void show();

		void hide();

		Widget asWidget();

		void setSelectedEntities(List<Reference> selected);

		void clearSelectedEntities();

	}

	Widget asWidget();
}
