package br.ufpe.cin.groundhog;

import java.util.Date;

/**
 * Represents a GitHub User in Groundhog
 * @author gustavopinto, Rodrigo Alves
 */
public class User {
	private int id;
	
	private String login;
	private String email;
	private String company;
	private String location;
	private String blog;
	
	private boolean hireable;
	
	private int followers;
	private int following;
	public int public_repos;
	public int public_gists;
	
	private Date created_at;
	private Date updated_at;
	
	/**
	 * Informs the GitHub ID for the {@link User} object in question
	 * This ID is unique in GitHub, which means no two users can have the same ID on GitHub
	 * @return the integer ID
	 */
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
    /**
     * Informs the login (username) of the {@link User}
     * @return
     */
	public String getLogin() {
		return this.login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getCompany() {
		return this.company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getBlog() {
		return this.blog;
	}

	public void setBlog(String blog) {
		this.blog = blog;
	}

	public boolean isHireable() {
		return this.hireable;
	}

	public void setHireable(boolean hireable) {
		this.hireable = hireable;
	}

	public int getFollowers() {
		return this.followers;
	}

	public void setFollowers(int followers) {
		this.followers = followers;
	}

	public int getFollowing() {
		return this.following;
	}

	public void setFollowing(int following) {
		this.following = following;
	}

	public Date getCreated_at() {
		return this.created_at;
	}

	public void setCreated_at(Date created_at) {
		this.created_at = created_at;
	}

	public Date getUpdatedAt() {
		return this.updated_at;
	}

	public void setUpdatedAt(Date updated_at) {
		this.updated_at = updated_at;
	}
	
	public int getPublic_repos() {
		return this.public_repos;
	}

	public void setPublic_repos(int public_repos) {
		this.public_repos = public_repos;
	}

	public int getPublic_gists() {
		return this.public_gists;
	}

	public void setPublic_gists(int public_gists) {
		this.public_gists = public_gists;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}