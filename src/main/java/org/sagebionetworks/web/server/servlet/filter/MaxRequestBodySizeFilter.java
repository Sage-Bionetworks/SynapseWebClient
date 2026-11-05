package org.sagebionetworks.web.server.servlet.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Responds 413 if the request body is larger than maxBytes.
 */
public class MaxRequestBodySizeFilter implements Filter {

  private final int maxBytes;

  public MaxRequestBodySizeFilter(int maxBytes) {
    this.maxBytes = maxBytes;
  }

  @Override
  public void doFilter(
    ServletRequest request,
    ServletResponse response,
    FilterChain filterChain
  ) throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    // Fast path: if Content-Length is over the limit, reject without reading the body. Requests without one
    // (e.g. chunked) are checked by reading the body below
    if (httpRequest.getContentLengthLong() > maxBytes) {
      httpResponse.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
      return;
    }

    byte[] body;

    try {
      // Read one byte past the limit, so a body that is too long can be detected.
      body = httpRequest.getInputStream().readNBytes(maxBytes + 1);
    } catch (IOException e) {
      // e.g. the client disconnected partway through; matches the servlets' current 400
      httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    if (body.length > maxBytes) {
      httpResponse.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
      return;
    }

    filterChain.doFilter(new BufferedBodyRequest(httpRequest, body), response);
  }

  /**
   * The request the filter passes on to the servlet. The filter reads the body to measure it (chunked requests have
   * no Content-Length), which uses up the original stream, so this request's getInputStream() and getReader() return
   * the bytes the filter read. The servlets read the body as usual.
   */
  private static final class BufferedBodyRequest
    extends HttpServletRequestWrapper {

    private final byte[] body;

    BufferedBodyRequest(HttpServletRequest request, byte[] body) {
      super(request);
      this.body = body;
    }

    @Override
    public ServletInputStream getInputStream() {
      ByteArrayInputStream in = new ByteArrayInputStream(body);
      return new ServletInputStream() {
        @Override
        public int read() {
          return in.read();
        }

        @Override
        public int read(byte[] b, int off, int len) {
          return in.read(b, off, len);
        }

        @Override
        public boolean isFinished() {
          return in.available() == 0;
        }

        @Override
        public boolean isReady() {
          return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {
          throw new UnsupportedOperationException();
        }
      };
    }

    @Override
    public BufferedReader getReader() throws UnsupportedEncodingException {
      String encoding = getCharacterEncoding();
      return new BufferedReader(
        new InputStreamReader(
          getInputStream(),
          encoding != null ? encoding : "UTF-8"
        )
      );
    }

    @Override
    public int getContentLength() {
      return body.length;
    }

    @Override
    public long getContentLengthLong() {
      return body.length;
    }
  }
}
