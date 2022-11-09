package org.sagebionetworks.web.client.view;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.ReviewerDashboardProps;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.presenter.DataAccessManagementPresenter;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;
import org.sagebionetworks.web.client.widget.entity.act.RejectDataAccessRequestModal;
import org.sagebionetworks.web.client.widget.header.Header;

public class DataAccessManagementViewImpl implements DataAccessManagementView {

  public interface DataAccessManagementViewImplUiBinder
    extends UiBinder<Widget, DataAccessManagementViewImpl> {}

  private DataAccessManagementPresenter presenter;

  RejectDataAccessRequestModal rejectModal;

  private SynapseContextPropsProvider propsProvider;
  private Header headerWidget;

  @UiField
  ReactComponentDiv reactComponent;

  Widget widget;

  @Inject
  public DataAccessManagementViewImpl(
    DataAccessManagementViewImplUiBinder binder,
    Header headerWidget,
    SynapseContextPropsProvider propsProvider,
    RejectDataAccessRequestModal rejectModal
  ) {
    widget = binder.createAndBindUi(this);
    this.headerWidget = headerWidget;
    this.propsProvider = propsProvider;
    this.rejectModal = rejectModal;
    headerWidget.configure();
  }

  @Override
  public void setPresenter(DataAccessManagementPresenter presenter) {
    this.presenter = presenter;
    headerWidget.configure();
    headerWidget.refresh();
    Window.scrollTo(0, 0);
    render();
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void render() {
    ReviewerDashboardProps.OnRejectSubmissionClicked onRejectCallback = onReject ->
      rejectModal.show(reason -> onReject.onReject(reason));
    ReviewerDashboardProps props = ReviewerDashboardProps.create(
      onRejectCallback
    );

    ReactNode node = React.createElementWithSynapseContext(
      SRC.SynapseComponents.ReviewerDashboard,
      props,
      propsProvider.getJsInteropContextProps()
    );
    reactComponent.render(node);
  }
}
