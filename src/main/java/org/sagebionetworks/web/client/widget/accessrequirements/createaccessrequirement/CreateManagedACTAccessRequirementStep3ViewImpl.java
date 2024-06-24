package org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.AccessRequirementAclEditorHandler;
import org.sagebionetworks.web.client.jsinterop.AccessRequirementAclEditorProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.ReactRef;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class CreateManagedACTAccessRequirementStep3ViewImpl
  implements CreateManagedACTAccessRequirementStep3View {

  public interface Binder
    extends UiBinder<Widget, CreateManagedACTAccessRequirementStep3ViewImpl> {}

  Widget widget;

  @UiField
  ReactComponent reactContainer;

  ReactRef<AccessRequirementAclEditorHandler> componentRef;

  SynapseReactClientFullContextPropsProvider propsProvider;
  Presenter presenter;

  @Inject
  public CreateManagedACTAccessRequirementStep3ViewImpl(
    Binder binder,
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    widget = binder.createAndBindUi(this);
    this.propsProvider = propsProvider;
  }

  @Override
  public void configure(String accessRequirementId) {
    componentRef = React.createRef();
    reactContainer.clear();
    ReactNode element = React.createElementWithSynapseContext(
      SRC.SynapseComponents.AccessRequirementAclEditor,
      AccessRequirementAclEditorProps.create(
        accessRequirementId,
        saveSuccessful -> {
          presenter.onSaveComplete(saveSuccessful);
        },
        componentRef
      ),
      propsProvider.getJsInteropContextProps()
    );
    reactContainer.render(element);
  }

  @Override
  public void saveAcl() {
    componentRef.current.save();
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setPresenter(Presenter p) {
    this.presenter = p;
  }
}
