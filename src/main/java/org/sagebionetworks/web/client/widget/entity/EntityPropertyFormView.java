package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;
import org.sagebionetworks.web.client.widget.entity.row.EntityFormModel;

import com.google.gwt.user.client.ui.IsWidget;

public interface EntityPropertyFormView extends SynapseWidgetView, IsWidget {
	
	public void setPresenter(Presenter presenter);
	public void refresh();
	public void hideLoading();
	public void showEditEntityDialog(final String windowTitle);
	public boolean isComponentVisible();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter extends SynapseWidgetView{
		public void removeAnnotation();
		public void addAnnotation();
		public Entity getEntity();
		public EntityFormModel getFormModel();
		public void saveButtonClicked();
		public void refreshEntityAttachments();
	}

}
