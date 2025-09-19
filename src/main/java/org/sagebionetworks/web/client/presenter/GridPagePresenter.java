package org.sagebionetworks.web.client.presenter;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

import com.google.common.util.concurrent.FutureCallback;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.grid.GridSession;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.GridPlace;
import org.sagebionetworks.web.client.view.GridPageView;

public class GridPagePresenter
  extends AbstractActivity
  implements Presenter<GridPlace> {

  private final GridPageView view;
  private final SynapseJavascriptClient jsClient;
  private final SynapseJSNIUtils jsniUtils;
  private final PopupUtilsView popupUtils;

  @Inject
  public GridPagePresenter(
    GridPageView view,
    SynapseJSNIUtils jsniUtils,
    SynapseJavascriptClient jsClient,
    PopupUtilsView popupUtils
  ) {
    this.view = view;
    this.jsClient = jsClient;
    this.jsniUtils = jsniUtils;
    this.popupUtils = popupUtils;

    jsniUtils.setPageTitle(DisplayConstants.WORKING_COPY);
    view.setTitle(DisplayConstants.WORKING_COPY);
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    panel.setWidget(view.asWidget());
  }

  @Override
  public void setPlace(final GridPlace place) {
    String sessionId = place.getGridSessionId();

    view.render(sessionId);

    // Update the title with more information
    jsClient
      .getGridSession(sessionId)
      .addCallback(
        new FutureCallback<GridSession>() {
          @Override
          public void onSuccess(GridSession session) {
            jsClient.getEntity(
              session.getSourceEntityId(),
              new AsyncCallback<Entity>() {
                @Override
                public void onSuccess(Entity sourceEntity) {
                  jsniUtils.setPageTitle(
                    DisplayConstants.WORKING_COPY +
                    " - " +
                    sourceEntity.getName()
                  );
                  view.setTitle(
                    DisplayConstants.WORKING_COPY +
                    " of " +
                    sourceEntity.getName()
                  );
                }

                @Override
                public void onFailure(Throwable caught) {
                  popupUtils.notify(
                    "Error",
                    caught.getMessage(),
                    DisplayUtils.NotificationVariant.DANGER
                  );
                }
              }
            );
          }

          @Override
          public void onFailure(Throwable caught) {
            popupUtils.notify(
              "Error",
              caught.getMessage(),
              DisplayUtils.NotificationVariant.DANGER
            );
          }
        },
        directExecutor()
      );
  }
}
