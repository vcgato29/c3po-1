package com.petpet.c3po.adaptor.tika;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A TIKA Result parser. Thanks to Per for his contribution
 * 
 * @author Per Møldrup-Dalum
 * 
 */
public class TIKAResultParser {

  /* join a String array with a delimiter */
  public static String join(String r[], String d) {
    if (r.length == 0)
      return "";
    StringBuilder sb = new StringBuilder();
    int i;
    for (i = 0; i < r.length - 1; i++)
      sb.append(r[i] + d);
    return sb.toString() + r[i];
  }

  public static String[] tail(String s[]) {
    return Arrays.copyOfRange(s, 1, s.length);
  }

  /**
   * Reads a file line by line and assumes it is a raw TIKA output.
   * 
   * @param input
   *          the input stream to the file
   * @return a {@link Map} with the properties as keys and the values as values
   * @throws IOException
   *           if an io problem occurs.
   */
  public static Map<String, String> getKeyValueMap(InputStream input) throws IOException {
    InputStreamReader streamReader = new InputStreamReader(input, "UTF-8");
    BufferedReader bufferedReader = new BufferedReader(streamReader);

    Map<String, String> map = new LinkedHashMap<String, String>();

    String line = bufferedReader.readLine();
    while (line != null) {
      // Regex to scan for 1 or more ": " colon- whitespace occurrences
      String[] tokens = line.split(": ", -1);
      if (tokens.length >= 2) {
        // remove ':' from head if it is the last character
        String head = "";
        for (int i = 0; i < tokens.length - 1; i++) {
          head += tokens[i];
        }

        if (head.length() > 0 && head.charAt(head.length() - 1) == ':') {
          head = head.substring(0, head.length() - 1);
        }

        map.put(head, join(tail(tokens), " "));

        if (head.equals("Content-Type")) {
          processtMimetype(map, join(tail(tokens), " "));
        }

      }

      line = bufferedReader.readLine();
    }

    return map;
  }

  /**
   * A simple helper method to process Apache Tikas extended Content-Type. It is
   * in the form of e.g 'text/html; charset=utf-8'. The method will split it
   * into two properties: Content-Type and charset and will store them in the
   * map accordingly.
   * 
   * @param map
   *          in which to store the new properties and their values.
   * @param value
   *          the the actual value of the original Content-Type as delivered by
   *          Tika.
   */
  private static void processtMimetype(Map<String, String> map, String value) {
    String[] split = value.split("; ");
    if (split.length > 1) {
      map.put("Content-Type", split[0]);
      map.put("charset", split[1].substring(8)); // 8 because of 'charset='
    }
  }
}
