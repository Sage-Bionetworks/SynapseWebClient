package org.sagebionetworks.web.client.widget;

import static org.sagebionetworks.web.client.DisplayConstants.WORKING_COPY;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalSize;
import org.gwtbootstrap3.client.ui.constants.ModalBackdrop;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.grid.CreateGridRequest;
import org.sagebionetworks.repo.model.grid.CreateGridResponse;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.GridPlace;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.shared.asynch.AsynchType;

public class CreateGridSessionDialogImpl implements CreateGridSessionDialog {

  private final GlobalApplicationState globalApplicationState;
  private final JobTrackingWidget jobTrackingWidget;
  private final Modal modal;

  @Inject
  public CreateGridSessionDialogImpl(
    GlobalApplicationState globalApplicationState,
    JobTrackingWidget jobTrackingWidget
  ) {
    this.globalApplicationState = globalApplicationState;
    this.jobTrackingWidget = jobTrackingWidget;
    this.modal = new Modal();
    ModalBody modalBody = new ModalBody();
    this.modal.setTitle("Loading...");
    this.modal.setClosable(false);
    this.modal.setDataBackdrop(ModalBackdrop.STATIC);
    this.modal.setDataKeyboard(true);
    this.modal.setSize(ModalSize.SMALL);
    this.modal.add(modalBody);
    modalBody.add(jobTrackingWidget.asWidget());
  }

  @Override
  public void createGridSession(CreateGridRequest request) {
    this.modal.show();

    jobTrackingWidget.startAndTrackJob(
      "Creating " + WORKING_COPY + "...",
      false,
      AsynchType.CreateGrid,
      request,
      new AsynchronousProgressHandler<AsynchronousResponseBody>() {
        @Override
        public void onCancel() {
          modal.hide();
        }

        @Override
        public void onComplete(
          AsynchronousResponseBody asynchronousResponseBody
        ) {
          modal.hide();
          Place place = new GridPlace(
            ((CreateGridResponse) asynchronousResponseBody).getGridSession()
              .getSessionId()
          );
          globalApplicationState.getPlaceChanger().goTo(place);
        }

        @Override
        public void onFailure(Throwable failure) {
          modal.hide();
          DisplayUtils.showErrorToast(failure.getMessage(), 0);
        }
      }
    );
  }

  @Override
  public Widget asWidget() {
    return modal.asWidget();
  }
}
