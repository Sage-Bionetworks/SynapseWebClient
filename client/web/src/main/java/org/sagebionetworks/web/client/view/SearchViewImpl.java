package org.sagebionetworks.web.client.view;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.search.Facet;
import org.sagebionetworks.repo.model.search.FacetConstraint;
import org.sagebionetworks.repo.model.search.FacetTypeNames;
import org.sagebionetworks.repo.model.search.Hit;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.KeyValue;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;

import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ComponentPlugin;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Slider;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SearchViewImpl extends Composite implements SearchView {

	private final int HIT_DESCRIPTION_LENGTH_CHAR = 270;
	private final int FACET_NAME_LENGTH_CHAR = 21;
	private final int MAX_PAGES_IN_PAGINATION = 9;	
	private final int NUM_SECONDS_PER_DAY = 86400;
	
	public interface SearchViewImplUiBinder extends
			UiBinder<Widget, SearchViewImpl> {
	}

	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	@UiField
	SimplePanel resultsPanel;
	@UiField
	SimplePanel searchBoxPanel;
	@UiField
	SimplePanel facetPanel;
	@UiField
	SimplePanel currentFacetsPanel;
	@UiField
	SimplePanel paginationPanel;
	
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	private Header headerWidget;
	private FlexTable horizontalTable;
	private TextBox searchField;
	private Button searchButton;
	private ContentPanel loadingPanel;
	private boolean loadShowing;
	private Map<String,FacetTypeNames> tempFacetToType;
	private Map<String, String> timeFacets;

	@Inject
	public SearchViewImpl(SearchViewImplUiBinder binder, Header headerWidget,
			Footer footerWidget, IconsImageBundle iconsImageBundle,
			SageImageBundle sageImageBundle) {
		initWidget(binder.createAndBindUi(this));

		this.iconsImageBundle = iconsImageBundle;
		this.sageImageBundle = sageImageBundle;
		this.headerWidget = headerWidget;

		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		loadShowing = false;
//		loadingPanel = DisplayUtils.getLoadingWidget(sageImageBundle);
//		loadingPanel.setSize(700, 100);
		
		tempFacetToType = new HashMap<String, FacetTypeNames>();
		tempFacetToType.put("node_type", FacetTypeNames.LITERAL);
		tempFacetToType.put("species", FacetTypeNames.LITERAL);
		tempFacetToType.put("disease", FacetTypeNames.LITERAL);
		tempFacetToType.put("modified_on", FacetTypeNames.DATE);
		tempFacetToType.put("tissue", FacetTypeNames.LITERAL);
		tempFacetToType.put("num_samples", FacetTypeNames.CONTINUOUS);
		tempFacetToType.put("created_by", FacetTypeNames.LITERAL);

	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		headerWidget.refresh();
		headerWidget.setSearchVisible(false);
		
		configureSearchBox();
	}

	@Override
	public void setSearchResults(SearchResults searchResults, String searchTerm, boolean newQuery) {		
		// TODO : set searchTerm into search box
		searchField.setText(searchTerm);
		
		// create search result list
		List<Hit> hits = searchResults.getHits();
		String resultsHtml = "";				
		if (hits != null && hits.size() > 0) {
			resultsHtml = createSearchResults(hits, resultsHtml, searchResults.getStart().intValue());			

			// create facet widgets
			createFacetWidgets(searchResults);			
											
			// show existing facets			
			createShownFacets(searchResults);
			
			// create pagination
			LayoutContainer lc = new LayoutContainer();
			lc.setStyleName("span-16 last clear");
			FlexTable table = new FlexTable();
			int start = searchResults.getStart().intValue();
			int calcMax = new Double(Math.ceil(searchResults.getFound()/10)).intValue();
			int maxPage = calcMax < MAX_PAGES_IN_PAGINATION ? calcMax : MAX_PAGES_IN_PAGINATION;		
			int firstPage = new Double(Math.floor(start / 10)).intValue() + 1;			
			int lastPage = maxPage;
			if(firstPage > 1) {
				lastPage = firstPage + maxPage - 1;
			}
				
			int tableCol = 0;
			if(firstPage != 1) {
				int newStart = (firstPage-2) * 10;			
				table.setWidget(0, tableCol, createPaginationAnchor("Prev", newStart, "btn tab02c grey"));
				tableCol++;
				table.setHTML(0, tableCol, "");
				tableCol++;
			} 			
			for(int i=firstPage; i<=lastPage; i++) {
				int newStart = (i-1) * 10;
				String style = "btn tab01a grey";
				if(i==firstPage) 
					style = "btn tab01a black";
				table.setWidget(0, tableCol, createPaginationAnchor("&nbsp;" + i +"&nbsp;", newStart, style));
				tableCol++;
				table.setHTML(0, tableCol, "");
				tableCol++;
			}
			if(firstPage + 1 < lastPage) {
				int newStart = firstPage * 10;			
				table.setWidget(0, tableCol, createPaginationAnchor("Next", newStart, "btn tab02d grey"));
				tableCol++;
				table.setHTML(0, tableCol, "");
				tableCol++;
			} 			
			lc.add(table);
			paginationPanel.clear();
			paginationPanel.add(lc);
			
		} else {
			resultsHtml = "<h4>" + DisplayConstants.LABEL_NO_SEARCH_RESULTS_PART1 + searchTerm + DisplayConstants.LABEL_NO_SEARCH_RESULTS_PART2 + "</h4>";
		}
		
		resultsPanel.clear();
		resultsPanel.add(new Html(resultsHtml));
		loadShowing = false;
		
		// scroll user to top of page
		Window.scrollTo(0, 0);
	}


	private void createShownFacets(SearchResults searchResults) {
		LayoutContainer currentFacets = new LayoutContainer();
		timeFacets = new HashMap<String, String>();

		// add size
		Html totalFound = new Html(searchResults.getFound() + " results found");
		totalFound.setStyleName("small-italic");
		totalFound.setStyleAttribute("margin", "6px 0 0 15px");
		currentFacets.add(totalFound);

		currentFacets.setWidth(513);
		currentFacets.setAutoHeight(true);
		for(final KeyValue facet : presenter.getAppliedFacets()) {
			String text = facet.getValue();
			if(text.contains("..")) {
				if(timeFacets.containsKey(facet.getValue())) {
					text = timeFacets.get(facet.getValue());
				} else {
					// continuous variable
					text = formatFacetName(facet.getKey()) + " >= " + facet.getValue().replaceAll("\\.\\.", "");
				}
			}
			Button btn = new Button(text, AbstractImagePrototype.create(iconsImageBundle.deleteButton16()));
			btn.setIconAlign(IconAlign.RIGHT);
			btn.addSelectionListener(new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					presenter.removeFacet(facet.getKey(), facet.getValue());						
				}
			});
			currentFacets.add(btn, new MarginData(6, 5, 0, 0));
		}
		currentFacets.layout(true);
		currentFacetsPanel.clear();
		currentFacetsPanel.add(currentFacets);
	}

	private void createFacetWidgets(SearchResults searchResults) {
		VerticalPanel vp = new VerticalPanel();
		for (String facetName : presenter.getFacetDisplayOrder()) {
			for (final Facet facet : searchResults.getFacets()) {
				if (facet.getName().equals(facetName)) {
					FacetTypeNames type = facet.getType();
					type = tempFacetToType.get(facet.getName());
					if (type != null) {
						Widget widget = null;
						switch (type) {
						case LITERAL:
							widget = createLiteralFacet(facet); 
							if(widget != null) vp.add(widget);
							break;
						case CONTINUOUS:
							widget = createContinuousFacet(facet); 
							if(widget != null) vp.add(widget);
							break;
						case DATE:
							widget = createDateFacet(facet); 
							if(widget != null) vp.add(widget);
							break;
						default:
							// facet type not supported
							break;
						}
					}
					continue;
				}
			}
		}
		facetPanel.clear();
		facetPanel.add(vp);
	}

	private String createSearchResults(List<Hit> hits, String resultsHtml, int start) {
		int i = start + 1;
		for(Hit hit : hits) {
			if(hit.getId() != null) {
				String a = getResultHtml(i, hit); 
				resultsHtml += a;
				i++;
			}
		}
		return resultsHtml;
	}

	@Override
	public void showErrorMessage(String message) {
		if(loadShowing) {
			resultsPanel.clear();
			loadShowing = false;
		}
		DisplayUtils.showErrorMessage(message);
		
	}

	@Override
	public void showLoading() {
		resultsPanel.clear();
		paginationPanel.clear();
		Html html = new Html();
		html.setHtml(DisplayUtils.getIconHtml(sageImageBundle.loading31()) + " Loading...");
		resultsPanel.add(html);
		loadShowing = true;
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
	}

	
	/*
	 * Private Methods
	 */	
	private void configureSearchBox() {
		if(horizontalTable == null) {
			// setup search box
			horizontalTable = new FlexTable();
			horizontalTable.setCellPadding(2);
			
			// setup serachButton
			searchButton = new Button(DisplayConstants.LABEL_SEARCH, AbstractImagePrototype.create(iconsImageBundle.magnify16()));
			searchButton.setHeight(32);		
			searchButton.setWidth(70);

			// setup field
			searchField = new TextBox();
			searchField.setStyleName("homesearchbox resultssearchbox");
			searchField.addKeyPressHandler(new KeyPressHandler() {				
				@Override
				public void onKeyPress(KeyPressEvent event) {
					char charCode = event.getCharCode();
					if (charCode == '\n' || charCode == '\r') {
		                searchButton.fireEvent(Events.Select);
		            }					
				}
			});				

			// add to table and page
			horizontalTable.setWidget(0, 0, searchField);
			horizontalTable.setHTML(0, 1, "&nbsp;");
			horizontalTable.setWidget(0, 2, searchButton);					
			searchBoxPanel.clear();
			searchBoxPanel.add(horizontalTable);
			
		} else {
			searchButton.removeAllListeners();	
		}
		searchButton.addSelectionListener(new SelectionListener<ButtonEvent>() {			
			@Override
			public void componentSelected(ButtonEvent ce) {				
				presenter.setSearchTerm(searchField.getText());
			}
		});

	}

	private String getResultHtml(int i, Hit hit) {
		String a =
				"<div class=\"span-18 last serv notopmargin\">\n" +					
				"	   <h4>"+ i +". \n" +
				"         <a class=\"link\" href=\""+ DisplayUtils.getSynapseHistoryToken(hit.getId()) +"\">" + hit.getName() + "</a>" +
				"      </h4>\n" +							 
				"	<p class=\"notopmargin small-italic\">" + DisplayUtils.stubStr(hit.getDescription(), HIT_DESCRIPTION_LENGTH_CHAR) + "</p>\n" +					
				"</div>\n";
		return a;
	}

	private LayoutContainer createDateFacet(final Facet facet) {
		if(facet == null) {
			return null;
		}		
		
		return null;
		
//		LayoutContainer lc = new LayoutContainer();
//		lc.add(new Html("<h6 style=\"margin-top: 15px;\">" + formatFacetName(facet.getName()) + "</h6>"));		
//		FlexTable table = new FlexTable();
//		
//		long min = facet.getMin();
//		long max = facet.getMax();
//		
//		timeFacets = new HashMap<String, String>();
//		// determine time diffs
//		//final int now = new Long(new Date().getTime()).intValue();
//		final int now = (int) max;
//		final int beginingOfTime = 0;
//		final int anHourAgo = now - (NUM_SECONDS_PER_DAY/24)*100;
//		final int aDayAgo = now - NUM_SECONDS_PER_DAY*100;
//		final int aWeekAgo = now - (NUM_SECONDS_PER_DAY*7)*100;
//		final int aMonthAgo = now - (NUM_SECONDS_PER_DAY*30)*100;
//		final int aYearAgo = now - (NUM_SECONDS_PER_DAY*365)*100;
//		
//		
//		int row = -1;								
//		if(min <= beginingOfTime && max >= now)
//			table.setWidget(++row, 0, createtimeFacet(facet, now, beginingOfTime, "Any Time"));
//		if(min <= anHourAgo && max >= now)
//			table.setWidget(++row, 0, createtimeFacet(facet, now, anHourAgo, "Past Hour"));
//		if(min <= aDayAgo && max >= now)
//			table.setWidget(++row, 0, createtimeFacet(facet, now, aDayAgo, "Past 24 Hours"));
//		if(min <= aWeekAgo && max >= now)
//			table.setWidget(++row, 0, createtimeFacet(facet, now, aWeekAgo, "Past Week"));
//		if(min <= aMonthAgo && max >= now)
//			table.setWidget(++row, 0, createtimeFacet(facet, now, aMonthAgo, "Past Month"));
//		if(min <= aYearAgo && max >= now)
//			table.setWidget(++row, 0, createtimeFacet(facet, now, aYearAgo, "Past Year"));
//		
//		if(row == -1) {
//			// no time facets were defined for the range
//			return null;
//		}
//		
//		lc.add(table);	  	     	 
//		return lc;
	}

	private Anchor createtimeFacet(final Facet facet, final int endTime,
			final int startTime, String title) {
		Anchor a;
		a = new Anchor(title);
		a.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				presenter.addFacet(facet.getName(), startTime + ".." + endTime);
			}
		});
		timeFacets.put(startTime + ".." + endTime, title);
		return a;
	}

	private LayoutContainer createContinuousFacet(final Facet facet) {		
		if(facet == null || facet.getMin() >= facet.getMax()) {
			return null;
		}
		
		LayoutContainer lc = new LayoutContainer();
		lc.add(new Html("<h6 style=\"margin-top: 15px;\">" + formatFacetName(facet.getName()) + "</h6>"));		
		final Slider slider = new Slider();
	    ComponentPlugin plugin = new ComponentPlugin() {  
	        public void init(Component component) {  
	          component.addListener(Events.Render, new Listener<ComponentEvent>() {  
	            public void handleEvent(ComponentEvent be) {  
	              El elem = be.getComponent().el().findParent(".facetSliderWrapper", 3);  
	              // should style in external CSS  rather than directly
	              elem.appendChild(XDOM.create("<div style='color: #615f5f;padding: 1 0 2 0px;'>From " + be.getComponent().getData("text") + "</div>"));	              
	            }  
	          });
	        }  
	      };  	      
		
		slider.setMaxValue(facet.getMax().intValue());
		slider.setMinValue(facet.getMin().intValue());
		int increment = 1;
		long range = facet.getMax() - facet.getMin();
		if(range >= 10000) {
			increment = 1000;
		} else if(range >= 1000) {
			increment = 100;
		} else if(range >= 100) {
			increment = 10;
		}
		slider.setIncrement(increment);
		slider.setMessage("At least {0}");
		slider.addPlugin(plugin);
		slider.setData("text", facet.getMin() + " to " + facet.getMax());

		final Button apply = new Button("Apply Filter");
		apply.hide();
		apply.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				// TODO : add an addContinuousFacet method to presenter
				presenter.addFacet(facet.getName(), slider.getValue() + "..");
			}
		});

		slider.addListener(Events.Change, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				apply.show();
				apply.setText("Apply: At least " + slider.getValue());				
			}
		});

		LayoutContainer lc2 = new LayoutContainer();
		lc2.setStyleName("facetSliderWrapper");
		lc2.add(slider);
		lc2.add(apply);				
		lc.setId("custom-slider");
	    lc.add(lc2);  
	    	    
	  	     	  
		return lc;
	}

	private LayoutContainer createLiteralFacet(final Facet facet) {
		LayoutContainer lc = new LayoutContainer();
		if(facet != null && facet.getConstraints() != null) {
			lc.setWidth(188);
			lc.add(new Html("<h6 style=\"margin-top: 15px;\">" + formatFacetName(facet.getName()) + "</h6>"));
			FlexTable flexTable = new FlexTable();
			int i=0;
			
			for(final FacetConstraint constraint : facet.getConstraints()) {
				// show top 10
				if(i>=10) {
					break;
				}
				
				// skip the prefixed facet values
				if(constraint.getValue().contains(":")) {
					continue;
				}
				
				Anchor a = new Anchor(DisplayUtils.stubStr(constraint.getValue(), FACET_NAME_LENGTH_CHAR) + " (" + constraint.getCount() + ")");			
				a.addClickHandler(new ClickHandler() {				
					@Override
					public void onClick(ClickEvent event) {
						presenter.addFacet(facet.getName(), constraint.getValue());				
					}
				});			
				flexTable.setWidget(i, 0, a);
				i++;
			}		
			lc.add(flexTable);
		}
		return lc;
	}

	private String formatFacetName(String name) {
		return DisplayUtils.uppercaseFirstLetter(name.replace("_", " "));
	}

	private Anchor createPaginationAnchor(String anchorName, final int newStart, String styleName) {
		Anchor a = new Anchor();
		a.setHTML(anchorName);
		a.setStyleName(styleName);
		a.addClickHandler(new ClickHandler() {					
			@Override
			public void onClick(ClickEvent event) {
				presenter.setStart(newStart);
			}
		});
		return a;
	}	

	
}



