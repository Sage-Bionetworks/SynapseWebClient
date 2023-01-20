package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.Date;
import javax.annotation.Nullable;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.help.HelpButton;
import org.sagebionetworks.web.client.jsinterop.CreatedByModifiedByProps;
import org.sagebionetworks.web.client.jsinterop.ReferenceJsObject;
import org.sagebionetworks.web.client.widget.user.UserBadge;

public class ModifiedCreatedByWidget implements IsWidget {

  private ModifiedCreatedByWidgetView view;

  @Inject
  public ModifiedCreatedByWidget(ModifiedCreatedByWidgetView view) {
    this.view = view;
  }

  public void configure(String entityId, @Nullable Long versionNumber) {
    view.setVisible(true);
    view.setProps(CreatedByModifiedByProps.create(entityId, versionNumber));
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  public void setVisible(boolean isVisible) {
    view.setVisible(isVisible);
  }
}
