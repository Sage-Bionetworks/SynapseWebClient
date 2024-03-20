package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class Promise<T> {

  @JsFunction
  public interface FunctionParam<T> {
    void exec(T o);
  }

  @JsFunction
  public interface ConstructorParam<T> {
    void exec(FunctionParam<T> resolve, FunctionParam<T> reject);
  }

  @JsConstructor
  public Promise(ConstructorParam parameters) {}

  public native Promise<T> then(FunctionParam<T> f);

  @JsMethod(name = "catch")
  public native Promise<T> catch_(FunctionParam<Object> f);
}
