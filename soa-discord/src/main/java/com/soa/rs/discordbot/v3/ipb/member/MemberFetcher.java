package com.soa.rs.discordbot.v3.ipb.member;

import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;

public class MemberFetcher {

	public MemberResults fetchMembers(int page, int[] groups) {
		Client client = ClientBuilder.newClient();
		HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(
				DiscordCfgFactory.getConfig().getEventListingEvent().getApiKey(), "");
		client.register(feature);
		client.register(JacksonJsonProvider.class);
		StringBuilder sb = new StringBuilder();
		sb.append("https://forums.soa-rs.com/api/core/members?");
		for (int g : groups) {
			sb.append("group[]=" + g + "&");
		}
		sb.append("page=" + page);
		sb.append("&perPage=50");
		String URL = sb.toString();
		SoaLogging.getLogger(this).debug("Sending forums query with URL " + URL);
		WebTarget target = client.target(URL);
		return target.request(MediaType.APPLICATION_JSON_TYPE).get(MemberResults.class);
	}
}
