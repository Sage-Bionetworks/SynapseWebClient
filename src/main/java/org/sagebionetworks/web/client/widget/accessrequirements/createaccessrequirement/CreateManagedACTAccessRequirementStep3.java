package org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;

/**
 * Second page of creating an access requirement (ACT)
 *
 * @author Jay
 *
 */
public class CreateManagedACTAccessRequirementStep3
  implements ModalPage, CreateManagedACTAccessRequirementStep3View.Presenter {

  CreateManagedACTAccessRequirementStep3View view;
  ModalPresenter modalPresenter;
  ManagedACTAccessRequirement accessRequirement;
  SynapseJavascriptClient jsClient;

  @Inject
  public CreateManagedACTAccessRequirementStep3(
    CreateManagedACTAccessRequirementStep3View view,
    SynapseJavascriptClient jsClient
  ) {
    super();
    this.view = view;
    this.jsClient = jsClient;
    view.setPresenter(this);
  }

  /**
   * Configure this widget before use.
   *
   */
  public void configure(ManagedACTAccessRequirement accessRequirement) {
    this.accessRequirement = accessRequirement;
    modalPresenter.setLoading(true);
    modalPresenter.clearErrors();
    view.configure(accessRequirement.getId().toString());
    modalPresenter.setLoading(false);
  }

  @Override
  public void onPrimary() {
    modalPresenter.setLoading(true);
    view.saveAcl();
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  @Override
  public void setModalPresenter(ModalPresenter modalPresenter) {
    this.modalPresenter = modalPresenter;
    modalPresenter.setTitle("Access Requirement Permissions");
    modalPresenter.setPrimaryButtonText(DisplayConstants.SAVE_BUTTON_LABEL);
  }

  @Override
  public void onSaveComplete(boolean saveSuccessful) {
    if (saveSuccessful) {
      modalPresenter.onFinished();
    }
    modalPresenter.setLoading(false);
  }
}
