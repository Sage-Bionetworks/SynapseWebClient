package org.sagebionetworks.web.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.sagebionetworks.web.client.widget.entity.SearchV2Impl;

public class SearchV2ViewImpl extends Composite implements SearchV2View {

  SearchV2Impl searchV2;

  public interface Binder extends UiBinder<Widget, SearchV2ViewImpl> {}

  @UiField
  SimplePanel componentContainer;

  private final Binder uiBinder = GWT.create(Binder.class);

  @Inject
  public SearchV2ViewImpl(SearchV2Impl searchV2) {
    initWidget(uiBinder.createAndBindUi(this));

    this.searchV2 = searchV2;

    componentContainer.add(searchV2.asWidget());
  }

  @Override
  public void render() {
    this.searchV2.configure();
  }
}
