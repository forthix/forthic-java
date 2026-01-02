package com.forthix.forthic.modules.standard;

import com.forthix.forthic.annotations.Word;
import com.forthix.forthic.module.DecoratedModule;

import java.util.*;

/**
 * Record (object/dictionary) manipulation operations for working with key-value data structures.
 *
 * Categories:
 * - Core: REC, REC@, |REC@, <REC!
 * - Transform: RELABEL, INVERT-KEYS, REC-DEFAULTS, <DEL
 * - Access: KEYS, VALUES
 */
public class RecordModule extends DecoratedModule {

    public RecordModule() {
        super("record");
    }

    // ===== Core Operations =====

    @Word(stackEffect = "( key_vals:any[] -- rec:any )", description = "Create record from [[key, val], ...] pairs")
    public Map<String, Object> REC(Object key_vals) {
        List<?> pairs = key_vals == null ? new ArrayList<>() : (List<?>) key_vals;

        Map<String, Object> result = new LinkedHashMap<>();
        for (Object pair : pairs) {
            if (pair instanceof List) {
                List<?> pairList = (List<?>) pair;
                Object key = pairList.size() >= 1 ? pairList.get(0) : null;
                Object val = pairList.size() >= 2 ? pairList.get(1) : null;
                if (key != null) {
                    result.put(key.toString(), val);
                }
            }
        }

        return result;
    }

    @Word(stackEffect = "( rec:any field:any -- value:any )", description = "Get value from record by field or array of fields", name = "REC@")
    public Object REC_at(Object rec, Object field) {
        if (rec == null) {
            return null;
        }

        List<String> fields;
        if (field instanceof List) {
            // Array of fields - drill down
            fields = new ArrayList<>();
            for (Object f : (List<?>) field) {
                fields.add(f == null ? "" : f.toString());
            }
        } else {
            // Single field
            fields = List.of(field == null ? "" : field.toString());
        }

        return drillForValue(rec, fields);
    }

    @Word(stackEffect = "( records:any field:any -- values:any )", description = "Map REC@ over array of records", name = "|REC@")
    public void pipe_REC_at(Object records, Object field) throws Exception {
        // This word uses the interpreter to execute MAP
        // Push records and execute: 'field REC@' MAP
        getInterp().stackPush(records);

        // Build the Forthic code to execute
        // We need to properly quote the field if it's an array or string
        String fieldCode;
        if (field instanceof List) {
            // Convert list to Forthic array literal
            StringBuilder sb = new StringBuilder("[");
            List<?> fieldList = (List<?>) field;
            for (int i = 0; i < fieldList.size(); i++) {
                if (i > 0) sb.append(" ");
                Object f = fieldList.get(i);
                sb.append("\"").append(f == null ? "" : f.toString()).append("\"");
            }
            sb.append("]");
            fieldCode = sb.toString();
        } else {
            // Single field
            fieldCode = "\"" + (field == null ? "" : field.toString()) + "\"";
        }

        getInterp().run("'" + fieldCode + " REC@' MAP");
        // Result is left on stack by MAP
    }

