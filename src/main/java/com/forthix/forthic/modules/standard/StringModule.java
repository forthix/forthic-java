package com.forthix.forthic.modules.standard;

import com.forthix.forthic.annotations.Word;
import com.forthix.forthic.interpreter.BareInterpreter;
import com.forthix.forthic.module.DecoratedModule;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String manipulation and processing operations with regex and URL encoding support.
 *
 * Categories:
 * - Conversion: >STR, URL-ENCODE, URL-DECODE
 * - Transform: LOWERCASE, UPPERCASE, STRIP, ASCII
 * - Split/Join: SPLIT, JOIN, CONCAT
 * - Pattern: REPLACE, RE-MATCH, RE-MATCH-ALL, RE-MATCH-GROUP
 * - Constants: /N, /R, /T
 *
 * Examples:
 * "hello" "world" CONCAT
 * ["a" "b" "c"] CONCAT
 * "hello world" " " SPLIT
 * ["hello" "world"] " " JOIN
 * "Hello" LOWERCASE
 * "test@example.com" "(@.+)" RE-MATCH 1 RE-MATCH-GROUP
 */
public class StringModule extends DecoratedModule {

    public StringModule() {
        super("string");
    }

    // ===== Concatenation =====

    /**
     * Concatenate two strings or array of strings.
     * DirectWord to handle both forms.
     */
    @Word(stackEffect = "( str1:string str2:string -- result:string ) OR ( strings:string[] -- result:string )",
          description = "Concatenate two strings or array of strings",
          name = "CONCAT",
          isDirect = true)
    public void CONCAT(BareInterpreter interp) {
        Object str2 = interp.stackPop();
        List<String> array;

        if (str2 instanceof List) {
            // Array case: join all elements
            array = new ArrayList<>();
            for (Object item : (List<?>) str2) {
                array.add(item == null ? "" : item.toString());
            }
        } else {
            // Two string case
            Object str1 = interp.stackPop();
            array = List.of(
                str1 == null ? "" : str1.toString(),
                str2 == null ? "" : str2.toString()
            );
        }

        String result = String.join("", array);
        interp.stackPush(result);
    }

    // ===== Conversion =====

    @Word(stackEffect = "( item:any -- string:string )", description = "Convert item to string", name = ">STR")
    public String to_STR(Object item) {
        if (item == null) {
            return "";
        }
        return item.toString();
    }

    @Word(stackEffect = "( str:string -- encoded:string )", description = "URL encode string", name = "URL-ENCODE")
    public String URL_ENCODE(Object str) {
        if (str == null || str.toString().isEmpty()) {
            return "";
        }
        return URLEncoder.encode(str.toString(), StandardCharsets.UTF_8);
    }

    @Word(stackEffect = "( urlencoded:string -- decoded:string )", description = "URL decode string", name = "URL-DECODE")
    public String URL_DECODE(Object urlencoded) {
        if (urlencoded == null || urlencoded.toString().isEmpty()) {
            return "";
        }
        return URLDecoder.decode(urlencoded.toString(), StandardCharsets.UTF_8);
    }

    // ===== Transform =====

    @Word(stackEffect = "( string:string -- result:string )", description = "Convert string to lowercase")
    public String LOWERCASE(Object string) {
        if (string == null) {
            return "";
        }
        return string.toString().toLowerCase();
    }

    @Word(stackEffect = "( string:string -- result:string )", description = "Convert string to uppercase")
    public String UPPERCASE(Object string) {
        if (string == null) {
            return "";
        }
        return string.toString().toUpperCase();
    }

    @Word(stackEffect = "( string:string -- result:string )", description = "Trim whitespace from string")
    public String STRIP(Object string) {
        if (string == null) {
            return null;
        }
        return string.toString().trim();
    }

