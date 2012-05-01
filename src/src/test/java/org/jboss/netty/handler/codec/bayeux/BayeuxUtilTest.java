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

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author daijun
 */
public class BayeuxUtilTest {

    @Test
    public void testGetCurrentTime() {
        System.out.println("getCurrentTime:");
        System.out.println(BayeuxUtil.getCurrentTime());
    }

    @Test
    public void testGenerateUUID() {
        System.out.println("getCurrentTime:");
        System.out.println(BayeuxUtil.generateUUID());
    }

    @Test
    public void testPrefixMatch(){
        System.out.println("Prefix matchingy...");
        String[] strings = {"/channel/*","/channel/**","/channel/a","/channel/a/aa","/channel/*/aa"};
        String[] expResult = {"/channel/*","/channel/**"};
        List<String> result =  BayeuxUtil.prefixMatch("/channel/abc", strings);
        assertArrayEquals(expResult, result.toArray());

        String[] expResult2 = {"/channel/*","/channel/**","/channel/a"};
        List<String> result2 =  BayeuxUtil.prefixMatch("/channel/*", strings);
        assertArrayEquals(expResult2, result2.toArray());

        String[] expResult3 = {"/channel/*","/channel/**","/channel/a","/channel/a/aa"};
        List<String> result3 =  BayeuxUtil.prefixMatch("/channel/**", strings);
        assertArrayEquals(expResult3, result3.toArray());
    }
}
