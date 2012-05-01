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

import java.util.Map;

/**
 * A Bayeux <a href="http://svn.cometd.org/trunk/bayeux/bayeux.html#toc_32">Advice</a>.
 *
 * It has three default properties more than BayeuxData, which are used by
 * protocol itself. If needed, it can be extended to have more properties.
 *
 * @author daijun
 */
public class BayeuxAdvice extends BayeuxData implements BayeuxInterface {

    public BayeuxAdvice() {
        super();
    }

    public BayeuxAdvice(Map map) {
        super(map);
    }

    /**
     * Construct a default Bayeux advice by three paramenters:
     *   reconnect: <a href="http://svn.cometd.org/trunk/bayeux/bayeux.html#toc_33">reconnect advice</a>
     *   interval: <a href="http://svn.cometd.org/trunk/bayeux/bayeux.html#toc_34">interval: interval seconds of reconnecting</a>
     *   multipleClients: <a href="http://svn.cometd.org/trunk/bayeux/bayeux.html#toc_35">multiple-clients advice</a>
     *
     * @param reconnect
     * @param interval
     * @param multipleClients
     */
    public BayeuxAdvice(String reconnect, int interval, boolean multipleClients) {
        map.put("reconnect", reconnect);
        map.put("interval", interval);
        map.put("multiple-clients", multipleClients);
    }
}
