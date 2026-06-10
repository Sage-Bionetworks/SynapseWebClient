package org.sagebionetworks.web.client.widget.entity.controller;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.utils.Callback;

public class URLProvEntryViewImpl implements URLProvEntryView {

  public interface URLProvEntryUIBinder
    extends UiBinder<Widget, URLProvEntryViewImpl> {}

  @UiField
  Text urlNameField;

  @UiField
  Anchor urlAddressField;

  @UiField
  Button removeButton;

  Widget widget;
  String title;
  String url;
  Callback removalCallback;

  private final URLProvEntryUIBinder binder = GWT.create(
    URLProvEntryUIBinder.class
  );

  @Inject
  public URLProvEntryViewImpl() {
    this.widget = binder.createAndBindUi(this);
    removeButton.addClickHandler(
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          if (removalCallback != null) removalCallback.invoke();
        }
      }
    );
  }

  @Override
  public void configure(String title, String url) {
    this.title = title;
    this.url = url;
    urlNameField.setText(title);
    urlAddressField.setText(url);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public String getURL() {
    return url;
  }

  @Override
  public void setRemoveCallback(Callback removalCallback) {
    this.removalCallback = removalCallback;
  }

  @Override
  public void setAnchorTarget(String targetURL) {
    urlAddressField.setHref(targetURL);
  }
}
