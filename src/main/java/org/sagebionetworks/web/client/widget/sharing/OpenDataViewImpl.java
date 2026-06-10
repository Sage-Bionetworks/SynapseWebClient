package org.sagebionetworks.web.client.widget.sharing;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.html.Div;

public class OpenDataViewImpl implements OpenDataView {

  public interface Binder extends UiBinder<Div, OpenDataViewImpl> {}

  private Div widget;

  @UiField
  Div isPublicAndOpen;

  @UiField
  Div isPublicAndAdmin;

  @UiField
  Div isPrivateAndOpenAndAdmin;

  private final Binder binder = GWT.create(Binder.class);

  @Inject
  public OpenDataViewImpl() {
    widget = binder.createAndBindUi(this);
  }

  @Override
  public void reset() {
    isPublicAndOpen.setVisible(false);
    isPublicAndAdmin.setVisible(false);
    isPrivateAndOpenAndAdmin.setVisible(false);
  }

  @Override
  public void showMustGivePublicReadToBeOpenData() {
    isPrivateAndOpenAndAdmin.setVisible(true);
  }

  @Override
  public void showMustContactACTToBeOpenData() {
    isPublicAndAdmin.setVisible(true);
  }

  @Override
  public void showIsOpenData() {
    isPublicAndOpen.setVisible(true);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }
}
