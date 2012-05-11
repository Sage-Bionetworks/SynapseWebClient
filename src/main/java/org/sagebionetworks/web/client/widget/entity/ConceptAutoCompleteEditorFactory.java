package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.ontology.AdapterModelData;

import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.JsonPagingLoadResultReader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.Loader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.ScriptTagProxy;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

/**
 * Creates an auto-complete editor for the concept services.
 * @author jmhill
 *
 */
public class ConceptAutoCompleteEditorFactory {
	
	public static final String KEY_QUERY = "query";
	public static final String KEY_PREFIX = "prefix";
	public static final String KEY_OFFSET = "offset";
	public static final String KEY_START = "start";
	public static final String KEY_DEFINITION = "definition";
	// This is the value to get and set.
	public static final String KEY_PREFERRED_LABEL = "preferredLabel";
	public static final String KEY_TOTAL_NUMBER_OF_RESULTS = "totalNumberOfResults";
	public static final String KEY_CHILDREN = "children";

	/**
	 * Create a new editor for a given concept URL.
	 * 
	 * @param url
	 * @return
	 */
	public static ComboBox<AdapterModelData> createConceptAutoCompleteEditor(String url) {
//		String url = "http://localhost:8080/services-repository-0.11-SNAPSHOT/repo/v1/concept/11291/childrenTransitive/";
		ScriptTagProxy<PagingLoadResult<AdapterModelData>> proxy = new ScriptTagProxy<PagingLoadResult<AdapterModelData>>(url);
		
		// Maps the model to our Concepts JSON format.
		ModelType type = new ModelType();
		type.setRoot(KEY_CHILDREN);
		type.setTotalName(KEY_TOTAL_NUMBER_OF_RESULTS);
		type.addField(KEY_PREFERRED_LABEL, KEY_PREFERRED_LABEL);
		type.addField(KEY_DEFINITION, KEY_DEFINITION);

		// The paginated reader
		JsonPagingLoadResultReader<PagingLoadResult<AdapterModelData>> reader = new JsonPagingLoadResultReader<PagingLoadResult<AdapterModelData>>(
				type);
		
		// the paging loader.
		PagingLoader<PagingLoadResult<AdapterModelData>> loader = new BasePagingLoader<PagingLoadResult<AdapterModelData>>(
				proxy, reader);

		// Map the offset and query to the prefix
		loader.addListener(Loader.BeforeLoad, new Listener<LoadEvent>() {
			public void handleEvent(LoadEvent be) {
				be.<ModelData> getConfig().set(KEY_START,	be.<ModelData> getConfig().get(KEY_OFFSET));
				be.<ModelData> getConfig().set(KEY_PREFIX,	be.<ModelData> getConfig().get(KEY_QUERY));
			}
		});
		
		ListStore<AdapterModelData> store = new ListStore<AdapterModelData>(loader);

		ComboBox<AdapterModelData> combo = new ComboBox<AdapterModelData>();
		combo.setWidth(580);
		combo.setDisplayField(KEY_PREFERRED_LABEL);
		combo.setItemSelector("div.search-item");
		combo.setTemplate(getTemplate());
		combo.setStore(store);
		combo.setHideTrigger(false);
		combo.setPageSize(10);
		combo.setAllowBlank(true);
		combo.setMinChars(0);
		return combo;
	}

	private static native String getTemplate() /*-{
		return [ '<tpl for="."><div class="search-item">',
				'<table cellspacing="0" cellpadding="0" width="100%"><tr valign="top">',
				'<td><span class="suggestSearchTerm">{preferredLabel}</span></td>',
				'<td align="right"><span class="suggestSearchOntology">{definition}</span></td>',
				'</tr></table>',
				'</div></tpl>' ].join("");
	}-*/;

}
