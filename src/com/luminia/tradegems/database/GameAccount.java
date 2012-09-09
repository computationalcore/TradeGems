package com.luminia.tradegems.database;

/**
 * This class is called GameAccount to avoid collision with the class android.accounts.Account.
 * The purpose of this class is just to encapsulate the account information
 * about a user.
 * 
 * Right now the only information we care about is the user's email. But this might
 * be extended in future version.
 * 
 * @version 	1.00 08 Sep 2012
 * @author Nelson R. Perez - bilthon@gmail.com
 *
 */
public class GameAccount {
	private String email;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
