package org.sagebionetworks.web.client.widget.table;

import java.util.List;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ListGroup;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.sagebionetworks.repo.model.entity.query.EntityFieldName;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.pagination.PaginationWidget;
import org.sagebionetworks.web.client.widget.user.UserBadge;

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
	Button addTable;
	@UiField
	Button addFileView;
	
	@UiField
	Button uploadTable;
	@UiField
	SimplePanel createTableModalPanel;
	@UiField
	SimplePanel uploadTableModalPanel;
	@UiField
	SimplePanel fileViewWizardContainer;
	@UiField
	SimplePanel paginationPanel;
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
	
	HTMLPanel panel;
	Presenter presenter;
	PortalGinInjector ginInjector;
	
	@Inject
	public TableListWidgetViewImpl(Binder binder, PortalGinInjector ginInjector) {
		this.panel = binder.createAndBindUi(this);
		this.ginInjector = ginInjector;
		HTML html = new HTML("<i class=\"fa fa-plus\" ></i>&nbsp;Add File View" + DisplayConstants.BETA_BADGE_HTML);
		addFileView.add(html);
		createdOnDesc.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sortButton.setText(createdOnDesc.getText());
				presenter.onSort(EntityFieldName.createdOn.name(), SortDirection.DESC);
			}
		});
		createdOnAsc.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sortButton.setText(createdOnAsc.getText());
				presenter.onSort(EntityFieldName.createdOn.name(), SortDirection.ASC);
			}
		});
		nameDesc.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sortButton.setText(nameDesc.getText());
				presenter.onSort(EntityFieldName.name.name(), SortDirection.DESC);
			}
		});

		nameAsc.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sortButton.setText(nameAsc.getText());
				presenter.onSort(EntityFieldName.name.name(), SortDirection.ASC);
			}
		});
	}

	@Override
	public void configure(List<EntityQueryResult> tables) {
		tablesList.clear();
		for(final EntityQueryResult header: tables){
			UserBadge createdByUserBadge = ginInjector.getUserBadgeWidget();
			createdByUserBadge.configure(header.getCreatedByPrincipalId().toString());
			UserBadge modifiedByUserBadge = ginInjector.getUserBadgeWidget();
			modifiedByUserBadge.configure(header.getModifiedByPrincipalId().toString());
			tablesList.add(new TableEntityListGroupItem(HeadingSize.H4, header, createdByUserBadge, modifiedByUserBadge, new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.onTableClicked(header.getId());
				}
			}));
		}
	}
	
	@Override
	public void resetSortUI() {
		sortButton.setText(createdOnDesc.getText());	
	}
	
	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		this.addTable.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onAddTable();
			}
		});
		this.addFileView.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onAddFileView();
			}
		});
		this.uploadTable.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onUploadTable();
			}
		});
	}
	
	@Override
	public void showLoading() {
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
	public void setAddTableVisible(boolean visibile) {
		this.addTable.setVisible(visibile);
	}
	
	@Override
	public void setAddFileViewVisible(boolean visible) {
		addFileView.setVisible(visible);
	}
	
	@Override
	public void setUploadTableVisible(boolean visibile) {
		this.uploadTable.setVisible(visibile);
	}

	@Override
	public void addCreateTableModal(IsWidget createTableModal) {
		this.createTableModalPanel.add(createTableModal);
	}

	@Override
	public void addPaginationWidget(PaginationWidget paginationWidget) {
		paginationPanel.add(paginationWidget);
	}

	@Override
	public void showPaginationVisible(boolean visible) {
		paginationPanel.setVisible(visible);
	}

	@Override
	public void addUploadTableModal(IsWidget uploadTableModalWidget) {
		this.uploadTableModalPanel.add(uploadTableModalWidget);
	}
	
	@Override
	public void addWizard(IsWidget wizard) {
		fileViewWizardContainer.clear();
		fileViewWizardContainer.add(wizard);
	}
	

}
