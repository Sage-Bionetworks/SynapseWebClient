package org.sagebionetworks.web.client.widget.accessrequirements.submission;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.dataaccess.OpenSubmission;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.accessrequirements.ManagedACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;

public class OpenSubmissionWidget
  implements OpenSubmissionWidgetView.Presenter, IsWidget {

  private OpenSubmissionWidgetView view;
  private ManagedACTAccessRequirementWidget accessRequirementWidget;
  private SynapseJavascriptClient jsClient;
  private SynapseAlert synAlert;
  private LazyLoadHelper lazyLoadHelper;
  private String accessRequirementId;

  @Inject
  public OpenSubmissionWidget(
    OpenSubmissionWidgetView view,
    ManagedACTAccessRequirementWidget accessRequirementWidget,
    SynapseJavascriptClient jsClient,
    SynapseAlert synAlert,
    LazyLoadHelper lazyLoadHelper
  ) {
    this.view = view;
    this.accessRequirementWidget = accessRequirementWidget;
    this.jsClient = jsClient;
    this.synAlert = synAlert;
    this.lazyLoadHelper = lazyLoadHelper;
    view.setSynAlert(synAlert);
    view.setACTAccessRequirementWidget(accessRequirementWidget);
    accessRequirementWidget.setReviewAccessRequestsVisible(true);
    view.setPresenter(this);
    Callback loadDataCallback = new Callback() {
      @Override
      public void invoke() {
        loadAccessRequirement();
      }
    };

    lazyLoadHelper.configure(loadDataCallback, view);
  }

  public void loadAccessRequirement() {
    synAlert.clear();
    jsClient.getAccessRequirement(
      accessRequirementId,
      new AsyncCallback<AccessRequirement>() {
        @Override
        public void onFailure(Throwable caught) {
          synAlert.handleException(caught);
        }

        @Override
        public void onSuccess(AccessRequirement ar) {
          if (ar instanceof ManagedACTAccessRequirement) {
            Callback refreshCallback = new Callback() {
              @Override
              public void invoke() {
                loadAccessRequirement();
              }
            };
            accessRequirementWidget.setRequirement(
              (ManagedACTAccessRequirement) ar,
              refreshCallback
            );
          } else {
            onFailure(
              new IllegalStateException(
                "Expected an ManagedACTAccessRequirement, but get " +
                ar.getConcreteType()
              )
            );
          }
        }
      }
    );
  }

  public void configure(OpenSubmission openSubmission) {
    view.setNumberOfSubmissions(
      openSubmission.getNumberOfSubmittedSubmission()
    );
    accessRequirementId = openSubmission.getAccessRequirementId();
    lazyLoadHelper.setIsConfigured();
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }
}
