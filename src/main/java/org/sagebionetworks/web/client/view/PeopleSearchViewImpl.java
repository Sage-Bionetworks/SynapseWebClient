package org.sagebionetworks.web.client.view;

import java.util.List;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.place.PeopleSearch;
import org.sagebionetworks.web.client.presenter.PeopleSearchPresenter;
import org.sagebionetworks.web.client.utils.UnorderedListPanel;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;
import org.sagebionetworks.web.client.widget.user.UserGroupListWidget;

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
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PeopleSearchViewImpl extends Composite implements PeopleSearchView {
	public interface PeopleSearchViewImplUiBinder extends UiBinder<Widget, PeopleSearchViewImpl> {}
	
	private static final int MAX_PAGES_IN_PAGINATION = 10;
	
	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	@UiField
	SimplePanel searchBoxPanel;
	@UiField
	SimplePanel peopleListPanel;
	@UiField
	SimplePanel paginationPanel;
	
	private Header headerWidget;
	private Footer footerWidget;
	private SageImageBundle sageImageBundle;
	private SynapseJSNIUtils synapseJsniUtils;
	private UserGroupListWidget userGroupListWidget;
	
	private Presenter presenter;
	private TextBox searchField;
	private Button searchButton;
	
	
	@Inject
	public PeopleSearchViewImpl(PeopleSearchViewImplUiBinder binder,
			Header headerWidget, 
			Footer footerWidget, 
			SageImageBundle sageImageBundle,
			SynapseJSNIUtils synapseJsniUtils,
			UserGroupListWidget userGroupListWidget) {
		initWidget(binder.createAndBindUi(this));
		this.sageImageBundle = sageImageBundle;
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.synapseJsniUtils = synapseJsniUtils;
		this.userGroupListWidget = userGroupListWidget;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
	}
	
//	@Override
//	public Widget asWidget() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public void showLoading() {
		peopleListPanel.clear();
		peopleListPanel.add(DisplayUtils.getLoadingWidget(sageImageBundle));
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
	public void clear() {
		userGroupListWidget.clear();
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
	public void configure(List<UserGroupHeader> users, String searchTerm) {
		userGroupListWidget.clear();
		userGroupListWidget.configure(users, true);
		peopleListPanel.setWidget(userGroupListWidget.asWidget());
		searchField.setValue(searchTerm);
		int start = presenter.getOffset();
		String pageTitleStartNumber = start > 0 ? " (from result " + (start+1) + ")" : "";
		String pageTitleSearchTerm = searchTerm != null && searchTerm.length() > 0 ? " '"+searchTerm + "' " : "";
		synapseJsniUtils.setPageTitle("People Search" + pageTitleSearchTerm + pageTitleStartNumber);
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
				presenter.goTo(new PeopleSearch(searchField.getValue()));
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
		
		List<PaginationEntry> entries = presenter.getPaginationEntries(PeopleSearchPresenter.SEARCH_PEOPLE_LIMIT, MAX_PAGES_IN_PAGINATION);
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
		a.setHref(DisplayUtils.getPeopleSearchHistoryToken(searchTerm, newStart));
		return a;
	}

}
