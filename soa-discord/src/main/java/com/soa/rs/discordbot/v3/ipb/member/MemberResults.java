package com.soa.rs.discordbot.v3.ipb.member;

import java.util.List;

public class MemberResults {
	private int page;
	private int perPage;
	private int totalResults;
	private int totalPages;
	private List<Member> results;

	public int getPage() {
		return page;
	}

	public int getPerPage() {
		return perPage;
	}

	public int getTotalResults() {
		return totalResults;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public List<Member> getResults() {
		return results;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public void setPerPage(int perPage) {
		this.perPage = perPage;
	}

	public void setTotalResults(int totalResults) {
		this.totalResults = totalResults;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public void setResults(List<Member> results) {
		this.results = results;
	}
}