package org.sagebionetworks.web.client.widget.asynch;

import com.google.gwt.i18n.client.NumberFormat;
import javax.inject.Inject;

/**
 * Simple wrapper for a GWT NumberFormat.
 *
 * @author John
 *
 */
public class NumberFormatProviderImpl implements NumberFormatProvider {

  @Inject
  public NumberFormatProviderImpl() {}

  NumberFormat format;

  @Override
  public void setFormat(String format) {
    this.format = NumberFormat.getFormat(format);
  }

  @Override
  public String format(double toformat) {
    return this.format.format(toformat);
  }
}