    @Word(stackEffect = "( rec:any value:any field:any -- rec:any )", description = "Set value in record at field path", name = "<REC!")
    public Map<String, Object> l_REC_bang(Object rec, Object value, Object field) {
        @SuppressWarnings("unchecked")
        Map<String, Object> record = rec == null ? new LinkedHashMap<>() : (Map<String, Object>) rec;

        List<String> fields;
        if (field instanceof List) {
            fields = new ArrayList<>();
            for (Object f : (List<?>) field) {
                fields.add(f == null ? "" : f.toString());
            }
        } else {
            fields = List.of(field == null ? "" : field.toString());
        }

        // Drill down, creating nested maps as needed
        @SuppressWarnings("unchecked")
        Map<String, Object> curRec = record;
        for (int i = 0; i < fields.size() - 1; i++) {
            String fieldName = fields.get(i);
            Object existing = curRec.get(fieldName);
            if (!(existing instanceof Map)) {
                existing = new LinkedHashMap<String, Object>();
                curRec.put(fieldName, existing);
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> next = (Map<String, Object>) existing;
            curRec = next;
        }

        // Set the value at the final field
        curRec.put(fields.get(fields.size() - 1), value);

        return record;
    }

    // ===== Transform Operations =====

    @Word(stackEffect = "( container:any old_keys:any[] new_keys:any[] -- container:any )", description = "Rename record keys")
    public Object RELABEL(Object container, Object old_keys, Object new_keys) {
        if (container == null) {
            return null;
        }

        List<?> oldKeysList = (List<?>) old_keys;
        List<?> newKeysList = (List<?>) new_keys;

        if (oldKeysList.size() != newKeysList.size()) {
            throw new IllegalArgumentException("RELABEL: old_keys and new_keys must be same length");
        }

        // Create mapping from new key to old key
        Map<String, String> newToOld = new LinkedHashMap<>();
        for (int i = 0; i < oldKeysList.size(); i++) {
            String newKey = newKeysList.get(i) == null ? "" : newKeysList.get(i).toString();
            String oldKey = oldKeysList.get(i) == null ? "" : oldKeysList.get(i).toString();
            newToOld.put(newKey, oldKey);
        }

        if (container instanceof List) {
            // For arrays, create new array with values at old indices
            List<?> containerList = (List<?>) container;
            List<Object> result = new ArrayList<>();

            // Sort keys and use old indices
            List<String> sortedNewKeys = new ArrayList<>(newToOld.keySet());
            Collections.sort(sortedNewKeys);

            for (String newKey : sortedNewKeys) {
                String oldKey = newToOld.get(newKey);
                try {
                    int oldIndex = Integer.parseInt(oldKey);
                    if (oldIndex >= 0 && oldIndex < containerList.size()) {
                        result.add(containerList.get(oldIndex));
                    }
                } catch (NumberFormatException e) {
                    // Skip non-numeric keys for arrays
                }
            }
            return result;
        } else {
            // For records, create new record with renamed keys
            @SuppressWarnings("unchecked")
            Map<String, Object> containerMap = (Map<String, Object>) container;
            Map<String, Object> result = new LinkedHashMap<>();

            for (Map.Entry<String, String> entry : newToOld.entrySet()) {
                String newKey = entry.getKey();
                String oldKey = entry.getValue();
                result.put(newKey, containerMap.get(oldKey));
            }
            return result;
        }
    }

    @Word(stackEffect = "( record:any -- inverted:any )", description = "Invert two-level nested record structure", name = "INVERT-KEYS")
    public Map<String, Object> INVERT_KEYS(Object record) {
        @SuppressWarnings("unchecked")
        Map<String, Object> recordMap = (Map<String, Object>) record;

        Map<String, Object> result = new LinkedHashMap<>();

        for (Map.Entry<String, Object> firstEntry : recordMap.entrySet()) {
            String firstKey = firstEntry.getKey();
            @SuppressWarnings("unchecked")
            Map<String, Object> subRecord = (Map<String, Object>) firstEntry.getValue();

            for (Map.Entry<String, Object> secondEntry : subRecord.entrySet()) {
                String secondKey = secondEntry.getKey();
                Object value = secondEntry.getValue();

                @SuppressWarnings("unchecked")
                Map<String, Object> innerMap = (Map<String, Object>) result.computeIfAbsent(
                    secondKey, k -> new LinkedHashMap<String, Object>()
                );
                innerMap.put(firstKey, value);
            }
        }

        return result;
    }

    @Word(stackEffect = "( record:any key_vals:any[] -- record:any )", description = "Set default values for missing/empty fields", name = "REC-DEFAULTS")
    public Map<String, Object> REC_DEFAULTS(Object record, Object key_vals) {
        @SuppressWarnings("unchecked")
        Map<String, Object> recordMap = (Map<String, Object>) record;
        List<?> keyValsList = (List<?>) key_vals;

        for (Object keyVal : keyValsList) {
            if (keyVal instanceof List) {
                List<?> pair = (List<?>) keyVal;
                if (pair.size() >= 2) {
                    String key = pair.get(0) == null ? "" : pair.get(0).toString();
                    Object value = recordMap.get(key);

                    // Set default if value is undefined, null, or empty string
                    if (value == null || (value instanceof String && ((String) value).isEmpty())) {
                        recordMap.put(key, pair.get(1));
                    }
                }
            }
        }

        return recordMap;
    }

    @Word(stackEffect = "( container:any key:any -- container:any )", description = "Delete key from record or index from array", name = "<DEL")
    public Object l_DEL(Object container, Object key) {
        if (container == null) {
            return null;
        }

        if (container instanceof List) {
            List<?> list = (List<?>) container;
            int index = ((Number) key).intValue();
            if (index >= 0 && index < list.size()) {
                // Create new list without the element (lists may be immutable)
                List<Object> result = new ArrayList<>(list);
                result.remove(index);
                return result;
            }
            return container;
        } else {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) container;
            map.remove(key == null ? "" : key.toString());
            return map;
        }
    }

    // ===== Access Operations =====

    @Word(stackEffect = "( container:any -- keys:any[] )", description = "Get keys from record or indices from array")
    public List<Object> KEYS(Object container) {
        if (container == null) {
            return new ArrayList<>();
        }

        if (container instanceof List) {
            // Return indices for arrays
            List<?> list = (List<?>) container;
            List<Object> result = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                result.add(i);
            }
            return result;
        } else {
            // Return keys for maps
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) container;
            return new ArrayList<>(map.keySet());
        }
    }

    @Word(stackEffect = "( container:any -- values:any[] )", description = "Get values from record or elements from array")
    public List<Object> VALUES(Object container) {
        if (container == null) {
            return new ArrayList<>();
        }

        if (container instanceof List) {
            // For arrays, just return the array itself
            return new ArrayList<>((List<?>) container);
        } else {
            // For maps, return the values
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) container;
            return new ArrayList<>(map.values());
        }
    }

    // ===== Helper Methods =====

    /**
     * Drill down into nested record/array structure
     * @param record The record/array to drill into
     * @param fields Array of field names or indices to traverse
     * @return The value at the end of the field path, or null if not found
     */
    private Object drillForValue(Object record, List<String> fields) {
        Object result = record;
        for (String field : fields) {
            if (result == null) {
                return null;
            }

            if (result instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) result;
                result = map.get(field);
            } else if (result instanceof List) {
                // Try to parse field as integer index
                try {
                    int index = Integer.parseInt(field);
                    List<?> list = (List<?>) result;
                    if (index >= 0 && index < list.size()) {
                        result = list.get(index);
                    } else {
                        return null;
                    }
                } catch (NumberFormatException e) {
                    return null;
                }
            } else {
                return null;
            }
        }

        return result;
    }
}
