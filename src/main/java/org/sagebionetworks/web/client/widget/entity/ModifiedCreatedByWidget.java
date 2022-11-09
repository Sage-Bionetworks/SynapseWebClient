package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.Date;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.help.HelpButton;
import org.sagebionetworks.web.client.widget.user.UserBadge;

public class ModifiedCreatedByWidget implements IsWidget {

  private ModifiedCreatedByWidgetView view;
  private UserBadge createdByBadge;
  private UserBadge modifiedByBadge;
  private DateTimeUtils dateTimeUtils;

  @Inject
  public ModifiedCreatedByWidget(
    ModifiedCreatedByWidgetView view,
    UserBadge createdByBadge,
    UserBadge modifiedByBadge,
    DateTimeUtils dateTimeUtils
  ) {
    this.view = view;
    this.createdByBadge = createdByBadge;
    this.modifiedByBadge = modifiedByBadge;
    this.dateTimeUtils = dateTimeUtils;
    view.setCreatedBadge(createdByBadge);
    view.setModifiedBadge(modifiedByBadge);
  }

  public void configure(
    Date createdOn,
    String createdBy,
    Date modifiedOn,
    String modifiedBy
  ) {
    createdByBadge.configure(createdBy);
    modifiedByBadge.configure(modifiedBy);
    view.setCreatedOnText(
      " on " + dateTimeUtils.getLongFriendlyDate(createdOn)
    );
    view.setModifiedOnText(
      " on " + dateTimeUtils.getLongFriendlyDate(modifiedOn)
    );
    view.setVisible(true);
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  public void setCreatedHelpWidgetVisible(boolean visible) {
    view.setCreatedHelpWidgetVisible(visible);
  }

  public void setCreatedHelpWidgetText(String text) {
    view.setCreatedHelpWidgetText(text);
  }

  public void setVisible(boolean isVisible) {
    view.setVisible(isVisible);
  }
}
