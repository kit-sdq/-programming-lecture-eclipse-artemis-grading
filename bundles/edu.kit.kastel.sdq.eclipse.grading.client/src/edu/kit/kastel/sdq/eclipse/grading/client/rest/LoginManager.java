package edu.kit.kastel.sdq.eclipse.grading.client.rest;

import java.io.Serializable;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.sdq.eclipse.grading.api.ArtemisClientException;
import edu.kit.kastel.sdq.eclipse.grading.api.artemis.mapping.Assessor;
import edu.kit.kastel.sdq.eclipse.grading.api.client.IAuthenticationArtemisClient;

public class LoginManager extends AbstractArtemisClient implements IAuthenticationArtemisClient {
	private static final ILog log = Platform.getLog(LoginManager.class);
	
	private String username;
	private String password;
	private String token;
	private WebTarget endpoint;
	private Assessor assessor;
	
	public LoginManager(String hostname, String username, String password) {
		super(hostname);
		this.username = username;
		this.password = password;	
		this.endpoint = getEndpoint(this.getApiRootURL());
	}
	
	public void init() throws ArtemisClientException {
		try {
			this.token = this.login();
			this.assessor = this.fetchAssesor();
		} catch(ProcessingException e) {
			throw new ArtemisClientException(e.getMessage(), e);
		}
	}
	
	@Override
	public String getRawToken() {
		return token;
	}
	
	@Override
	public String getBearerToken() {
		return "Bearer " + token;
	}
	
	@Override
	public Assessor getAssessor() {
		return assessor;
	}
	
	private Assessor fetchAssesor() throws ArtemisClientException {
		final Response rsp = this.endpoint.path(USERS_PATHPART).path(username).request().header(AUTHORIZATION_NAME, getBearerToken()).buildGet()
				.invoke();
		this.throwIfStatusUnsuccessful(rsp);
		return this.read(rsp.readEntity(String.class), Assessor.class);
	}
	
	private String login() throws ArtemisClientException,ProcessingException {
		String payload = this.payload(this.getAuthenticationEntity());
		final Response authenticationResponse = this.endpoint.path("authenticate").request().buildPost(Entity.json(payload)).invoke();

		this.throwIfStatusUnsuccessful(authenticationResponse);
		final String authRspEntity = authenticationResponse.readEntity(String.class);
		final String rawToken = this.readTree(authRspEntity).get("id_token").asText();
		return rawToken;
	}
	
	private final AuthenticationEntity getAuthenticationEntity() {
		AuthenticationEntity entity = new AuthenticationEntity();
		entity.username = this.username;
		entity.password = this.password;
		return entity;
	}

	private static final class AuthenticationEntity implements Serializable {
		private static final long serialVersionUID = -6291795795865534155L;
		@JsonProperty
		private String username;
		@JsonProperty
		private String password;
		@JsonProperty
		private boolean rememberMe = true;
	}	
}