package com.soa.rs.discordbot.v3.ipb.events;

import java.util.List;

public class CalendarResults {
	private int page;
	private int perPage;
	private int totalResults;
	private int totalPages;
	private List<Event> results;

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPerPage() {
		return perPage;
	}

	public void setPerPage(int perPage) {
		this.perPage = perPage;
	}

	public int getTotalResults() {
		return totalResults;
	}

	public void setTotalResults(int totalResults) {
		this.totalResults = totalResults;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public List<Event> getResults() {
		return results;
	}

	public void setResults(List<Event> results) {
		this.results = results;
	}
}
