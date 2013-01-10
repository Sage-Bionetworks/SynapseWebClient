package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.AttachmentSelectedEvent;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedEvent;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.presenter.BaseEditWidgetDescriptorPresenter;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.client.widget.entity.row.EntityFormModel;

import com.google.gwt.user.client.ui.IsWidget;

public interface EntityPropertyFormView extends SynapseWidgetView, IsWidget {
	
	public void setPresenter(Presenter presenter);
	public void refresh();
	public void hideLoading();
	public void showPreview(String html, EntityBundle bundle, WidgetRegistrar widgetRegistrar, SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator, JSONObjectAdapter jsonObjectAdapter) throws JSONObjectAdapterException;
	public void insertMarkdown(String md);
	public void insertWidgetMarkdown(String attachmentName);
	public BaseEditWidgetDescriptorPresenter getWidgetDescriptorEditor();
	public void removeAllOccurrences(String oldMd);
	public void replaceAllOccurrences(String oldMd, String newMd);
	public void showEditEntityDialog(final String windowTitle);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter extends SynapseWidgetView{
		public void removeAnnotation();
		public void addAnnotation();
		public Entity getEntity();
		public EntityFormModel getFormModel();
		public void insertWidget(String contentTypeKey);
		public void attachmentUpdated(WidgetDescriptorUpdatedEvent event);
		public void attachmentSelected(AttachmentSelectedEvent event);
		public void saveButtonClicked();
		public void showPreview(String html, String attachmentBaseUrl);
	}

}
