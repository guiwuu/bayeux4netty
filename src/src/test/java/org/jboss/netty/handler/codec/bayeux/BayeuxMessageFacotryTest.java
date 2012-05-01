package org.jboss.netty.handler.codec.bayeux;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

public class BayeuxMessageFacotryTest {

	@Test
	public void testCreateFromJSON(){
		String json="{\"clientId\":123,    \"id\":1}";
		JSONParser parser=new JSONParser();
		Map map=(Map)parser.parse(json);
		BayeuxMessage bayeux=BayeuxMessageFactory.getInstance().create(map);
		BayeuxMessage expect=new BayeuxMessage();
		expect.clientId="123";
		expect.id="1";
		assertEquals(expect,bayeux);
	}
}
