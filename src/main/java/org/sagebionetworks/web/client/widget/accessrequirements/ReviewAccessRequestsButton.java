package org.sagebionetworks.web.client.widget.accessrequirements;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.JsonSchemaAccessRequirement;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.ACTDataAccessSubmissionsPlace;
import org.sagebionetworks.web.client.place.DataAccessManagementPlace;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;

public class ReviewAccessRequestsButton implements IsWidget {

  public static final String REVIEW_ACCESS_BUTTON_TEXT = "Review Requests";
  public Button button;
  public IsACTMemberAsyncHandler isACTMemberAsyncHandler;
  AccessRequirement ar;

  @Inject
  public ReviewAccessRequestsButton(
    Button button,
    IsACTMemberAsyncHandler isACTMemberAsyncHandler,
    final GlobalApplicationState globalAppState
  ) {
    this.button = button;
    this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
    button.setVisible(false);
    button.addStyleName("margin-left-10");
    button.setType(ButtonType.PRIMARY);
    button.setText(REVIEW_ACCESS_BUTTON_TEXT);
    button.addClickHandler(
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          globalAppState.getPlaceChanger().goTo(getReviewPlace());
        }
      }
    );
  }

  /**
   * The GWT submissions page only renders ManagedACTAccessRequirement submissions, so a
   * JsonSchemaAccessRequirement's submissions are reviewed in the React submission dashboard,
   * which renders the schema answers.
   */
  private Place getReviewPlace() {
    if (ar instanceof JsonSchemaAccessRequirement) {
      return new DataAccessManagementPlace(
        "default/Submissions?accessRequirementId=" + ar.getId()
      );
    }
    return new ACTDataAccessSubmissionsPlace(
      ACTDataAccessSubmissionsPlace.ACCESS_REQUIREMENT_ID_PARAM +
      "=" +
      ar.getId()
    );
  }

  public void configure(AccessRequirement ar) {
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
