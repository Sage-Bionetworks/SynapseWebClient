package org.sagebionetworks.web.client.widget.accessrequirements;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.AccessRequirementRelatedProjectsListProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.ReactComponent;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;

public class AccessRequirementRelatedProjectsList implements IsWidget {

  ReactComponent container;
  public IsACTMemberAsyncHandler isACTMemberAsyncHandler;
  public SynapseReactClientFullContextPropsProvider propsProvider;

  @Inject
  public AccessRequirementRelatedProjectsList(
    IsACTMemberAsyncHandler isACTMemberAsyncHandler,
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    container = new ReactComponent();
    this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
    this.propsProvider = propsProvider;
    container.setVisible(false);
  }

  public void configure(String accessRequirementId) {
    AccessRequirementRelatedProjectsListProps props =
      AccessRequirementRelatedProjectsListProps.create(accessRequirementId);
    ReactNode component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.AccessRequirementRelatedProjectsList,
      props,
      propsProvider.getJsInteropContextProps()
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
