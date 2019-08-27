package com.soa.rs.discordbot.v3.jdbi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.soa.rs.discordbot.v3.jdbi.entities.GuildUser;
import com.soa.rs.discordbot.v3.jdbi.entities.RecentAction;
import com.soa.rs.discordbot.v3.jdbi.entities.UsernameRecentAction;

import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RecentActionUtilityTest {

	RecentActionUtility util = new RecentActionUtility();
	GuildUserUtility userUtility = new GuildUserUtility();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-dd-MM");

	@BeforeClass
	public static void setUp() {
		Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
		JdbiWrapper.getInstance().setJdbi(jdbi);
	}

	@Before
	public void createDb() {
		util.createRecentActionsTable();
	}

	@After
	public void tearDown() {
		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.execute("drop table recentactions"));
	}

	@Test
	public void addRecentActionViaArgsActionOnly()
	{
		util.addRecentAction(1234, 6789, "Joined the server.");
		Date now = new Date();

		List<RecentAction> actionList = JdbiWrapper.getInstance().getJdbi().withHandle(handle -> handle.createQuery("select * from recentactions").mapToBean(RecentAction.class).list());

		Assert.assertEquals(1, actionList.size());

		RecentAction action = actionList.get(0);

		Assert.assertEquals(1234, action.getGuildSnowflake());
		Assert.assertEquals(1, action.getActionId());
		Assert.assertEquals(6789, action.getUserSnowflake());
		Assert.assertEquals(sdf.format(now), sdf.format(action.getDate()));
		Assert.assertEquals("Joined the server.", action.getAction());

		Assert.assertNull(action.getNewValue());
		Assert.assertNull(action.getOriginalValue());
	}

	@Test
	public void addRecentActionViaArgsActionAndNewValueOnly()
	{
		util.addRecentAction(1234, 6789, "Joined the server.", "Today");
		Date now = new Date();

		List<RecentAction> actionList = JdbiWrapper.getInstance().getJdbi().withHandle(handle -> handle.createQuery("select * from recentactions").mapToBean(RecentAction.class).list());

		Assert.assertEquals(1, actionList.size());

		RecentAction action = actionList.get(0);

		Assert.assertEquals(1234, action.getGuildSnowflake());
		Assert.assertEquals(1, action.getActionId());
		Assert.assertEquals(6789, action.getUserSnowflake());
		Assert.assertEquals(sdf.format(now), sdf.format(action.getDate()));
		Assert.assertEquals("Joined the server.", action.getAction());
		Assert.assertEquals("Today", action.getNewValue());


		Assert.assertNull(action.getOriginalValue());
	}

	@Test
	public void addRecentActionViaArgsActionAllValues()
	{
		util.addRecentAction(1234, 6789, "Joined the server.", "Yesterday", "Today");
		Date now = new Date();

		List<RecentAction> actionList = JdbiWrapper.getInstance().getJdbi().withHandle(handle -> handle.createQuery("select * from recentactions").mapToBean(RecentAction.class).list());

		Assert.assertEquals(1, actionList.size());

		RecentAction action = actionList.get(0);

		Assert.assertEquals(1234, action.getGuildSnowflake());
		Assert.assertEquals(1, action.getActionId());
		Assert.assertEquals(6789, action.getUserSnowflake());
		Assert.assertEquals(sdf.format(now), sdf.format(action.getDate()));
		Assert.assertEquals("Joined the server.", action.getAction());
		Assert.assertEquals("Yesterday", action.getOriginalValue());
		Assert.assertEquals("Today", action.getNewValue());
	}

	@Test
	public void addRecentActionViaObjectArgs()
	{
		RecentAction actionToAdd = new RecentAction();
		actionToAdd.setGuildSnowflake(1234);
		actionToAdd.setUserSnowflake(6789);
		actionToAdd.setAction("Joined the server.");

		Date now = new Date();

		util.addRecentAction(actionToAdd);

		List<RecentAction> actionList = JdbiWrapper.getInstance().getJdbi().withHandle(handle -> handle.createQuery("select * from recentactions").mapToBean(RecentAction.class).list());

		Assert.assertEquals(1, actionList.size());

		RecentAction action = actionList.get(0);

		Assert.assertEquals(1234, action.getGuildSnowflake());
		Assert.assertEquals(1, action.getActionId());
		Assert.assertEquals(6789, action.getUserSnowflake());
		Assert.assertEquals(sdf.format(now), sdf.format(action.getDate()));
		Assert.assertEquals("Joined the server.", action.getAction());

		Assert.assertNull(action.getNewValue());
		Assert.assertNull(action.getOriginalValue());
	}

	@Test
	public void addRecentActionViaObjectAllArgs()
	{
		RecentAction actionToAdd = new RecentAction();
		actionToAdd.setGuildSnowflake(1234);
		actionToAdd.setUserSnowflake(6789);
		actionToAdd.setAction("Joined the server.");
		actionToAdd.setOriginalValue("Yesterday");
		actionToAdd.setNewValue("Today");

		Date now = new Date();

		util.addRecentAction(actionToAdd);

		List<RecentAction> actionList = JdbiWrapper.getInstance().getJdbi().withHandle(handle -> handle.createQuery("select * from recentactions").mapToBean(RecentAction.class).list());

		Assert.assertEquals(1, actionList.size());

		RecentAction action = actionList.get(0);

		Assert.assertEquals(1234, action.getGuildSnowflake());
		Assert.assertEquals(1, action.getActionId());
		Assert.assertEquals(6789, action.getUserSnowflake());
		Assert.assertEquals(sdf.format(now), sdf.format(action.getDate()));
		Assert.assertEquals("Joined the server.", action.getAction());
		Assert.assertEquals("Yesterday", action.getOriginalValue());
		Assert.assertEquals("Today", action.getNewValue());
	}

	@Test
	public void getRecentActionList() {
		RecentAction actionToAdd = new RecentAction();
		actionToAdd.setGuildSnowflake(1234);
		actionToAdd.setUserSnowflake(6789);
		actionToAdd.setAction("Joined the server.");

		Date now = new Date();

		util.addRecentAction(actionToAdd);

		List<RecentAction> actionList = util.getRecentActions();

		Assert.assertEquals(1, actionList.size());

		RecentAction action = actionList.get(0);

		Assert.assertEquals(1234, action.getGuildSnowflake());
		Assert.assertEquals(1, action.getActionId());
		Assert.assertEquals(6789, action.getUserSnowflake());
		Assert.assertEquals(sdf.format(now), sdf.format(action.getDate()));
		Assert.assertEquals("Joined the server.", action.getAction());
	}

	@Test
	public void getRecentActionListLimit10() {
		RecentAction actionToAdd = new RecentAction();
		actionToAdd.setGuildSnowflake(1234);
		actionToAdd.setUserSnowflake(6789);
		actionToAdd.setAction("Joined the server.");

		Date now = new Date();

		for(int i = 0; i < 15; i++)
			util.addRecentAction(actionToAdd);

		List<RecentAction> actionList = util.getRecentActionsLimitByN(10);

		Assert.assertEquals(10, actionList.size());

		RecentAction action = actionList.get(0);

		Assert.assertEquals(1234, action.getGuildSnowflake());
		Assert.assertEquals(15, action.getActionId());
		Assert.assertEquals(6789, action.getUserSnowflake());
		Assert.assertEquals(sdf.format(now), sdf.format(action.getDate()));
		Assert.assertEquals("Joined the server.", action.getAction());

		Assert.assertEquals(actionList.get(1).getActionId(), 14);
	}

	@Test
	public void getRecentActionListLimit10WithUserName() {
		RecentAction actionToAdd = new RecentAction();
		actionToAdd.setGuildSnowflake(1234);
		actionToAdd.setUserSnowflake(6789);
		actionToAdd.setAction("Joined the server.");

		Date now = new Date();

		for(int i = 0; i < 15; i++)
			util.addRecentAction(actionToAdd);

		userUtility.createUsersTable();

		GuildUser user = new GuildUser();
		user.setSnowflake(6789);
		user.setGuildSnowflake(1234);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");

		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);

		userUtility.addNewUser(user);

		List<UsernameRecentAction> actionList = util.getRecentActionsLimitByNWithUsername(10);

		Assert.assertEquals(10, actionList.size());

		UsernameRecentAction action = actionList.get(0);

		Assert.assertEquals(1234, action.getGuildSnowflake());
		Assert.assertEquals(15, action.getActionId());
		Assert.assertEquals(6789, action.getUserSnowflake());
		Assert.assertEquals(sdf.format(now), sdf.format(action.getDate()));
		Assert.assertEquals("Joined the server.", action.getAction());
		Assert.assertEquals("@User#1234", action.getUsername());

		Assert.assertEquals(actionList.get(1).getActionId(), 14);

		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.execute("drop table users"));
	}

	@Test
	public void getRecentActionListLimit10WithUserNameAndGuild() {
		RecentAction actionToAdd = new RecentAction();
		actionToAdd.setGuildSnowflake(1234);
		actionToAdd.setUserSnowflake(6789);
		actionToAdd.setAction("Joined the server.");

		Date now = new Date();

		for(int i = 0; i < 15; i++)
			util.addRecentAction(actionToAdd);
		actionToAdd.setGuildSnowflake(5678);
		util.addRecentAction(actionToAdd);

		userUtility.createUsersTable();

		GuildUser user = new GuildUser();
		user.setSnowflake(6789);
		user.setGuildSnowflake(1234);
		user.setUsername("@User#1234");
		user.setKnownName("User");
		user.setDisplayName("User");

		Date date = new Date();
		user.setJoinedServer(date);
		user.setLastSeen(date);

		userUtility.addNewUser(user);
		user.setGuildSnowflake(6789);
		userUtility.addNewUser(user);

		List<UsernameRecentAction> actionList = util.getRecentActionsLimitByNWithUsername(10, 5678);

		Assert.assertEquals(1, actionList.size());

		JdbiWrapper.getInstance().getJdbi().useHandle(handle -> handle.execute("drop table users"));
	}

}