//@SuppressWarnings({ "unchecked", "rawtypes" })
//private Widget createLiteralFacet(final Facet facet) {	     
//	ListStore<BaseModelData> store = new ListStore<BaseModelData>();
//	
//	for(FacetConstraint constraint : facet.getConstraints()) {
//		BaseModelData model = new BaseModelData();
//		String num = " (" + constraint.getCount() + ")";
//		model.set("name", constraint.getValue() + num);
//		model.set("shortName", Format.ellipse(constraint.getValue(), FACET_NAME_LENGTH_CHAR) + num);
//		model.set("facet", facet.getName());
//		model.set("value", constraint.getValue());		
//		store.add(model);
//	}
//	
//	final ContentPanel panel = new ContentPanel();
//	panel.setFrame(false);
//	panel.setHeaderVisible(false);
//	panel.setWidth(191);
//	panel.setHeight(222);
//	panel.setBodyBorder(false);
//	panel.setScrollMode(Scroll.AUTO);
//
//	final CheckBoxListView<BaseModelData> view = new CheckBoxListView<BaseModelData>() {
//		@Override
//		protected BaseModelData prepareData(BaseModelData model) {
//			// no preparations needed
//			return model;
//		}
//
//	};
//
//	final List<BaseModelData> prevSelected = new ArrayList<BaseModelData>();
//	view.setStore(store);
//	view.setDisplayProperty("shortName");
//	view.addListener(Events.OnClick, new Listener() {
//		public void handleEvent(BaseEvent be) {
//			if (((DomEvent) be).getTarget(".x-view-item-checkbox", 2) != null) {
//				List<BaseModelData> selected = view.getChecked();					
//				BaseModelData model = selected.get(0);
//				presenter.addFacet((String)model.get("facet"), (String)model.get("value"));
//
////this was cool but wrong					
////				if(selected.size() > prevSelected.size()) {
////					// addition
////					selected.removeAll(prevSelected);
////					BaseModelData model = selected.get(0);
////					presenter.addFacet((String)model.get("facet"), (String)model.get("value"));
////					prevSelected.add(model);
////				} else {
////					// subtraction
////					prevSelected.removeAll(selected);
////					BaseModelData model = prevSelected.get(0);						
////					presenter.removeFacet((String)model.get("facet"), (String)model.get("value"));
////					prevSelected.remove(model);						
////					prevSelected.addAll(selected);
////				}					
//			}
//		}
//	});
//	panel.add(view);
//	
//	LayoutContainer lc = new LayoutContainer();
//	lc.add(new Html("<h6>" + facet.getName() + "</h6><p>"));
//	lc.add(panel);
//	return lc;
//}
