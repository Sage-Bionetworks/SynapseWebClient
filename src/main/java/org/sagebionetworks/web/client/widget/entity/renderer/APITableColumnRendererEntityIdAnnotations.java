package org.sagebionetworks.web.client.widget.entity.renderer;

import static org.sagebionetworks.web.shared.EntityBundleTransport.ANNOTATIONS;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ENTITY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.PropertyWidget;
import org.sagebionetworks.web.client.widget.entity.row.EntityRow;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * Given an Entity ID to render, this renderer will generate a column for each annotation
 * This is an expensive renderer, as it requires a call per ID.
 * @author jayhodgson
 *
 */
public class APITableColumnRendererEntityIdAnnotations implements APITableColumnRenderer {

	SynapseClientAsync synapseClient;
	NodeModelCreator nodeModelCreator;
	AsyncCallback<APITableInitializedColumnRenderer> finalCallback;
	Map<String, List<EntityRow<?>>> value2Annotations;
	Map<String, String> value2Error;
	List<String> entityIds;
	List<EntityRow<?>> masterAnnotationList;
	AdapterFactory factory;
	EntitySchemaCache cache;
	
	@Inject
	public APITableColumnRendererEntityIdAnnotations(AdapterFactory factory, EntitySchemaCache cache, SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator) {
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.factory = factory;
		this.cache = cache;
	}
	
	@Override
	public void init(List<String> columnData, AsyncCallback<APITableInitializedColumnRenderer> callback) {
		Set<String> uniqueIds = new HashSet<String>(columnData.size());
		uniqueIds.addAll(columnData);
		entityIds = new ArrayList<String>();
		entityIds.addAll(uniqueIds);
		value2Annotations = new HashMap<String, List<EntityRow<?>>>();
		value2Error = new HashMap<String, String>();
		masterAnnotationList = null;
		finalCallback = callback;
		if (entityIds.size() > 0)
			columnDataInit(entityIds, 0);
	}
	
	/**
	 * Recursive serially initializes the column renderers
	 * @param columnData
	 * @param columnNames
	 * @param displayColumnNames
	 * @param renderers
	 * @param currentIndex
	 */
	private void columnDataInit(final List<String> columnData, final int currentIndex) {
		AsyncCallback<EntityBundleTransport> callback = new AsyncCallback<EntityBundleTransport>() {
			@Override
			public void onSuccess(EntityBundleTransport result) {
				
				try {
					EntityBundle bundle = nodeModelCreator.createEntityBundle(result);
					List<EntityRow<?>> entityRowList =  PropertyWidget.getRows(bundle.getEntity(), bundle.getAnnotations(), factory, cache);

					if (masterAnnotationList == null && entityRowList.size() > 0)
						masterAnnotationList = entityRowList;
					value2Annotations.put(columnData.get(currentIndex), entityRowList);
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}

				processNext();
			}
			@Override
			public void onFailure(Throwable caught) {
				try{
					value2Error.put(columnData.get(currentIndex), caught.getMessage());
				} catch (Throwable t) {};
				//keep going
				processNext();
			}
			private void processNext() {
				//after all values have initialized, then do the final callback
				if (currentIndex == columnData.size()-1) {
					finalCallback.onSuccess(new APITableInitializedColumnRenderer() {
						@Override
						public int getColumnCount() {
							return masterAnnotationList.size();
						}
						@Override
						public String getRenderedColumnName(int rendererColIndex) {
							return masterAnnotationList.get(rendererColIndex).getLabel();
						}
						
						@Override
						public String render(String value, int rendererColIndex) {
							//was there an error processing this value?
							String error = value2Error.get(value);
							if (error != null) {
								return error;
							}
							
							int colIndex = rendererColIndex;
							String masterAnnotationLabel = masterAnnotationList.get(colIndex).getLabel();
							
							List<EntityRow<?>> row = value2Annotations.get(value);
							//does this row have the same annotation
							String renderedValue = "";
							if (row != null) {
								for (Iterator iterator = row.iterator(); iterator.hasNext();) {
									EntityRow<?> entityRow = (EntityRow<?>) iterator.next();
									if (entityRow.getLabel().equals(masterAnnotationLabel)) {
										//report this display value
										if (entityRow.getValue() != null)
											renderedValue = entityRow.getDislplayValue();
										break;
									}
								}
							}
							return renderedValue;
						}

					});
				} else
					columnDataInit(columnData, currentIndex+1);
			}
		};
		synapseClient.getEntityBundle(columnData.get(currentIndex), ENTITY| ANNOTATIONS, callback);
	}
}
