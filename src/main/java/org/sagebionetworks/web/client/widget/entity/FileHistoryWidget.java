package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import java.util.Objects;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.VersionableEntity;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * This widget shows the properties and annotations as a non-editable table grid.
 *
 * @author jayhodgson
 */
public class FileHistoryWidget implements FileHistoryWidgetView.Presenter, IsWidget {
	
	private FileHistoryWidgetView view;
	private EntityBundle bundle;
	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalApplicationState;
	public static final Integer VERSION_LIMIT = 100;
	public PreflightController preflightController;
	private SynapseAlert synAlert;
	private boolean canEdit;
	private Long versionNumber;
	int currentOffset;
	
	@Inject
	public FileHistoryWidget(FileHistoryWidgetView view,
			 SynapseClientAsync synapseClient, 
			 GlobalApplicationState globalApplicationState, 
			 PreflightController preflightController,
			 SynapseAlert synAlert) {
		super();
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.preflightController = preflightController;
		this.view.setPresenter(this);
		this.synAlert = synAlert;
		view.setSynAlert(synAlert);
	}
	
	public void setEntityBundle(EntityBundle bundle, Long versionNumber) {
		this.bundle = bundle;
		this.versionNumber = versionNumber;
		this.canEdit = bundle.getPermissions().getCanCertifiedUserEdit();
		refreshFileHistory();
	}

	@Override
	public void updateVersionInfo(String newLabel, String newComment) {
		editCurrentVersionInfo(bundle.getEntity(), newLabel, newComment);
	}

	private void editCurrentVersionInfo(Entity entity, String version, String comment) {
		if (entity instanceof VersionableEntity) {
			final VersionableEntity vb = (VersionableEntity)entity;
			if (Objects.equals(version, vb.getVersionLabel()) &&
				Objects.equals(comment, vb.getVersionComment())) {
				// no-op
				view.hideEditVersionInfo();
				return;
			}
			String versionLabel = null;
			if (version!=null)
				versionLabel = version.toString();
			vb.setVersionLabel(versionLabel);
			vb.setVersionComment(comment);
			synAlert.clear();
			synapseClient.updateEntity(vb,
					new AsyncCallback<Entity>() {
						@Override
						public void onFailure(Throwable caught) {
							synAlert.handleException(caught);
						}
						
						@Override
						public void onSuccess(Entity result) {
							view.hideEditVersionInfo();
							view.showInfo(DisplayConstants.VERSION_INFO_UPDATED + ": " + vb.getName());
							refreshFileHistory();
						}
					});
		}
	}
	
	@Override
	public void deleteVersion(final Long versionNumber) {
		synAlert.clear();
		synapseClient.deleteEntityVersionById(bundle.getEntity().getId(), versionNumber, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
			@Override
			public void onSuccess(Void result) {
				view.showInfo("Version "+ versionNumber + " of " + bundle.getEntity().getId() + " " + DisplayConstants.LABEL_DELETED);
				//SWC-4002: if deleting the version that we're looking at, go to the latest version
				if (versionNumber.equals(FileHistoryWidget.this.versionNumber)) {
					gotoCurrentVersion();
				} else {
					refreshFileHistory();
				}
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		// The view is the real widget.
		return view.asWidget();
	}
	
	public void refreshFileHistory() {
		synAlert.clear();
		view.clearVersions();
		currentOffset = 0;
		onMore();
	}
	
	public void gotoCurrentVersion() {
		Long targetVersion = null;
		Synapse synapse = new Synapse(bundle.getEntity().getId(), targetVersion, EntityArea.FILES, null);
		globalApplicationState.getPlaceChanger().goTo(synapse);
	}
	
	public void onMore() {
		synapseClient.getEntityVersions(bundle.getEntity().getId(), currentOffset, VERSION_LIMIT,
			new AsyncCallback<PaginatedResults<VersionInfo>>() {
				@Override
				public void onSuccess(PaginatedResults<VersionInfo> result) {
					PaginatedResults<VersionInfo> paginatedResults;
					paginatedResults = result;
					view.setMoreButtonVisible(paginatedResults.getResults().size() == VERSION_LIMIT);
					if (currentOffset == 0) {
						//we know the current version based on this
						Long currentVersion = paginatedResults.getResults().get(0).getVersionNumber();
						boolean isCurrentVersion = versionNumber == null || currentVersion.equals(versionNumber);
						view.setEntityBundle(bundle.getEntity(), !isCurrentVersion);
						view.setEditVersionInfoButtonVisible(isCurrentVersion && canEdit);
					}
					if (versionNumber == null && currentOffset == 0 && paginatedResults.getResults().size() > 0) {
						versionNumber = paginatedResults.getResults().get(0).getVersionNumber();
					}
					for (VersionInfo versionInfo : paginatedResults.getResults()) {
						view.addVersion(versionInfo, canEdit, versionInfo.getVersionNumber().equals(versionNumber));
					}
					currentOffset += VERSION_LIMIT;
				}

				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(caught.getMessage());
				}
			});
	}

	
	/**
	 * For testing purposes only
	 * @return
	 */
	public Long getVersionNumber() {
		return versionNumber;
	}
	
	@Override
	public void onEditVersionInfoClicked() {
		preflightController.checkUploadToEntity(bundle, new Callback() {
			@Override
			public void invoke() {
				final VersionableEntity vb = (VersionableEntity)bundle.getEntity();
				view.showEditVersionInfo(vb.getVersionLabel(), vb.getVersionComment());
			}
		});
	}
}
