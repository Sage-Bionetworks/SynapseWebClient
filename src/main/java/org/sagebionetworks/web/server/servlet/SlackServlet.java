package org.sagebionetworks.web.server.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.annotation.v2.Annotations;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.web.client.StackEndpoints;
import org.sagebionetworks.web.client.exceptions.IllegalArgumentException;
import org.sagebionetworks.web.shared.WebConstants;

public class SlackServlet extends HttpServlet {

	public static final String IS_INVALID_SYN_ID = " is not a valid Synapse ID (in the form syn123).";

	public static final String INVALID_COMMAND_MESSAGE = "Sorry, I didn't recognize your command.";

	private static final long serialVersionUID = 1L;

	protected static final ThreadLocal<HttpServletRequest> perThreadRequest = new ThreadLocal<HttpServletRequest>();
	public static final String COMMA = ", ";
	public static final String SYNAPSE_ID_REGEX = "\\s*[sS]{1}[yY]{1}[nN]{1}\\d+\\s*";
	Pattern p = Pattern.compile(SYNAPSE_ID_REGEX);

	private SynapseProvider synapseProvider = new SynapseProviderImpl();

	/**
	 * Unit test can override this.
	 *
	 * @param fileHandleProvider
	 */
	public void setSynapseProvider(SynapseProvider synapseProvider) {
		this.synapseProvider = synapseProvider;
	}

	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
		SlackServlet.perThreadRequest.set(arg0);
		super.service(arg0, arg1);
	}

	@Override
	public void service(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException {
		super.service(arg0, arg1);
	}

	@Override
	public void doGet(HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws ServletException, IOException {
		// instruct not to cache
		response.setHeader(WebConstants.CACHE_CONTROL_KEY, WebConstants.CACHE_CONTROL_VALUE_NO_CACHE); // Set standard HTTP/1.1 no-cache headers.
		response.setHeader(WebConstants.PRAGMA_KEY, WebConstants.NO_CACHE_VALUE); // Set standard HTTP/1.0 no-cache header.
		response.setDateHeader(WebConstants.EXPIRES_KEY, 0L);
		String text = request.getParameter("text");
		String command = request.getParameter("command");
		// other information available
		// String token = request.getParameter("token");
		// String teamId = request.getParameter("team_id");
		// String teamDomain = request.getParameter("team_domain");
		// String channelId = request.getParameter("channel_id");
		// String channelName = request.getParameter("channel_name");
		// String userName = request.getParameter("user_name");
		// String responseUrl = request.getParameter("response_url");
		ByteArrayOutputStream bytes = new ByteArrayOutputStream(1024);
		PrintWriter out = new PrintWriter(bytes, true);
		String entityURL = "";
		try {
			response.setContentType("application/json");
			String lowercaseCommand = command.toLowerCase();
			if (lowercaseCommand.equals("/synapse") || lowercaseCommand.equals("/synapsestaging")) {
				String title = null;
				StringBuilder sb = new StringBuilder();
				if (text.toLowerCase().equals("help")) {
					sb.append("Given a Synapse ID (like syn123), post public information about that entity.");
				} else {
					Matcher m = p.matcher(text);
					if (m.matches()) {
						entityURL = "https://www.synapse.org/#!Synapse:" + text.trim();
						sb.append(entityURL);
					} else {
						throw new IllegalArgumentException(text + IS_INVALID_SYN_ID);
					}
					SynapseClient client = createNewClient();
					// extend
					// int partsMask = ENTITY | ENTITY_PATH | ANNOTATIONS | ROOT_WIKI_ID | FILE_HANDLES | PERMISSIONS |
					// BENEFACTOR_ACL | THREAD_COUNT;
					EntityBundleRequest bundleRequest = new EntityBundleRequest();
					bundleRequest.setIncludeEntity(true);
					bundleRequest.setIncludeEntityPath(true);
					bundleRequest.setIncludeThreadCount(true);
					bundleRequest.setIncludeAnnotations(true);
					EntityBundle bundle = client.getEntityBundleV2(text, bundleRequest);
					title = bundle.getEntity().getName();
					List<EntityHeader> path = bundle.getPath().getPath();
					if (path.size() > 2) {
						sb.append("\n*Project:* " + path.get(1).getName());
					}
					if (bundle.getThreadCount() > 0) {
						sb.append("\n*Discussion thread count:* " + bundle.getThreadCount());
					}
					String annotationString = getAnnotationString(bundle.getAnnotations());
					if (!annotationString.isEmpty()) {
						sb.append("\n*Annotations:*");
						sb.append(annotationString);
					}
				}

				JSONObject json = new JSONObject();
				// to send response to user, change "response_type" to "ephemeral"
				// to send response to channel, change "response_type" to "in_channel"
				json.put("response_type", "in_channel");
				JSONObject attachments = new JSONObject();
				if (title != null) {
					attachments.put("title", title);
				}
				attachments.put("text", sb.toString());
				JSONArray jsonArray = new JSONArray();
				jsonArray.put("text");
				attachments.put("mrkdwn_in", jsonArray);
				jsonArray = new JSONArray();
				jsonArray.put(attachments);
				json.put("attachments", jsonArray);
				out.println(json.toString());
				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				sendFailure(out, INVALID_COMMAND_MESSAGE);
			}
		} catch (Exception e) {
			try {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				sendFailure(out, entityURL + "\n" + text + ": " + e.getMessage());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		response.setContentLength(bytes.size());
		bytes.writeTo(response.getOutputStream());
	}

	private void sendFailure(PrintWriter out, String error) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("response_type", "in_channel");
		json.put("text", error);
		out.println(json.toString());
	}

	private String getAnnotationString(Annotations annos) {
		StringBuilder sb = new StringBuilder();
		if (annos != null) {
			// Strings
			if (annos.getAnnotations() != null) {
				for (String key : annos.getAnnotations().keySet()) {
					sb.append("\n ");
					sb.append(key);
					sb.append(" : ");

					sb.append(join(annos.getAnnotations().get(key).getValue()));
				}
			}
		}
		return sb.toString();
	}

	public static String join(List list) {
		StringBuilder sb = new StringBuilder();
		for (Object s : list) {
			sb.append(COMMA).append(s);
		}
		String result = sb.toString();
		if (list.size() > 0) {
			result = result.substring(COMMA.length());
		}
		return result;
	}

	/**
	 * Create a new Synapse client.
	 *
	 * @return
	 */
	private SynapseClient createNewClient() {
		SynapseClient client = synapseProvider.createNewClient();
		client.setAuthEndpoint(StackEndpoints.getAuthenticationServicePublicEndpoint());
		client.setRepositoryEndpoint(StackEndpoints.getRepositoryServiceEndpoint());
		client.setFileEndpoint(StackEndpoints.getFileServiceEndpoint());
		return client;
	}

}
