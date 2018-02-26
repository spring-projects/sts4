package org.test;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class TradingUser {

	@Id
	private String id;

	private String userName;

	private String fullName;

	public TradingUser() {
	}

	public TradingUser(String id, String userName, String fullName) {
		this.id = id;
		this.userName = userName;
		this.fullName = fullName;
	}

	public TradingUser(String userName, String fullName) {
		this.userName = userName;
		this.fullName = fullName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TradingUser that = (TradingUser) o;

		if (!id.equals(that.id)) return false;
		return userName.equals(that.userName);
	}

	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + userName.hashCode();
		return result;
	}
}

