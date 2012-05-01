/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.netty.handler.codec.bayeux;

import java.util.HashMap;
import org.jboss.netty.handler.codec.bayeux.BayeuxConnection.TYPE;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author daijun
 */
public class JSONParserTest {

    @Test
    public void testParseJSONObject() throws Exception{
        System.out.println("Parsing JSON object...");
        String s = "{\"string\": \"channel\",\"int\":123,\"float\":1000.1,\"double\":1.23E10,\"boolean\":true,\"null\":null}";
        JSONParser instance = new JSONParser();
        HashMap expResult = new HashMap();
        expResult.put("string", "channel");
        expResult.put("int", 123l);
        expResult.put("float", 1000.1d);
        expResult.put("double", 1.23E10d);
        expResult.put("boolean", true);
        expResult.put("null", null);
        Object result = instance.parse(s);
        assertEquals(expResult, result);
    }

    @Test
    public void testParseJSONArray() throws Exception{
        System.out.println("Parsing JSON array...");
        String s = "[\"string\",123,1000.1,null,true,{},[]]";
        JSONParser instance = new JSONParser();
        Object[] expResult = new Object[7];
        expResult[0] = "string";
        expResult[1] = 123l;
        expResult[2] = 1000.1;
        expResult[3] = null;
        expResult[4] = true;
        expResult[5] = new HashMap();
        expResult[6] = new Object[0];
        Object[] result = (Object[]) instance.parse(s);
        assertArrayEquals(expResult, result);
    }

    @Test
    public void testParseJSONArrayObjectNested() throws Exception{
        System.out.println("Parsing JSON array and object nested...");
        String s = "[{\"id\":1,\"name\":\"Peter\",\"car\":{\"name\":\"Pasta\",\"date\":\"12/5/1999\"}}," + "{\"id\":2,\"name\":\"Bruce\"}]";
        JSONParser instance = new JSONParser();
        
        HashMap peter = new HashMap();
        peter.put("id", 1l);
        peter.put("name", "Peter");
        HashMap car = new HashMap();
        car.put("name", "Pasta");
        car.put("date", "12/5/1999");
        peter.put("car", car);

        HashMap bruce = new HashMap();
        bruce.put("id", 2l);
        bruce.put("name", "Bruce");
        Object[] expResult = new Object[2];
        expResult[0]=peter;
        expResult[1]=bruce;
        Object[] result = (Object[]) instance.parse(s);
        assertArrayEquals(expResult, result);
    }

    @Test
    public void testArrayToJSON() throws Exception {
        System.out.println("Converting Array to JSON array...");
        String[] array = {"123", "xzzzz", "tttt"};
        Object obj = array;
        String expResult = "[\"123\",\"xzzzz\",\"tttt\"]";
        String result = JSONParser.toJSON(obj);
        assertEquals(expResult, result);
    }

    @Test
    public void testMapToJSON() throws Exception{
        System.out.println("Converting Map to JSON object...");
        Map map = new HashMap();
        map.put("id", 1);
        map.put("name", "Danny Green");
        map.put("age", 12l);
        map.put("boy", true);
        map.put("girl", false);
        map.put("money", 5000.0);
        map.put("kids", null);
        Object obj = map;
        String expResult = "{\"id\":1,\"girl\":false,\"age\":12,\"name\":\"Danny Green\",\"money\":5000.0,\"kids\":null,\"boy\":true}";
        String result = JSONParser.toJSON(obj);
        assertEquals(expResult, result);
    }

    @Test
    public void testBayeuxMessageToJSON() throws Exception{
        System.out.println("Converting BayeuxMessage to JSON...");
        BayeuxMessage bayeux=new BayeuxMessage();
        bayeux.channel="/meta/handshake";
        bayeux.version="1.0";
        bayeux.supportedConnectionTypes=new TYPE[]{TYPE.CALLBACK_POLLING,TYPE.LONG_POLLING};
        HandshakeRequest handshakeRequest=new HandshakeRequest(bayeux);
        HandshakeResponse handshakeResponse=new HandshakeResponse(handshakeRequest);
        handshakeResponse.setTimestamp(null);
        BayeuxMessage[] array=new BayeuxMessage[2];
        array[0]=handshakeRequest;
        array[1]=handshakeResponse;
        Object obj = array;
        String expResult = "[{\"channel\":\"/meta/handshake\",\"supportedConnectionTypes\":[\"callback-polling\",\"long-polling\"],\"version\":\"1.0\"}," +
                "{\"channel\":\"/meta/handshake\"}]";
        String result = JSONParser.toJSON(obj);
        assertEquals(expResult, result);
    }
}
