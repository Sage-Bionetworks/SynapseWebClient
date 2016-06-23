package org.sagebionetworks.web.client.widget.table.v2;

import org.gwtbootstrap3.client.shared.event.HiddenEvent;
import org.gwtbootstrap3.client.shared.event.HiddenHandler;
import org.gwtbootstrap3.client.shared.event.ShownEvent;
import org.gwtbootstrap3.client.shared.event.ShownHandler;
import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.table.modal.fileview.ScopeWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsWidget;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Basic implementation of the TableEntityWidgetView with zero business logic
 * 
 * @author John
 * 
 */
public class TableEntityWidgetViewImpl extends Composite implements
		TableEntityWidgetView {

	public interface Binder extends UiBinder<Widget, TableEntityWidgetViewImpl> {
	}

	@UiField
	Collapse schemaCollapse;
	@UiField
	Collapse scopeCollapse;

	@UiField
	PanelBody columnDetailsPanel;
	@UiField
	Div scopePanel;
	@UiField
	Alert tableMessage;
	@UiField
	SimplePanel queryInputPanel;
	@UiField
	SimplePanel queryResultsPanel;
	@UiField
	SimplePanel downloadResultsPanel;
	@UiField
	SimplePanel uploadResultsPanel;
	
	PortalGinInjector ginInjector;
	ColumnModelsWidget columnModelsWidget;
	ScopeWidget scopeWidget;
	Presenter presenter;

	@Inject
	public TableEntityWidgetViewImpl(final Binder uiBinder,
			PortalGinInjector ginInjector, ScopeWidget scopeWidget) {
		initWidget(uiBinder.createAndBindUi(this));
		this.ginInjector = ginInjector;
		this.columnModelsWidget = ginInjector.createNewColumnModelsWidget();
		this.columnDetailsPanel.add(this.columnModelsWidget.asWidget());
		this.scopeWidget = scopeWidget;
		this.scopePanel.add(scopeWidget.asWidget());
		scopePanel.getElement().setAttribute("highlight-box-title", "Scope");
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		this.schemaCollapse.addShownHandler(new ShownHandler() {
			@Override
			public void onShown(ShownEvent event) {
				presenter.onSchemaToggle(true);
				
			}
		});
		this.schemaCollapse.addHiddenHandler(new HiddenHandler() {
			@Override
			public void onHidden(HiddenEvent event) {
				presenter.onSchemaToggle(false);
			}
		});
		
		this.scopeCollapse.addShownHandler(new ShownHandler() {
			@Override
			public void onShown(ShownEvent event) {
				presenter.onScopeToggle(true);
				
			}
		});
		this.scopeCollapse.addHiddenHandler(new HiddenHandler() {
			@Override
			public void onHidden(HiddenEvent event) {
				presenter.onScopeToggle(false);
			}
		});
	}

	@Override
	public void configure(EntityBundle bundle, boolean isEditable) {
		this.columnModelsWidget.configure(bundle, isEditable, this.presenter);
		this.scopeWidget.configure(bundle, isEditable, this.presenter);
	}

	@Override
	public void setQueryResultsVisible(boolean visible) {
		this.queryResultsPanel.setVisible(visible);
	}

	@Override
	public void showTableMessage(AlertType type, String message) {
		this.tableMessage.setType(type);
		this.tableMessage.setText(message);
	}

	@Override
	public void setTableMessageVisible(boolean visible) {
		this.tableMessage.setVisible(visible);
	}

	@Override
	public void setQueryProgressVisible(boolean isVisible) {
		this.queryResultsPanel.setVisible(isVisible);
	}

	@Override
	public void setQueryResultsWidget(IsWidget queryResultsWidget) {
		this.queryResultsPanel.add(queryResultsWidget);
		
	}

	@Override
	public void setQueryInputWidget(IsWidget queryInputWidget) {
		this.queryInputPanel.add(queryInputWidget);
		
	}

	@Override
	public void setQueryInputVisible(boolean visible) {
		this.queryInputPanel.setVisible(visible);
	}

	@Override
	public void setDownloadTableQueryModalWidget(
			IsWidget downloadTableQueryModalWidget) {
		downloadResultsPanel.add(downloadTableQueryModalWidget);
	}

	@Override
	public void setUploadTableModalWidget(IsWidget uploadTableModalWidget) {
		this.uploadResultsPanel.add(uploadTableModalWidget);
	}

	@Override
	public void toggleSchema() {
		this.schemaCollapse.toggle();
	}
	
	@Override
	public void toggleScope() {
		this.scopeCollapse.toggle();
	}
	
	@Override
	public void setScopeVisible(boolean visible) {
		scopeCollapse.setVisible(visible);
	}
}
