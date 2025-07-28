package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.web.client.place.AccessRequirementsPlace;
import org.sagebionetworks.web.client.view.AccessRequirementsSRCView;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;

//Uses a DivView.  Configures and uses AccessRequirementsForACT if ACT, or AccessRequirementsSRCView if not ACT.
public class AccessRequirementsPresenter
  extends AbstractActivity
  implements Presenter<AccessRequirementsPlace> {

  private AccessRequirementsPlace place;
  DivView view;
  IsACTMemberAsyncHandler isACTMemberAsyncHandler;
  AccessRequirementsForACT accessRequirementsForACT;
  AccessRequirementsSRCView accessRequirementsSRCView;

  @Inject
  public AccessRequirementsPresenter(
    DivView view,
    IsACTMemberAsyncHandler isACTMemberAsyncHandler,
    AccessRequirementsForACT accessRequirementsForACT,
    AccessRequirementsSRCView accessRequirementsSRCView
  ) {
    this.view = view;
    this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
    this.accessRequirementsForACT = accessRequirementsForACT;
    this.accessRequirementsSRCView = accessRequirementsSRCView;
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    panel.setWidget(view);
  }

  @Override
  public void setPlace(AccessRequirementsPlace place) {
    this.place = place;
    view.clear();
    String id = place.getParam(AccessRequirementsPlace.ID_PARAM);
    String typeString = place.getParam(AccessRequirementsPlace.TYPE_PARAM);
    RestrictableObjectType type = RestrictableObjectType.valueOf(
      typeString.toUpperCase()
    );
    RestrictableObjectDescriptor subject = new RestrictableObjectDescriptor();
    subject.setType(type);
    subject.setId(id);

    isACTMemberAsyncHandler.isACTMember(isACT -> {
      if (isACT) {
        view.add(accessRequirementsForACT.asWidget());
        accessRequirementsForACT.configure(subject);
      } else {
        view.add(accessRequirementsSRCView.asWidget());
        accessRequirementsSRCView.configure(subject);
      }
    });
  }

  public AccessRequirementsPlace getPlace() {
    return place;
  }

  @Override
  public String mayStop() {
    return null;
  }
}
