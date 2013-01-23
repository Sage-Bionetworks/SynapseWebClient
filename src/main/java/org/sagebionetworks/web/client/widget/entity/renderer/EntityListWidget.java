package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;

import org.sagebionetworks.repo.model.EntityGroupRecord;
import org.sagebionetworks.repo.model.widget.EntityListWidgetDescriptor;
import org.sagebionetworks.repo.model.widget.WidgetDescriptor;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.EntityGroupRecordDisplay;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListUtil.RowLoadedHandler;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityListWidget implements EntityListWidgetView.Presenter, WidgetRendererPresenter {
	
	private EntityListWidgetView view;
	private EntityListWidgetDescriptor descriptor;
	private AuthenticationController authenticationController;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;

	
	@Inject
	public EntityListWidget(EntityListWidgetView view,
			AuthenticationController authenticationController,
			SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		view.setPresenter(this);
	}
	
	@Override
	public void configure(String entityId, WidgetDescriptor widgetDescriptor) {
		if (!(widgetDescriptor instanceof EntityListWidgetDescriptor))
			throw new IllegalArgumentException(DisplayConstants.INVALID_WIDGET_DESCRIPTOR_TYPE);
		//set up view based on descriptor parameters
		descriptor = (EntityListWidgetDescriptor)widgetDescriptor;
		final boolean isLoggedIn = authenticationController.isLoggedIn();
		
		view.configure();

		List<EntityGroupRecord> records = descriptor.getRecords();
		if(records != null) {
			for(int i=0; i<records.size(); i++) {
				final int rowIndex = i;
				EntityListUtil.loadIndividualRowDetails(synapseClient, nodeModelCreator, isLoggedIn, descriptor, rowIndex, new RowLoadedHandler() {					
					@Override
					public void onLoaded(EntityGroupRecordDisplay entityGroupRecordDisplay) {
						view.setEntityGroupRecordDisplay(rowIndex, entityGroupRecordDisplay, isLoggedIn);
					}
				});
			}			
		}		
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	/*
	 * Private Methods
	 */

}
