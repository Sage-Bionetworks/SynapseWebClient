package org.sagebionetworks.web.client.widget.sharing;

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

public class UserGroupSearchBox {
	
	public static final String USER_GROUP_HEADER_URL = "/userGroupHeaders";

	public static final String KEY_QUERY = "query";
	public static final String KEY_PREFIX = "prefix";
	public static final String KEY_OFFSET = "offset";
	public static final String KEY_START = "start";
	public static final String KEY_LIMIT = "limit";

	// This is the value to get and set.
	public static final String KEY_DISPLAY_NAME = "displayName";
	public static final String KEY_TOTAL_NUMBER_OF_RESULTS = "totalNumberOfResults";
	public static final String KEY_CHILDREN = "children";
	public static final String KEY_PROFILE_PICTURE = "pic";
	public static final String KEY_IS_INDIVIDUAL = "isIndividual";
	public static final String KEY_PRINCIPAL_ID = "ownerId";
	public static final String KEY_EMAIL = "email";

	/**
	 * Create a new editor for a given concept URL.
	 * 
	 * @param url
	 * @return
	 */
	public static ComboBox<AdapterModelData> createUserGroupSearchSuggestBox(String repositoryUrl) {
		String url = repositoryUrl + USER_GROUP_HEADER_URL;
		ScriptTagProxy<PagingLoadResult<AdapterModelData>> proxy = 
				new ScriptTagProxy<PagingLoadResult<AdapterModelData>>(url);
		
		// Maps the model to our UserGroupHeader JSON format.
		ModelType type = new ModelType();
		type.setRoot(KEY_CHILDREN);
		type.setTotalName(KEY_TOTAL_NUMBER_OF_RESULTS);
		type.addField(KEY_DISPLAY_NAME, KEY_DISPLAY_NAME);
		type.addField(KEY_PROFILE_PICTURE, KEY_PROFILE_PICTURE);
		type.addField(KEY_IS_INDIVIDUAL, KEY_IS_INDIVIDUAL);
		type.addField(KEY_PRINCIPAL_ID, KEY_PRINCIPAL_ID);
		type.addField(KEY_EMAIL, KEY_EMAIL);

		// The paginated reader
		JsonPagingLoadResultReader<PagingLoadResult<AdapterModelData>> reader = 
				new JsonPagingLoadResultReader<PagingLoadResult<AdapterModelData>>(type);
		
		// the paging loader.
		PagingLoader<PagingLoadResult<AdapterModelData>> loader = 
				new BasePagingLoader<PagingLoadResult<AdapterModelData>>(proxy, reader);

		// Map the offset and query to the prefix
		loader.addListener(Loader.BeforeLoad, new Listener<LoadEvent>() {
			public void handleEvent(LoadEvent be) {
				be.<ModelData> getConfig().set(KEY_START,	be.<ModelData> getConfig().get(KEY_OFFSET));
				be.<ModelData> getConfig().set(KEY_LIMIT,	be.<ModelData> getConfig().get(KEY_LIMIT));
				be.<ModelData> getConfig().set(KEY_PREFIX,	be.<ModelData> getConfig().get(KEY_QUERY));
			}
		});
		
		ListStore<AdapterModelData> store = new ListStore<AdapterModelData>(loader);

		ComboBox<AdapterModelData> combo = new ComboBox<AdapterModelData>();
		combo.setDisplayField(KEY_DISPLAY_NAME);
		combo.setItemSelector("div.search-item");
		combo.setTemplate(getTemplate());
		combo.setStore(store);
		combo.setHideTrigger(false);
		combo.setAllowBlank(false);
		combo.setMinChars(3);
		combo.setPageSize(10);
		return combo;
	}

	private static native String getTemplate() /*-{
		return [ '<tpl for="."><div class="search-item">',
				'<table cellspacing="0" cellpadding="0" width="100%"><tr valign="top">',
				'<td>{displayName}</span></td>',
				'<td align="right"><span class="suggestSearchOntology">{email}</a></span></td>',
				'</tr></table>',
				'</div></tpl>' ].join("");
	}-*/;

}