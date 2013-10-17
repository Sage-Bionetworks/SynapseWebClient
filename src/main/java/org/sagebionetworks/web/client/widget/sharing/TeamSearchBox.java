package org.sagebionetworks.web.client.widget.sharing;

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
import com.extjs.gxt.ui.client.widget.form.ListModelPropertyEditor;

public class TeamSearchBox {
	
	public static final String TEAM_URL = "/team";

	public static final String KEY_QUERY = "query";
	public static final String KEY_PREFIX = "fragment";
	public static final String KEY_OFFSET = "offset";
	public static final String KEY_START = "start";
	public static final String KEY_LIMIT = "limit";

	// This is the value to get and set.
	public static final String KEY_DISPLAY_NAME = "name";
	public static final String KEY_TOTAL_NUMBER_OF_RESULTS = "totalNumberOfResults";
	public static final String KEY_CHILDREN = "children";
	public static final String KEY_TEAM_PICTURE = "icon";
	public static final String KEY_TEAM_ID = "id";
	
	/**
	 * Create a new editor for a given concept URL.
	 * 
	 * @param url
	 * @return
	 */
	public static ComboBox<ModelData> createTeamSearchSuggestBox(String repositoryUrl) {
		String url = repositoryUrl + TEAM_URL;
		ScriptTagProxy<PagingLoadResult<ModelData>> proxy = 
				new ScriptTagProxy<PagingLoadResult<ModelData>>(url);
		
		// Maps the model to our Team JSON format.
		ModelType type = new ModelType();
		type.setRoot(KEY_CHILDREN);
		type.setTotalName(KEY_TOTAL_NUMBER_OF_RESULTS);
		type.addField(KEY_DISPLAY_NAME, KEY_DISPLAY_NAME);
		type.addField(KEY_TEAM_PICTURE, KEY_TEAM_PICTURE);
		type.addField(KEY_TEAM_ID, KEY_TEAM_ID);
		
		// The paginated reader
		JsonPagingLoadResultReader<PagingLoadResult<ModelData>> reader = 
				new JsonPagingLoadResultReader<PagingLoadResult<ModelData>>(type);
		
		// the paging loader.
		PagingLoader<PagingLoadResult<ModelData>> loader = 
				new BasePagingLoader<PagingLoadResult<ModelData>>(proxy, reader);

		// Map the offset and query to the fragment
		loader.addListener(Loader.BeforeLoad, new Listener<LoadEvent>() {
			@Override
			public void handleEvent(LoadEvent be) {
				be.<ModelData> getConfig().set(KEY_START,	be.<ModelData> getConfig().get(KEY_OFFSET));
				be.<ModelData> getConfig().set(KEY_LIMIT,	be.<ModelData> getConfig().get(KEY_LIMIT));
				be.<ModelData> getConfig().set(KEY_PREFIX,	be.<ModelData> getConfig().get(KEY_QUERY));
			}
		});
		
		ListStore<ModelData> store = new ListStore<ModelData>(loader);

		ComboBox<ModelData> combo = new ComboBox<ModelData>();
		
		combo.setPropertyEditor(new ListModelPropertyEditor<ModelData>() {
			@Override
			public String getStringValue(ModelData value) {
				// Example output:
				// dev usr  |  3
				
				StringBuilder sb = new StringBuilder();
				sb.append(value.get(KEY_DISPLAY_NAME).toString());
				sb.append("  |  " + value.get(KEY_TEAM_ID));				
				return sb.toString();
			}
			
			@Override
			public ModelData convertStringValue(String entry) {
				// Extract Principal ID from entry String
				String[] split = entry.split("\\|  ");
				String id = split[split.length - 1].trim();
				
				// Search for matching ModelData
				for (ModelData md : models) {
					String key = md.get(displayProperty);
					if (id.equals(key)) {
						return md;
					}
			    }
			    return null;
			}
		});
		combo.setDisplayField(KEY_TEAM_ID);
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
		return [ '<tpl for=".">',
				'<div class="search-item" qtitle="{name}" qtip="{name}">',
				'{name}</div></tpl>' ].join("");
	}-*/;

}