package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.file.ExternalS3UploadDestination;
import org.sagebionetworks.repo.model.file.ExternalUploadDestination;
import org.sagebionetworks.repo.model.file.S3UploadDestination;
import org.sagebionetworks.repo.model.file.UploadDestination;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.EntityMetadataView.Presenter;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationsRendererWidget;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityMetadata implements Presenter {

	private EntityMetadataView view;
	private EntityUpdatedHandler entityUpdatedHandler;
	private AnnotationsRendererWidget annotationsWidget;
	private DoiWidget doiWidget;
	private FileHistoryWidget fileHistoryWidget;
	private RestrictionWidget restrictionWidget;
	private SynapseClientAsync synapseClient;
	private SynapseJSNIUtils jsni;
	
	@Inject
	public EntityMetadata(EntityMetadataView view, 
			DoiWidget doiWidget,
			AnnotationsRendererWidget annotationsWidget,
			RestrictionWidget restrictionWidget,
			FileHistoryWidget fileHistoryWidget, 
			SynapseClientAsync synapseClient, 
			SynapseJSNIUtils jsni) {
		this.view = view;
		this.doiWidget = doiWidget;
		this.annotationsWidget = annotationsWidget;
		this.fileHistoryWidget = fileHistoryWidget;
		this.restrictionWidget = restrictionWidget;
		this.synapseClient = synapseClient;
		this.jsni = jsni;
		this.view.setDoiWidget(doiWidget);
		this.view.setAnnotationsRendererWidget(annotationsWidget);
		this.view.setFileHistoryWidget(fileHistoryWidget);
		this.view.setRestrictionWidget(restrictionWidget);
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void setEntityBundle(EntityBundle bundle, Long versionNumber) {
		clear();
		Entity en = bundle.getEntity();
		view.setEntityId(en.getId());
		boolean canEdit = bundle.getPermissions().getCanCertifiedUserEdit();
		boolean showDetailedMetadata = true;
		if (bundle.getEntity() instanceof FileEntity) {
			fileHistoryWidget.setEntityBundle(bundle, versionNumber);
			fileHistoryWidget.setEntityUpdatedHandler(entityUpdatedHandler);
			view.setFileHistoryWidget(fileHistoryWidget);
			view.setRestrictionPanelVisible(true);
		} else {
			view.setRestrictionPanelVisible(en instanceof TableEntity
					|| en instanceof Folder);
		}
		configureStorageLocation(en);
		restrictionWidget.configure(bundle, true, false, true, new Callback() {
			@Override
			public void invoke() {
				fireEntityUpdatedEvent();
			}
		});
		doiWidget.configure(bundle.getDoi(), en.getId());
		boolean isCurrentVersion = versionNumber == null;
		annotationsWidget.configure(bundle, canEdit, isCurrentVersion);
		view.setDetailedMetadataVisible(showDetailedMetadata);
	}	

	@Override
	public void fireEntityUpdatedEvent() {
		if (entityUpdatedHandler != null)
			entityUpdatedHandler.onPersistSuccess(new EntityUpdatedEvent());
	}
	
	public void setEntityUpdatedHandler(EntityUpdatedHandler handler) {
		this.entityUpdatedHandler = handler;
		this.annotationsWidget.setEntityUpdatedHandler(entityUpdatedHandler);
	}

	public void setAnnotationsVisible(boolean visible) {
		view.setAnnotationsVisible(visible);
	}
	
	public void setFileHistoryVisible(boolean visible) {
		view.setFileHistoryVisible(visible);
	}
	
	public void clear() {
		doiWidget.clear();
		view.clear();
	}
	
	 public void configureStorageLocation(Entity en) {
		 view.setUploadDestinationPanelVisible(false);
		 if (en instanceof Folder || en instanceof Project) {
			 String containerEntityId = en.getId();
			 synapseClient.getUploadDestinations(containerEntityId, new AsyncCallback<List<UploadDestination>>() {
				public void onSuccess(List<UploadDestination> uploadDestinations) {
					if (uploadDestinations == null || uploadDestinations.isEmpty() || uploadDestinations.get(0) instanceof S3UploadDestination) {
						view.setUploadDestinationText("Synapse Storage");
					} else if (uploadDestinations.get(0) instanceof ExternalUploadDestination){
						ExternalUploadDestination externalUploadDestination = (ExternalUploadDestination) uploadDestinations.get(0);
						view.setUploadDestinationText(externalUploadDestination.getUrl());
					} else if (uploadDestinations.get(0) instanceof ExternalS3UploadDestination) {
						ExternalS3UploadDestination externalUploadDestination = (ExternalS3UploadDestination) uploadDestinations.get(0);
						String description = "s3://" + externalUploadDestination.getBucket() + "/";
						if (externalUploadDestination.getBaseKey() != null) {
							description += externalUploadDestination.getBaseKey();
						};
						view.setUploadDestinationText(description);
					}
					view.setUploadDestinationPanelVisible(true);
				}
	
				@Override
				public void onFailure(Throwable err) {
					jsni.consoleLog(err.getMessage());
				};
			});
		 }
    }
	
}
