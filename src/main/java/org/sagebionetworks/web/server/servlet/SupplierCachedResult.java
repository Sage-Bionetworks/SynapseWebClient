package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.client.exceptions.SynapseException;

public class SupplierCachedResult<T> {

  private final T value;
  private final SynapseException error;

  private SupplierCachedResult(T value, SynapseException error) {
    this.value = value;
    this.error = error;
  }

  public static <T> SupplierCachedResult<T> success(T value) {
    return new SupplierCachedResult<>(value, null);
  }

  public static <T> SupplierCachedResult<T> failure(SynapseException error) {
    return new SupplierCachedResult<>(null, error);
  }

  public boolean isSuccess() {
    return error == null;
  }

  public T getValue() {
    return value;
  }

  public Throwable getError() {
    return error;
  }
}
