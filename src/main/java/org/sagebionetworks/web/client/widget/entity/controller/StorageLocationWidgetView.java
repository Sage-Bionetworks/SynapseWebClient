package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface StorageLocationWidgetView {

	public interface Presenter {

		Widget asWidget();

		void onSave();

		void clear();

		void configure(EntityBundle entityBundle,
				EntityUpdatedHandler entityUpdateHandler);
		
	}

	Widget asWidget();

	void setSynAlertWidget(IsWidget asWidget);
	
	void setPresenter(Presenter presenter);

	void clear();

	void hide();

	void show();

	void selectSynapseStorage();
	boolean isSynapseStorageSelected();
	
	void selectExternalS3Storage();
	boolean isExternalS3StorageSelected();
	String getBucket();
	
	void selectSFTPStorage();
	boolean isSFTPStorageSelected();
	String getSFTPUrl();
}
