package org.sagebionetworks.web.client.widget.table.v2;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactDOM;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.FullWidthAlert;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;
import org.sagebionetworks.web.client.widget.table.modal.fileview.EntityViewScopeWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.SubmissionViewScopeWidget;
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
public class TableEntityWidgetViewImpl extends Composite implements TableEntityWidgetView {

	public interface Binder extends UiBinder<Widget, TableEntityWidgetViewImpl> {
	}

	@UiField
	Div schemaCollapse;
	@UiField
	Div scopeCollapse;
	@UiField
	Div tableToolbar;

	@UiField
	SimplePanel columnDetailsPanel;
	@UiField
	Div scopePanel;
	@UiField
	FullWidthAlert tableMessage;
	@UiField
	SimplePanel queryInputPanel;
	@UiField
	SimplePanel queryResultsPanel;
	@UiField
	Div modalContainer;
	@UiField
	Div addToDownloadListContainer;
	@UiField
	ReactComponentDiv itemsEditorContainer;
	PortalGinInjector ginInjector;
	ColumnModelsWidget columnModelsWidget;
	EntityViewScopeWidget scopeWidget;
	SubmissionViewScopeWidget submissionViewScopeWidget;
	TableEntityWidgetView.Presenter presenter;
	SynapseContextPropsProvider propsProvider;

	@Inject
	public TableEntityWidgetViewImpl(final Binder uiBinder, PortalGinInjector ginInjector, EntityViewScopeWidget scopeWidget, SubmissionViewScopeWidget submissionViewScopeWidget, SynapseContextPropsProvider propsProvider) {
		initWidget(uiBinder.createAndBindUi(this));
		this.ginInjector = ginInjector;
		this.columnModelsWidget = ginInjector.createNewColumnModelsWidget();
		this.columnDetailsPanel.setWidget(this.columnModelsWidget.asWidget());
		this.scopeWidget = scopeWidget;
		this.submissionViewScopeWidget = submissionViewScopeWidget;
		this.scopePanel.add(scopeWidget.asWidget());
		this.scopePanel.add(submissionViewScopeWidget.asWidget());
		this.propsProvider = propsProvider;
		scopePanel.getElement().setAttribute("highlight-box-title", "Scope");
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void configure(EntityBundle bundle, boolean isEditable) {
		this.columnModelsWidget.configure(bundle, isEditable);
		this.scopeWidget.configure(bundle, isEditable);
		this.submissionViewScopeWidget.configure(bundle, isEditable);
	}

	@Override
	public void setQueryResultsVisible(boolean visible) {
		this.queryResultsPanel.setVisible(visible);
	}

	@Override
	public void showTableMessage(AlertType type, String message) {
		this.tableMessage.setAlertType(type);
		this.tableMessage.setMessage(message);
	}

	@Override
	public void setTableMessageVisible(boolean visible) {
		this.tableMessage.setVisible(visible);
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
	public void addModalWidget(IsWidget w) {
		modalContainer.add(w);
	}

	@Override
	public void setSchemaVisible(boolean visible) {
		schemaCollapse.setVisible(visible);
	}

	@Override
	public boolean isSchemaVisible() {
		return schemaCollapse.isVisible();
	}

	@Override
	public void setScopeVisible(boolean visible) {
		scopeCollapse.setVisible(visible);
	}

	@Override
	public boolean isScopeVisible() {
		return scopeCollapse.isVisible();
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showConfirmDialog(String title, String message, Callback yesCallback) {
		DisplayUtils.showConfirmDialog(title, message, yesCallback);
	}

	@Override
	public void setTableToolbarVisible(boolean visible) {
		tableToolbar.setVisible(visible);
	}

	@Override
	public void setAddToDownloadList(IsWidget w) {
		addToDownloadListContainer.clear();
		addToDownloadListContainer.add(w);
	}

	@Override
	public void setItemsEditorVisible(boolean visible) {
		itemsEditorContainer.setVisible(visible);
		if (visible) {
			ReactDOM.render(
					React.createElementWithSynapseContext(
							SRC.SynapseComponents.DatasetItemsEditor,
							this.presenter.getItemsEditorProps(),
							propsProvider.getJsInteropContextProps()
					),
					itemsEditorContainer.getElement()
			);
		} else {
			itemsEditorContainer.clear();
		}
	}

	@Override
	public boolean isItemsEditorVisible() {
		return itemsEditorContainer.isVisible();
	}
}
