package org.sagebionetworks.web.client.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.search.Facet;
import org.sagebionetworks.repo.model.search.FacetConstraint;
import org.sagebionetworks.repo.model.search.FacetTypeNames;
import org.sagebionetworks.repo.model.search.Hit;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.KeyRange;
import org.sagebionetworks.repo.model.search.query.KeyValue;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.MarkdownIt;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.user.BadgeSize;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.SearchQueryUtils;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
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
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SearchViewImpl extends Composite implements SearchView {
	private static final int MAX_FACET_VALUES_SHOWN = 10;
	private static final int HIT_DESCRIPTION_LENGTH_CHAR = 270;
	private static final int FACET_NAME_LENGTH_CHAR = 21;
	private static final int MINUTE_IN_SEC = 60;
	private static final int HOUR_IN_SEC = MINUTE_IN_SEC * 60;
	private static final int DAY_IN_SEC = HOUR_IN_SEC * 24;
	private static final int WEEK_IN_SEC = DAY_IN_SEC * 7;
	private static final int MONTH_IN_SEC = DAY_IN_SEC * 30;
	private static final int YEAR_IN_SEC = DAY_IN_SEC * 365;
	private static Map<String,String> facetToDisplay;
	
	static {
		facetToDisplay = new HashMap<String, String>();
		facetToDisplay.put("node_type", "Type");
		facetToDisplay.put("modified_by", "Last Modified By");
	}
	
	public interface SearchViewImplUiBinder extends
			UiBinder<Widget, SearchViewImpl> {
	}
	@UiField
	SimplePanel resultsPanel;
	@UiField
	SimplePanel facetPanel;
	@UiField
	SimplePanel currentFacetsPanel;
	@UiField
	SimplePanel synAlertPanel;
	@UiField
	HTMLPanel narrowResultsPanel;
	
	private Presenter presenter;
	private Header headerWidget;
	@UiField
	TextBox searchField;
	@UiField
	Button searchButton;
	private List<Button> facetButtons;
	private SynapseJSNIUtils synapseJSNIUtils;
	private PortalGinInjector ginInjector;
	DateTimeUtils dateTimeUtils;
	private MarkdownIt markdownIt;
	public static final String BUTTON_HEIGHT = "38px";
	@Inject
	public SearchViewImpl(SearchViewImplUiBinder binder, 
			Header headerWidget,
			SynapseJSNIUtils synapseJSNIUtils, 
			PortalGinInjector ginInjector,
			DateTimeUtils dateTimeUtils,
			MarkdownIt markdownIt) {
		initWidget(binder.createAndBindUi(this));
		
		this.headerWidget = headerWidget;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.ginInjector = ginInjector;
		this.dateTimeUtils = dateTimeUtils;
		this.markdownIt = markdownIt;
		headerWidget.configure();
		searchButton.addClickHandler(new ClickHandler() {				
			@Override
			public void onClick(ClickEvent event) {					
				presenter.setSearchTerm(searchField.getText());
			}
		});
		searchField.addKeyDownHandler(new KeyDownHandler() {				
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
	                searchButton.fireEvent(new ClickEvent() {});
	            }					
			}
		});
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;		
		headerWidget.configure();
		headerWidget.refresh();
		Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public void setSearchResults(SearchResults searchResults,
			String searchTerm) {
		// set searchTerm into search box
		searchField.setText(searchTerm);
		facetButtons = new ArrayList<Button>();
		// show existing facets
		String facetNames = createShownFacets(searchResults);
		String pageTitleSearchTerm = searchTerm != null && searchTerm.length() > 0 ? "'"+searchTerm + "' " : "";
		synapseJSNIUtils.setPageTitle("Search: " + pageTitleSearchTerm + facetNames);
		narrowResultsPanel.setVisible(true);
		currentFacetsPanel.setVisible(true);
	}
	
	@Override
	public void setLoadingMoreContainerWidget(Widget w) {
		resultsPanel.clear();
		resultsPanel.add(w);
	}
	
	@Override
	public Widget getResults(SearchResults searchResults, String searchTerm, boolean isFirstPage) {
		// create search result list
		List<Hit> hits = searchResults.getHits();
		Panel searchResultsPanel;				
		if (hits != null && hits.size() > 0) {
			searchResultsPanel = createSearchResults(hits, searchResults.getStart().intValue());			
			if (isFirstPage) {
				// create facet widgets
				createFacetWidgets(searchResults);			
			}
		} else if (isFirstPage) {
			searchResultsPanel = new HTMLPanel(new SafeHtmlBuilder().appendHtmlConstant("<h4>" + DisplayConstants.LABEL_NO_SEARCH_RESULTS_PART1)
			.appendEscaped(searchTerm)
			.appendHtmlConstant(DisplayConstants.LABEL_NO_SEARCH_RESULTS_PART2 + "</h4>").toSafeHtml());
			facetPanel.clear();
		} else { 
			searchResultsPanel = new SimplePanel();
		}
		return searchResultsPanel;
	}
	
	private String createShownFacets(SearchResults searchResults) {
		StringBuilder facetNames = new StringBuilder();
		FlowPanel currentFacets = new FlowPanel();

		// add size
		HTML totalFound = new HTML(searchResults.getFound() + " results found");
		totalFound.setStyleName("small-italic margin-10");
		currentFacets.add(totalFound);
		
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
				btn.setHeight(BUTTON_HEIGHT);
				btn.addStyleName("margin-right-5 margin-top-5");
				btn.setPull(Pull.LEFT);
				currentFacets.add(btn);
				facetButtons.add(0, btn);				
				continue;
			}
			
			String text = facet.getValue();
			facetNames.append(text);
			facetNames.append(" ");
			Button btn = new Button("", IconType.TIMES, event -> {				
				// disable all buttons to allow only one click
				for(Button facetButton : facetButtons) {
					facetButton.setEnabled(false);
				}
				Window.scrollTo(0, 0);
				presenter.removeFacet(facet.getKey(), facet.getValue());						
			});
			if ("created_by".equalsIgnoreCase(facet.getKey()) || "modified_by".equalsIgnoreCase(facet.getKey())) {
				String buttonText = "created_by".equalsIgnoreCase(facet.getKey()) ? "Created by " : "Last modified by ";
				UserBadge createdByBadge = ginInjector.getUserBadgeWidget();
				createdByBadge.configure(facet.getValue());
				createdByBadge.setSize(BadgeSize.SMALLER);
				createdByBadge.setCustomClickHandler(event -> {
					btn.click();
				});
				btn.add(new Text(buttonText));
				btn.add(createdByBadge);
			} else {
				btn.add(new Text(text));
			}
			btn.setHeight(BUTTON_HEIGHT);
			btn.addStyleName("margin-right-5 margin-top-5");
			btn.setPull(Pull.LEFT);
			currentFacets.add(btn);
			facetButtons.add(btn);
		}
		addTimeFacets(searchResults, facetNames, currentFacets);
		currentFacetsPanel.clear();
		currentFacetsPanel.add(currentFacets);
		return facetNames.toString();
	}
	
	private void addTimeFacets(SearchResults searchResults, StringBuilder facetNames, FlowPanel currentFacets) {
		for(final KeyRange facet : presenter.getAppliedTimeFacets()) {
			String text = presenter.getDisplayForTimeFacet(facet.getKey(), facet.getMin());
			if (text != null) {
				text = formatFacetName(facet.getKey()) + ": " + text;
			} else {
				// continuous variable, in this case time in seconds
				String valueAsString = facet.getMin();
				
				String formattedDateString;
				try{
					long valueInMiliseconds = Long.parseLong(valueAsString) * 1000;
					formattedDateString = valueInMiliseconds == 0 ? "any time": dateTimeUtils.getDateString(new Date(valueInMiliseconds));
				}catch (NumberFormatException e){
					formattedDateString = valueAsString;
				}
				
				text = formatFacetName(facet.getKey()) + " >= " + formattedDateString;
			}
			
			facetNames.append(text);
			facetNames.append(" ");
			Button btn = new Button(text, IconType.TIMES, event -> {				
				// disable all buttons to allow only one click
				for(Button facetButton : facetButtons) {
					facetButton.setEnabled(false);
				}
				Window.scrollTo(0, 0);
				presenter.removeTimeFacetAndRefresh(facet.getKey());						
			});
			
			btn.setHeight(BUTTON_HEIGHT);
			btn.addStyleName("margin-right-5 margin-top-5");
			btn.setPull(Pull.LEFT);
			currentFacets.add(btn);
			facetButtons.add(btn);
		}
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
		DisplayUtils.showErrorMessage(message);
		
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void clear() {
		narrowResultsPanel.setVisible(false);
		currentFacetsPanel.setVisible(false);
	}

	
	/*
	 * Private Methods
	 */	
	
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
		
		IconType iconType = presenter.getIconForHit(hit);
		
		UserBadge createdByBadge = ginInjector.getUserBadgeWidget();
		createdByBadge.configure(getSearchUserId(hit.getCreated_by()));
		UserBadge modifiedByBadge = ginInjector.getUserBadgeWidget();
		modifiedByBadge.configure(getSearchUserId(hit.getModified_by()));
		
		InlineHTML inlineHtml = new InlineHTML("Created by");
		inlineHtml.addStyleName("hitattribution");
		attributionPanel.add(inlineHtml);
		Widget createdByBadgeWidget = createdByBadge.asWidget();
		attributionPanel.add(createdByBadgeWidget);
		
		inlineHtml = new InlineHTML(" on " + dateTimeUtils.getDateString(new Date(hit.getCreated_on()*1000)) + ", Updated by ");
		inlineHtml.addStyleName("hitattribution");
		
		attributionPanel.add(inlineHtml);
		Widget modifiedByBadgeWidget = modifiedByBadge.asWidget();
		attributionPanel.add(modifiedByBadgeWidget);
		inlineHtml = new InlineHTML(" on " + dateTimeUtils.getDateString(new Date(hit.getModified_on()*1000)));
		inlineHtml.addStyleName("hitattribution");
		
		attributionPanel.add(inlineHtml);
		
		FlowPanel hitPanel = new FlowPanel();
		hitPanel.addStyleName("serv hit margin-bottom-20");
		Heading h4 = new Heading(HeadingSize.H4);
		FlowPanel headingPanel = new FlowPanel();
		h4.add(headingPanel);
		org.gwtbootstrap3.client.ui.Anchor link = new org.gwtbootstrap3.client.ui.Anchor(hit.getName(), DisplayUtils.getSynapseHistoryToken(hit.getId()));
		headingPanel.add(new Text(i+"."));
		Icon icon = new Icon(iconType);
		icon.addStyleName("lightGreyText margin-right-5 margin-left-5");
		headingPanel.add(icon);
		headingPanel.add(link);
		hitPanel.add(h4);
		
		SafeHtmlBuilder resultBuilder = new SafeHtmlBuilder();
		if(null != hit.getPath()) {
			resultBuilder.append(getPathHtml(hit.getPath())).appendHtmlConstant("<br/>\n");
		}
		if(null != hit.getDescription()) {
			String description = markdownIt.markdown2Html(hit.getDescription(), "");
			description = new HTML(description).getText().replace("<Synapse widget>", "").replace("|", "").replace("#", "");
			resultBuilder.appendHtmlConstant("<span class=\"hitdescription\">")
			.appendEscaped(DisplayUtils.stubStr(description, HIT_DESCRIPTION_LENGTH_CHAR))
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
		//TODO: remove the comment
		//if(facet.getMin() == null || facet.getMax() == null || facet.getMin() >= facet.getMax()) return null;		
		
		FlowPanel lc = new FlowPanel();
		Heading facetTitle = new Heading(HeadingSize.H4, formatFacetName(facet.getName()));
		lc.add(facetTitle);		
		FlexTable table = new FlexTable();
		
		// convert to miliseconds
		//long min = facet.getMin() * 1000;
		//long max = facet.getMax() * 1000;
		
		// determine time diffs
		long curTimeInSec = System.currentTimeMillis() / 1000;
		long anHourAgo = curTimeInSec - HOUR_IN_SEC;
		long aDayAgo = curTimeInSec - DAY_IN_SEC;
		long aWeekAgo = curTimeInSec - WEEK_IN_SEC;
		long aMonthAgo = curTimeInSec - MONTH_IN_SEC;
		long aYearAgo = curTimeInSec - YEAR_IN_SEC;
		
		int row = -1;
		table.setWidget(++row, 0, createRemoveFacet(facet, "Any Time"));
		//if(anHourAgo <= max)
			table.setWidget(++row, 0, createTimeFacet(facet, anHourAgo, "Past Hour"));
		//if(aDayAgo <= max)
			table.setWidget(++row, 0, createTimeFacet(facet, aDayAgo, "Past 24 Hours"));
		//if(aWeekAgo <= max)
			table.setWidget(++row, 0, createTimeFacet(facet, aWeekAgo, "Past Week"));
		//if(aMonthAgo <= max)
			table.setWidget(++row, 0, createTimeFacet(facet, aMonthAgo, "Past Month"));
		//if(aYearAgo <= max)
			table.setWidget(++row, 0, createTimeFacet(facet, aYearAgo, "Past Year"));
		
		if(row == -1) {
			// no time facets were defined for the range
			return null;
		}
		
		lc.add(table);	  	     	 
		return lc;
	}

	private Anchor createTimeFacet(final Facet facet, final Long startTime, final String title) {
		Anchor a;
		a = new Anchor(title);
		a.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				Window.scrollTo(0, 0);
				presenter.addTimeFacet(facet.getName(), startTime.toString(), title);
			}
		});
		return a;
	}
	
	private Anchor createRemoveFacet(final Facet facet, final String title) {
		Anchor a;
		a = new Anchor(title);
		a.addClickHandler( event -> {
			Window.scrollTo(0, 0);
			presenter.removeTimeFacetAndRefresh(facet.getName());
		});
		return a;
	}


	private FlowPanel createLiteralFacet(final Facet facet) {
		FlowPanel lc = null; 
		if(facet != null && facet.getConstraints() != null && facet.getConstraints().size() > 0) {
			lc = new FlowPanel();
			String displayName = facetToDisplay.containsKey(facet.getName()) ? formatFacetName(facetToDisplay.get(facet.getName())) : formatFacetName(facet.getName());
			Heading facetTitle = new Heading(HeadingSize.H4, displayName);
			lc.add(facetTitle);
			FlowPanel flowPanel = new FlowPanel();
			lc.add(flowPanel);
			for (int i = 0; i < facet.getConstraints().size(); i++) {
				// show top 10
				if(i>=MAX_FACET_VALUES_SHOWN) {
					// add a button to show the rest of the facet values
					Button showAll = new Button();
					boolean isMax = facet.getConstraints().size() == SearchQueryUtils.MAX_FACET_VALUES_COUNT;
					String buttonText = isMax ? "Show " : "Show all ";
					showAll.setText(buttonText + facet.getConstraints().size());
					showAll.setSize(ButtonSize.EXTRA_SMALL);
					showAll.setMarginTop(10);
					lc.add(showAll);
					showAll.addClickHandler(event -> {
						showAll.setVisible(false);
						showAllFacetValues(flowPanel, facet);
					});
					break;
				}
				
				FlowPanel valueContainer = getFacetValue(facet, i);
				if (valueContainer != null) {
					flowPanel.add(valueContainer);	
				}
			}		
		}
		return lc;
	}
	
	private void showAllFacetValues(FlowPanel p, Facet facet) {
		for (int i = MAX_FACET_VALUES_SHOWN; i < facet.getConstraints().size(); i++) {
			FlowPanel valueContainer = getFacetValue(facet, i);
			if (valueContainer != null) {
				p.add(valueContainer);	
			}
		}
	}
	
	private FlowPanel getFacetValue(Facet facet, int i) {
		//special case.  if this is the created_by facet, then add a UserBadge
		boolean isModifiedByFacet = "modified_by".equalsIgnoreCase(facet.getName());
		boolean isCreatedByFacet = "created_by".equalsIgnoreCase(facet.getName());
		
		FacetConstraint constraint  = facet.getConstraints().get(i);
		
		// skip the prefixed facet values
		if(constraint.getValue().contains(":")) {
			return null;
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
		if (isCreatedByFacet || isModifiedByFacet) {
			stub = "";
			UserBadge badge = ginInjector.getUserBadgeWidget();
			badge.configure(getSearchUserId(constraint.getValue()));
			badge.setCustomClickHandler(clickHandler);
			Widget widget = badge.asWidget();
			valueContainer.add(widget);
		}
		Anchor a = new Anchor(stub + " (" + constraint.getCount() + ")");
		if (!stub.equalsIgnoreCase(constraint.getValue()) && !isCreatedByFacet && !isModifiedByFacet) {
			valueContainer.add(DisplayUtils.addTooltip(a, constraint.getValue(), Placement.RIGHT));
		} else {
			valueContainer.add(a);
		}
		
		a.addClickHandler(clickHandler);
		return valueContainer;
	}

	private String formatFacetName(String name) {
		return DisplayUtils.uppercaseFirstLetter(name.replace("_", " "));
	}

	@Override
	public void setSynAlertWidget(Widget synAlert) {
		synAlertPanel.setWidget(synAlert);
	}	

	
}