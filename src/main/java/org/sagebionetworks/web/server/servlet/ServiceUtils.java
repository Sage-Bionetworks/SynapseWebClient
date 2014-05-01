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
	
	@Deprecated
	public static StringBuilder getBaseUrlBuilder(ServiceUrlProvider urlProvider, NodeType type) {
		StringBuilder builder = new StringBuilder();
		builder.append(urlProvider.getRepositoryServiceUrl() + "/");
		builder.append(REPOSVC_PATH_ENTITY);
		return builder;
	}
	
	@Deprecated
	public static String handleHttpClientErrorException(HttpClientErrorException ex) {
	//		if(ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
	//		throw new UnauthorizedException();
	//	} else if(ex.getStatusCode() == HttpStatus.FORBIDDEN) {
	//		throw new ForbiddenException();
	//	} else {
	//		throw new UnknownError("Status code:" + ex.getStatusCode().value());
	//	}
		
		// temporary solution to not being able to throw caught exceptions (due to Gin 1.0)
		JSONObject obj = new JSONObject();
		JSONObject errorObj = new JSONObject();
		try {
			Integer code = ex.getStatusCode().value(); 
			if(code != null) errorObj.put("statusCode", code);
			obj.put("error", errorObj);
			
		} catch (JSONException e) {
			throw new UnknownError();
		}
		
		String body = ex.getResponseBodyAsString();
		JSONObject reasonObj;
		try {
			reasonObj = new JSONObject(body);
			if(reasonObj.has(ERROR_REASON)) {
				String message = reasonObj.getString(ERROR_REASON);
				logger.info("Error Reason: " + message);
				obj.put(ERROR_REASON, message);
			}
		} catch (JSONException e) {
			logger.info(e.getMessage());			
		}
		return obj.toString();
	}
	
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

	/**
	 * Extracts a QueryDetails object from a TableQuery string
	 * @param query - A table query string
	 * @return
	 * @throws BadRequestException
	 */
	public static QueryDetails extractQueryDetails(String query) throws BadRequestException {
		try {			
			QuerySpecification spec = TableQueryParser.parserQuery(query);			
			QueryDetails details = new QueryDetails();
						
			// extract sorting
			if (spec != null 
					&& spec.getTableExpression() != null
					&& spec.getTableExpression().getOrderByClause() != null
					&& spec.getTableExpression().getOrderByClause().getSortSpecificationList() != null
					&& spec.getTableExpression().getOrderByClause().getSortSpecificationList().getSortSpecifications() != null
					&& spec.getTableExpression().getOrderByClause().getSortSpecificationList().getSortSpecifications().size() > 0) {
				SortSpecification sortSpec = spec.getTableExpression().getOrderByClause().getSortSpecificationList().getSortSpecifications().get(0);
				
				if (sortSpec != null
						&& sortSpec.getSortKey() != null
						&& sortSpec.getSortKey().getColumnReference() != null
						&& sortSpec.getSortKey().getColumnReference().getNameLHS() != null
						&& sortSpec.getSortKey().getColumnReference().getNameLHS().getIdentifier() != null
						&& sortSpec.getSortKey().getColumnReference().getNameLHS().getIdentifier().getActualIdentifier() != null) {					
					details.setSortedColumnName(sortSpec.getSortKey().getColumnReference().getNameLHS().getIdentifier().getActualIdentifier().getRegularIdentifier());
				}
				if(sortSpec.getOrderingSpecification() != null) {
					if(sortSpec.getOrderingSpecification() == OrderingSpecification.ASC) 
						details.setSortDirection(SortDirection.ASC);
					else
						details.setSortDirection(SortDirection.DESC);
				}
			}
			
			// extract pagination
			if(spec != null && spec.getTableExpression() != null && spec.getTableExpression().getPagination() != null) {
				details.setLimit(spec.getTableExpression().getPagination().getLimit());
				details.setOffset(spec.getTableExpression().getPagination().getOffset());
			}
			
			return details;
		} catch (ParseException e) {
			throw new BadRequestException("Query is malformed: " + e.getMessage());
		}		
	}

	/**
	 * Returns a new TableQuery string with modifications given by the provided QueryDetails
	 * @param query
	 * @param modifyingQd What to change in the query
	 * @return
	 * @throws BadRequestException
	 */
	public static String modifyQuery(String query, QueryDetails modifyingQd) throws BadRequestException {
		if(modifyingQd == null) return query;
		
		try {
			// parse
			QuerySpecification spec = TableQueryParser.parserQuery(query);				
			
			// order by
			OrderByClause newOrderByClause = null;
			if(modifyingQd.getSortedColumnName() != null && modifyingQd.getSortDirection() != null) {
				OrderingSpecification orderingSpec = modifyingQd.getSortDirection() == SortDirection.ASC ? OrderingSpecification.ASC : OrderingSpecification.DESC; 
				List<SortSpecification> sortList = new ArrayList<SortSpecification>();
				sortList.add(new SortSpecification(new SortKey(SqlElementUntils.createColumnReference(modifyingQd.getSortedColumnName())), orderingSpec));
				SortSpecificationList sortSpecList = new SortSpecificationList(sortList);
				newOrderByClause = new OrderByClause(sortSpecList);					
			}
			
			// pagination
			TableExpression table = spec.getTableExpression();
			String existingLimit = null;
			String exitingOffset = null;
			if(table.getPagination() != null) {
				if(table.getPagination().getLimit() != null) existingLimit = String.valueOf(table.getPagination().getLimit());
				if(table.getPagination().getOffset() != null) exitingOffset = String.valueOf(table.getPagination().getOffset());
			}
				
			String limit = modifyingQd.getLimit() != null ? modifyingQd.getLimit().toString() : existingLimit;
			String offset = modifyingQd.getOffset() != null ? modifyingQd.getOffset().toString() : exitingOffset;
			
			Pagination pagination = (limit != null || offset != null) ? new Pagination(limit, offset) : table.getPagination();
			OrderByClause orderByClause = newOrderByClause != null ? newOrderByClause : table.getOrderByClause();
			
			TableExpression newTableExpr = new TableExpression(table.getFromClause(), table.getWhereClause(), table.getGroupByClause(), orderByClause, pagination);
			QuerySpecification executedSpec = new QuerySpecification(spec.getSetQuantifier(), spec.getSelectList(), newTableExpr);
			 
			StringBuilder sb = new StringBuilder(); 
			executedSpec.toSQL(sb);
			return sb.toString();
		} catch (ParseException e) {
			throw new BadRequestException("Query is malformed: " + e.getMessage());
		}
	}

	
}
