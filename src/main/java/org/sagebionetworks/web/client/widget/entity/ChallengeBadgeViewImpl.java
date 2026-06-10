package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.Anchor;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.place.Synapse;

public class ChallengeBadgeViewImpl implements ChallengeBadgeView {

  public interface Binder extends UiBinder<Widget, ChallengeBadgeViewImpl> {}

  @UiField
  Anchor link;

  Widget widget;
  String projectId;
  public static PlaceChanger placeChanger;
  public static final String CHALLENGE_PROJECT_ID = "data-challenge-project-id";
  public static final ClickHandler STANDARD_CLICKHANDLER = event -> {
    if (!DisplayUtils.isAnyModifierKeyDown(event)) {
      event.preventDefault();
      Widget panel = (Widget) event.getSource();
      String projectId = panel.getElement().getAttribute(CHALLENGE_PROJECT_ID);
      placeChanger.goTo(new Synapse(projectId));
    }
  };

  private final Binder uiBinder = GWT.create(Binder.class);

  @Inject
  public ChallengeBadgeViewImpl(GlobalApplicationState globalAppState) {
    widget = uiBinder.createAndBindUi(this);
    placeChanger = globalAppState.getPlaceChanger();
    link.addClickHandler(STANDARD_CLICKHANDLER);
  }

  @Override
  public void setProjectId(String projectId) {
    this.projectId = projectId;
    link.setHref(DisplayUtils.getSynapseHistoryToken(projectId));
    link.getElement().setAttribute(CHALLENGE_PROJECT_ID, projectId);
  }

  public void setProjectName(String projectName) {
    link.setText(projectName);
    widget.setVisible(true);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void showErrorMessage(String message) {
    DisplayUtils.showErrorMessage(message);
  }
}
