package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.AccessRequirementPlace;
import org.sagebionetworks.web.client.place.AccessRequirementsPlace;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.view.PlaceView;
import org.sagebionetworks.web.client.widget.accessrequirements.AccessRequirementWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

public class AccessRequirementPresenter
  extends AbstractActivity
  implements Presenter<AccessRequirementPlace> {

  private AccessRequirementPlace place;
  private PlaceView view;
  private AccessRequirementWidget arWidget;
  private String requirementId;
  private SynapseJavascriptClient jsClient;
  private SynapseAlert synAlert;

  @Inject
  public AccessRequirementPresenter(
    PlaceView view,
    AccessRequirementWidget arWidget,
    DivView arDiv,
    SynapseJavascriptClient jsClient,
    SynapseAlert synAlert
  ) {
    this.view = view;
    this.arWidget = arWidget;
    this.jsClient = jsClient;
    this.synAlert = synAlert;
    arDiv.addStyleName("markdown");
    arDiv.add(arWidget.asWidget());
    view.add(arDiv.asWidget());
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    // Install the view
    panel.setWidget(view);
  }

  @Override
  public void setPlace(AccessRequirementPlace place) {
    this.place = place;
    view.initHeaderAndFooter();
    synAlert.clear();
    requirementId = place.getParam(AccessRequirementPlace.AR_ID_PARAM);
    String id = place.getParam(AccessRequirementsPlace.ID_PARAM);
    String typeString = place.getParam(AccessRequirementsPlace.TYPE_PARAM);

    jsClient.getAccessRequirement(
      requirementId,
      new AsyncCallback<AccessRequirement>() {
        @Override
        public void onSuccess(AccessRequirement result) {
          String titleInfo = DisplayUtils.isDefined(result.getName())
            ? ": " + result.getName()
            : "";
          view.addTitle("Access Requirement" + titleInfo);

          // Note: configuring the Access Requirement widget without a target subject will result in notifications sent to the user will not have the context (Project/Folder/File associated with the restriction).
          if (id != null && typeString != null) {
            RestrictableObjectDescriptor targetSubject =
              new RestrictableObjectDescriptor();
            RestrictableObjectType type = RestrictableObjectType.valueOf(
              typeString.toUpperCase()
            );
            targetSubject.setType(type);
            targetSubject.setId(id);

            arWidget.configure(requirementId, targetSubject);
          } else {
            // SWC-6700: No subject specified, pick a random one since some code assumes a subject has been specified.
            // configure using the first subject, if available
            RestrictableObjectDescriptor firstSubject = null;
            if (result.getSubjectIds().size() > 0) {
              firstSubject = result.getSubjectIds().get(0);
            }
            arWidget.configure(requirementId, firstSubject);
          }
        }

        @Override
        public void onFailure(Throwable caught) {
          synAlert.handleException(caught);
        }
      }
    );
  }

  public AccessRequirementPlace getPlace() {
    return place;
  }

  @Override
  public String mayStop() {
    return null;
  }
}
