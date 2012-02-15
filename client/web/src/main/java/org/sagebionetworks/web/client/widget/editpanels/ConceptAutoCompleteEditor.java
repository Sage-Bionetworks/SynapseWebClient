package org.sagebionetworks.web.client.widget.editpanels;



import java.util.List;

import org.sagebionetworks.web.client.ontology.AdapterModelData;
import org.sagebionetworks.web.client.ontology.NcboOntologyTerm;

import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.JsonPagingLoadResultReader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.Loader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.ScriptTagProxy;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ConceptAutoCompleteEditor {
	
	public static ComboBox<AdapterModelData> createNcboSuggestField() {
		String url = "http://localhost:8080/services-repository-0.11-SNAPSHOT/repo/v1/concept/11291/childrenTransitive/";
		ScriptTagProxy<PagingLoadResult<AdapterModelData>> proxy = new ScriptTagProxy<PagingLoadResult<AdapterModelData>>(url);
		

		ModelType type = new ModelType();
		type.setRoot("children");
		type.setTotalName("totalNumberOfResults");
		type.addField("preferredLabel", "preferredLabel");
		type.addField("definition", "definition");

		JsonPagingLoadResultReader<PagingLoadResult<AdapterModelData>> reader = new JsonPagingLoadResultReader<PagingLoadResult<AdapterModelData>>(
				type);
		
		
		PagingLoader<PagingLoadResult<AdapterModelData>> loader = new BasePagingLoader<PagingLoadResult<AdapterModelData>>(
				proxy, reader);

		loader.addListener(Loader.BeforeLoad, new Listener<LoadEvent>() {
			public void handleEvent(LoadEvent be) {
				be.<ModelData> getConfig().set("start",	be.<ModelData> getConfig().get("offset"));
				be.<ModelData> getConfig().set("prefix",	be.<ModelData> getConfig().get("query"));
			}
		});
		

		ListStore<AdapterModelData> store = new ListStore<AdapterModelData>(loader);

		ComboBox<AdapterModelData> combo = new ComboBox<AdapterModelData>();
		combo.setWidth(580);
		combo.setDisplayField("preferredLabel");
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
