package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;

import com.extjs.gxt.ui.client.data.BaseModelData;
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
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

/**
 * This widget is a Synapse entity Id search box
 * 
 * @author dburdick
 *
 */
public class EntitySearchBoxViewImpl extends LayoutContainer implements EntitySearchBoxView, IsWidget {

	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	private ComboBox<BaseModelData> searchBox;
	
	@Inject
	public EntitySearchBoxViewImpl(IconsImageBundle iconsImageBundle) {
		this.iconsImageBundle = iconsImageBundle;
	}
	
	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void build(int width) {
		this.removeAll();
		HorizontalPanel searchPanel = new HorizontalPanel();
		searchBox = createTypeaheadField();
		searchBox.setHeight(DisplayUtils.BIG_BUTTON_HEIGHT_PX);
		searchBox.setWidth(width);		
		searchPanel.add(searchBox);
		Html icon = new Html(AbstractImagePrototype.create(iconsImageBundle.magnify16()).getHTML());
		icon.addStyleName("margin-top-left-10");
		searchPanel.add(icon);
		
		this.add(searchPanel);
		this.layout(true);
	}

	@Override
	public void setSearchResults(SearchResults results) {
		results.getHits();
	}
	
	@Override
	public void showLoading() {
	}

	@Override
	public void clear() {
		this.removeAll(true);
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	@Override
	public void clearSelection() {
		searchBox.clear();		
	}

	private ComboBox<BaseModelData> createTypeaheadField() {
		String url = "/Portal/simplesearch";
		ScriptTagProxy<PagingLoadResult<SearchResultModelData>> proxy = new ScriptTagProxy<PagingLoadResult<SearchResultModelData>>(url);

		ModelType type = new ModelType();
		type.setRoot("hits");
		type.setTotalName("found");
		type.addField(SearchResultModelData.ID, "id");
		type.addField(SearchResultModelData.NAME, "name");

		JsonPagingLoadResultReader<PagingLoadResult<SearchResultModelData>> reader = new JsonPagingLoadResultReader<PagingLoadResult<SearchResultModelData>>(
				type);
		
		PagingLoader<PagingLoadResult<SearchResultModelData>> loader = new BasePagingLoader<PagingLoadResult<SearchResultModelData>>(
				proxy, reader);

		loader.addListener(Loader.BeforeLoad, new Listener<LoadEvent>() {
			public void handleEvent(LoadEvent be) {
				be.<ModelData> getConfig().set("start",
						be.<ModelData> getConfig().get("offset"));
			}
		});

		ListStore<BaseModelData> store = new ListStore<BaseModelData>(loader);

		ComboBox<BaseModelData> combo = new ComboBox<BaseModelData>();
		combo.setDisplayField(SearchResultModelData.NAME);
		combo.setItemSelector("div.search-item");
		combo.setTemplate(getTemplate());
		combo.setStore(store);
		combo.setHideTrigger(true);
		combo.setPageSize(10);

		combo.addSelectionChangedListener(new SelectionChangedListener<BaseModelData>() {			
			@Override
			public void selectionChanged(SelectionChangedEvent<BaseModelData> se) {
				BaseModelData selected = se.getSelectedItem();
				if(selected != null) {
					presenter.entitySelected(selected.get(SearchResultModelData.ID).toString(), selected.get(SearchResultModelData.NAME).toString());
				}
			}
		});
		
		return combo;
	}

	private static native String getTemplate() /*-{
		return [ '<tpl for="."><div class="search-item">',
				'<table cellspacing="0" cellpadding="0" width="100%"><tr valign="top">',
				'<td><span class="suggestSearchTerm">{name}</span></td>',
				'<td align="right"><span class="suggestSearchOntology">{id}</span></td>',
				'</tr></table>',
				'</div></tpl>' ].join("");
	}-*/;

}
