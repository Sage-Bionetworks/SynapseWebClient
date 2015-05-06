package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.repo.model.EntityBundle.ACCESS_REQUIREMENTS;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY;
import static org.sagebionetworks.repo.model.EntityBundle.PERMISSIONS;
import static org.sagebionetworks.repo.model.EntityBundle.UNMET_ACCESS_REQUIREMENTS;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.presenter.EntityPresenter;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SharingAndDataUseConditionWidget implements SharingAndDataUseConditionWidgetView.Presenter, SynapseWidgetPresenter {
	
	private SharingAndDataUseConditionWidgetView view;
	SynapseClientAsync synapseClient;
	Callback entityUpdatedCallback;
	boolean showChangeLink;
	
	@Inject
	public SharingAndDataUseConditionWidget(SharingAndDataUseConditionWidgetView view, 
			SynapseClientAsync synapseClient) {
		this.synapseClient = synapseClient;
		this.view = view;
		view.setPresenter(this);
	}
	
	public void configure(String entityId, boolean showChangeLink, Callback entityUpdatedCallback) {
		this.entityUpdatedCallback = entityUpdatedCallback;
		this.showChangeLink = showChangeLink;
		setEntity(entityId);
	}
	
	public void setEntity(String entityId) {
		//get entity bundle (only the parts required by the public/private widget and restrictions widget
		int mask = ENTITY | PERMISSIONS |  ACCESS_REQUIREMENTS | UNMET_ACCESS_REQUIREMENTS ;
		synapseClient.getEntityBundle(entityId, mask, new AsyncCallback<EntityBundle>() {
			
			@Override
			public void onSuccess(EntityBundle bundle) {
				EntityPresenter.filterToDownloadARs(bundle);
				setEntity(bundle);	
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}
	public void setEntity(EntityBundle bundle) {
		if(bundle == null)  throw new IllegalArgumentException("Entity bundle is required");
		view.configure(bundle, showChangeLink);
	}

	@Override
	public void entityUpdated() {
		if (entityUpdatedCallback != null)
			entityUpdatedCallback.invoke();
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

}
