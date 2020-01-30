package org.sagebionetworks.web.server.servlet;

import static org.sagebionetworks.web.client.cookie.CookieKeys.USER_LOGIN_TOKEN;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.shared.WebConstants;

/**
 * Servlet for setting the session token HttpOnly cookie.
 */
public class InitSessionServlet extends HttpServlet {
	public static final String EMPTY_JSON = "{}";
	public static final String JSON_CONTENT_TYPE = "application/json";
	static private Log log = LogFactory.getLog(InitSessionServlet.class);
	public static final String ROOT_PATH = "/";
	public static final String SYNAPSE_ORG = "synapse.org";
	private static final long serialVersionUID = 1L;
	protected static final ThreadLocal<HttpServletRequest> perThreadRequest = new ThreadLocal<HttpServletRequest>();
	public static final int ONE_DAY_IN_SECONDS = 60 * 60 * 24;

	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
		InitSessionServlet.perThreadRequest.set(arg0);
		super.service(arg0, arg1);
	}

	@Override
	public void service(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException {
		super.service(arg0, arg1);
	}

	@Override
	public void doPost(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// return the Set-Cookie response with the session token
		try {
			String sessionJson = IOUtils.toString(request.getReader());
			JSONObjectAdapter adapter = new JSONObjectAdapterImpl(sessionJson);
			Session s = new Session(adapter);
			String sessionToken = s.getSessionToken();
			if (sessionToken == null || sessionToken.isEmpty()) {
				sessionToken = WebConstants.EXPIRE_SESSION_TOKEN;
			}
			Cookie cookie = new Cookie(USER_LOGIN_TOKEN, sessionToken);

			if (!WebConstants.EXPIRE_SESSION_TOKEN.equals(sessionToken)) {
				cookie.setMaxAge(ONE_DAY_IN_SECONDS);
			} else {
				cookie.setMaxAge(0);
			}
			boolean isSecure = "https".equals(request.getScheme().toLowerCase());
			cookie.setSecure(isSecure);
			cookie.setHttpOnly(true);
			cookie.setPath(ROOT_PATH);

			String domain = request.getServerName();
			String lowerCaseDomain = domain.toLowerCase();
			if (lowerCaseDomain.contains("." + SYNAPSE_ORG)) {
				cookie.setDomain(SYNAPSE_ORG);
			}
			response.addCookie(cookie);
			response.setContentType(JSON_CONTENT_TYPE);
			PrintWriter out = response.getWriter();
			out.print(EMPTY_JSON);
			out.flush();
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getOutputStream().write(("Invalid session token - " + e.getMessage()).getBytes("UTF-8"));
			response.getOutputStream().flush();
		}
	}

	/**
	 * Unit test uses this to provide a mock token provider
	 *
	 * @param tokenProvider
	 */
	public void setTokenProvider(TokenProvider tokenProvider) {
		this.tokenProvider = tokenProvider;
	}

	private TokenProvider tokenProvider = new TokenProvider() {
		@Override
		public String getSessionToken() {
			return UserDataProvider.getThreadLocalUserToken(InitSessionServlet.perThreadRequest.get());
		}
	};

	public String getSessionToken(final HttpServletRequest request) {
		return tokenProvider.getSessionToken();
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// instruct not to cache
		response.setHeader(WebConstants.CACHE_CONTROL_KEY, WebConstants.CACHE_CONTROL_VALUE_NO_CACHE); // Set standard HTTP/1.1 no-cache headers.
		response.setHeader(WebConstants.PRAGMA_KEY, WebConstants.NO_CACHE_VALUE); // Set standard HTTP/1.0 no-cache header.
		response.setDateHeader(WebConstants.EXPIRES_KEY, 0L); // Proxy

		String token = getSessionToken(request);
		if (token != null) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.getOutputStream().write(token.getBytes("UTF-8"));
			response.getOutputStream().flush();
		} else {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}
}
