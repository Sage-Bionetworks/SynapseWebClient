package org.sagebionetworks.web.server.servlet;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.SynapseClientImpl;
import org.sagebionetworks.client.SynapseProfileProxy;
import org.sagebionetworks.table.query.ParseException;
import org.sagebionetworks.table.query.TableQueryParser;
import org.sagebionetworks.table.query.model.OrderByClause;
import org.sagebionetworks.table.query.model.OrderingSpecification;
import org.sagebionetworks.table.query.model.Pagination;
import org.sagebionetworks.table.query.model.QuerySpecification;
import org.sagebionetworks.table.query.model.SortKey;
import org.sagebionetworks.table.query.model.SortSpecification;
import org.sagebionetworks.table.query.model.SortSpecificationList;
import org.sagebionetworks.table.query.model.TableExpression;
import org.sagebionetworks.table.query.util.SqlElementUntils;
import org.sagebionetworks.web.shared.NodeType;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.table.QueryDetails;
import org.sagebionetworks.web.shared.table.QueryDetails.SortDirection;
import org.springframework.web.client.HttpClientErrorException;

public class ServiceUtils {

	private static final String ERROR_REASON = "reason";

	private static Logger logger = Logger.getLogger(ServiceUtils.class.getName());
	
	public static final String REPOSVC_PATH_ENTITY = "entity";
	public static final String REPOSVC_PATH_HAS_ACCESS = "access";
	public static final String REPOSVC_PATH_GET_USERS = "user";
	public static final String REPOSVC_PATH_PUBLIC_PROFILE = "publicprofile";
	public static final String REPOSVC_SUFFIX_PATH_ANNOTATIONS = "annotations";
	public static final String REPOSVC_SUFFIX_PATH_PREVIEW = "preview";
	public static final String REPOSVC_SUFFIX_LOCATION_PATH = "location";	
	public static final String REPOSVC_SUFFIX_PATH_SCHEMA = "schema";
	public static final String REPOSVC_SUFFIX_PATH_ACL = "acl"; 	
	public static final String REPOSVC_SUFFIX_PATH_TYPE = "type";
	public static final String REPOSVC_SUFFIX_PATH_BENEFACTOR = "benefactor"; 
	
	public static final String AUTHSVC_GET_GROUPS_PATH = "userGroup";
	
	public static final String AUTHSVC_ACL_PRINCIPAL_NAME = "name";
	public static final String AUTHSVC_ACL_PRINCIPAL_ID = "id";
	public static final String AUTHSVC_ACL_PRINCIPAL_CREATION_DATE = "creationDate";
	public static final String AUTHSVC_ACL_PRINCIPAL_URI = "uri";
	public static final String AUTHSVC_ACL_PRINCIPAL_ETAG = "etag";
	public static final String AUTHSVC_ACL_PRINCIPAL_INDIVIDUAL = "individual";
	
	/**
	 * The synapse client is stateful so we must create a new one for each request
	 */
	public static SynapseClient createSynapseClient(TokenProvider tokenProvider, ServiceUrlProvider urlProvider) {
		SynapseClient synapseClient = SynapseProfileProxy.createProfileProxy(new SynapseClientImpl());
		synapseClient.setSessionToken(tokenProvider.getSessionToken());
		synapseClient.setRepositoryEndpoint(urlProvider.getRepositoryServiceUrl());
		synapseClient.setAuthEndpoint(urlProvider.getPublicAuthBaseUrl());
		return synapseClient;
	}	
	
	public static SynapseClient createSynapseClient(SynapseProvider synapseProvider, ServiceUrlProvider urlProvider, String sessionToken) {
		SynapseClient client = synapseProvider.createNewClient();
		client.setAuthEndpoint(urlProvider.getPrivateAuthBaseUrl());
		client.setRepositoryEndpoint(urlProvider.getRepositoryServiceUrl());
		client.setSessionToken(sessionToken);
		return client;
	}

	
	
	public static void writeToFile(File temp, InputStream stream, final long maxAttachmentSizeBytes) throws IOException {
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(temp, false));
		try {
			long size = 0;
			byte[] buffer = new byte[1024];
			int length = 0;
			while ((length = stream.read(buffer)) > 0) {
				out.write(buffer, 0, length);
				size += length;
				if(size > maxAttachmentSizeBytes) throw new IllegalArgumentException("File size exceeds the limit of "+maxAttachmentSizeBytes+" MB for attachments");
			}
		} catch (Throwable e) {
			// if is any errors delete the tmp file
			if (out != null) {
				out.close();
			}
			temp.delete();
			throw new RuntimeException(e);
		}finally {
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	 * Write the data in the passed input stream to a temp file.
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	public static File writeToTempFile(InputStream stream, final long maxAttachmentSizeBytes) throws IOException {
		File temp = File.createTempFile("tempUploadedFile", ".tmp");
		writeToFile(temp, stream, maxAttachmentSizeBytes);
		return temp;
	}
}
