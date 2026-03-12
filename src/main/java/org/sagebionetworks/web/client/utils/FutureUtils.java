package org.sagebionetworks.web.client.utils;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.function.Consumer;
import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;
import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousJobTracker;
import org.sagebionetworks.web.client.widget.asynch.UpdatingAsynchProgressHandler;
import org.sagebionetworks.web.shared.asynch.AsynchType;

public class FutureUtils {

  /**
   * Calls the given closure, passing it an AsyncCallback that transmits the success failure scenarios
   * to a FluentFuture; then it returns the future.
   *
   * @param closure
   * @param <T>
   * @return a FluentFuture that represents the outcome of calling the closure
   */
  public static <T> FluentFuture<T> getFuture(
    Consumer<AsyncCallback<T>> closure
  ) {
    SettableFuture<T> future = SettableFuture.create();
    closure.accept(
      new AsyncCallback<T>() {
        @Override
        public void onFailure(Throwable caught) {
          future.setException(caught);
        }

        @Override
        public void onSuccess(T result) {
          future.set(result);
        }
      }
    );
    return FluentFuture.from(future);
  }

  /**
   * Returns a future that is already completed with the given value as the result.
   *
   * @param result
   * @param <T>
   * @return a future that is already completed with the given value as the result
   */
  public static <T> FluentFuture<T> getDoneFuture(T result) {
    SettableFuture<T> future = SettableFuture.create();
    future.set(result);
    return FluentFuture.from(future);
  }

  /**
   * Returns a failed future with its exception set to the given Throwable
   *
   * @return a failed future with its exception set to the given Throwable
   */
  public static <T> FluentFuture<T> getFailedFuture(Throwable e) {
    SettableFuture<T> future = SettableFuture.create();
    future.setException(e);
    return FluentFuture.from(future);
  }

  /**
   * Returns a failed future with its exception set to a new Throwable.
   *
   * @return a failed future with its exception set to a new Throwable
   */
  public static <T> FluentFuture<T> getFailedFuture() {
    return getFailedFuture(new Throwable());
  }

  /**
   * Starts and tracks an asynchronous job, returning a FluentFuture that completes with the response.
   * On failure, the future fails with the exception. On cancel, the future is cancelled.
   *
   * @param tracker
   * @param type
   * @param requestBody
   * @param waitTimeMS
   * @param <T>
   * @return a FluentFuture that represents the outcome of the asynchronous job
   */
  public static <T extends AsynchronousResponseBody> FluentFuture<
    T
  > getAsyncJobFuture(
    AsynchronousJobTracker tracker,
    AsynchType type,
    AsynchronousRequestBody requestBody,
    int waitTimeMS
  ) {
    SettableFuture<T> future = SettableFuture.create();
    tracker.startAndTrack(
      type,
      requestBody,
      waitTimeMS,
      new UpdatingAsynchProgressHandler<T>() {
        @Override
        public void onUpdate(AsynchronousJobStatus status) {}

        @Override
        public void onComplete(T response) {
          future.set(response);
        }

        @Override
        public void onFailure(Throwable caught) {
          future.setException(caught);
        }

        @Override
        public void onCancel() {
          future.cancel(false);
        }

        @Override
        public boolean isAttached() {
          return true;
        }
      }
    );
    return FluentFuture.from(future);
  }
}
