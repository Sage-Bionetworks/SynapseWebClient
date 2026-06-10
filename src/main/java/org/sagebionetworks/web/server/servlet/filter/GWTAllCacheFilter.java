package org.sagebionetworks.web.server.servlet.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class GWTAllCacheFilter implements Filter {

  public static final long CACHE_TIME = 1000 * 60 * 60 * 24 * 365; // 1 year
  public static final long CACHE_TIME_SECONDS = 60 * 60 * 24 * 365; // 1 year

  private FilterConfig filterConfig;

  @Override
  public void destroy() {
    this.filterConfig = null;
  }

  @Override
  public void doFilter(
    ServletRequest request,
    ServletResponse response,
    FilterChain filterChain
  ) throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    String requestURI = httpRequest.getRequestURI().toLowerCase();
    long now = new Date().getTime();
    httpResponse.setDateHeader("Date", now);
    httpResponse.setHeader("Cache-Control", "max-age=" + CACHE_TIME_SECONDS);
    httpResponse.setHeader("Pragma", "");
    httpResponse.setDateHeader("Expires", now + CACHE_TIME);
    filterChain.doFilter(request, response);
  }

  @Override
  public void init(FilterConfig config) throws ServletException {
    this.filterConfig = config;
  }
}
