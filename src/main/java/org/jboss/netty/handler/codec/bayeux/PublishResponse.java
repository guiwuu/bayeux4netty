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

import java.util.regex.Pattern;

/**
 * A Bayeux <a href="http://svn.cometd.org/trunk/bayeux/bayeux.html#toc_66">Publish Response</a>
 *
 * Required properties: channel, successful
 * Optional properties: clientId, id, error, ext
 * 
 * @author daijun
 */
public class PublishResponse extends BayeuxMessage implements BayeuxInterface {

    public PublishResponse(PublishRequest publishRequest) {
        super(publishRequest);
        this.timestamp = BayeuxUtil.getCurrentTime();
    }

    public PublishResponse(String channel, boolean successful){
        this.channel=channel;
        this.successful=successful;
        this.timestamp = BayeuxUtil.getCurrentTime();
    }

    public static boolean isValid(BayeuxMessage bayeux) {
        if ("/meta".equals(bayeux.channel) || Pattern.matches("/meta/.*", bayeux.channel) || !bayeux.channel.matches("/.+")) {
            return false;
        }
        if (bayeux.successful == null) {
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

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
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

    public Boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(Boolean successful) {
        this.successful = successful;
    }
}
