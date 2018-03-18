package com.tryane.saas.personal.wurth.item;

/**
 * Description Here !
 *
 * @author Bastien
 * @version $Id $
 */
public class TryaneExportUser {

	private final String	PREFIX	= "i:0#.w|wf\\";

	private String			spId;

	private String			id;

	private Boolean			isAdmin;

	private String			email;

	public String getSpId() {
		return spId;
	}

	public void setSpId(String spId) {
		this.spId = spId;

		// Post Treatment
		String formatId = this.getSpId().replace(PREFIX, "").toLowerCase();
		this.isAdmin = formatId.startsWith("a-");

		if (this.isAdmin) {
			this.id = formatId.replace("a-", "").toLowerCase();
		} else {
			this.id = formatId.toLowerCase();
		}
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getId() {
		return id;
	}

	public Boolean getIsAdmin() {
		return isAdmin;
	}

	public String toPrint() {
		StringBuilder toPrint = new StringBuilder();
		toPrint.append(this.getSpId()).append(" | ").append(this.getEmail());
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
		TryaneExportUser other = (TryaneExportUser) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
