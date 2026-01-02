package com.forthix.forthic.modules.standard;

import com.forthix.forthic.annotations.Word;
import com.forthix.forthic.interpreter.BareInterpreter;
import com.forthix.forthic.module.DecoratedModule;

import java.util.*;

/**
 * Array and collection operations for manipulating arrays and records.
 *
 * Categories:
 * - Access: NTH, LAST, SLICE, TAKE, DROP, LENGTH, INDEX, KEY-OF
 * - Transform: MAP, REVERSE
 * - Combine: APPEND, ZIP, ZIP_WITH
 * - Filter: SELECT, UNIQUE, DIFFERENCE, INTERSECTION, UNION
 * - Sort: SORT, SHUFFLE, ROTATE
 * - Group: BY_FIELD, GROUP-BY-FIELD, GROUP_BY, GROUPS_OF
 * - Utility: <REPEAT, FOREACH, REDUCE, UNPACK, FLATTEN
 */
public class ArrayModule extends DecoratedModule {

    private final Random random = new Random();

    public ArrayModule() {
        super("array");
    }

    // ===== Access Operations =====

    @Word(stackEffect = "( container:any -- length:number )", description = "Get length of array or record")
    public Integer LENGTH(Object container) {
        if (container == null) {
            return 0;
        }

        if (container instanceof List) {
            return ((List<?>) container).size();
        } else if (container instanceof Map) {
            return ((Map<?, ?>) container).size();
        }
        return 0;
    }

