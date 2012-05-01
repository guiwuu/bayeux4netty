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

import org.jboss.netty.handler.codec.bayeux.BayeuxConnection.TYPE;

/**
 * A Bayeux <a href="http://svn.cometd.org/trunk/bayeux/bayeux.html#toc_49">Handshake Request</a>
 *
 * Required properties: channel, version, supportedConnectionTypes
 * Optional properties: minimumVersion, id, ext
 *
 * @author daijun
 */
public class HandshakeRequest extends BayeuxMessage implements BayeuxInterface {

    public HandshakeRequest(BayeuxMessage bayeux) {
        super(bayeux);
        this.version = bayeux.version;
        this.supportedConnectionTypes = bayeux.supportedConnectionTypes;
        this.minimumVersion = bayeux.minimumVersion;
        this.ext = bayeux.ext;
    }

    public HandshakeRequest(String version, TYPE[] supportedConnectionTypes){
        this.channel="/meta/handshake";
        this.version=version;
        this.supportedConnectionTypes=supportedConnectionTypes;
    }

    public static boolean isValid(BayeuxMessage bayeux) {
        if (!bayeux.channel.equals("/meta/handshake")) {
            return false;
        }
        if (bayeux.version == null || bayeux.version.length() == 0) {
            return false;
        }
        if (bayeux.supportedConnectionTypes == null || bayeux.supportedConnectionTypes.length == 0) {
            return false;
        }
        if (bayeux.ext != null && !bayeux.ext.isValid()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isValid() {
        return isValid(this);
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public BayeuxExt getExt() {
        return ext;
    }

    public void setExt(BayeuxExt ext) {
        this.ext = ext;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMinimumVersion() {
        return minimumVersion;
    }

    public void setMinimumVersion(String minimumVersion) {
        this.minimumVersion = minimumVersion;
    }

    public TYPE[] getSupportedConnectionTypes() {
        return supportedConnectionTypes;
    }

    public void setSupportedConnectionTypes(TYPE[] supportedConnectionTypes) {
        this.supportedConnectionTypes = supportedConnectionTypes;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
