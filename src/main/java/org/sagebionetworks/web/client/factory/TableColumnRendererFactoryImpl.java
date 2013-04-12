package org.sagebionetworks.web.client.factory;

import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererDate;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererEntityIdAnnotations;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererNone;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererSynapseID;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererUserId;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Factory for table column renderers.
 * 
 * @author John
 *
 */
public class TableColumnRendererFactoryImpl implements TableColumnRendererFactory {
	
	@Inject
	private Provider<APITableColumnRendererNone> apiTableColumnRendererNonetProvider;
	@Inject
	private Provider<APITableColumnRendererUserId> apiTableColumnRendererUserIdProvider;
	@Inject
	private Provider<APITableColumnRendererDate> apiTableColumnRendererDateProvider;
	@Inject
	private Provider<APITableColumnRendererSynapseID> apiTableColumnRendererSynapseIDProvider;
	@Inject
	private Provider<APITableColumnRendererEntityIdAnnotations> apiTableColumnRendererEntityIdAnnotationsProvider;
	
	@Override
	public APITableColumnRendererNone getAPITableColumnRendererNone() {
		return apiTableColumnRendererNonetProvider.get();
	}

	@Override
	public APITableColumnRendererUserId getAPITableColumnRendererUserId() {
		return apiTableColumnRendererUserIdProvider.get();
	}

	@Override
	public APITableColumnRendererDate getAPITableColumnRendererDate() {
		return apiTableColumnRendererDateProvider.get();
	}

	@Override
	public APITableColumnRendererSynapseID getAPITableColumnRendererSynapseID() {
		return apiTableColumnRendererSynapseIDProvider.get();
	}

	@Override
	public APITableColumnRendererEntityIdAnnotations getAPITableColumnRendererEntityAnnotations() {
		return apiTableColumnRendererEntityIdAnnotationsProvider.get();
	}

}
