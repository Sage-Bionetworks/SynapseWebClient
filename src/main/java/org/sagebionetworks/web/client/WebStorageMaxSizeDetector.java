package org.sagebionetworks.web.client;

import com.google.inject.Inject;
import org.sagebionetworks.web.client.cache.ClientCache;

public class WebStorageMaxSizeDetector {

  public static final double MAX_SIZE = 4 * ClientProperties.MB; // cleared when approaching ~5MB limit that most browsers implement
  public static final int INTERVAL_MS = 1000 * 60 * 5; // check every 5 minutes
  ClientCache clientCache;
  GWTWrapper gwt;

  @Inject
  public WebStorageMaxSizeDetector(GWTWrapper gwt, ClientCache clientCache) {
    this.gwt = gwt;
    this.clientCache = clientCache;
  }

  public void start() {
    checkMaxSizeNow();
    checkMaxSizeLater();
  }

  public void checkMaxSizeLater() {
    gwt.scheduleExecution(
      () -> {
        checkMaxSizeNow();
        // continue the loop, check later
        checkMaxSizeLater();
      },
      INTERVAL_MS
    );
  }

  private void checkMaxSizeNow() {
    // if > the max size bytes then clear (approaching ~5MB limit that most browsers implement)
    double currentSize = clientCache.getBytesUsed();
    SynapseJSNIUtilsImpl._consoleLog(
      "Web storage usage: ~" + DisplayUtils.getFriendlySize(currentSize, true)
    );
    if (currentSize > MAX_SIZE) {
      SynapseJSNIUtilsImpl._consoleLog("Clearing web storage due to the size");
      clientCache.clear();
    }
  }
}
