package com.tryane.saas.personal.wurth.item;

public class WurthADUser {

	private String	id;

	private String	email;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String toPrint() {
		StringBuilder toPrint = new StringBuilder();
		toPrint.append(this.getId()).append(" | ");

		if (this.getEmail() == null || this.getEmail().isEmpty()) {
			toPrint.append("NULL");
		} else {
			toPrint.append(this.getEmail());
		}

		return toPrint.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WurthADUser other = (WurthADUser) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
