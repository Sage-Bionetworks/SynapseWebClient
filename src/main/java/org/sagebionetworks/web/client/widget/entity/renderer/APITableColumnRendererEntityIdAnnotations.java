package org.sagebionetworks.web.client.widget.entity.renderer;

import static org.sagebionetworks.repo.model.EntityBundle.ANNOTATIONS;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.AnnotationsWidget;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;
import org.sagebionetworks.web.client.widget.entity.row.EntityRow;
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
	private List<String> outputColumnNames;
	private Map<String, List<String>> outputColumnData;
	private List<String> sourceColumnData;
	
	@Inject
	public APITableColumnRendererEntityIdAnnotations(AdapterFactory factory, EntitySchemaCache cache, SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator) {
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.factory = factory;
		this.cache = cache;
	}
	
	@Override
	public void init(Map<String, List<String>> columnData,
			APITableColumnConfig config,
			AsyncCallback<APITableInitializedColumnRenderer> callback) {
		outputColumnNames = null;
		outputColumnData = null;
		Set<String> inputColumnNames = config.getInputColumnNames();
		Set<String> uniqueIds = new HashSet<String>(columnData.size());
		String idColumnName = inputColumnNames.iterator().next();
		sourceColumnData = columnData.get(idColumnName); 
		uniqueIds.addAll(sourceColumnData);
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
		AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				
				try {
					List<EntityRow<?>> entityRowList =  AnnotationsWidget.getRows(bundle.getEntity(), bundle.getAnnotations(), factory, cache);

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
					finalCallback.onSuccess(getOutputColumnRenderer());
				} else
					columnDataInit(columnData, currentIndex+1);
			}
		};
		synapseClient.getEntityBundle(columnData.get(currentIndex), ENTITY| ANNOTATIONS, callback);
	}
	
	private APITableInitializedColumnRenderer getOutputColumnRenderer() {
		return new APITableInitializedColumnRenderer() {
			@Override
			public Map<String, List<String>> getColumnData() {
				if (outputColumnData == null) {
					outputColumnData = new HashMap<String,List<String>>();
					//create
					for (String outputColumnName : getColumnNames()) {
						List<String> outputColumn = new ArrayList<String>();
						//go through every entry in sourceColumnData for this output column
						for (String originalValue : sourceColumnData) {
							String error = value2Error.get(originalValue);
							if (error != null) {
								outputColumn.add(error);
							}
							else {
								List<EntityRow<?>> row = value2Annotations.get(originalValue);
								//does this row have the same annotation
								String renderedValue = "";
								if (row != null) {
									for (EntityRow<?> entityRow : row) {
										if (entityRow.getLabel().equals(outputColumnName)) {
											//report this display value
											if (entityRow.getValue() != null)
												renderedValue = entityRow.getDislplayValue();
											break;
										}
									}
								}
								outputColumn.add(renderedValue);
							}
						}
						outputColumnData.put(outputColumnName, outputColumn);
					}
				}
				return outputColumnData;
			}
			
			@Override
			public List<String> getColumnNames() {
				if (outputColumnNames == null) {
					outputColumnNames =  new ArrayList<String>();
					if (masterAnnotationList != null) {
						for (EntityRow<?> entityRow : masterAnnotationList) {
							outputColumnNames.add(entityRow.getLabel());
						}
					}
				}
				return outputColumnNames;
			}
		};
	}
}
