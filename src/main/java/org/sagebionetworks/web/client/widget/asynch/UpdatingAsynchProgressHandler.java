package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;

/**
 * An AsynchronousProgressHandler that can also receive updates.
 *
 * @author jmhill
 *
 */
public interface UpdatingAsynchProgressHandler<
  TResponse extends AsynchronousResponseBody
>
  extends AsynchronousProgressHandler<TResponse> {
  public void onUpdate(AsynchronousJobStatus status);

  /**
   * The job tracker needs to stop tracking if the UI that started the job is no longer attached.
   *
   * @return
   */
  public boolean isAttached();
}
