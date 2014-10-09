package org.sagebionetworks.web.client.view;

import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.PeopleSearch;
import org.sagebionetworks.web.client.presenter.TeamSearchPresenter;
import org.sagebionetworks.web.client.utils.UnorderedListPanel;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;
import org.sagebionetworks.web.client.widget.user.UserGroupListWidget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
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
	@UiField
	Button searchButton;
	@UiField
	TextBox searchField;
	
	private Header headerWidget;
	private Footer footerWidget;
	private SageImageBundle sageImageBundle;
	private SynapseJSNIUtils synapseJsniUtils;
	private UserGroupListWidget userGroupListWidget;
	
	private Presenter presenter;
	
	
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
		configureSearchBox();
	}

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
		searchButton.addClickHandler(new ClickHandler() {				
			@Override
			public void onClick(ClickEvent event) {					
				presenter.goTo(new PeopleSearch(searchField.getValue()));
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

	private void createPagination(String searchTerm) {
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
		
		paginationPanel.clear();
		if (entries.size() > 1)
			paginationPanel.add(ul);
	}
	
	private Anchor createPaginationAnchor(String anchorName, String searchTerm, final int newStart) {
		Anchor a = new Anchor();
		a.setHTML(anchorName);
		a.setHref(DisplayUtils.getPeopleSearchHistoryToken(searchTerm, newStart));
		return a;
	}

}
