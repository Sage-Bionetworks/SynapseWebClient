package org.sagebionetworks.web.client.widget.sharing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	public static ComboBox<ModelData> createUserGroupSearchSuggestBox(String repositoryUrl, final Long publicPrincipleId, final Long authenticatedPrincipleId) {
		String url = repositoryUrl + USER_GROUP_HEADER_URL;
		ScriptTagProxy<PagingLoadResult<ModelData>> proxy = 
				new ScriptTagProxy<PagingLoadResult<ModelData>>(url);
		
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
		JsonPagingLoadResultReader<PagingLoadResult<ModelData>> reader = 
				new JsonPagingLoadResultReader<PagingLoadResult<ModelData>>(type);
		
		// the paging loader.
		PagingLoader<PagingLoadResult<ModelData>> loader = 
				new BasePagingLoader<PagingLoadResult<ModelData>>(proxy, reader);

		// Map the offset and query to the prefix
		loader.addListener(Loader.BeforeLoad, new Listener<LoadEvent>() {
			@Override
			public void handleEvent(LoadEvent be) {
				be.<ModelData> getConfig().set(KEY_START,	be.<ModelData> getConfig().get(KEY_OFFSET));
				be.<ModelData> getConfig().set(KEY_LIMIT,	be.<ModelData> getConfig().get(KEY_LIMIT));
				be.<ModelData> getConfig().set(KEY_PREFIX,	be.<ModelData> getConfig().get(KEY_QUERY));
			}
		});
		if (authenticatedPrincipleId != null || publicPrincipleId != null) {
			//don't show the authenticated users group
			loader.addListener(Loader.Load, new Listener<LoadEvent>() {
				@Override
				public void handleEvent(LoadEvent be) {
					PagingLoadResult<ModelData> pagedResults = be.getData();
					if (pagedResults != null) {
						List<ModelData> modelDataList = pagedResults.getData();
						if (modelDataList != null)  {
							String authenticatedPrincipleIdString = authenticatedPrincipleId != null ? authenticatedPrincipleId.toString() : "";
							String publicPrincipleIdString = publicPrincipleId != null ? publicPrincipleId.toString() : "";
							List<ModelData> removeItems = new ArrayList<ModelData>();
							for (Iterator iterator = modelDataList.iterator(); iterator
									.hasNext();) {
								ModelData modelData = (ModelData) iterator.next();
								String testPrincipleId = modelData.get(KEY_PRINCIPAL_ID);
								if (authenticatedPrincipleIdString.equals(testPrincipleId)) {
									removeItems.add(modelData);
								} else if (publicPrincipleIdString.equals(testPrincipleId)) {
									removeItems.add(modelData);
								}
							}
							modelDataList.removeAll(removeItems);
						}
					}
				}
			});
		}
		
		ListStore<ModelData> store = new ListStore<ModelData>(loader);

		ComboBox<ModelData> combo = new ComboBox<ModelData>();
		
		combo.setPropertyEditor(new ListModelPropertyEditor<ModelData>() {
			@Override
			public String getStringValue(ModelData value) {
				// Example output:
				// dev usr  |  dev....1@sagebase.org  |  syn114085
				
				StringBuilder sb = new StringBuilder();
				sb.append(value.get(KEY_DISPLAY_NAME).toString());
				String email = value.get(KEY_EMAIL);
				if (email != null)
					sb.append("  |  " + email);
				sb.append("  |  " + value.get(KEY_PRINCIPAL_ID));				
				return sb.toString();
			}
			
			@Override
			public ModelData convertStringValue(String entry) {
				// Extract Principal ID from entry String
				String[] split = entry.split("  ");
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
		combo.setDisplayField(KEY_PRINCIPAL_ID);
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
				'<div class="search-item" qtitle="{displayName}" qtip="{email}">',
				'{displayName}</div></tpl>' ].join("");
	}-*/;

}