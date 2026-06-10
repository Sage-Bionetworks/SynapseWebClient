package org.sagebionetworks.web.server.servlet.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * We are using
 *
 * @author jmhill
 *
 */
public class RPCValidationFilter implements Filter {

  @Override
  public void destroy() {
    // TODO Auto-generated method stub

  }

  @Override
  public void doFilter(
    ServletRequest request,
    ServletResponse response,
    FilterChain chain
  ) throws IOException, ServletException {
    if (request.getContentType() == null) {
      // Proxy the request so the content type is not null
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      HttpServletRequest requestProxy =
        (HttpServletRequest) Proxy.newProxyInstance(
          HttpServletRequest.class.getClassLoader(),
          new Class[] { HttpServletRequest.class },
          new Handler(httpRequest)
        );
      chain.doFilter(requestProxy, response);
    } else {
      // Do nothing
      chain.doFilter(request, response);
    }
  }

  @Override
  public void init(FilterConfig arg0) throws ServletException {
    // TODO Auto-generated method stub

  }

  /**
   * This handler will never return a null content type.
   *
   * @author jmhill
   *
   */
  private static class Handler implements InvocationHandler {

    private HttpServletRequest wrapped;

    public Handler(HttpServletRequest httpRequest) {
      this.wrapped = httpRequest;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable {
      if (method.getName().equals("getContentType")) {
        return "text/x-gwt-rpc; charset=utf-8";
      } else if (method.getName().equals("getCharacterEncoding")) {
        return "UTF-8";
      } else {
        return method.invoke(wrapped, args);
      }
    }
  }
}
