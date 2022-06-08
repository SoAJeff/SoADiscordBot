package com.soa.rs.discordbot.v3.ipb.member;

public enum ForumRank {
	ONTARI(22, "Ontari"),
	ELDAR(4, "Eldar"),
	LIAN(7, "Lian"),
	ADMINISTRATOR(34, "Administrator"),
	ARQUENDI(6, "Arquendi"),
	ADELE(31, "Adele"),
	VORONWE(44, "Voronwë"),
	ELENDUR(37, "Elendur"),
	SADRON(24, "Sadron"),
	ATHEM(8, "Athem"),
	MYRTH(9, "Myrth"),
	BAEL(15, "Bael"),
	TYLAR(10, "Tylar"),
	APPLICANT(36, "Applicant"),
	REGISTERED(3, "Registered User");

	private final int id;
	private final String name;
	ForumRank(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
