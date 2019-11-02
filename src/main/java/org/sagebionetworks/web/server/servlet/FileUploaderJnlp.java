package org.sagebionetworks.web.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.versionInfo.SynapseVersionInfo;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.shared.WebConstants;

/**
 * Dynamically creates a JavaWebStart JNLP for the Synapse File Uploader Client
 */
public class FileUploaderJnlp extends HttpServlet {

	private static Logger logger = Logger.getLogger(FileUploaderJnlp.class.getName());
	private static final long serialVersionUID = 1L;
	private static String jarUrl;

	protected static final ThreadLocal<HttpServletRequest> perThreadRequest = new ThreadLocal<HttpServletRequest>();

	private SynapseProvider synapseProvider = new SynapseProviderImpl();
	private TokenProvider tokenProvider = new TokenProvider() {
		@Override
		public String getSessionToken() {
			return UserDataProvider.getThreadLocalUserToken(FileUploaderJnlp.perThreadRequest.get());
		}
	};

	/**
	 * Unit test can override this.
	 *
	 * @param fileHandleProvider
	 */
	public void setSynapseProvider(SynapseProvider synapseProvider) {
		this.synapseProvider = synapseProvider;
	}

	/**
	 * Unit test uses this to provide a mock token provider
	 *
	 * @param tokenProvider
	 */
	public void setTokenProvider(TokenProvider tokenProvider) {
		this.tokenProvider = tokenProvider;
	}

	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
		FileUploaderJnlp.perThreadRequest.set(arg0);
		super.service(arg0, arg1);
	}

	@Override
	public void service(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException {
		super.service(arg0, arg1);
	}


	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String entityId = request.getParameter(WebConstants.ENTITY_PARAM_KEY);
		Boolean isUpdate = Boolean.parseBoolean(request.getParameter(WebConstants.FILE_UPLOADER_IS_UPDATE_PARAM));

		if (jarUrl == null)
			try {
				setJarUrl();
			} catch (Exception e) {
				logger.warning(e.getMessage());
				throw new ServletException(e);
			}

		// TODO : remove this once deployment of the jar to artifactoryonline is complete
		jarUrl = "https://s3.amazonaws.com/versions.synapse.sagebase.org/fileUploadClient-develop-SNAPSHOT-jar-with-dependencies.jar";

		response.setContentType(WebConstants.CONTENT_TYPE_JNLP);
		PrintWriter out = null;
		try {
			out = response.getWriter();
			out.println(buildJnlp(getFullURL(request), jarUrl, getSessionToken(request), entityId, isUpdate));
		} finally {
			if (out != null)
				out.close();
		}
	}

	private void setJarUrl() throws SynapseException, JSONObjectAdapterException {
		SynapseClient synapse = synapseProvider.createNewClient();
		SynapseVersionInfo version = synapse.getVersionInfo();
		jarUrl = "http://sagebionetworks.artifactoryonline.com/sagebionetworks/simple/libs-releases-local/org/sagebionetworks/fileUploadClient/" + version.getVersion() + "/fileUploadClient-" + version.getVersion() + "-jar-with-dependencies.jar";
	}

	/**
	 * Get the session token
	 * 
	 * @param request
	 * @return
	 */
	public String getSessionToken(final HttpServletRequest request) {
		return tokenProvider.getSessionToken();
	}

	private static String buildJnlp(String jnlpServletUrl, String fileUploaderJarUrl, String sessionToken, String entityId, boolean isUpdate) {
		String jnlp = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<jnlp spec=\"1.0+\" href=\"" + jnlpServletUrl + "\" codebase=\"https://s3.amazonaws.com/versions.synapse.sagebase.org/\">\n" + "    <information>\n" + "        <title>Synapse File Uploader</title>\n" + "        <vendor>Synapse</vendor>\n" + "    </information>\n" + "    <resources>\n" + "        <!-- Application Resources -->\n" + "        <j2se version=\"1.7+\" href=\"http://java.sun.com/products/autodl/j2se\"/>\n" + "        <jar href=\"" + fileUploaderJarUrl + "\" main=\"true\" />\n" + "    </resources>\n" + "    <application-desc\n" + "         main-class=\"org.sagebionetworks.client.fileuploader.App\"\n" +
		// " width=\"800\"\n" + " height=\"600\"" +
				">\n" + "        <argument>--sessionToken=" + sessionToken + "</argument>\n" + "        <argument>--entityId=" + entityId + "</argument>\n" + "     </application-desc>\n" + "     <security><all-permissions/></security>\n" + "</jnlp>";
		return jnlp;
	}

	private static String getFullURL(HttpServletRequest request) {
		StringBuffer requestURL = request.getRequestURL();
		String queryString = request.getQueryString();

		if (queryString == null) {
			return requestURL.toString();
		} else {
			return requestURL.append('?').append(queryString).toString();
		}
	}
}
