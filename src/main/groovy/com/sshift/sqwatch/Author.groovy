package com.sshift.sqwatch

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Transient

@Entity
class Author {

	private @Id @GeneratedValue Long id
	private String name
	private String primaryEmail
	private String secondaries
	@Transient
	private HashSet<String> secondaryEmails
	private String team

	static final NO_AUTHOR = new Author("", "", "other", [])

	Author() {}

	Author(String name, String primaryEmail, String team, List<String> secondaryEmails = []) {
		this.name = name;
		this.primaryEmail = primaryEmail
		this.secondaryEmails = secondaryEmails.toSet()
		this.secondaries = secondaryEmails.join(' ')
		this.team = team
	}

	@Override
	boolean equals(Object o) {
		if (this.is(o)) return true;
		if (o.is(null) || getClass() != o.getClass()) return false
		Author author = (Author) o
		return Objects.equals(id, author.id) &&
				Objects.equals(primaryEmail, author.primaryEmail) &&
				Objects.equals(secondaryEmails, author.secondaryEmails.toSet()) &&
				Objects.equals(team, author.team)
	}

	@Override
	int hashCode() {
		return Objects.hash(id, name, primaryEmail, secondaryEmails, team)
	}

	Long getId() {
		return id
	}

	String getName() {
		return name
	}

	void setName(String name) {
		this.name = name
	}

	String getTeam() {
		return team
	}

	String setTeam(String team) {
		this.team = team
	}

	String getPrimaryEmail() {
		return primaryEmail
	}

	void setPrimaryEmail(String primaryEmail) {
		this.primaryEmail = primaryEmail
	}

	List<String> getSecondaryEmails() {
		return this.secondaryEmails?.toList() ?: []
	}

	void setSecondaryEmails(List<String> secondaryEmails) {
		this.secondaryEmails = secondaryEmails.toSet()
		this.secondaries = secondaryEmails.join(' ')
	}

	String getSecondaries() {
		return this.secondaries
	}

	void setSecondaries(String secondaries) {
		List<String> emails = secondaries.split(' ')
		this.secondaryEmails = emails.toSet()
		this.secondaries = this.secondaryEmails.join(' ')
	}

}
