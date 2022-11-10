package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.DataAccessApprovalTokenPlace;
import org.sagebionetworks.web.client.view.DataAccessApprovalTokenView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

public class DataAccessApprovalTokenPresenter
  extends AbstractActivity
  implements
    DataAccessApprovalTokenView.Presenter,
    Presenter<DataAccessApprovalTokenPlace> {

  public static final String EMPTY_TOKEN_ERROR_MESSAGE =
    "Please enter at least one data access approval token and try again.";
  private DataAccessApprovalTokenView view;
  private PopupUtilsView popupUtils;
  private SynapseAlert synAlert;
  private SynapseJavascriptClient jsClient;

  @Inject
  public DataAccessApprovalTokenPresenter(
    DataAccessApprovalTokenView view,
    PopupUtilsView popupUtils,
    SynapseAlert synAlert,
    SynapseJavascriptClient jsClient
  ) {
    this.view = view;
    this.popupUtils = popupUtils;
    this.synAlert = synAlert;
    this.jsClient = jsClient;
    view.setPresenter(this);
    view.setSynAlert(synAlert);
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    panel.setWidget(view.asWidget());
  }

  @Override
  public void setPlace(DataAccessApprovalTokenPlace place) {
    String token = place.toToken();
    view.refreshHeader();
    if (
      token != null &&
      !token.equalsIgnoreCase(ClientProperties.DEFAULT_PLACE_TOKEN)
    ) {
      view.setAccessApprovalToken(token);
    }
  }

  @Override
  public void onSubmitToken() {
    // call the service (see IT-834)
    String token = view.getAccessApprovalToken();
    if (token.trim().isEmpty()) {
      synAlert.showError(EMPTY_TOKEN_ERROR_MESSAGE);
      return;
    }
    synAlert.clear();
    view.setLoading(true);
    jsClient.submitNRGRDataAccessToken(
      token,
      new AsyncCallback<String>() {
        @Override
        public void onSuccess(String responseMessage) {
          view.setLoading(false);
          // on 201, pop up the message (note that this might be an error or success message, so do not clear the text area)
          popupUtils.showInfoDialog("", responseMessage, null);
        }

        @Override
        public void onFailure(Throwable caught) {
          view.setLoading(false);
          // on error, show in synAlert
          synAlert.handleException(caught);
        }
      }
    );
  }
}
