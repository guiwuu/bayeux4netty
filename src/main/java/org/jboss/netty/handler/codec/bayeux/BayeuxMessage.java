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

import java.util.ArrayList;
import java.util.List;
import static org.jboss.netty.handler.codec.bayeux.BayeuxUtil.*;

/**
 * A common parent class for all types of Bayeux messages.
 * 
 * @author daijun
 */
public class BayeuxMessage implements BayeuxInterface {

	String channel;// Bayeux Channle Name, like /meta/handshake
	BayeuxConnection.TYPE[] supportedConnectionTypes;// A array, like
														// ["long-polling","http-streaming","flash","iframe"]
	String clientId;// An unify 32 chars length String for each client assigned
					// by server
	String connectionId;// An unify 32 chars length String for each connection
						// assigned by server
	String minimumVersion;// Support minimum version of Bayeux
	Boolean successful;// boolean
	Boolean authSuccessful;// boolean
	String version;// Version of Bayeux protocal, like 1.0
	String subscription;// Subscription name of Bayeux Channel, like /chat/netty
	String error;// Error information includes error code, error args and error
					// message, like 404:/foo/bar:Unknown Channel
	BayeuxConnection.TYPE connectionType;// Connection type, must be one of four
											// supported connection types
	String id;// An unify 32 chars length String for each message of one
				// application, assigned by app
	String timestamp;// ISO 8601 format in GMT time,YYYY-MM-DDThh:mm:ss.ss
	BayeuxExt ext;// Extension property for Bayeux messages
	BayeuxAdvice advice;// Advice property of some Bayeux messages
	BayeuxData data;// Data property of some Bayeux messages

	public BayeuxMessage() {
	}

	public BayeuxMessage(BayeuxMessage bayeux) {
		this.channel = bayeux.channel;
		this.clientId = bayeux.clientId;
		this.connectionId = bayeux.connectionId;
		this.id = bayeux.id;
		this.ext = bayeux.ext;
		if (bayeux.timestamp != null) {
			this.timestamp = bayeux.timestamp;
		}
	}

	@Override
	public String toJSON() {
		StringBuilder json = new StringBuilder();
		json.append("{");
		if (this.channel != null && this.channel.length() != 0) {
			json.append("\"channel\":").append(JSONParser.toJSON(this.channel));
		}
		if (supportedConnectionTypes != null
				&& supportedConnectionTypes.length != 0) {
			List<String> supportedConnectionTypeList = new ArrayList<String>();
			for (int i = 0; i < supportedConnectionTypes.length; i++) {
				supportedConnectionTypeList.add(BayeuxConnection
						.getValueOfType(supportedConnectionTypes[i]));
			}
			json.append(",\"supportedConnectionTypes\":").append(
					JSONParser.toJSON(supportedConnectionTypeList));
		}
		if (clientId != null && clientId.length() != 0) {
			json.append(",\"clientId\":").append(JSONParser.toJSON(clientId));
		}
		if (connectionId != null && connectionId.length() != 0) {
			json.append(",\"connectionId\":").append(
					JSONParser.toJSON(connectionId));
		}
		if (minimumVersion != null && minimumVersion.length() != 0) {
			json.append(",\"minimumVersion\":").append(
					JSONParser.toJSON(minimumVersion));
		}
		if (successful != null) {
			json.append(",\"successful\":").append(
					JSONParser.toJSON(successful));
		}
		if (version != null && version.length() != 0) {
			json.append(",\"version\":").append(JSONParser.toJSON(version));
		}
		if (subscription != null && subscription.length() != 0) {
			json.append(",\"subscription\":").append(
					JSONParser.toJSON(subscription));
		}
		if (error != null && error.length() != 0) {
			json.append(",\"error\":").append(JSONParser.toJSON(error));
		}
		if (connectionType != null) {
			json.append(",\"connectionType\":").append(
					JSONParser.toJSON(BayeuxConnection
							.getValueOfType(connectionType)));
		}
		if (id != null && id.length() != 0) {
			json.append(",\"id\":").append(JSONParser.toJSON(id));
		}
		if (timestamp != null && timestamp.length() != 0) {
			json.append(",\"timestamp\":").append(JSONParser.toJSON(timestamp));
		}
		if (ext != null) {
			json.append(",\"ext\":" + ext.toJSON());
		}
		if (advice != null) {
			json.append(",\"advice\":" + advice.toJSON());
		}
		if (data != null) {
			json.append(",\"data\":" + data.toJSON());
		}
		json.append("}");
		return json.toString();
	}

	@Override
	public boolean isValid() {
		return isValid(this);
	}

	public static boolean isValid(BayeuxMessage bayeux) {
		return bayeux.channel != null && bayeux.channel.length() != 0;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof BayeuxMessage))
			return false;
		BayeuxMessage bayeux = (BayeuxMessage) o;
		if (!isEqual(this.advice, bayeux.advice)) {
			return false;
		}

		if (!isEqual(this.authSuccessful, bayeux.authSuccessful)) {
			return false;
		}

		if (!isEqual(this.channel, bayeux.channel)) {
			return false;
		}

		if (!isEqual(this.clientId, bayeux.clientId)) {
			return false;
		}

		if (!isEqual(this.connectionId, bayeux.connectionId)) {
			return false;
		}

		if (!isEqual(this.connectionType, bayeux.connectionType)) {
			return false;
		}

		if (!isEqual(this.data, bayeux.data)) {
			return false;
		}

		if (!isEqual(this.error, bayeux.error)) {
			return false;
		}

		if (!isEqual(this.ext, bayeux.ext)) {
			return false;
		}

		if (!isEqual(this.id, bayeux.id)) {
			return false;
		}
		if (!isEqual(this.minimumVersion, bayeux.minimumVersion)) {
			return false;
		}

		if (!isEqual(this.subscription, bayeux.subscription)) {
			return false;
		}

		if (!isEqual(this.successful, bayeux.successful)) {
			return false;
		}

		if (!isEqual(this.supportedConnectionTypes,
				bayeux.supportedConnectionTypes)) {
			return false;
		}

		if (!isEqual(this.timestamp, bayeux.timestamp)) {
			return false;
		}

		if (!isEqual(this.version, bayeux.version)) {
			return false;
		}
		return true;
	}
}
