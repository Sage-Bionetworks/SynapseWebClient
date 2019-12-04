package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SharingAndDataUseConditionWidget implements SynapseWidgetPresenter {

	private SharingAndDataUseConditionWidgetView view;
	SynapseJavascriptClient jsClient;

	@Inject
	public SharingAndDataUseConditionWidget(SharingAndDataUseConditionWidgetView view, SynapseJavascriptClient jsClient) {
		this.jsClient = jsClient;
		this.view = view;
	}

	public void configure(String entityId) {
		setEntity(entityId);
	}

	public void setEntity(String entityId) {
		// get entity bundle (only the parts required by the public/private widget and restrictions widget
		EntityBundleRequest bundleRequest = new EntityBundleRequest();
		bundleRequest.setIncludeEntity(true);
		bundleRequest.setIncludePermissions(true);
		jsClient.getEntityBundle(entityId, bundleRequest, new AsyncCallback<EntityBundle>() {

			@Override
			public void onSuccess(EntityBundle bundle) {
				setEntity(bundle);
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}

	public void setEntity(EntityBundle bundle) {
		if (bundle == null)
			throw new IllegalArgumentException("Entity bundle is required");
		view.configure(bundle);
	}

	@SuppressWarnings("unchecked")
	public void clearState() {}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

}
