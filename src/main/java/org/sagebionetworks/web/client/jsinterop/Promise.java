package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class Promise {

  @JsFunction
  public interface FunctionParam {
    void exec(Object o);
  }

  @JsFunction
  public interface ConstructorParam {
    void exec(FunctionParam resolve, FunctionParam reject);
  }

  @JsConstructor
  public Promise(ConstructorParam parameters) {}

  public native Promise then(FunctionParam f);

  @JsMethod(name = "catch")
  public native Promise catch_(FunctionParam f);
}
