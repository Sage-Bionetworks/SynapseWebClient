package org.sagebionetworks.web.client.widget.accessrequirements.submission;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.widget.lazyload.SupportsLazyLoadInterface;

public interface OpenSubmissionWidgetView
  extends IsWidget, SupportsLazyLoadInterface {
  public interface Presenter {}

  void setPresenter(Presenter presenter);

  void setSynAlert(IsWidget w);

  void setACTAccessRequirementWidget(IsWidget w);

  void setNumberOfSubmissions(long number);
}
