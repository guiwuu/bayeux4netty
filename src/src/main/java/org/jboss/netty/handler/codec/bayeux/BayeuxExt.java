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
 * A Bayuex <a href="http://svn.cometd.org/trunk/bayeux/bayeux.html#toc_45">Ext</a>
 * 
 * Bayeux ext property is a extensible property, which can be used to transport
 * some custom information by user.
 * 
 * Like Bayeux data and Bayeux advice, it's also a Javascript object, which is
 * wrapped in BayeuxExt class for more usability.
 *
 * @author daijun
 */
public class BayeuxExt extends BayeuxData implements BayeuxInterface {

    public BayeuxExt() {
        super();
    }

    public BayeuxExt(Map map) {
        super(map);
    }
}
