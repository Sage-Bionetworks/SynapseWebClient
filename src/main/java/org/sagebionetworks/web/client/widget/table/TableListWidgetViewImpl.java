package org.sagebionetworks.web.client.widget.table;

import org.gwtbootstrap3.client.ui.ListGroup;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.entity.Direction;
import org.sagebionetworks.repo.model.entity.SortBy;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.LoadingSpinner;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * View of a widget that lists table entities.  
 * 
 * @author jmhill
 *
 */
public class TableListWidgetViewImpl implements TableListWidgetView {
	
	public interface Binder extends UiBinder<HTMLPanel, TableListWidgetViewImpl> {}
	
	@UiField
	ListGroup tablesList;
	@UiField
	Div loadMoreWidgetContainer;
	@UiField
	Div sortButtonContainer;
	@UiField
	Div synAlertContainer;
	
	HTMLPanel panel;
	Presenter presenter;
	PortalGinInjector ginInjector;
	@UiField
	LoadingSpinner loadingUI;
	SortEntityChildrenDropdownButton sortEntityChildrenDropdownButton;
	@Inject
	public TableListWidgetViewImpl(Binder binder, 
			PortalGinInjector ginInjector, 
			SortEntityChildrenDropdownButton sortEntityChildrenDropdownButton) {
		this.panel = binder.createAndBindUi(this);
		this.ginInjector = ginInjector;
		this.sortEntityChildrenDropdownButton = sortEntityChildrenDropdownButton;
		sortButtonContainer.add(sortEntityChildrenDropdownButton);
	}

	@Override
	public void addTableListItem(final EntityHeader header) {
		tablesList.add(new TableEntityListGroupItem(HeadingSize.H4, header, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onTableClicked(header.getId());
			}
		}));
	}
	
	@Override
	public void clearTableWidgets() {
		tablesList.clear();
	}
	
	@Override
	public void setLoadMoreWidget(IsWidget w) {
		loadMoreWidgetContainer.clear();
		loadMoreWidgetContainer.add(w);
	}
	
	@Override
	public void setSortUI(SortBy sortBy, Direction dir) {
		sortEntityChildrenDropdownButton.setSortUI(sortBy, dir);
	}
	
	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		sortEntityChildrenDropdownButton.setListener(presenter);
	}
	
	@Override
	public void showLoading() {
		loadingUI.setVisible(true);
	}
	@Override
	public void hideLoading() {
		loadingUI.setVisible(false);	
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
	public Widget asWidget() {
		return panel;
	}

	@Override
	public void clear() {
		tablesList.clear();
	}
	
	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
}
