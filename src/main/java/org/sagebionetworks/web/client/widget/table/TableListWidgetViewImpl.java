package org.sagebionetworks.web.client.widget.table;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ListGroup;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.entity.Direction;
import org.sagebionetworks.repo.model.entity.SortBy;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.LoadingSpinner;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
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
	AnchorListItem createdOnDesc;
	@UiField
	AnchorListItem createdOnAsc;
	@UiField
	AnchorListItem nameAsc;
	@UiField
	AnchorListItem nameDesc;
	@UiField
	Button sortButton;
	@UiField
	Div synAlertContainer;
	
	HTMLPanel panel;
	Presenter presenter;
	PortalGinInjector ginInjector;
	@UiField
	LoadingSpinner loadingUI;
	
	@Inject
	public TableListWidgetViewImpl(Binder binder, PortalGinInjector ginInjector) {
		this.panel = binder.createAndBindUi(this);
		this.ginInjector = ginInjector;
		createdOnDesc.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sortButton.setText(createdOnDesc.getText());
				presenter.onSort(SortBy.CREATED_ON, Direction.DESC);
			}
		});
		createdOnAsc.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sortButton.setText(createdOnAsc.getText());
				presenter.onSort(SortBy.CREATED_ON, Direction.ASC);
			}
		});
		nameDesc.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sortButton.setText(nameDesc.getText());
				presenter.onSort(SortBy.NAME, Direction.DESC);
			}
		});

		nameAsc.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sortButton.setText(nameAsc.getText());
				presenter.onSort(SortBy.NAME, Direction.ASC);
			}
		});
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
	public void resetSortUI() {
		sortButton.setText(createdOnDesc.getText());	
	}
	
	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
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
