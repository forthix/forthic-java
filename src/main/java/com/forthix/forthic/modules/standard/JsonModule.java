package com.forthix.forthic.modules.standard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forthix.forthic.annotations.Word;
import com.forthix.forthic.module.DecoratedModule;

/**
 * JSON serialization, parsing, and formatting operations.
 *
 * Categories:
 * - Conversion: >JSON, JSON>
 * - Formatting: JSON-PRETTIFY
 *
 * Examples:
 * {name: "Alice", age: 30} >JSON
 * '{"name":"Alice"}' JSON>
 * '{"a":1}' JSON-PRETTIFY
 */
public class JsonModule extends DecoratedModule {

    private final ObjectMapper objectMapper;
    private final ObjectMapper prettyMapper;

    public JsonModule() {
        super("json");
        this.objectMapper = new ObjectMapper();
        this.prettyMapper = new ObjectMapper();
    }

    /**
     * Convert object to JSON string
     */
    @Word(stackEffect = "( object:any -- json:string )", description = "Convert object to JSON string", name = ">JSON")
    public String to_JSON(Object object) throws JsonProcessingException {
        if (object == null) {
            return "null";
        }
        return objectMapper.writeValueAsString(object);
    }

    /**
     * Parse JSON string to object
     */
    @Word(stackEffect = "( json:string -- object:any )", description = "Parse JSON string to object", name = "JSON>")
    public Object from_JSON(Object json) throws JsonProcessingException {
        if (json == null) {
            return null;
        }

        String jsonStr = json.toString().trim();
        if (jsonStr.isEmpty()) {
            return null;
        }

        return objectMapper.readValue(jsonStr, Object.class);
    }

    /**
     * Format JSON with 2-space indentation
     */
    @Word(stackEffect = "( json:string -- pretty:string )", description = "Format JSON with 2-space indentation", name = "JSON-PRETTIFY")
    public String JSON_PRETTIFY(Object json) throws JsonProcessingException {
        if (json == null) {
            return "";
        }

        String jsonStr = json.toString().trim();
        if (jsonStr.isEmpty()) {
            return "";
        }

        // Parse then prettify
        Object obj = objectMapper.readValue(jsonStr, Object.class);
        return prettyMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }
}
