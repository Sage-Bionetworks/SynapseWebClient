package org.sagebionetworks.web.client.factory;

import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererDate;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererEntityIdAnnotations;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererEpochDate;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererNone;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererSynapseID;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererUserId;

/**
 * Factory for table columns.
 * 
 * @author John
 *
 */
public interface TableColumnRendererFactory {

	//////API Table Column Renderers
	public APITableColumnRendererNone getAPITableColumnRendererNone();
	public APITableColumnRendererUserId getAPITableColumnRendererUserId();
	public APITableColumnRendererDate getAPITableColumnRendererDate();
	public APITableColumnRendererEpochDate getAPITableColumnRendererEpochDate();
	public APITableColumnRendererSynapseID getAPITableColumnRendererSynapseID();
	public APITableColumnRendererEntityIdAnnotations getAPITableColumnRendererEntityAnnotations();
}
