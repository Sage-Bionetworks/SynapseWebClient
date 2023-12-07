package org.sagebionetworks.web.client.widget.accessrequirements;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.List;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;

public class TeamSubjectsWidget implements IsWidget {

  DivView view;
  PortalGinInjector ginInjector;
  IsACTMemberAsyncHandler isACTMemberAsyncHandler;
  CallbackP<TeamSubjectWidget> subjectWidgetDeletedCallback;

  @Inject
  public TeamSubjectsWidget(
    DivView view,
    PortalGinInjector ginInjector,
    IsACTMemberAsyncHandler isACTMemberAsyncHandler
  ) {
    this.view = view;
    this.ginInjector = ginInjector;
    this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
    view.addStyleName("margin-bottom-5");
    view.setVisible(false);
  }

  @Override
  public Widget asWidget() {
    return this.view.asWidget();
  }

  public void configure(final List<RestrictableObjectDescriptor> subjects) {
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
    view.clear();
    for (RestrictableObjectDescriptor rod : subjects) {
      if (rod.getType().equals(RestrictableObjectType.TEAM)) {
        TeamSubjectWidget subjectWidget = ginInjector.getSubjectWidget();
        subjectWidget.configure(rod, subjectWidgetDeletedCallback);
        view.add(subjectWidget);
      }
    }
  }

  public void setDeleteCallback(
    final CallbackP<RestrictableObjectDescriptor> subjectDeletedCallback
  ) {
    subjectWidgetDeletedCallback =
      new CallbackP<TeamSubjectWidget>() {
        @Override
        public void invoke(TeamSubjectWidget subjectWidget) {
          view.remove(subjectWidget);
          subjectDeletedCallback.invoke(
            subjectWidget.getRestrictableObjectDescriptor()
          );
        }
      };
  }
}
