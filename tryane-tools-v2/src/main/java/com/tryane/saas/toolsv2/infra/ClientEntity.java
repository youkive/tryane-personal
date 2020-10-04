package com.tryane.saas.toolsv2.infra;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.tryane.saas.utils.jpa.CryptedStringToByteArrayConverter;

@Entity
@Table(name = "core_client")
public class ClientEntity {

	@Id
	@Column(name = "clientid")
	private String	id;

	@Column(name = "name")
	@Convert(converter = CryptedStringToByteArrayConverter.class)
	private String	name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
