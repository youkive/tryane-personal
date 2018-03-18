package com.tryane.saas.personal.model.viewer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tryane.saas.core.client.Client;
import com.tryane.saas.core.client.ClientPaymentStatus;
import com.tryane.saas.core.client.ClientStatus;
import com.tryane.saas.core.client.module.Module;

public class ClientWrapper {

	@JsonIgnore
	private Client				client;

	@JsonProperty("id")
	private Long				clientId;

	@JsonProperty("name")
	private String				clientName;

	@JsonProperty("external_id")
	private String				externalId;

	@JsonProperty("subscription_sips_id")
	private String				subscriptionSIPSId;

	@JsonProperty("status")
	private ClientStatus		status;

	@JsonProperty("payment_status")
	private ClientPaymentStatus	paymentStatus;

	@JsonProperty("enabled")
	private Boolean				enabled;

	@JsonProperty("dedicated_url")
	private String				dedicatedUrl;

	@JsonProperty("modules")
	private List<Module>		modules;

	public ClientWrapper(Client client) {
		this.client = client;
		this.clientId = client.getClientId();
		this.clientName = client.getClientName();
		this.externalId = client.getExternalId();
		this.subscriptionSIPSId = client.getSubscriptionSIPSId();
		this.status = client.getStatus();
		this.paymentStatus = client.getPaymentStatus();
		this.enabled = client.getEnabled();
		this.dedicatedUrl = client.getDedicatedURL();
		this.modules = client.getModules();
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getSubscriptionSIPSId() {
		return subscriptionSIPSId;
	}

	public void setSubscriptionSIPSId(String subscriptionSIPSId) {
		this.subscriptionSIPSId = subscriptionSIPSId;
	}

	public ClientStatus getStatus() {
		return status;
	}

	public void setStatus(ClientStatus status) {
		this.status = status;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public String getDedicatedUrl() {
		return dedicatedUrl;
	}

	public void setDedicatedUrl(String dedicatedUrl) {
		this.dedicatedUrl = dedicatedUrl;
	}

	public List<Module> getModules() {
		return modules;
	}

	public void setModules(List<Module> modules) {
		this.modules = modules;
	}

}
