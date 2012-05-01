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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Parse JSON string to Java Object following mappings below:
 * 
 * JSON => Java: object => Map array => Object[] string => String number =>
 * long, or double null => null boolean => boolean
 * 
 * And convert from Java Object to JSON types following this:
 * 
 * Java => JSON: Map => object, List => array, Array => array, Integer, Long,
 * Float, Double => number, int, long, float, double => number, null => null,
 * boolean, Boolean=> boolean String => string
 * 
 * @author daijun
 */
public class JSONParser {

	private JSONString json;// Reprents the JSON string, which is been parsing

	/**
	 * Validate and parse a string to a JSON object. If succesful it will
	 * returns a Map or Array object, otherwise, it returns null. In below,
	 * there are data types mappings JSON to Java.
	 * 
	 * JSON => Java: object => Map array => Object[] string => String number =>
	 * long, or double null => null boolean => boolean
	 * 
	 * @param s
	 * @return
	 * @throws java.lang.IllegalStateException
	 */
	public Object parse(String s) throws IllegalStateException {
		if (s == null || s.trim().length() == 0) {
			return null;
		}

		json = new JSONString(s);
		while (json.hasNext()) {
			switch (json.next()) {
			case '/':
				if (json.hasNext()
						&& (json.next() == '/' || json.current() == '*')) {
					skipComment();
				} else {
					throwIllegalJSONStatementException();
				}
				skipWhitespace();
				break;
			case '{':
				return parseToObject();
			case '[':
				return parseToArray();
			default:
				throwIllegalJSONStatementException();
				break;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	static public String toJSON(Object obj) {
		StringBuilder sb = new StringBuilder();
		if (obj == null) {
			sb.append("null");
		} else if (obj instanceof Map) {
			sb.append("{");
			Map<String, Object> map = (Map<String, Object>) obj;
			for (Entry<String, Object> entry : map.entrySet()) {
				sb.append(quote(entry.getKey())).append(":").append(
						toJSON(entry.getValue())).append(",");
			}
			if (!map.isEmpty()) {
				sb.deleteCharAt(sb.length() - 1);
			}
			sb.append("}");
		} else if (obj instanceof Boolean) {
			Boolean bool = (Boolean) obj;
			if (bool) {
				sb.append("true");
			} else {
				sb.append("false");
			}
		} else if (obj instanceof Long) {
			sb.append((Long) obj);
		} else if (obj instanceof Integer) {
			sb.append((Integer) obj);
		} else if (obj instanceof Float) {
			sb.append((Float) obj);
		} else if (obj instanceof Double) {
			sb.append((Double) obj);
		} else if (obj instanceof String) {
			sb.append(quote((String) obj));
		} else if (obj instanceof List) {
			sb.append("[");
			for (Object o : (List) obj) {
				sb.append(toJSON(o)).append(",");
			}
			if (((List) obj).size() > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}
			sb.append("]");
		} else if (obj instanceof Object[]) {
			sb.append("[");
			for (Object o : (Object[]) obj) {
				sb.append(toJSON(o)).append(",");
			}
			if (((Object[]) obj).length > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}
			sb.append("]");
		} else if (obj instanceof BayeuxMessage) {
			sb.append(((BayeuxMessage) obj).toJSON());
		}
		return sb.toString();
	}

	static private String quote(String s) {
		return "\"" + s.replace("\"", "\\\"") + "\"";
	}

	/**
	 * Throw illegal JSON statement exception during parsing
	 * 
	 * When detects an unknown(or unvalid) char, it throws a
	 * IllegalStateException with a message that where and what the char is.
	 */
	private void throwIllegalJSONStatementException() {
		throw new IllegalStateException("Unknown char '" + json.current()
				+ "' at position " + json.getIndex() + " :  " + json.getStr());

	}

	/**
	 * Handle JSON comment
	 * 
	 * Skip the comments in the head of a JSON string. It supports two types
	 * comments, \"//coments\" and \"/*coments*\/\"
	 */
	private void skipComment() {
		if (json.current() == '/') {
			while (json.hasNext()) {
				if (json.next() == '\n') {
					break;
				}
			}
		} else if (json.current() == '*') {
			while (json.hasNext()) {
				if (json.next() == '*' && json.next() == '/') {
					break;
				}
			}
		}
	}

	/**
	 * Skip white spaces inside the JSON string
	 */
	private void skipWhitespace() {
		while (json.hasNext()) {
			if (json.next() != ' ') {
				json.back();
				break;
			}
		}
	}

	/**
	 * Skip white spaces and determine whether the first non-white character is
	 * expected
	 * 
	 * @param c
	 * @return
	 */
	private boolean skipWhitespace(char c) {
		skipWhitespace();
		return (json.hasNext() && json.next() == c);
	}

	/**
	 * Parse string to a object
	 * 
	 * Current character is \"{\", it means next serveral characters imply a
	 * JSON object. According to mappings before, them should be parsed to a
	 * Java object.
	 * 
	 * @return
	 */
	private Map<String, Object> parseToObject() {
		skipWhitespace();
		Map<String, Object> map = new HashMap<String, Object>();
		while (json.hasNext()) {
			switch (json.next()) {
			case '}':
				return map;
			case '"':
				String key = parseToString();
				if (skipWhitespace(':')) {
					Object value = parseValue();
					map.put(key, value);
				} else {
					throwIllegalJSONStatementException();
				}
				break;
			case ',':
				skipWhitespace();
				break;
			default:
				throwIllegalJSONStatementException();
			}
		}
		return map;
	}

	/**
	 * Parse string to a array
	 * 
	 * Current character is \"[\", it means next serveral characters imply a
	 * JSON array. According to mappings before, them should be parsed to a Java
	 * array.
	 * 
	 * @return
	 */
	private Object[] parseToArray() {
		skipWhitespace();
		ArrayList<Object> list = new ArrayList<Object>();
		while (json.hasNext()) {
			switch (json.next()) {
			case ']':
				return list.toArray();
			case ',':
				skipWhitespace();
				break;
			default:
				json.back();
				list.add(parseValue());
				break;
			}
		}
		return null;
	}

	/**
	 * Parse a JSON value
	 * 
	 * Any element of JSON array, or a property value of a JSON object, it maybe
	 * a JSON value. So according to mappings before, them should be parsed to a
	 * Java array.
	 * 
	 * @return
	 */
	private Object parseValue() {
		skipWhitespace();
		while (json.hasNext()) {
			switch (json.next()) {
			case '"':
				return parseToString();
			case '{':
				return parseToObject();
			case '[':
				return parseToArray();
			case 't':
			case 'T':
				json.next();
				json.next();
				json.next();
				return true;
			case 'f':
			case 'F':
				json.next();
				json.next();
				json.next();
				json.next();
				return false;
			case 'n':
			case 'N':
				json.next();
				json.next();
				json.next();
				return null;
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				return parseToNumber();
			default:
				throwIllegalJSONStatementException();
			}
		}
		return null;
	}

	/**
	 * Parse string to a string
	 * 
	 * Current character is \"\\"\", it means next serveral characters imply a
	 * JSON string. According to mappings before, them should be parsed to a
	 * Java string.
	 * 
	 * @return
	 */
	private String parseToString() {
		skipWhitespace();
		StringBuilder sb = new StringBuilder();
		while (json.hasNext()) {
			switch (json.next()) {
			case '"':
				return sb.toString();
			default:
				sb.append(json.current());
				break;
			}
		}
		return sb.toString();
	}

	/**
	 * Parse string to a number
	 * 
	 * In JSON value, if it starts with demical digits, it means next serveral
	 * characters may reprent a number. According to mappings before, them
	 * should be parsed to a Java long or double.
	 * 
	 * @return
	 */
	private Object parseToNumber() {
		StringBuilder sb = new StringBuilder();
		sb.append(json.current());
		boolean isLong = true;
		while (json.hasNext()) {
			switch (json.next()) {
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				sb.append(json.current());
				break;
			case '.':
			case 'e':
			case 'E':
				sb.append(json.current());
				isLong = false;
				break;
			default:
				json.back();
				if (isLong) {
					return Long.parseLong(sb.toString());
				} else {
					return Double.parseDouble(sb.toString());
				}
			}
		}
		return null;
	}

	/**
	 * A wrapper class of JSON string. It supports some simple and usefull
	 * methods, which are used in JSONParser class.
	 */
	class JSONString {

		private String str;
		private int index;

		public JSONString(String str) {
			this.str = str;
			this.index = -1;
		}

		/**
		 * Get current index of JSON string
		 * 
		 * @return
		 */
		public int getIndex() {
			return index;
		}

		/**
		 * Get current character of JSON string
		 * 
		 * @return
		 */
		public char current() {
			return index == -1 ? next() : str.charAt(index);
		}

		/**
		 * Determine whether JSON string has next character or not
		 * 
		 * @return
		 */
		public boolean hasNext() {
			return (index + 1) < str.length();
		}

		/**
		 * Get a character following the current one
		 * 
		 * @return
		 */
		public char next() {
			return str.charAt(++index);
		}

		/**
		 * Get the rest of JSON string from current character
		 * 
		 * @return
		 */
		public String sub() {
			return str.substring(index);
		}

		/**
		 * Move the index back by one
		 */
		private void back() {
			index--;
		}

		public String getStr() {
			return str;
		}

		public void setStr(String str) {
			this.str = str;
		}

	}
}
