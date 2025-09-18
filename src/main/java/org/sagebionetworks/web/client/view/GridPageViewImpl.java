package org.sagebionetworks.web.client.view;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.widget.entity.SynapseGridImpl;

public class GridPageViewImpl extends Composite implements GridPageView {

  SynapseGridImpl synapseGrid;

  public interface Binder extends UiBinder<Widget, GridPageViewImpl> {}

  @UiField
  Heading pageHeaderTitle;

  @UiField
  Span gridSessionNameContainer;

  @UiField
  SimplePanel componentContainer;

  @Inject
  public GridPageViewImpl(final Binder uiBinder, SynapseGridImpl synapseGrid) {
    initWidget(uiBinder.createAndBindUi(this));

    this.synapseGrid = synapseGrid;

    componentContainer.add(synapseGrid.asWidget());
  }

  @Override
  public void setTitle(String title) {
    pageHeaderTitle.setText(title);
  }

  @Override
  public void render(String sessionId) {
    this.gridSessionNameContainer.setText(sessionId);
    this.synapseGrid.configure(sessionId, false);
  }
}
