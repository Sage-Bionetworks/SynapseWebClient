package org.sagebionetworks.web.client.widget.table.modal.download;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.Form;

public class DownloadFilePageViewImpl implements DownloadFilePageView {

  public interface Binder extends UiBinder<Form, DownloadFilePageViewImpl> {}

  Form form;

  private final Binder binder = GWT.create(Binder.class);

  @Inject
  public DownloadFilePageViewImpl() {
    form = binder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return form;
  }
}
