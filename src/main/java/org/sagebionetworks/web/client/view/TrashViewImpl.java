package org.sagebionetworks.web.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.trash.TrashCanList;

public class TrashViewImpl extends Composite implements TrashView {

  public interface TrashViewImplUiBinder
    extends UiBinder<Widget, TrashViewImpl> {}

  @UiField
  SimplePanel componentContainer;

  private Header headerWidget;

  private final TrashViewImplUiBinder binder = GWT.create(
    TrashViewImplUiBinder.class
  );

  @Inject
  public TrashViewImpl(Header headerWidget) {
    initWidget(binder.createAndBindUi(this));
    this.headerWidget = headerWidget;
    headerWidget.configure();
  }

  @Override
  public void createReactComponentWidget() {
    TrashCanList component = new TrashCanList();
    componentContainer.clear();
    componentContainer.add(component);
  }
}
