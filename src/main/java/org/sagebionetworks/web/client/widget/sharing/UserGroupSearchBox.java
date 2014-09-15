package org.sagebionetworks.web.client.widget.sharing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.shared.PublicPrincipalIds;

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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class UserGroupSearchBox {
	
	public static final String USER_GROUP_HEADER_URL = "/userGroupHeaders";

	public static final String KEY_QUERY = "query";
	public static final String KEY_PREFIX = "prefix";
	public static final String KEY_OFFSET = "offset";
	public static final String KEY_START = "start";
	public static final String KEY_LIMIT = "limit";

	// This is the value to get and set.
	public static final String KEY_TOTAL_NUMBER_OF_RESULTS = "totalNumberOfResults";
	public static final String KEY_CHILDREN = "children";
	public static final String KEY_PROFILE_PICTURE = "pic";
	public static final String KEY_IS_INDIVIDUAL = "isIndividual";
	public static final String KEY_PRINCIPAL_ID = "ownerId";
	public static final String KEY_USERNAME = "userName";
	public static final String KEY_FIRSTNAME = "firstName";
	public static final String KEY_LASTNAME = "lastName";

	/**
	 * Create a new editor for a given concept URL.
	 * 
	 * @param url
	 * @return
	 */
	public static ComboBox<ModelData> createUserGroupSearchSuggestBox(String repositoryUrl, String baseFileHandleUrl, String baseProfileAttachmentUrl, final PublicPrincipalIds publicPrincipalIds) {
		String url = repositoryUrl + USER_GROUP_HEADER_URL;
		ScriptTagProxy<PagingLoadResult<ModelData>> proxy = 
				new ScriptTagProxy<PagingLoadResult<ModelData>>(url);
		
		// Maps the model to our UserGroupHeader JSON format.
		ModelType type = new ModelType();
		type.setRoot(KEY_CHILDREN);
		type.setTotalName(KEY_TOTAL_NUMBER_OF_RESULTS);
		type.addField(KEY_FIRSTNAME, KEY_FIRSTNAME);
		type.addField(KEY_LASTNAME, KEY_LASTNAME);
		type.addField(KEY_PROFILE_PICTURE, KEY_PROFILE_PICTURE);
		type.addField(KEY_IS_INDIVIDUAL, KEY_IS_INDIVIDUAL);
		type.addField(KEY_PRINCIPAL_ID, KEY_PRINCIPAL_ID);
		type.addField(KEY_USERNAME, KEY_USERNAME);

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
		if (publicPrincipalIds != null) {
			//don't show the authenticated users group
			loader.addListener(Loader.Load, new Listener<LoadEvent>() {
				@Override
				public void handleEvent(LoadEvent be) {
					PagingLoadResult<ModelData> pagedResults = be.getData();
					if (pagedResults != null) {
						List<ModelData> modelDataList = pagedResults.getData();
						if (modelDataList != null)  {
							String authenticatedPrincipleIdString = publicPrincipalIds.getAuthenticatedAclPrincipalId() != null ? publicPrincipalIds.getAuthenticatedAclPrincipalId().toString() : "";
							String publicPrincipleIdString = publicPrincipalIds.getPublicAclPrincipalId() != null ? publicPrincipalIds.getPublicAclPrincipalId().toString() : "";
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
		loader.addListener(Loader.LoadException, new Listener<LoadEvent>() {
			@Override
			public void handleEvent(LoadEvent be) {
				if (be.exception != null) {
					DisplayUtils.showErrorMessage(be.exception.getMessage());
				}
				
			}
		});
		ListStore<ModelData> store = new ListStore<ModelData>(loader);

		ComboBox<ModelData> combo = new ComboBox<ModelData>();
		
		combo.setPropertyEditor(new ListModelPropertyEditor<ModelData>() {
			@Override
			public String getStringValue(ModelData value) {
				// Example output:
				// jane42  |  114085
				
				StringBuilder sb = new StringBuilder();
				Boolean isIndividual = value.get(KEY_IS_INDIVIDUAL);
				if (isIndividual != null && !isIndividual)
					sb.append("(Team) ");

				String firstName = value.get(KEY_FIRSTNAME);
				String lastName = value.get(KEY_LASTNAME);
				String username = value.get(KEY_USERNAME);
				sb.append(DisplayUtils.getDisplayName(firstName, lastName, username));
				sb.append("  |  "+ value.get(KEY_PRINCIPAL_ID));
	
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
		combo.setDisplayField(KEY_PRINCIPAL_ID);
		combo.setItemSelector("span.search-item");
		combo.setTemplate(getTemplate(baseFileHandleUrl, baseProfileAttachmentUrl));
		combo.setStore(store);
		combo.setHideTrigger(false);
		combo.setAllowBlank(false);
		combo.setMinChars(3);
		combo.setPageSize(10);
		return combo;
	}
	
	public static SuggestBox createUserGroupSearchGWTSuggestBox(String repositoryUrl, String baseFileHandleUrl, String baseProfileAttachmentUrl, final PublicPrincipalIds publicPrincipalIds) {
		String url = repositoryUrl + USER_GROUP_HEADER_URL;
		ScriptTagProxy<PagingLoadResult<ModelData>> proxy = 
				new ScriptTagProxy<PagingLoadResult<ModelData>>(url);
		
		
//		MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
//		oracle.add("Cat");
//		oracle.add("Dog");
//		oracle.add("Horse");
//		oracle.add("Canary");
		SuggestBox result = new SuggestBox();
		MySuggestOracle oracle = new MySuggestOracle(result);
		return result;
	}

	private static native String getTemplate(String baseFileHandleUrl, String baseProfileAttachmentUrl) /*-{
		return [ '<tpl for=".">',
				'<div class="margin-left-5" style="height:23px">',
				'<img class="margin-right-5 vertical-align-center tiny-thumbnail-image-container" onerror="this.style.display=\'none\';" src="',
				'<tpl if="isIndividual">',
					baseProfileAttachmentUrl,
					'?userId={ownerId}&waitForUrl=true" />',
				'</tpl>',
				
				'<tpl if="!isIndividual">',
					baseFileHandleUrl,
					'?teamId={ownerId}" />',
			    '</tpl>',
				'<span class="search-item movedown-1 margin-right-5">',
				'<span class="font-italic">{firstName} {lastName} </span> ',
				'<span>{userName} </span> ',
				'</span>',
				'<tpl if="!isIndividual">',
			        '(Team)',
			    '</tpl>',
				
				'</div>',
				'</tpl>' ].join("");
				
	}-*/;
	
	
	
	
}

