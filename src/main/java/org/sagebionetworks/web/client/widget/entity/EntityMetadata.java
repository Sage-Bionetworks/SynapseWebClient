package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.file.ExternalObjectStoreUploadDestination;
import org.sagebionetworks.repo.model.file.ExternalS3UploadDestination;
import org.sagebionetworks.repo.model.file.ExternalUploadDestination;
import org.sagebionetworks.repo.model.file.S3UploadDestination;
import org.sagebionetworks.repo.model.file.UploadDestination;
import org.sagebionetworks.repo.model.file.UploadType;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.widget.doi.DoiWidgetV2;
import org.sagebionetworks.web.client.widget.entity.EntityMetadataView.Presenter;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationsRendererWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.restriction.v2.RestrictionWidget;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityMetadata implements Presenter {

	private EntityMetadataView view;
	private AnnotationsRendererWidget annotationsWidget;
	private DoiWidget doiWidget;
	private DoiWidgetV2 doiWidgetV2;
	private FileHistoryWidget fileHistoryWidget;
	private SynapseJavascriptClient jsClient;
	private SynapseJSNIUtils jsni;
	private org.sagebionetworks.web.client.widget.entity.restriction.v2.RestrictionWidget restrictionWidgetV2;
	private CookieProvider cookies;

	boolean isShowingAnnotations, isShowingFileHistory;
	@Inject
	public EntityMetadata(EntityMetadataView view,
						  DoiWidget doiWidget,
						  DoiWidgetV2 doiWidgetV2,
			AnnotationsRendererWidget annotationsWidget,
			FileHistoryWidget fileHistoryWidget, 
			SynapseJavascriptClient jsClient, 
			SynapseJSNIUtils jsni,
			RestrictionWidget restrictionWidgetV2,
			CookieProvider cookies) {
		this.view = view;
		this.doiWidget = doiWidget;
		this.doiWidgetV2 = doiWidgetV2;
		this.annotationsWidget = annotationsWidget;
		this.fileHistoryWidget = fileHistoryWidget;
		this.jsClient = jsClient;
		this.jsni = jsni;
		this.restrictionWidgetV2 = restrictionWidgetV2;
		this.cookies = cookies;
		if (DisplayUtils.isInTestWebsite(cookies)) {
			this.view.setDoiWidget(doiWidgetV2);
		} else {
			this.view.setDoiWidget(doiWidget);
		}
		this.view.setAnnotationsRendererWidget(annotationsWidget);
		this.view.setFileHistoryWidget(fileHistoryWidget);
		this.view.setRestrictionWidgetV2(restrictionWidgetV2);
		restrictionWidgetV2.setShowChangeLink(true);
		restrictionWidgetV2.setShowIfProject(false);
		restrictionWidgetV2.setShowFlagLink(true);
		view.setRestrictionWidgetV2Visible(true);
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void configure(EntityBundle bundle, Long versionNumber, ActionMenuWidget actionMenu) {
		clear();
		Entity en = bundle.getEntity();
		view.setEntityId(en.getId());
		boolean canEdit = bundle.getPermissions().getCanCertifiedUserEdit();
		
		boolean isCurrentVersion = versionNumber == null;
		if (bundle.getEntity() instanceof FileEntity) {
			fileHistoryWidget.setEntityBundle(bundle, versionNumber);
			view.setFileHistoryWidget(fileHistoryWidget);
			view.setRestrictionPanelVisible(true);
		} else {
			view.setRestrictionPanelVisible(en instanceof TableEntity
					|| en instanceof Folder || en instanceof DockerRepository);
		}
		configureStorageLocation(en);
		if (DisplayUtils.isInTestWebsite(cookies)) {
			doiWidgetV2.configure(bundle.getDoiAssociation());
		} else {
			doiWidget.configure(bundle.getDoi(), en.getId());
		}
		annotationsWidget.configure(bundle, canEdit, isCurrentVersion);
		restrictionWidgetV2.configure(en, bundle.getPermissions().getCanChangePermissions());
		isShowingAnnotations = false;
		setAnnotationsVisible(isShowingAnnotations);
		isShowingFileHistory = false;
		setFileHistoryVisible(isShowingFileHistory);
		actionMenu.setActionListener(Action.SHOW_ANNOTATIONS, action -> {
			isShowingAnnotations = !isShowingAnnotations;
			setAnnotationsVisible(isShowingAnnotations);
		});
		
		actionMenu.setActionListener(Action.SHOW_FILE_HISTORY, action -> {
			isShowingFileHistory = !isShowingFileHistory;
			setFileHistoryVisible(isShowingFileHistory);
		});
	}
	public void setVisible(boolean visible) {
		view.setDetailedMetadataVisible(visible);
	}
		
	public void setAnnotationsVisible(boolean visible) {
		view.setAnnotationsVisible(visible);
	}
	
	public void setFileHistoryVisible(boolean visible) {
		view.setFileHistoryVisible(visible);
	}
	
	public void clear() {
		doiWidget.clear();
		doiWidgetV2.clear();
		view.clear();
	}
	
	 public void configureStorageLocation(Entity en) {
		 view.setUploadDestinationPanelVisible(false);
		 if (en instanceof Folder || en instanceof Project) {
			 String containerEntityId = en.getId();
			 jsClient.getUploadDestinations(containerEntityId, new AsyncCallback<List<UploadDestination>>() {
				public void onSuccess(List<UploadDestination> uploadDestinations) {
					if (uploadDestinations == null || uploadDestinations.isEmpty() || uploadDestinations.get(0) instanceof S3UploadDestination) {
						view.setUploadDestinationText("Synapse Storage");
					} else if (uploadDestinations.get(0) instanceof ExternalUploadDestination){
						ExternalUploadDestination externalUploadDestination = (ExternalUploadDestination) uploadDestinations.get(0);
						String externalUrl = externalUploadDestination.getUrl();
						UploadType type = externalUploadDestination.getUploadType();
						if (type == UploadType.SFTP){
							int indexOfLastSlash = externalUrl.lastIndexOf('/');
							if (indexOfLastSlash > -1) {
								externalUrl = externalUrl.substring(0, indexOfLastSlash);	
							}
						}
						view.setUploadDestinationText(externalUrl);
					} else if (uploadDestinations.get(0) instanceof ExternalS3UploadDestination) {
						ExternalS3UploadDestination externalUploadDestination = (ExternalS3UploadDestination) uploadDestinations.get(0);
						String description = "s3://" + externalUploadDestination.getBucket() + "/";
						if (externalUploadDestination.getBaseKey() != null) {
							description += externalUploadDestination.getBaseKey();
						};
						view.setUploadDestinationText(description);
					} else if (uploadDestinations.get(0) instanceof ExternalObjectStoreUploadDestination) {
						ExternalObjectStoreUploadDestination destination = (ExternalObjectStoreUploadDestination) uploadDestinations.get(0);
						String description = destination.getEndpointUrl() + "/" + destination.getBucket();
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
	 
	 public void setAnnotationsTitleText(String text) {
		 view.setAnnotationsTitleText(text);
	 }
	
}