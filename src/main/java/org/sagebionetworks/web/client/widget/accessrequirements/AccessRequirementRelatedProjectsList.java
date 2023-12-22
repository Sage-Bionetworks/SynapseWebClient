package org.sagebionetworks.web.client.widget.accessrequirements;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.AccessRequirementRelatedProjectsListProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;

public class AccessRequirementRelatedProjectsList implements IsWidget {

  ReactComponentDiv container;
  public IsACTMemberAsyncHandler isACTMemberAsyncHandler;

  @Inject
  public AccessRequirementRelatedProjectsList(
    IsACTMemberAsyncHandler isACTMemberAsyncHandler
  ) {
    container = new ReactComponentDiv();
    this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
    container.setVisible(false);
  }

  public void configure(String accessRequirementId) {
    AccessRequirementRelatedProjectsListProps props =
      AccessRequirementRelatedProjectsListProps.create(accessRequirementId);
    ReactNode component = React.createElementWithThemeContext(
      SRC.SynapseComponents.AccessRequirementRelatedProjectsList,
      props
    );
    container.render(component);
    showIfACTMember();
  }

  private void showIfACTMember() {
    isACTMemberAsyncHandler.isACTActionAvailable(
      new CallbackP<Boolean>() {
        @Override
        public void invoke(Boolean isACTMember) {
          container.setVisible(isACTMember);
        }
      }
    );
  }

  @Override
  public Widget asWidget() {
    return container;
  }
}
