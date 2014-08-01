package org.sagebionetworks.web.client.view;

import java.util.List;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.TeamSearch;
import org.sagebionetworks.web.client.presenter.TeamSearchPresenter;
import org.sagebionetworks.web.client.utils.UnorderedListPanel;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;
import org.sagebionetworks.web.client.widget.team.TeamListWidget;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamSearchViewImpl extends Composite implements TeamSearchView {
	public interface TeamSearchViewImplUiBinder extends UiBinder<Widget, TeamSearchViewImpl> {}
	
	private static final int MAX_PAGES_IN_PAGINATION = 10;
	
	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	@UiField
	SimplePanel searchBoxPanel;
	@UiField
	FlowPanel mainContainer;
	@UiField
	SimplePanel paginationPanel;
	
	private Header headerWidget;
	private Footer footerWidget;
	private SageImageBundle sageImageBundle;
	private Presenter presenter;
	private SynapseJSNIUtils synapseJsniUtils;
	private TeamListWidget teamListWidget;
	private TextBox searchField;
	private Button searchButton;
	private LayoutContainer searchButtonContainer;
	
	@Inject
	public TeamSearchViewImpl(TeamSearchViewImplUiBinder binder,
			Header headerWidget, 
			Footer footerWidget, 
			SageImageBundle sageImageBundle,
			SynapseJSNIUtils synapseJsniUtils,
			TeamListWidget teamListWidget) {
		initWidget(binder.createAndBindUi(this));
		this.sageImageBundle = sageImageBundle;
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.synapseJsniUtils = synapseJsniUtils;
		this.teamListWidget = teamListWidget;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
	}
	
	@Override
	public void clear() {
	}
	
	@Override
	public void showLoading() {
		mainContainer.clear();
		mainContainer.add(DisplayUtils.getLoadingWidget(sageImageBundle));
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
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		header.clear();
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		headerWidget.refresh();	
		Window.scrollTo(0, 0); // scroll user to top of page
		configureSearchBox();
	}
	
	@Override
	public void configure(List<Team> teams, String searchTerm) {
		mainContainer.clear();
		searchField.setValue(searchTerm);
		teamListWidget.configure(teams, true);
		int start = presenter.getOffset();
		String pageTitleStartNumber = start > 0 ? " (from result " + (start+1) + ")" : ""; 
		String pageTitleSearchTerm = searchTerm != null && searchTerm.length() > 0 ? " '"+searchTerm + "' " : "";
		synapseJsniUtils.setPageTitle("Team Search" + pageTitleSearchTerm + pageTitleStartNumber);
		mainContainer.add(teamListWidget.asWidget());
		createPagination(searchTerm);
	}
	
	private void configureSearchBox() {
		// setup search box
		SimplePanel container;
		LayoutContainer horizontalTable = new LayoutContainer();
		horizontalTable.addStyleName("row");
		
		// setup serachButton
		searchButton = DisplayUtils.createIconButton(DisplayConstants.LABEL_SEARCH, ButtonType.DEFAULT, "glyphicon-search");
		searchButton.addStyleName("btn-lg btn-block");
		searchButton.addClickHandler(new ClickHandler() {				
			@Override
			public void onClick(ClickEvent event) {					
				presenter.goTo(new TeamSearch(searchField.getValue()));
			}
		});

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
	
	private void createPagination(String searchTerm) {
		LayoutContainer lc = new LayoutContainer();
		UnorderedListPanel ul = new UnorderedListPanel();
		ul.setStyleName("pagination pagination-lg");
		
		List<PaginationEntry> entries = presenter.getPaginationEntries(TeamSearchPresenter.SEARCH_TEAM_LIMIT, MAX_PAGES_IN_PAGINATION);
		if(entries != null) {
			for(PaginationEntry pe : entries) {
				if(pe.isCurrent())
					ul.add(createPaginationAnchor(pe.getLabel(), searchTerm, pe.getStart()), "active");
				else
					ul.add(createPaginationAnchor(pe.getLabel(), searchTerm, pe.getStart()));
			}
		}
		
		lc.add(ul);
		paginationPanel.clear();
		if (entries.size() > 1)
			paginationPanel.add(lc);
	}
	
	private Anchor createPaginationAnchor(String anchorName, String searchTerm, final int newStart) {
		Anchor a = new Anchor();
		a.setHTML(anchorName);
		a.setHref(DisplayUtils.getTeamSearchHistoryToken(searchTerm, newStart));
		return a;
	}	

}
