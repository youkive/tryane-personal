package com.tryane.saas.modelviewer.model.common;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.tryane.saas.utils.jpa.CryptedStringToByteArrayConverter;

@Entity
@Table(name = "core_user")
public class User {

	@Id
	@Column(name = "userid")
	private Integer	id;

	@Column(name = "clientid")
	private Long	clientId;

	@Column(name = "displayname_enc")
	@Convert(converter = CryptedStringToByteArrayConverter.class)
	private String	displayName;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

}