    @Word(stackEffect = "( container:any n:number -- item:any )", description = "Get nth element from array or record")
    public Object NTH(Object container, Object n) {
        if (container == null || n == null) {
            return null;
        }

        int index = ((Number) n).intValue();

        if (container instanceof List) {
            List<?> list = (List<?>) container;
            if (index < 0 || index >= list.size()) {
                return null;
            }
            return list.get(index);
        } else if (container instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) container;
            List<String> keys = new ArrayList<>(map.keySet());
            Collections.sort(keys);
            if (index < 0 || index >= keys.size()) {
                return null;
            }
            return map.get(keys.get(index));
        }
        return null;
    }

    @Word(stackEffect = "( container:any -- item:any )", description = "Get last element from array or record")
    public Object LAST(Object container) {
        if (container == null) {
            return null;
        }

        if (container instanceof List) {
            List<?> list = (List<?>) container;
            if (list.isEmpty()) {
                return null;
            }
            return list.get(list.size() - 1);
        } else if (container instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) container;
            List<String> keys = new ArrayList<>(map.keySet());
            Collections.sort(keys);
            if (keys.isEmpty()) {
                return null;
            }
            return map.get(keys.get(keys.size() - 1));
        }
        return null;
    }

    @Word(stackEffect = "( container:any start:number end:number -- result:any )", description = "Extract slice from array or record")
    public Object SLICE(Object container, Object start, Object end) {
        if (container == null) {
            return new ArrayList<>();
        }

        int startIdx = ((Number) start).intValue();
        int endIdx = ((Number) end).intValue();

        int length;
        if (container instanceof List) {
            length = ((List<?>) container).size();
        } else if (container instanceof Map) {
            length = ((Map<?, ?>) container).size();
        } else {
            return new ArrayList<>();
        }

        // Normalize negative indices
        if (startIdx < 0) startIdx += length;
        if (endIdx < 0) endIdx += length;

        int step = startIdx > endIdx ? -1 : 1;
        List<Integer> indices = new ArrayList<>();
        indices.add(startIdx);

        if (startIdx < 0 || startIdx >= length) {
            return container instanceof List ? new ArrayList<>() : new LinkedHashMap<>();
        }

        int current = startIdx;
        while (current != endIdx) {
            current += step;
            if (current >= 0 && current < length) {
                indices.add(current);
            }
        }

        if (container instanceof List) {
            List<?> list = (List<?>) container;
            List<Object> result = new ArrayList<>();
            for (Integer idx : indices) {
                if (idx != null && idx >= 0 && idx < list.size()) {
                    result.add(list.get(idx));
                }
            }
            return result;
        } else {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) container;
            List<String> keys = new ArrayList<>(map.keySet());
            Collections.sort(keys);
            Map<String, Object> result = new LinkedHashMap<>();
            for (Integer idx : indices) {
                if (idx != null && idx >= 0 && idx < keys.size()) {
                    String key = keys.get(idx);
                    result.put(key, map.get(key));
                }
            }
            return result;
        }
    }

    @Word(stackEffect = "( container:any[] n:number -- result:any[] )", description = "Take first n elements")
    public Object TAKE(Object container, Object n) {
        if (container == null) {
            return new ArrayList<>();
        }

        int count = ((Number) n).intValue();

        if (container instanceof List) {
            List<?> list = (List<?>) container;
            return new ArrayList<>(list.subList(0, Math.min(count, list.size())));
        } else if (container instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) container;
            List<String> keys = new ArrayList<>(map.keySet());
            Collections.sort(keys);
            List<Object> result = new ArrayList<>();
            for (int i = 0; i < Math.min(count, keys.size()); i++) {
                result.add(map.get(keys.get(i)));
            }
            return result;
        }
        return new ArrayList<>();
    }

    @Word(stackEffect = "( container:any n:number -- result:any )", description = "Drop first n elements from array or record")
    public Object DROP(Object container, Object n) {
        if (container == null) {
            return new ArrayList<>();
        }

        int count = ((Number) n).intValue();
        if (count <= 0) {
            return container;
        }

        if (container instanceof List) {
            List<?> list = (List<?>) container;
            if (count >= list.size()) {
                return new ArrayList<>();
            }
            return new ArrayList<>(list.subList(count, list.size()));
        } else if (container instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) container;
            List<String> keys = new ArrayList<>(map.keySet());
            Collections.sort(keys);
            List<Object> result = new ArrayList<>();
            for (int i = count; i < keys.size(); i++) {
                result.add(map.get(keys.get(i)));
            }
            return result;
        }
        return new ArrayList<>();
    }

    // ===== Transform Operations =====

    @Word(stackEffect = "( container:any item:any -- container:any )", description = "Append item to array or add key-value to record")
    public Object APPEND(Object container, Object item) {
        Object result = container;
        if (result == null) {
            result = new ArrayList<>();
        }

        if (result instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) result;
            list.add(item);
            return list;
        } else if (result instanceof Map) {
            // Item should be [key, value]
            if (item instanceof List) {
                List<?> pair = (List<?>) item;
                if (pair.size() >= 2) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) result;
                    map.put(pair.get(0).toString(), pair.get(1));
                }
            }
            return result;
        }
        return result;
    }

    @Word(stackEffect = "( container:any -- container:any )", description = "Reverse array")
    public Object REVERSE(Object container) {
        if (container == null) {
            return container;
        }

        if (container instanceof List) {
            List<?> list = (List<?>) container;
            List<Object> result = new ArrayList<>(list);
            Collections.reverse(result);
            return result;
        }
        return container;
    }

    @Word(stackEffect = "( container:any -- container:any )", description = "Rotate container by moving last element to front")
    public Object ROTATE(Object container) {
        if (container == null || !(container instanceof List)) {
            return container;
        }

        List<?> list = (List<?>) container;
        if (list.size() <= 1) {
            return container;
        }

        List<Object> result = new ArrayList<>();
        result.add(list.get(list.size() - 1));
        for (int i = 0; i < list.size() - 1; i++) {
            result.add(list.get(i));
        }
        return result;
    }

    // ===== Combine Operations =====

    @Word(stackEffect = "( container1:any[] container2:any[] -- result:any[] )", description = "Zip two arrays into array of pairs")
    public List<List<Object>> ZIP(Object container1, Object container2) {
        List<?> list1 = container1 instanceof List ? (List<?>) container1 : new ArrayList<>();
        List<?> list2 = container2 instanceof List ? (List<?>) container2 : new ArrayList<>();

        List<List<Object>> result = new ArrayList<>();
        int minSize = Math.min(list1.size(), list2.size());
        for (int i = 0; i < minSize; i++) {
            result.add(List.of(list1.get(i), list2.get(i)));
        }
        return result;
    }

    @Word(stackEffect = "( container1:any[] container2:any[] forthic:string -- result:any[] )", description = "Zip two arrays with combining function", name = "ZIP-WITH")
    public List<Object> ZIP_WITH(Object container1, Object container2, Object forthic) throws Exception {
        List<?> list1 = container1 instanceof List ? (List<?>) container1 : new ArrayList<>();
        List<?> list2 = container2 instanceof List ? (List<?>) container2 : new ArrayList<>();
        String code = forthic == null ? "" : forthic.toString();

        List<Object> result = new ArrayList<>();
        int minSize = Math.min(list1.size(), list2.size());
        for (int i = 0; i < minSize; i++) {
            getInterp().stackPush(list1.get(i));
            getInterp().stackPush(list2.get(i));
            getInterp().run(code);
            result.add(getInterp().stackPop());
        }
        return result;
    }

    // ===== Filter Operations =====

    @Word(stackEffect = "( array:any[] -- array:any[] )", description = "Remove duplicates from array")
    public Object UNIQUE(Object array) {
        if (array == null) {
            return array;
        }

        if (array instanceof List) {
            List<?> list = (List<?>) array;
            return new ArrayList<>(new LinkedHashSet<>(list));
        }
        return array;
    }

    @Word(stackEffect = "( lcontainer:any rcontainer:any -- result:any )", description = "Set difference between two containers")
    public Object DIFFERENCE(Object lcontainer, Object rcontainer) {
        if (lcontainer == null) {
            return new ArrayList<>();
        }
        if (rcontainer == null) {
            return lcontainer;
        }

        if (lcontainer instanceof List && rcontainer instanceof List) {
            List<?> left = (List<?>) lcontainer;
            List<?> right = (List<?>) rcontainer;
            List<Object> result = new ArrayList<>();
            for (Object item : left) {
                if (!right.contains(item)) {
                    result.add(item);
                }
            }
            return result;
        } else if (lcontainer instanceof Map && rcontainer instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> left = (Map<String, Object>) lcontainer;
            @SuppressWarnings("unchecked")
            Map<String, Object> right = (Map<String, Object>) rcontainer;
            Map<String, Object> result = new LinkedHashMap<>();
            for (String key : left.keySet()) {
                if (!right.containsKey(key)) {
                    result.put(key, left.get(key));
                }
            }
            return result;
        }
        return new ArrayList<>();
    }

    @Word(stackEffect = "( lcontainer:any rcontainer:any -- result:any )", description = "Set intersection between two containers")
    public Object INTERSECTION(Object lcontainer, Object rcontainer) {
        if (lcontainer == null || rcontainer == null) {
            return new ArrayList<>();
        }

        if (lcontainer instanceof List && rcontainer instanceof List) {
            List<?> left = (List<?>) lcontainer;
            List<?> right = (List<?>) rcontainer;
            List<Object> result = new ArrayList<>();
            for (Object item : left) {
                if (right.contains(item)) {
                    result.add(item);
                }
            }
            return result;
        } else if (lcontainer instanceof Map && rcontainer instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> left = (Map<String, Object>) lcontainer;
            @SuppressWarnings("unchecked")
            Map<String, Object> right = (Map<String, Object>) rcontainer;
            Map<String, Object> result = new LinkedHashMap<>();
            for (String key : left.keySet()) {
                if (right.containsKey(key)) {
                    result.put(key, left.get(key));
                }
            }
            return result;
        }
        return new ArrayList<>();
    }

    @Word(stackEffect = "( lcontainer:any rcontainer:any -- result:any )", description = "Set union between two containers")
    public Object UNION(Object lcontainer, Object rcontainer) {
        if (lcontainer == null && rcontainer == null) {
            return new ArrayList<>();
        }
        if (lcontainer == null) {
            return rcontainer;
        }
        if (rcontainer == null) {
            return lcontainer;
        }

        if (lcontainer instanceof List && rcontainer instanceof List) {
            List<?> left = (List<?>) lcontainer;
            List<?> right = (List<?>) rcontainer;
            Set<Object> unionSet = new LinkedHashSet<>();
            unionSet.addAll(left);
            unionSet.addAll(right);
            return new ArrayList<>(unionSet);
        } else if (lcontainer instanceof Map && rcontainer instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> left = (Map<String, Object>) lcontainer;
            @SuppressWarnings("unchecked")
            Map<String, Object> right = (Map<String, Object>) rcontainer;
            Map<String, Object> result = new LinkedHashMap<>(left);
            for (Map.Entry<String, Object> entry : right.entrySet()) {
                if (!result.containsKey(entry.getKey())) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            return result;
        }
        return new ArrayList<>();
    }

    // ===== Sort Operations =====

    @Word(stackEffect = "( container:any[] -- array:any[] )", description = "Sort container")
    public Object SORT(Object container) {
        if (container == null || !(container instanceof List)) {
            return container;
        }

        List<?> list = (List<?>) container;
        List<Object> result = new ArrayList<>(list);
        result.sort((a, b) -> {
            if (a == null && b == null) return 0;
            if (a == null) return -1;
            if (b == null) return 1;
            if (a instanceof Comparable && b instanceof Comparable) {
                try {
                    @SuppressWarnings("unchecked")
                    Comparable<Object> ca = (Comparable<Object>) a;
                    return ca.compareTo(b);
                } catch (ClassCastException e) {
                    return a.toString().compareTo(b.toString());
                }
            }
            return a.toString().compareTo(b.toString());
        });
        return result;
    }

    @Word(stackEffect = "( array:any[] -- array:any[] )", description = "Shuffle array randomly")
    public Object SHUFFLE(Object array) {
        if (array == null || !(array instanceof List)) {
            return array;
        }

        List<?> list = (List<?>) array;
        List<Object> result = new ArrayList<>(list);
        Collections.shuffle(result, random);
        return result;
    }

    // ===== Utility Operations =====

    @Word(stackEffect = "( container:any -- elements:any )", description = "Unpack array or record elements onto stack", isDirect = true)
    public void UNPACK(BareInterpreter interp) throws Exception {
        Object container = interp.stackPop();
        if (container == null) {
            return;
        }

        if (container instanceof List) {
            List<?> list = (List<?>) container;
            for (Object item : list) {
                interp.stackPush(item);
            }
        } else if (container instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) container;
            for (Object value : map.values()) {
                interp.stackPush(value);
            }
        }
    }

    @Word(stackEffect = "( container:any value:any -- key:any )", description = "Find key of value in container", name = "KEY-OF")
    public Object KEY_OF(Object container, Object value) {
        if (container == null) {
            return null;
        }

        if (container instanceof List) {
            List<?> list = (List<?>) container;
            int index = list.indexOf(value);
            return index >= 0 ? index : null;
        } else if (container instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) container;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (Objects.equals(entry.getValue(), value)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    @Word(stackEffect = "( items:any[] forthic:string -- indexed:any )", description = "Create index mapping from array indices to values")
    public Map<String, Object> INDEX(Object items, Object forthic) throws Exception {
        if (items == null || !(items instanceof List)) {
            return new LinkedHashMap<>();
        }

        List<?> list = (List<?>) items;
        String code = forthic == null ? "" : forthic.toString();
        Map<String, Object> result = new LinkedHashMap<>();

        for (Object item : list) {
            getInterp().stackPush(item);
            getInterp().run(code);
            Object key = getInterp().stackPop();
            if (key != null) {
                result.put(key.toString(), item);
            }
        }
        return result;
    }

    @Word(stackEffect = "( container:any[] field:string -- indexed:any )", description = "Index records by field value", name = "BY-FIELD")
    public Map<String, Object> BY_FIELD(Object container, Object field) {
        if (container == null || !(container instanceof List)) {
            return new LinkedHashMap<>();
        }

        List<?> list = (List<?>) container;
        String fieldName = field == null ? "" : field.toString();
        Map<String, Object> result = new LinkedHashMap<>();

        for (Object item : list) {
            if (item instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> record = (Map<String, Object>) item;
                Object key = record.get(fieldName);
                if (key != null) {
                    result.put(key.toString(), item);
                }
            }
        }
        return result;
    }

    @Word(stackEffect = "( container:any[] field:string -- grouped:any )", description = "Group records by field value", name = "GROUP-BY-FIELD")
    public Map<String, List<Object>> GROUP_BY_FIELD(Object container, Object field) {
        if (container == null || !(container instanceof List)) {
            return new LinkedHashMap<>();
        }

        List<?> list = (List<?>) container;
        String fieldName = field == null ? "" : field.toString();
        Map<String, List<Object>> result = new LinkedHashMap<>();

        for (Object item : list) {
            if (item instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> record = (Map<String, Object>) item;
                Object key = record.get(fieldName);
                if (key != null) {
                    String keyStr = key.toString();
                    result.computeIfAbsent(keyStr, k -> new ArrayList<>()).add(item);
                }
            }
        }
        return result;
    }

    @Word(stackEffect = "( container:any[] n:number -- groups:any[] )", description = "Split array into groups of size n", name = "GROUPS-OF")
    public List<List<Object>> GROUPS_OF(Object container, Object n) {
        if (container == null || !(container instanceof List)) {
            return new ArrayList<>();
        }

        List<?> list = (List<?>) container;
        int groupSize = ((Number) n).intValue();
        if (groupSize <= 0) {
            return new ArrayList<>();
        }

        List<List<Object>> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i += groupSize) {
            List<Object> group = new ArrayList<>();
            for (int j = i; j < Math.min(i + groupSize, list.size()); j++) {
                group.add(list.get(j));
            }
            result.add(group);
        }
        return result;
    }

    // ===== Complex Operations (MAP, FOREACH, REDUCE, etc.) =====

    @Word(stackEffect = "( container:any forthic:string -- result:any )", description = "Map forthic over container")
    public Object MAP(Object container, Object forthic) throws Exception {
        if (container == null) {
            return new ArrayList<>();
        }

        String code = forthic == null ? "" : forthic.toString();

        if (container instanceof List) {
            List<?> list = (List<?>) container;
            List<Object> result = new ArrayList<>();
            for (Object item : list) {
                getInterp().stackPush(item);
                getInterp().run(code);
                result.add(getInterp().stackPop());
            }
            return result;
        } else if (container instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) container;
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                getInterp().stackPush(entry.getValue());
                getInterp().run(code);
                result.put(entry.getKey(), getInterp().stackPop());
            }
            return result;
        }
        return new ArrayList<>();
    }

    @Word(stackEffect = "( container:any forthic:string -- )", description = "Execute forthic for each item in container")
    public void FOREACH(Object container, Object forthic) throws Exception {
        if (container == null) {
            return;
        }

        String code = forthic == null ? "" : forthic.toString();

        if (container instanceof List) {
            List<?> list = (List<?>) container;
            for (Object item : list) {
                getInterp().stackPush(item);
                getInterp().run(code);
            }
        } else if (container instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) container;
            for (Object value : map.values()) {
                getInterp().stackPush(value);
                getInterp().run(code);
            }
        }
    }

    @Word(stackEffect = "( container:any forthic:string -- filtered:any )", description = "Filter items with predicate")
    public Object SELECT(Object container, Object forthic) throws Exception {
        if (container == null) {
            return new ArrayList<>();
        }

        String code = forthic == null ? "" : forthic.toString();

        if (container instanceof List) {
            List<?> list = (List<?>) container;
            List<Object> result = new ArrayList<>();
            for (Object item : list) {
                getInterp().stackPush(item);
                getInterp().run(code);
                Object predResult = getInterp().stackPop();
                if (isTruthy(predResult)) {
                    result.add(item);
                }
            }
            return result;
        } else if (container instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) container;
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                getInterp().stackPush(entry.getValue());
                getInterp().run(code);
                Object predResult = getInterp().stackPop();
                if (isTruthy(predResult)) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            return result;
        }
        return new ArrayList<>();
    }

    @Word(stackEffect = "( container:any initial:any forthic:string -- result:any )", description = "Reduce array or record with accumulator")
    public Object REDUCE(Object container, Object initial, Object forthic) throws Exception {
        if (container == null) {
            return initial;
        }

        String code = forthic == null ? "" : forthic.toString();
        Object accumulator = initial;

        if (container instanceof List) {
            List<?> list = (List<?>) container;
            for (Object item : list) {
                getInterp().stackPush(accumulator);
                getInterp().stackPush(item);
                getInterp().run(code);
                accumulator = getInterp().stackPop();
            }
        } else if (container instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) container;
            for (Object value : map.values()) {
                getInterp().stackPush(accumulator);
                getInterp().stackPush(value);
                getInterp().run(code);
                accumulator = getInterp().stackPop();
            }
        }
        return accumulator;
    }

    @Word(stackEffect = "( container:any forthic:string -- grouped:any )", description = "Group container by key function", name = "GROUP-BY")
    public Map<String, List<Object>> GROUP_BY(Object container, Object forthic) throws Exception {
        if (container == null) {
            return new LinkedHashMap<>();
        }

        String code = forthic == null ? "" : forthic.toString();
        Map<String, List<Object>> result = new LinkedHashMap<>();

        if (container instanceof List) {
            List<?> list = (List<?>) container;
            for (Object item : list) {
                getInterp().stackPush(item);
                getInterp().run(code);
                Object key = getInterp().stackPop();
                if (key != null) {
                    String keyStr = key.toString();
                    result.computeIfAbsent(keyStr, k -> new ArrayList<>()).add(item);
                }
            }
        }
        return result;
    }

    @Word(stackEffect = "( container:any[] -- flattened:any[] )", description = "Flatten nested array structure")
    public List<Object> FLATTEN(Object container) {
        if (container == null || !(container instanceof List)) {
            return new ArrayList<>();
        }

        List<Object> result = new ArrayList<>();
        flattenHelper((List<?>) container, result, Integer.MAX_VALUE);
        return result;
    }

    private void flattenHelper(List<?> list, List<Object> result, int depth) {
        if (depth <= 0) {
            result.addAll(list);
            return;
        }

        for (Object item : list) {
            if (item instanceof List) {
                flattenHelper((List<?>) item, result, depth - 1);
            } else {
                result.add(item);
            }
        }
    }

    @Word(stackEffect = "( item:any forthic:string num_times:number -- )", description = "Repeat execution of forthic num_times", isDirect = true, name = "<REPEAT")
    public void l_REPEAT(BareInterpreter interp) throws Exception {
        Object numTimes = interp.stackPop();
        Object forthic = interp.stackPop();
        Object item = interp.stackPop();

        int count = numTimes == null ? 0 : ((Number) numTimes).intValue();
        String code = forthic == null ? "" : forthic.toString();

        for (int i = 0; i < count; i++) {
            interp.stackPush(item);
            interp.run(code);
        }
    }

    // ===== Helper Methods =====

    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).doubleValue() != 0.0;
        if (value instanceof String) return !((String) value).isEmpty();
        if (value instanceof List) return true; // Arrays always truthy
        return true;
    }
}
