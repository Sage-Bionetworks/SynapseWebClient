package org.sagebionetworks.web.client.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.search.Facet;
import org.sagebionetworks.repo.model.search.FacetConstraint;
import org.sagebionetworks.repo.model.search.FacetTypeNames;
import org.sagebionetworks.repo.model.search.Hit;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.KeyValue;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.UnorderedListPanel;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SearchViewImpl extends Composite implements SearchView {

	private static final int HIT_DESCRIPTION_LENGTH_CHAR = 270;
	private static final int FACET_NAME_LENGTH_CHAR = 21;
	private static final int MAX_PAGES_IN_PAGINATION = 10;
	private static final int MAX_RESULTS_PER_PAGE = 10;
	private static final int MINUTE_MS = 1000*60;
	private static final int HOUR_MS = MINUTE_MS * 60;
	private static final int DAY_MS = HOUR_MS * 24;
	private static final int WEEK_MS = DAY_MS * 7;
	private static final int MONTH_MS = DAY_MS * 30;
	private static final int YEAR_MS = DAY_MS * 365;
	private static Map<String,String> facetToDisplay;
	
	static {
		facetToDisplay = new HashMap<String, String>();
		facetToDisplay.put("node_type", "Type");
	}
	
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
	@UiField
	SimplePanel synAlertPanel;
	@UiField
	HTMLPanel narrowResultsPanel;
	
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private Header headerWidget;
	private TextBox searchField;
	private Button searchButton;
	private boolean loadShowing;
	private List<Button> facetButtons;
	private SynapseJSNIUtils synapseJSNIUtils;
	private Footer footerWidget;
	private PortalGinInjector ginInjector;
	
	@Inject
	public SearchViewImpl(SearchViewImplUiBinder binder, Header headerWidget,
			Footer footerWidget,
			SageImageBundle sageImageBundle, 
			SynapseJSNIUtils synapseJSNIUtils, PortalGinInjector ginInjector) {
		initWidget(binder.createAndBindUi(this));
		
		this.sageImageBundle = sageImageBundle;
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.ginInjector = ginInjector;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		loadShowing = false;
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;		
		header.clear();
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		headerWidget.refresh();
		headerWidget.setSearchVisible(false);		
		Window.scrollTo(0, 0); // scroll user to top of page
		
		configureSearchBox();
	}

	@Override
	public void setSearchResults(SearchResults searchResults,
			String searchTerm, boolean newQuery) {
		// set searchTerm into search box
		searchField.setText(searchTerm);
		facetButtons = new ArrayList<Button>();
		
		// create search result list
		List<Hit> hits = searchResults.getHits();
		Panel searchResultsPanel;				
		if (hits != null && hits.size() > 0) {
			searchResultsPanel = createSearchResults(hits, searchResults.getStart().intValue());			

			// create facet widgets
			createFacetWidgets(searchResults);			
											
			// create pagination
			createPagination(searchResults);
			
		} else {
			searchResultsPanel = new HTMLPanel(new SafeHtmlBuilder().appendHtmlConstant("<h4>" + DisplayConstants.LABEL_NO_SEARCH_RESULTS_PART1)
			.appendEscaped(searchTerm)
			.appendHtmlConstant(DisplayConstants.LABEL_NO_SEARCH_RESULTS_PART2 + "</h4>").toSafeHtml());
		}

		// show existing facets			
		String facetNames = createShownFacets(searchResults);
		Long start = presenter.getStart();
		String pageTitleStartNumber = start != null && start > 0 ? " (from result " + (start+1) + ")" : ""; 
		String pageTitleSearchTerm = searchTerm != null && searchTerm.length() > 0 ? "'"+searchTerm + "' " : "";
		synapseJSNIUtils.setPageTitle("Search: " + pageTitleSearchTerm + facetNames + pageTitleStartNumber);

		resultsPanel.clear();
		resultsPanel.add(searchResultsPanel);
		loadShowing = false;
		narrowResultsPanel.setVisible(true);
		currentFacetsPanel.setVisible(true);
		// scroll user to top of page
		Window.scrollTo(0, 0);
	}

	private void createPagination(SearchResults searchResults) {
		SimplePanel lc = new SimplePanel();
		UnorderedListPanel ul = new UnorderedListPanel();
		ul.setStyleName("pagination pagination-lg");
				
		List<PaginationEntry> entries = presenter.getPaginationEntries(MAX_RESULTS_PER_PAGE, MAX_PAGES_IN_PAGINATION);
		String currentSearchJSON = presenter.getCurrentSearchJSON();
		if(entries != null) {
			for(PaginationEntry pe : entries) {
				if(pe.isCurrent())
					ul.add(createPaginationAnchor(pe.getLabel(), currentSearchJSON, pe.getStart()), "current");
				else
					ul.add(createPaginationAnchor(pe.getLabel(), currentSearchJSON, pe.getStart()));
			}
		}
		
		lc.add(ul);
		paginationPanel.clear();
		paginationPanel.add(lc);
	}


	private String createShownFacets(SearchResults searchResults) {
		StringBuilder facetNames = new StringBuilder();
		FlowPanel currentFacets = new FlowPanel();

		// add size
		HTML totalFound = new HTML(searchResults.getFound() + " results found");
		totalFound.setStyleName("small-italic margin-10");
		currentFacets.add(totalFound);

		currentFacets.setWidth("513px");
		for(final KeyValue facet : presenter.getAppliedFacets()) {
			// Don't display the !link node_type facet
			if("link".equals(facet.getValue()) && "node_type".equals(facet.getKey()))
				continue;
			
			// show project facet differently
			if("project".equals(facet.getValue()) && "node_type".equals(facet.getKey())) {
				Button btn = new Button(DisplayConstants.SHOW_ALL_RESULTS, IconType.SEARCH, new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						// disable all buttons to allow only one click
						for(Button btn : facetButtons) {
							btn.setEnabled(false);
						}
						Window.scrollTo(0, 0);
						presenter.removeFacet(facet.getKey(), facet.getValue());						
					}
				});
				btn.addStyleName("margin-right-2");
				btn.setPull(Pull.LEFT);
				currentFacets.add(btn);
				facetButtons.add(0, btn);				
				continue;
			}
			
			String text = facet.getValue();
			if(text.contains("..")) {				
				text = presenter.getDisplayForTimeFacet(facet.getKey(), facet.getValue());
				if (text != null) {
					text = formatFacetName(facet.getKey()) + ": " + text;
				} else {
					// continuous variable
					text = formatFacetName(facet.getKey()) + " >= " + facet.getValue().replaceAll("\\.\\.", "");
				}
			}
			facetNames.append(text);
			facetNames.append(" ");
			Button btn = new Button(text, IconType.TIMES, new ClickHandler() {				
				@Override
				public void onClick(ClickEvent event) {
					// disable all buttons to allow only one click
					for(Button btn : facetButtons) {
						btn.setEnabled(false);
					}
					Window.scrollTo(0, 0);
					presenter.removeFacet(facet.getKey(), facet.getValue());						
				}
			});
			btn.addStyleName("margin-right-2");
			btn.setPull(Pull.LEFT);
			currentFacets.add(btn);
			facetButtons.add(btn);
		}
		currentFacetsPanel.clear();
		currentFacetsPanel.add(currentFacets);
		return facetNames.toString();
	}

	private void createFacetWidgets(SearchResults searchResults) {
		VerticalPanel vp = new VerticalPanel();
		for (String facetName : presenter.getFacetDisplayOrder()) {
			for (final Facet facet : searchResults.getFacets()) {
				if (facet.getName().equals(facetName)) {
					FacetTypeNames type = facet.getType();					
					if (type != null) {
						Widget widget = null;
						switch (type) {
						case LITERAL:
							widget = createLiteralFacet(facet); 
							break;
						case DATE:
							widget = createDateFacet(facet); 
							break;
						default:
							// facet type not supported
							break;
						}
						if(widget != null) vp.add(widget);
					}
					continue;
				}
			}
		}
		facetPanel.clear();
		facetPanel.add(vp);
	}

	private Panel createSearchResults(List<Hit> hits, int start) {
		FlowPanel resultsPanel = new FlowPanel();
		int i = start + 1;
		for(Hit hit : hits) {
			if(hit.getId() != null) {
				resultsPanel.add(getResult(i, hit));
				i++;
			}
		}
		return resultsPanel;
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
		resultsPanel.add(new HTMLPanel(DisplayUtils.getIconHtml(sageImageBundle.loading31()) + " Loading..."));
		loadShowing = true;
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
		resultsPanel.clear();
		paginationPanel.clear();
		narrowResultsPanel.setVisible(false);
		currentFacetsPanel.setVisible(false);
	}

	
	/*
	 * Private Methods
	 */	
	private void configureSearchBox() {
		// setup search box
		SimplePanel container;
		Row horizontalTable = new Row();
		
		// setup serachButton
		searchButton = new Button(DisplayConstants.LABEL_SEARCH, IconType.SEARCH, new ClickHandler() {				
			@Override
			public void onClick(ClickEvent event) {					
				presenter.setSearchTerm(searchField.getText());
			}
		});
		searchButton.setSize(ButtonSize.LARGE);
		searchButton.setBlock(true);

		// setup field
		searchField = new TextBox();
		searchField.setStyleName("form-control input-lg");
		searchField.addKeyDownHandler(new KeyDownHandler() {				
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
	                searchButton.fireEvent(new ClickEvent() {});
	            }					
			}
		});				

		// add to table and page
		container = new SimplePanel(searchField);
		container.addStyleName("col-md-9 padding-right-5");
		horizontalTable.add(container);
		container = new SimplePanel(searchButton);
		container.addStyleName("col-md-3 padding-left-5");
		horizontalTable.add(container);
		searchBoxPanel.clear();
		searchBoxPanel.add(horizontalTable);

	}

	/**
	 * stack-27 temporary change (until the index is updated, there may be usernames in the created_by and modified_by values).  Can remove
	 * @param userId
	 * @return
	 */
	private String getSearchUserId(String userId){
		String createdBy = userId;
		if (DisplayUtils.isTemporaryUsername(createdBy)) {
			createdBy = createdBy.substring(WebConstants.TEMPORARY_USERNAME_PREFIX.length());
		}
		return createdBy;
	}
	
	private Panel getResult(int i, Hit hit) {				
		FlowPanel attributionPanel = new FlowPanel();		
		
		ImageResource icon = presenter.getIconForHit(hit);
		
		UserBadge createdByBadge = ginInjector.getUserBadgeWidget();
		createdByBadge.configure(getSearchUserId(hit.getCreated_by()));
		UserBadge modifiedByBadge = ginInjector.getUserBadgeWidget();
		modifiedByBadge.configure(getSearchUserId(hit.getModified_by()));
		
		InlineHTML inlineHtml = new InlineHTML("Created by");
		inlineHtml.addStyleName("hitattribution");
		attributionPanel.add(inlineHtml);
		Widget createdByBadgeWidget = createdByBadge.asWidget();
		createdByBadgeWidget.addStyleName("movedown-7");
		attributionPanel.add(createdByBadgeWidget);
		
		inlineHtml = new InlineHTML(" on " + DisplayUtils.converDateaToSimpleString(new Date(hit.getCreated_on()*1000)) + ", Updated by ");
		inlineHtml.addStyleName("hitattribution");
		
		attributionPanel.add(inlineHtml);
		Widget modifiedByBadgeWidget = modifiedByBadge.asWidget();
		modifiedByBadgeWidget.addStyleName("movedown-7");
		attributionPanel.add(modifiedByBadgeWidget);
		inlineHtml = new InlineHTML(" on " + DisplayUtils.converDateaToSimpleString(new Date(hit.getModified_on()*1000)));
		inlineHtml.addStyleName("hitattribution");
		
		attributionPanel.add(inlineHtml);
		
		FlowPanel hitPanel = new FlowPanel();
		hitPanel.addStyleName("serv hit margin-bottom-20");
		SafeHtmlBuilder resultBuilder = new SafeHtmlBuilder();
		resultBuilder.appendHtmlConstant("	   <h4>" + i + ". \n");
		if(icon != null) 
			resultBuilder.appendHtmlConstant(DisplayUtils.getIconHtml(icon));
		resultBuilder.appendHtmlConstant("         <a class=\"link\" href=\"" + DisplayUtils.getSynapseHistoryToken(hit.getId()) + "\">")
		.appendEscaped(hit.getName())
		.appendHtmlConstant("</a>");
		
		resultBuilder.appendHtmlConstant("    </h4>\n");
		if(null != hit.getPath()) {
			resultBuilder.append(getPathHtml(hit.getPath())).appendHtmlConstant("<br/>\n");
		}
		if(null != hit.getDescription()) {
			resultBuilder.appendHtmlConstant("<span class=\"hitdescription\">")
			.appendEscaped(DisplayUtils.stubStr(hit.getDescription(), HIT_DESCRIPTION_LENGTH_CHAR))
			.appendHtmlConstant("</span><br>\n");
		}
		hitPanel.add(new HTMLPanel(resultBuilder.toSafeHtml()));
		hitPanel.add(attributionPanel);
		
		return hitPanel;
	}
	
	private SafeHtml getPathHtml(EntityPath path) {		
		List<EntityHeader> headers = path.getPath();
		SafeHtmlBuilder pathBuilder = new SafeHtmlBuilder();
		for(int i=0; i<headers.size(); i++) {
			if(i == 0) continue; // skip "root"
			EntityHeader header = headers.get(i); 
			String safeLink = "<a href=\"" + DisplayUtils.getSynapseHistoryToken(header.getId()) + "\""; 
			if(i >= headers.size()-1) {
				// last one show full color
				safeLink += " class=\"hitBreadcrumbElement\"";
			} else {
				// grey parents
				safeLink += " class=\"hitBreadcrumbParent\"";
			}
			safeLink += ">" + SafeHtmlUtils.fromString(header.getName()).asString() + "</a>";
			pathBuilder.appendHtmlConstant(safeLink);
			
			if(i<headers.size() - 1) {
				pathBuilder.appendHtmlConstant(ClientProperties.BREADCRUMB_SEP);
			}
		}
		return pathBuilder.toSafeHtml();
	}

	private FlowPanel createDateFacet(final Facet facet) {
		if(facet == null) return null;
		if(facet.getMin() == null || facet.getMax() == null || facet.getMin() >= facet.getMax()) return null;		
		
		FlowPanel lc = new FlowPanel();
		lc.add(new HTML("<h6 style=\"margin-top: 15px;\">" + formatFacetName(facet.getName()) + "</h6>"));		
		FlexTable table = new FlexTable();
		
		// convert to miliseconds
		long min = facet.getMin() * 1000;
		long max = facet.getMax() * 1000;
		
		// determine time diffs
		Date now = presenter.getSearchStartTime();		
		long beginingOfTime = 0;
		long anHourAgo = now.getTime()-HOUR_MS;
		long aDayAgo = now.getTime()-DAY_MS;
		long aWeekAgo = now.getTime()-WEEK_MS;
		long aMonthAgo = now.getTime()-MONTH_MS;
		long aYearAgo = now.getTime()-YEAR_MS;
		
		int row = -1;
		table.setWidget(++row, 0, createTimeFacet(facet, beginingOfTime, "Any Time"));
		if(anHourAgo <= max)
			table.setWidget(++row, 0, createTimeFacet(facet, anHourAgo, "Past Hour"));
		if(aDayAgo <= max)
			table.setWidget(++row, 0, createTimeFacet(facet, aDayAgo, "Past 24 Hours"));
		if(aWeekAgo <= max)
			table.setWidget(++row, 0, createTimeFacet(facet, aWeekAgo, "Past Week"));
		if(aMonthAgo <= max)
			table.setWidget(++row, 0, createTimeFacet(facet, aMonthAgo, "Past Month"));
		if(aYearAgo <= max)
			table.setWidget(++row, 0, createTimeFacet(facet, aYearAgo, "Past Year"));
		
		if(row == -1) {
			// no time facets were defined for the range
			return null;
		}
		
		lc.add(table);	  	     	 
		return lc;
	}

	private Anchor createTimeFacet(final Facet facet, final long startTime, final String title) {
		Anchor a;
		a = new Anchor(title);
		final String facetValue = startTime + "..";
		a.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				Window.scrollTo(0, 0);
				presenter.addTimeFacet(facet.getName(), facetValue, title);
			}
		});
		return a;
	}


	private FlowPanel createLiteralFacet(final Facet facet) {
		FlowPanel lc = null; 
		if(facet != null && facet.getConstraints() != null && facet.getConstraints().size() > 0) {
			lc = new FlowPanel();
			String displayName = facetToDisplay.containsKey(facet.getName()) ? formatFacetName(facetToDisplay.get(facet.getName())) : formatFacetName(facet.getName());
			//special case.  if this is the created_by facet, then add a UserBadge
			boolean isCreatedByFacet = "created_by".equalsIgnoreCase(facet.getName());
			lc.add(new HTML("<h6 style=\"margin-top: 15px;\">" + displayName + "</h6>"));
			FlowPanel flowPanel = new FlowPanel();
			//FlexTable flexTable = new FlexTable();
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
				FlowPanel valueContainer = new FlowPanel();
				String stub = DisplayUtils.stubStr(constraint.getValue(), FACET_NAME_LENGTH_CHAR);
				ClickHandler clickHandler = new ClickHandler() {				
					@Override
					public void onClick(ClickEvent event) {
						Window.scrollTo(0, 0);
						presenter.addFacet(facet.getName(), constraint.getValue());				
					}
				};
				if (isCreatedByFacet) {
					stub = "";
					UserBadge badge = ginInjector.getUserBadgeWidget();
					badge.configure(getSearchUserId(constraint.getValue()));
					badge.setCustomClickHandler(clickHandler);
					Widget widget = badge.asWidget();
					widget.addStyleName("movedown-7");
					valueContainer.add(widget);
				}
				Anchor a = new Anchor(stub + " (" + constraint.getCount() + ")");
				if (!stub.equalsIgnoreCase(constraint.getValue()) && !isCreatedByFacet) {
					DisplayUtils.addTooltip(a, constraint.getValue(), Placement.RIGHT);
				}
				
				a.addClickHandler(clickHandler);	
				valueContainer.add(a);
				flowPanel.add(valueContainer);
				i++;
			}		
			lc.add(flowPanel);
		}
		return lc;
	}

	private String formatFacetName(String name) {
		return DisplayUtils.uppercaseFirstLetter(name.replace("_", " "));
	}

	private Anchor createPaginationAnchor(String anchorName, String currentSearchJSON, final long newStart) {
		Anchor a = new Anchor();
		a.setHTML(anchorName);
		a.setHref(DisplayUtils.getSearchHistoryToken(currentSearchJSON, newStart));
		return a;
	}

	@Override
	public void setSynAlertWidget(Widget synAlert) {
		synAlertPanel.setWidget(synAlert);
	}	

	
}