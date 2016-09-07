package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.repo.model.EntityBundle.ENTITY;
import static org.sagebionetworks.repo.model.EntityBundle.FILE_HANDLES;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.file.FileDownloadButton;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityListRowBadge implements EntityListRowBadgeView.Presenter, SynapseWidgetPresenter {
	
	private EntityListRowBadgeView view;
	private UserBadge createdByUserBadge;
	private SynapseJSNIUtils synapseJSNIUtils;
	private SynapseClientAsync synapseClient;
	private GWTWrapper gwt;
	private Callback invokeCheckForInViewAndLoadData;
	private boolean isConfigured;
	private boolean isAttached;
	private FileDownloadButton fileDownloadButton;
	private String entityId;
	private Long version;
	
	@Inject
	public EntityListRowBadge(EntityListRowBadgeView view, 
			UserBadge userBadge,
			SynapseJSNIUtils synapseJSNIUtils,
			SynapseClientAsync synapseClient,
			GWTWrapper gwt,
			FileDownloadButton fileDownloadButton) {
		this.view = view;
		this.createdByUserBadge = userBadge;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.synapseClient = synapseClient;
		this.gwt = gwt;
		this.fileDownloadButton = fileDownloadButton;
		view.setCreatedByWidget(userBadge.asWidget());
		view.setPresenter(this);
		invokeCheckForInViewAndLoadData = new Callback() {
			@Override
			public void invoke() {
				checkForInViewAndLoadData();
			}
		};
		isConfigured = false;
		isAttached = false;
		fileDownloadButton.setSize(ButtonSize.EXTRA_SMALL);
		fileDownloadButton.setEntityUpdatedHandler(new EntityUpdatedHandler() {
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				getEntityBundle();
			}
		});
	}
	public void startCheckingIfAttachedAndConfigured() {
		if (isAttached && isConfigured) {
			checkForInViewAndLoadData();
		}
	}
	public void checkForInViewAndLoadData() {
		if (!view.isAttached()) {
			//Done, view has been detached and widget was never in the viewport
			return;
		} else if (view.isInViewport()) {
			//try to load data!
			getEntityBundle();
		} else {
			//wait for a few seconds and see if we should load data
			gwt.scheduleExecution(invokeCheckForInViewAndLoadData, DisplayConstants.DELAY_UNTIL_IN_VIEW);
		}
	}
	@Override
	public void viewAttached() {
		isAttached = true;
		startCheckingIfAttachedAndConfigured();
	}
	public void setNote(String note) {
		view.setNote(note);
	}
	public void setDescriptionVisible(boolean visible) {
		view.setDescriptionVisible(visible);
	}
	
	public void getEntityBundle() {
		int partsMask = ENTITY | FILE_HANDLES;
		
		AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorIcon();
				view.setError(caught.getMessage());
			}
			public void onSuccess(EntityBundle eb) {
				setEntityBundle(eb);
			};
		};
		if (version == null) {
			synapseClient.getEntityBundle(entityId, partsMask, callback);	
		} else {
			synapseClient.getEntityBundleForVersion(entityId, version, partsMask, callback);
		}
		
	}
	
	
	public void configure(Reference reference) {
		this.entityId = reference.getTargetId();
		this.version = reference.getTargetVersionNumber();
		
		isConfigured = true;
		startCheckingIfAttachedAndConfigured();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void setEntityBundle(EntityBundle eb) {
		view.setIcon(EntityTypeUtils.getIconTypeForEntity(eb.getEntity()));
		view.setEntityLink(eb.getEntity().getName(), DisplayUtils.getSynapseHistoryToken(entityId, version));
		if (eb.getEntity().getCreatedBy() != null) {
			createdByUserBadge.configure(eb.getEntity().getCreatedBy());
		}
		
		if (eb.getEntity().getCreatedOn() != null) {
			String dateString = synapseJSNIUtils.convertDateToSmallString(eb.getEntity().getCreatedOn());
			view.setCreatedOn(dateString);
		} else {
			view.setCreatedOn("");
		}
		view.setDescription(eb.getEntity().getDescription());
		
		if (eb.getEntity() instanceof FileEntity) {
			fileDownloadButton.configure(eb);
			fileDownloadButton.setClientsHelpVisible(false);
			view.setFileDownloadButton(fileDownloadButton.asWidget());
		}
		
		if (eb.getEntity() instanceof Versionable) {
			Versionable versionable = (Versionable) eb.getEntity();
			view.setVersion(versionable.getVersionNumber().toString());
		} else {
			view.setVersion("N/A");
		}
	}
	
	public String getEntityId() {
		return entityId;
	}
}
