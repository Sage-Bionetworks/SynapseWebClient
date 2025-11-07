package org.sagebionetworks.web.client.view;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.widget.entity.GridPageImpl;

public class GridPageViewImpl extends Composite implements GridPageView {

  GridPageImpl gridPage;

  public interface Binder extends UiBinder<Widget, GridPageViewImpl> {}

  @UiField
  SimplePanel componentContainer;

  @Inject
  public GridPageViewImpl(final Binder uiBinder, GridPageImpl gridPage) {
    initWidget(uiBinder.createAndBindUi(this));

    this.gridPage = gridPage;

    componentContainer.add(gridPage.asWidget());
  }

  @Override
  public void render() {
    this.gridPage.configure();
  }
}
