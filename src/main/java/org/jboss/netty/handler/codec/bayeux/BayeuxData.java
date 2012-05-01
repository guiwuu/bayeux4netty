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
import java.util.Map;

/**
 * A Bayeux <a href="http://svn.cometd.org/trunk/bayeux/bayeux.html#toc_40">Data</a>
 * 
 * Bayeux data is a normal Javascript object in client, and it is mapped to a
 * Java Map in server. BayeuxData wraps a Map with useful functions to make it
 * easy to use.
 *
 * @author daijun
 */
public class BayeuxData implements BayeuxInterface {

    protected Map map;

    public BayeuxData() {
        map = new HashMap();
    }

    public BayeuxData(Map map) {
        this.map = map;
    }

    @Override
    public String toJSON() {
        return JSONParser.toJSON(map);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean contains(String property) {
        return map.containsKey(property);
    }

    public Object get(String key) {
        return map.get(key);
    }

    public void put(String key, Object o) {
        map.put(key, o);
    }
}
