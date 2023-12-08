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
  String currentEntityIDsValue = "";

  @Inject
  public EntitySubjectsWidget(
    EntitySubjectsWidgetView view,
    PortalGinInjector ginInjector,
    IsACTMemberAsyncHandler isACTMemberAsyncHandler
  ) {
    this.view = view;
    this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
    view.setVisible(false);
    view.setPresenter(this);
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  public void configure(final List<RestrictableObjectDescriptor> subjects) {
    configure(subjects, null);
  }

  public void configure(
    final List<RestrictableObjectDescriptor> subjects,
    CallbackP<List<RestrictableObjectDescriptor>> onUpdate
  ) {
    this.onUpdate = onUpdate;
    isACTMemberAsyncHandler.isACTActionAvailable(
      new CallbackP<Boolean>() {
        @Override
        public void invoke(Boolean isACT) {
          view.setVisible(isACT);
          if (isACT) {
            configureAfterACTCheck(subjects);
          }
        }
      }
    );
  }

  private void configureAfterACTCheck(
    List<RestrictableObjectDescriptor> subjects
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
    boolean isEditable = onUpdate != null;
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
      newSubjects.add(newRod);
    }
    onUpdate.invoke(newSubjects);
  }

  @Override
  public void onChangeEntityIDsValue(String newEntityIDsValue) {
    this.currentEntityIDsValue = newEntityIDsValue;
  }

  public boolean isEntityIDsTextboxEmpty() {
    return currentEntityIDsValue.trim().isEmpty();
  }
}
