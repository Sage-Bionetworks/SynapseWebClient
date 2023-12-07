package org.sagebionetworks.web.client.widget.accessrequirements;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;

public class EntitySubjectsWidget
  implements EntitySubjectsWidgetView.Presenter, IsWidget {

  EntitySubjectsWidgetView view;
  IsACTMemberAsyncHandler isACTMemberAsyncHandler;

  CallbackP<List<RestrictableObjectDescriptor>> onUpdate;

  @Inject
  public EntitySubjectsWidget(
    EntitySubjectsWidgetView view,
    PortalGinInjector ginInjector,
    IsACTMemberAsyncHandler isACTMemberAsyncHandler
  ) {
    this.view = view;
    this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
    view.setVisible(false);
  }

  @Override
  public Widget asWidget() {
    return this.view.asWidget();
  }

  public void configure(
    final List<RestrictableObjectDescriptor> subjects,
    boolean isEditable,
    CallbackP<List<RestrictableObjectDescriptor>> onUpdate
  ) {
    this.onUpdate = onUpdate;
    isACTMemberAsyncHandler.isACTActionAvailable(
      new CallbackP<Boolean>() {
        @Override
        public void invoke(Boolean isACT) {
          view.setVisible(isACT);
          if (isACT) {
            configureAfterACTCheck(subjects, isEditable);
          }
        }
      }
    );
  }

  private void configureAfterACTCheck(
    List<RestrictableObjectDescriptor> subjects,
    boolean isEditable
  ) {
    ReferenceList entityReferences = new ReferenceList();
    List<Reference> referenceList = new ArrayList<>();
    for (RestrictableObjectDescriptor rod : subjects) {
      if (rod.getType().equals(RestrictableObjectType.ENTITY)) {
        Reference ref = new Reference();
        ref.setTargetId(rod.getId());
        referenceList.add(ref);
      }
    }
    entityReferences.setReferences(referenceList);
    view.showEntityHeadersTable(entityReferences, isEditable);
  }

  @Override
  public void onChange(ReferenceList updatedEntityReferences) {
    // translate ReferenceList to RestrictableObjectDescriptors
    List<RestrictableObjectDescriptor> newSubjects = new ArrayList<>();
    for (Reference ref : updatedEntityReferences.getReferences()) {
      RestrictableObjectDescriptor newRod = new RestrictableObjectDescriptor();
      newRod.setId(ref.getTargetId());
      newRod.setType(RestrictableObjectType.ENTITY);
    }
    onUpdate.invoke(newSubjects);
  }
}
