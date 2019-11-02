package org.sagebionetworks.web.client.widget.entity;

import java.util.List;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.file.ExternalGoogleCloudUploadDestination;
import org.sagebionetworks.repo.model.file.ExternalObjectStoreUploadDestination;
import org.sagebionetworks.repo.model.file.ExternalS3UploadDestination;
import org.sagebionetworks.repo.model.file.ExternalUploadDestination;
import org.sagebionetworks.repo.model.file.S3UploadDestination;
import org.sagebionetworks.repo.model.file.UploadDestination;
import org.sagebionetworks.repo.model.file.UploadType;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.doi.DoiWidgetV2;
import org.sagebionetworks.web.client.widget.entity.EntityMetadataView.Presenter;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationsRendererWidget;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.restriction.v2.RestrictionWidget;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityMetadata implements Presenter {

	private EntityMetadataView view;
	private AnnotationsRendererWidget annotationsWidget;
	private DoiWidgetV2 doiWidgetV2;
	private VersionHistoryWidget versionHistoryWidget;
	private SynapseJavascriptClient jsClient;
	private SynapseJSNIUtils jsni;
	private PortalGinInjector ginInjector;
	private ContainerItemCountWidget containerItemCountWidget;
	private org.sagebionetworks.web.client.widget.entity.restriction.v2.RestrictionWidget restrictionWidgetV2;

	boolean isShowingAnnotations;

	@Inject
	public EntityMetadata(EntityMetadataView view, DoiWidgetV2 doiWidgetV2, AnnotationsRendererWidget annotationsWidget, SynapseJavascriptClient jsClient, SynapseJSNIUtils jsni, RestrictionWidget restrictionWidgetV2, ContainerItemCountWidget containerItemCountWidget, PortalGinInjector ginInjector) {
		this.view = view;
		this.doiWidgetV2 = doiWidgetV2;
		this.annotationsWidget = annotationsWidget;
		this.jsClient = jsClient;
		this.jsni = jsni;
		this.restrictionWidgetV2 = restrictionWidgetV2;
		this.containerItemCountWidget = containerItemCountWidget;
		this.ginInjector = ginInjector;
		this.view.setDoiWidget(doiWidgetV2);
		this.view.setAnnotationsRendererWidget(annotationsWidget);
		this.view.setRestrictionWidgetV2(restrictionWidgetV2);
		this.view.setContainerItemCountWidget(containerItemCountWidget);
		restrictionWidgetV2.setShowChangeLink(true);
		restrictionWidgetV2.setShowIfProject(false);
		restrictionWidgetV2.setShowFlagLink(true);
		view.setRestrictionWidgetV2Visible(true);
	}

	public Widget asWidget() {
		return view.asWidget();
	}

	public VersionHistoryWidget getVersionHistoryWidget() {
		if (versionHistoryWidget == null) {
			versionHistoryWidget = ginInjector.getVersionHistoryWidget();
			view.setVersionHistoryWidget(versionHistoryWidget);
		}
		return versionHistoryWidget;
	}

	public void configure(EntityBundle bundle, Long versionNumber, ActionMenuWidget actionMenu) {
		clear();
		Entity en = bundle.getEntity();
		view.setEntityId(en.getId());
		boolean canEdit = bundle.getPermissions().getCanCertifiedUserEdit();
		isShowingAnnotations = false;
		setAnnotationsVisible(isShowingAnnotations);
		actionMenu.setActionListener(Action.SHOW_ANNOTATIONS, action -> {
			isShowingAnnotations = !isShowingAnnotations;
			setAnnotationsVisible(isShowingAnnotations);
		});

		actionMenu.setActionListener(Action.SHOW_VERSION_HISTORY, action -> {
			getVersionHistoryWidget().setVisible(!getVersionHistoryWidget().isVisible());
		});

		boolean isCurrentVersion = versionNumber == null;
		if (EntityActionControllerImpl.isVersionSupported(bundle.getEntity(), ginInjector.getCookieProvider())) {
			getVersionHistoryWidget().setVisible(false);
			getVersionHistoryWidget().setEntityBundle(bundle, versionNumber);
			view.setRestrictionPanelVisible(true);
		} else {
			if (versionHistoryWidget != null) {
				versionHistoryWidget.setVisible(false);
			}
			view.setRestrictionPanelVisible(en instanceof TableEntity || en instanceof Folder || en instanceof DockerRepository);
		}
		if (bundle.getEntity() instanceof Folder) {
			containerItemCountWidget.configure(bundle.getEntity().getId());
		}
		configureStorageLocation(en);
		doiWidgetV2.configure(bundle.getDoiAssociation());
		annotationsWidget.configure(bundle, canEdit, isCurrentVersion);
		restrictionWidgetV2.configure(en, bundle.getPermissions().getCanChangePermissions());
	}

	public void setVisible(boolean visible) {
		view.setDetailedMetadataVisible(visible);
	}

	public void setAnnotationsVisible(boolean visible) {
		view.setAnnotationsVisible(visible);
	}

	public void clear() {
		doiWidgetV2.clear();
		containerItemCountWidget.clear();
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
					} else if (uploadDestinations.get(0) instanceof ExternalUploadDestination) {
						ExternalUploadDestination externalUploadDestination = (ExternalUploadDestination) uploadDestinations.get(0);
						String externalUrl = externalUploadDestination.getUrl();
						UploadType type = externalUploadDestination.getUploadType();
						if (type == UploadType.SFTP) {
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
						} ;
						view.setUploadDestinationText(description);
					} else if (uploadDestinations.get(0) instanceof ExternalGoogleCloudUploadDestination) {
						ExternalGoogleCloudUploadDestination externalUploadDestination = (ExternalGoogleCloudUploadDestination) uploadDestinations.get(0);
						String description = "gs://" + externalUploadDestination.getBucket() + "/";
						if (externalUploadDestination.getBaseKey() != null) {
							description += externalUploadDestination.getBaseKey();
						} ;
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
