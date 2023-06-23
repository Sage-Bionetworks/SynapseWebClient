package org.sagebionetworks.web.client.widget.table.v2;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.QueryWrapperPlotNavProps.OnQueryCallback;
import org.sagebionetworks.web.client.jsinterop.QueryWrapperPlotNavProps.OnQueryResultBundleCallback;
import org.sagebionetworks.web.client.jsinterop.QueryWrapperPlotNavProps.OnViewSharingSettingsHandler;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.FullWidthAlert;
import org.sagebionetworks.web.client.widget.IconSvg;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;
import org.sagebionetworks.web.client.widget.table.explore.QueryWrapperPlotNav;
import org.sagebionetworks.web.client.widget.table.explore.StandaloneQueryWrapper;
import org.sagebionetworks.web.client.widget.table.modal.fileview.EntityViewScopeWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.SubmissionViewScopeWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsWidget;

/**
 * Basic implementation of the TableEntityWidgetView with zero business logic
 *
 * @author John
 *
 */
public class TableEntityWidgetViewImpl
  extends Composite
  implements TableEntityWidgetView {

  public interface Binder extends UiBinder<Widget, TableEntityWidgetViewImpl> {}

  @UiField
  Collapse schemaCollapse;

  @UiField
  IconSvg schemaCollapseCloseButton;

  @UiField
  Collapse scopeCollapse;

  @UiField
  Button scopeCollapseCloseButton;

  @UiField
  SimplePanel columnDetailsPanel;

  @UiField
  Div scopePanel;

  @UiField
  FullWidthAlert tableMessage;

  @UiField
  Div modalContainer;

  @UiField
  Div plotNavContainer;

  @UiField
  Div addToDownloadListContainer;

  @UiField
  ReactComponentDiv itemsEditorContainer;

  PortalGinInjector ginInjector;
  ColumnModelsWidget columnModelsWidget;
  EntityViewScopeWidget scopeWidget;
  SubmissionViewScopeWidget submissionViewScopeWidget;
  TableEntityWidgetView.Presenter presenter;
  SynapseReactClientFullContextPropsProvider propsProvider;

  @Inject
  public TableEntityWidgetViewImpl(
    final Binder uiBinder,
    PortalGinInjector ginInjector,
    EntityViewScopeWidget scopeWidget,
    SubmissionViewScopeWidget submissionViewScopeWidget,
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    initWidget(uiBinder.createAndBindUi(this));
    this.ginInjector = ginInjector;
    this.columnModelsWidget = ginInjector.createNewColumnModelsWidget();
    this.columnDetailsPanel.setWidget(this.columnModelsWidget.asWidget());
    this.scopeWidget = scopeWidget;
    this.submissionViewScopeWidget = submissionViewScopeWidget;
    this.scopePanel.add(scopeWidget.asWidget());
    this.scopePanel.add(submissionViewScopeWidget.asWidget());
    this.propsProvider = propsProvider;
    schemaCollapseCloseButton.addClickHandler(event ->
      this.presenter.toggleSchemaCollapse()
    );
    scopeCollapseCloseButton.addClickHandler(event ->
      this.presenter.toggleScopeCollapse()
    );
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
  public void showTableMessage(AlertType type, String message) {
    this.tableMessage.setAlertType(type);
    this.tableMessage.setMessage(message);
  }

  @Override
  public void setTableMessageVisible(boolean visible) {
    this.tableMessage.setVisible(visible);
  }

  @Override
  public void addModalWidget(IsWidget w) {
    modalContainer.add(w);
  }

  @Override
  public void setSchemaVisible(boolean visible) {
    if (visible) {
      schemaCollapse.show();
    } else {
      schemaCollapse.hide();
    }
  }

  @Override
  public boolean isSchemaVisible() {
    return schemaCollapse.isShown();
  }

  @Override
  public void setScopeVisible(boolean visible) {
    if (visible) {
      scopeCollapse.show();
    } else {
      scopeCollapse.hide();
    }
  }

  @Override
  public boolean isScopeVisible() {
    return scopeCollapse.isShown();
  }

  @Override
  public void showErrorMessage(String message) {
    DisplayUtils.showErrorMessage(message);
  }

  @Override
  public void showConfirmDialog(
    String title,
    String message,
    Callback yesCallback
  ) {
    DisplayUtils.showConfirmDialog(title, message, yesCallback);
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
      ReactNode component = React.createElementWithSynapseContext(
        SRC.SynapseComponents.DatasetItemsEditor,
        this.presenter.getItemsEditorProps(),
        propsProvider.getJsInteropContextProps()
      );
      itemsEditorContainer.render(component);
    } else {
      itemsEditorContainer.clear();
    }
  }

  @Override
  public void configureTableOnly(String sql) {
    StandaloneQueryWrapper widget = new StandaloneQueryWrapper(
      propsProvider,
      sql
    );
    plotNavContainer.clear();
    plotNavContainer.add(widget);
  }

  @Override
  public void configureQueryWrapperPlotNav(
    String sql,
    String initQueryJson,
    OnQueryCallback onQueryChange,
    OnQueryResultBundleCallback onQueryResultBundleChange,
    OnViewSharingSettingsHandler onViewSharingSettingsHandler,
    boolean hideSqlEditorControl
  ) {
    QueryWrapperPlotNav plotNav = new QueryWrapperPlotNav(
      propsProvider,
      sql,
      initQueryJson,
      onQueryChange,
      onQueryResultBundleChange,
      onViewSharingSettingsHandler,
      hideSqlEditorControl
    );
    plotNavContainer.clear();
    plotNavContainer.add(plotNav);
  }

  @Override
  public void setQueryWrapperPlotNavVisible(boolean visible) {
    plotNavContainer.setVisible(visible);
  }
}
