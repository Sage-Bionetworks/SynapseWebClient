package org.sagebionetworks.web.client.widget.accessrequirements;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.IntendedDataUseReportWidget;
import org.sagebionetworks.web.client.widget.modal.DialogView;

public class IntendedDataUseReportButton implements IsWidget {

  public static final String IDU_MODAL_FIELD_NAME = "Markdown";
  public static final String IDU_MODAL_TITLE =
    "Approved Intended Data Use Statements";
  public static final String GENERATE_REPORT_BUTTON_TEXT =
    "Generate IDU Report";
  public Button button;
  public IsACTMemberAsyncHandler isACTMemberAsyncHandler;
  AccessRequirement ar;
  DialogView dialog;
  IntendedDataUseReportWidget iduReportWidget;
  SynapseAlert synAlert;

  @Inject
  public IntendedDataUseReportButton(
    Button button,
    IsACTMemberAsyncHandler isACTMemberAsyncHandler,
    DialogView dialog,
    DivView divView,
    SynapseAlert synAlert,
    IntendedDataUseReportWidget iduReportWidget
  ) {
    this.button = button;
    this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
    this.dialog = dialog;
    this.iduReportWidget = iduReportWidget;
    this.synAlert = synAlert;
    divView.add(synAlert);
    divView.add(iduReportWidget);
    dialog.configure(IDU_MODAL_TITLE, divView, null, "Cancel", null, true);
    dialog.addStyleName("modal-fullscreen");
    button.setVisible(false);
    button.addStyleName("margin-left-10");
    button.addClickHandler(event -> {
      iduReportWidget.configure(ar.getId().toString());
      dialog.show();
    });
  }

  public void configure(AccessRequirement ar) {
    synAlert.showError(
      "Please use the dynamic IDU Report wiki widget instead of this snapshot. ${iduReport?accessRestrictionId=" +
      ar.getId() +
      "}"
    );
    button.setText(GENERATE_REPORT_BUTTON_TEXT);
    button.setSize(ButtonSize.DEFAULT);
    button.setType(ButtonType.DEFAULT);
    this.ar = ar;
    showIfACTMember();
  }

  private void showIfACTMember() {
    isACTMemberAsyncHandler.isACTActionAvailable(
      new CallbackP<Boolean>() {
        @Override
        public void invoke(Boolean isACTMember) {
          button.setVisible(isACTMember);
        }
      }
    );
  }

  public Widget asWidget() {
    return button.asWidget();
  }
}