    @Word(stackEffect = "( string:string -- result:string )", description = "Keep only ASCII characters (< 256)")
    public String ASCII(Object string) {
        if (string == null) {
            return "";
        }

        String str = string.toString();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch < 256) {
                result.append(ch);
            }
        }
        return result.toString();
    }

    // ===== Split/Join =====

    @Word(stackEffect = "( string:string sep:string -- items:any[] )", description = "Split string by separator")
    public List<String> SPLIT(Object string, Object sep) {
        String str = string == null ? "" : string.toString();
        String separator = sep == null ? "" : sep.toString();

        if (separator.isEmpty()) {
            // Split into individual characters
            List<String> result = new ArrayList<>();
            for (char c : str.toCharArray()) {
                result.add(String.valueOf(c));
            }
            return result;
        }

        return List.of(str.split(Pattern.quote(separator), -1));
    }

    @Word(stackEffect = "( strings:string[] sep:string -- result:string )", description = "Join strings with separator")
    public String JOIN(Object strings, Object sep) {
        if (strings == null || !(strings instanceof List)) {
            return "";
        }

        String separator = sep == null ? "" : sep.toString();
        List<?> items = (List<?>) strings;
        List<String> stringItems = new ArrayList<>();

        for (Object item : items) {
            stringItems.add(item == null ? "" : item.toString());
        }

        return String.join(separator, stringItems);
    }

    // ===== Constants =====

    @Word(stackEffect = "( -- char:string )", description = "Newline character", name = "/N")
    public String slash_N() {
        return "\n";
    }

    @Word(stackEffect = "( -- char:string )", description = "Carriage return character", name = "/R")
    public String slash_R() {
        return "\r";
    }

    @Word(stackEffect = "( -- char:string )", description = "Tab character", name = "/T")
    public String slash_T() {
        return "\t";
    }

    // ===== Pattern Matching =====

    @Word(stackEffect = "( string:string text:string replace:string -- result:string )",
          description = "Replace all occurrences of text with replace")
    public String REPLACE(Object string, Object text, Object replace) {
        if (string == null) {
            return null;
        }

        String str = string.toString();
        String pattern = text == null ? "" : text.toString();
        String replacement = replace == null ? "" : replace.toString();

        // Use Pattern.quote to treat the text as literal, then replace with replaceAll
        return str.replaceAll(Pattern.quote(pattern), Matcher.quoteReplacement(replacement));
    }

    @Word(stackEffect = "( string:string pattern:string -- match:any )",
          description = "Match string against regex pattern",
          name = "RE-MATCH")
    public Object RE_MATCH(Object string, Object pattern) {
        if (string == null) {
            return false;
        }

        String str = string.toString();
        String patternStr = pattern == null ? "" : pattern.toString();

        try {
            Pattern regex = Pattern.compile(patternStr);
            Matcher matcher = regex.matcher(str);

            if (matcher.find()) {
                // Return a match result that can be used with RE-MATCH-GROUP
                return new MatchResult(matcher);
            }
        } catch (Exception e) {
            // Invalid regex pattern
            return false;
        }

        return false;
    }

    @Word(stackEffect = "( string:string pattern:string -- matches:any[] )",
          description = "Find all regex matches in string",
          name = "RE-MATCH-ALL")
    public List<String> RE_MATCH_ALL(Object string, Object pattern) {
        List<String> result = new ArrayList<>();

        if (string == null) {
            return result;
        }

        String str = string.toString();
        String patternStr = pattern == null ? "" : pattern.toString();

        try {
            Pattern regex = Pattern.compile(patternStr);
            Matcher matcher = regex.matcher(str);

            while (matcher.find()) {
                // Get first capture group if it exists, otherwise full match
                if (matcher.groupCount() >= 1) {
                    result.add(matcher.group(1));
                } else {
                    result.add(matcher.group(0));
                }
            }
        } catch (Exception e) {
            // Invalid regex pattern, return empty list
        }

        return result;
    }

    @Word(stackEffect = "( match:any num:number -- result:any )",
          description = "Get capture group from regex match",
          name = "RE-MATCH-GROUP")
    public Object RE_MATCH_GROUP(Object match, Object num) {
        if (match == null || !(match instanceof MatchResult)) {
            return null;
        }

        if (num == null) {
            return null;
        }

        int groupNum = ((Number) num).intValue();
        MatchResult matchResult = (MatchResult) match;

        try {
            return matchResult.group(groupNum);
        } catch (Exception e) {
            return null;
        }
    }

    // ===== Helper Classes =====

    /**
     * Wrapper for regex match results to support RE-MATCH-GROUP
     */
    public static class MatchResult {
        private final String[] groups;

        public MatchResult(Matcher matcher) {
            int groupCount = matcher.groupCount() + 1; // +1 for group 0 (full match)
            groups = new String[groupCount];
            for (int i = 0; i < groupCount; i++) {
                groups[i] = matcher.group(i);
            }
        }

        public String group(int index) {
            if (index >= 0 && index < groups.length) {
                return groups[index];
            }
            return null;
        }

        @Override
        public String toString() {
            return groups[0]; // Return full match by default
        }
    }
}
