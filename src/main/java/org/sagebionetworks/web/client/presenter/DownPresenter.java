package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.status.StackStatus;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.StackConfigServiceAsync;
import org.sagebionetworks.web.client.place.Down;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.DownView;

public class DownPresenter extends AbstractActivity implements Presenter<Down> {

  public static final int SECOND_MS = 1000;
  // check back every 20s if down.
  public static final int DELAY_MS = 20000;
  public int timeToNextRefresh;
  private DownView view;
  GlobalApplicationState globalAppState;
  Callback updateTimerCallback;
  StackConfigServiceAsync stackConfigService;
  boolean isCheckingStatus = false;

  @Inject
  public DownPresenter(
    final DownView view,
    final GWTWrapper gwt,
    GlobalApplicationState globalAppState,
    StackConfigServiceAsync stackConfigService
  ) {
    this.view = view;
    this.globalAppState = globalAppState;
    this.stackConfigService = stackConfigService;
    fixServiceEntryPoint(stackConfigService);
    timeToNextRefresh = DELAY_MS;
    updateTimerCallback =
      new Callback() {
        @Override
        public void invoke() {
          if (!isCheckingStatus && view.isAttached()) {
            timeToNextRefresh -= SECOND_MS;
            if (timeToNextRefresh <= 1) {
              checkForRepoDown();
            }
          }
        }
      };
    gwt.scheduleFixedDelay(updateTimerCallback, SECOND_MS);
  }

  public void checkForRepoDown() {
    isCheckingStatus = true;
    stackConfigService.getCurrentStatus(
      new AsyncCallback<StackStatus>() {
        @Override
        public void onSuccess(StackStatus status) {
          switch (status.getStatus()) {
            case READ_WRITE:
              // it's up!
              repoIsUp();
              break;
            case READ_ONLY:
            case DOWN:
              // it's down, report the message and check again later
              view.setMessage(status.getCurrentMessage());
          }
          reset();
        }

        @Override
        public void onFailure(Throwable caught) {
          view.setMessage(caught.getMessage());
          reset();
        }

        private void repoIsUp() {
          // note: go back in the browser history
          globalAppState.back();
        }

        private void reset() {
          isCheckingStatus = false;
          timeToNextRefresh = DELAY_MS;
        }
      }
    );
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    panel.setWidget(this.view.asWidget());
  }

  @Override
  public void setPlace(Down place) {
    view.init();
    timeToNextRefresh = 0;
  }

  public int getTimeToNextRefresh() {
    return timeToNextRefresh;
  }
}
