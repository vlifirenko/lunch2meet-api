package com.lunchandmeet.daos.impl;

import static org.junit.Assert.*;

import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;

import com.lunchandmeet.dto.User;

public class MessageDaoMongoTest {

	private MessageDaoMongo dao;

	@Before
	public void setUp() throws UnknownHostException {
		dao = new MessageDaoMongo();
	}

	@Test
	public void testGetMessages() {
		dao.getMessages(0, 59.939525604248047f, 30.312021255493164f);
	}

	// 51c5e07d993809b811d37674
	// 51c5e0a3993809b811d37677
	@Test
	public void testGetDialog() {
	}

	@Test
	public void testGetDialogs() {
		User user = new User();
		user.id = "51c5e0a3993809b811d37677";
		dao.getDialogs(user);
	}

}
