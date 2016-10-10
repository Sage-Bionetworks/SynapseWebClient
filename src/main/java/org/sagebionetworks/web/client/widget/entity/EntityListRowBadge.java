package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.repo.model.EntityBundle.ACCESS_REQUIREMENTS;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY;
import static org.sagebionetworks.repo.model.EntityBundle.FILE_HANDLES;
import static org.sagebionetworks.repo.model.EntityBundle.UNMET_ACCESS_REQUIREMENTS;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityGroupRecord;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SelectableListItem;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.file.FileDownloadButton;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityListRowBadge implements EntityListRowBadgeView.Presenter, SynapseWidgetPresenter, SelectableListItem {
	
	public static final String N_A = "N/A";
	private EntityListRowBadgeView view;
	private UserBadge createdByUserBadge;
	private SynapseJSNIUtils synapseJSNIUtils;
	private SynapseClientAsync synapseClient;
	private FileDownloadButton fileDownloadButton;
	private String entityId;
	private Long version;
	private Callback selectionChangedCallback;
	private LazyLoadHelper lazyLoadHelper;
	
	@Inject
	public EntityListRowBadge(EntityListRowBadgeView view, 
			UserBadge userBadge,
			SynapseJSNIUtils synapseJSNIUtils,
			SynapseClientAsync synapseClient,
			FileDownloadButton fileDownloadButton,
			LazyLoadHelper lazyLoadHelper) {
		this.view = view;
		this.createdByUserBadge = userBadge;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.synapseClient = synapseClient;
		this.fileDownloadButton = fileDownloadButton;
		this.lazyLoadHelper = lazyLoadHelper;
		view.setCreatedByWidget(userBadge.asWidget());
		view.setPresenter(this);
		fileDownloadButton.setSize(ButtonSize.EXTRA_SMALL);
		fileDownloadButton.setEntityUpdatedHandler(new EntityUpdatedHandler() {
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				getEntityBundle();
			}
		});
		Callback loadDataCallback = new Callback() {
			@Override
			public void invoke() {
				getEntityBundle();
			}
		};
		
		lazyLoadHelper.configure(loadDataCallback, view);
	}
	
	public void setNote(String note) {
		view.setNote(note);
	}
	public void setDescriptionVisible(boolean visible) {
		view.setDescriptionVisible(visible);
	}
	public void setIsSelectable(boolean isSelectable) {
		view.setIsSelectable(isSelectable);
	}
	public boolean isSelected() {
		return view.isSelected();
	}
	public void setSelected(boolean isSelected) {
		view.setSelected(isSelected);
	}
	
	public void getEntityBundle() {
		int partsMask = ENTITY | FILE_HANDLES | ACCESS_REQUIREMENTS | UNMET_ACCESS_REQUIREMENTS;
		view.showLoading();
		AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onFailure(Throwable caught) {
				view.setEntityLink(entityId, DisplayUtils.getSynapseHistoryToken(entityId, version));
				view.showErrorIcon(caught.getMessage());
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
		
		lazyLoadHelper.setIsConfigured();
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
			createdByUserBadge.setTarget("_blank");
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
			view.setVersion(N_A);
		}
		view.showRow();
	}
	
	public EntityGroupRecord getRecord() {
		Reference ref = new Reference();
		ref.setTargetId(entityId);			
		ref.setTargetVersionNumber(version);

		EntityGroupRecord record = new EntityGroupRecord();
		record.setEntityReference(ref);
		record.setNote(view.getNote());
		return record;
	}
	
	public String getNote() {
		return view.getNote();
	}
	
	public String getEntityId() {
		return entityId;
	}
	
	public void setSelectionChangedCallback(Callback selectionChangedCallback) {
		this.selectionChangedCallback = selectionChangedCallback;
	}
	
	@Override
	public void onSelectionChanged() {
		if (selectionChangedCallback != null) {
			selectionChangedCallback.invoke();
		}
	}
}
