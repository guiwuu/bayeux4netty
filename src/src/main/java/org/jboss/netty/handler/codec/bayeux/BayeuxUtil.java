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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Contains simple and useful of this codec. They are abstracted from other
 * classes for reusing and testing.
 *
 * @author daijun
 */
public class BayeuxUtil {

    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    static {
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Returns current time in format of ISO8601:2000(YYYY-MM-DDTHH:MM:SS).
     *
     * @return
     */
    public static synchronized String getCurrentTime() {
        return df.format(new Date());
    }

    /**
     * Returns a string of 16 HEX chars.
     *
     * @return
     */
    public static String generateUUID() {
        UUID uuid = UUID.randomUUID();
        return Long.toHexString(uuid.getMostSignificantBits());
    }

    /**
     * Channel prefix matching algorithm using "*" or "**" as wild chars.
     *
     * @param match
     * @param strings
     * @return
     */
    public static List<String> prefixMatch(String match, String[] strings) {
        List<String> matched = new ArrayList<String>();
        Pattern prefix = null;
        if (match.endsWith("**")) {
            match = match.substring(0, match.length() - 2);
            prefix = Pattern.compile(match + "[\\w/]*",
                    Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE);
        } else if (match.endsWith("*")) {
            match = match.substring(0, match.length() - 1);
            prefix = Pattern.compile(match + "\\w*",
                    Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE);
        } else {
            prefix = Pattern.compile(match, Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE);

        }
        Pattern prefix2 = null;
        for (int i = 0; i < strings.length; i++) {
            if (strings[i].endsWith("**")) {
                prefix2 = Pattern.compile(strings[i].substring(0,
                        strings[i].length() - 2) + "[\\w/]*", Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE);
            } else if (strings[i].endsWith("*")) {
                prefix2 = Pattern.compile(strings[i].substring(0,
                        strings[i].length() - 1) + "\\w*", Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE);
            } else {
                prefix2 = Pattern.compile(strings[i],
                        Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE);
            }
            if (prefix.matcher(strings[i]).matches()) {
                matched.add(strings[i]);
            } else if (prefix2.matcher(match).matches()) {
                matched.add(strings[i]);
            }
        }
        return matched;
    }

    /**
     * Channel prefix matching algorithm using "*" or "**" as wild chars.
     *
     * @param match
     * @param strings
     * @return
     */
    public static List<String> prefixMatch(String match, Set<String> strings) {
        return prefixMatch(match, strings.toArray(new String[0]));
    }
    
    public static boolean isEqual(Object o1, Object o2){
    	 if(o1 != null && !o1.equals(o2)){
    		return false;
    	}else if(o2 != null && !o2.equals(o1)){
    		return false;
    	}
    	return true;
    }
}
