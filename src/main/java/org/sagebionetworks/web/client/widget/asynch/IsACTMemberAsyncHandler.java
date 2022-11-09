package org.sagebionetworks.web.client.widget.asynch;

import com.google.common.util.concurrent.FluentFuture;
import org.sagebionetworks.web.client.utils.CallbackP;

public interface IsACTMemberAsyncHandler {
  /**
   * Main call. Returns true if the current user is a member of the ACT, and we are currently showing
   * ACT UI.
   *
   * @param callback
   */
  void isACTActionAvailable(CallbackP<Boolean> callback);

  FluentFuture<Boolean> isACTActionAvailable();

  /**
   * In most cases, isACTActionsAvailable() should be used instead of this method.
   *
   * @param callback
   */
  void isACTMember(CallbackP<Boolean> callback);

  FluentFuture<Boolean> isACTMember();

  /**
   * If the current user is a member of the ACT, should ACT UI be shown?
   *
   * @param visible
   */
  void setACTActionVisible(boolean visible);

  boolean isACTActionVisible();
}
