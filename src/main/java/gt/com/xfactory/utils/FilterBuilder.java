package gt.com.xfactory.utils;

import org.apache.commons.lang3.*;

import java.util.*;

public class FilterBuilder {

    private final List<String> conditions = new ArrayList<>();
    private final Map<String, Object> params = new HashMap<>();

    public static FilterBuilder create() {
        return new FilterBuilder();
    }

    public FilterBuilder addLike(String value, String fieldName, String paramName) {
        QueryUtils.addLikeCondition(value, fieldName, paramName, conditions, params);
        return this;
    }

    public FilterBuilder addLike(String value, String fieldName) {
        return addLike(value, fieldName, fieldName);
    }

    public <T> FilterBuilder addEquals(T value, String fieldName, String paramName) {
        if (value != null) {
            conditions.add(fieldName + " = :" + paramName);
            params.put(paramName, value);
        }
        return this;
    }

    public <T> FilterBuilder addEquals(T value, String fieldName) {
        return addEquals(value, fieldName, fieldName);
    }

    public FilterBuilder addCondition(boolean condition, String jpql, String paramName, Object value) {
        if (condition && value != null) {
            conditions.add(jpql);
            params.put(paramName, value);
        }
        return this;
    }

    /**
     * Búsqueda por nombre con soporte de múltiples tokens.
     * "marisol paredes" → firstName LIKE '%marisol%' AND (firstName LIKE '%paredes%' OR lastName LIKE '%paredes%')
     * Usa prefix para evitar conflictos de parámetros cuando se llama más de una vez en la misma query.
     */
    public FilterBuilder addNameSearch(String q, String firstNameField, String lastNameField, String prefix) {
        if (StringUtils.isBlank(q)) return this;
        conditions.add(buildNameTokenCondition(q, firstNameField, lastNameField, params, prefix));
        return this;
    }

    public FilterBuilder addNameSearch(String q, String firstNameField, String lastNameField) {
        return addNameSearch(q, firstNameField, lastNameField, "name");
    }

    public static String buildNameTokenCondition(String q, String firstNameField, String lastNameField,
                                                 Map<String, Object> params, String prefix) {
        String[] tokens = q.trim().split("\\s+");
        List<String> groups = new ArrayList<>();
        for (int i = 0; i < tokens.length; i++) {
            String p = prefix + i;
            params.put(p, tokens[i]);
            groups.add("(LOWER(" + firstNameField + ") LIKE LOWER(CONCAT('%', :" + p + ", '%')) " +
                       "OR LOWER(" + lastNameField + ") LIKE LOWER(CONCAT('%', :" + p + ", '%')))");
        }
        return "(" + String.join(" AND ", groups) + ")";
    }

    public FilterBuilder addCondition(boolean condition, String jpql, Map<String, Object> multiParams) {
        if (condition) {
            conditions.add(jpql);
            params.putAll(multiParams);
        }
        return this;
    }

    public FilterBuilder addDateRange(Object startDate, String startField, String startParam,
                                      Object endDate, String endField, String endParam) {
        if (startDate != null) {
            conditions.add(startField + " >= :" + startParam);
            params.put(startParam, startDate);
        }
        if (endDate != null) {
            conditions.add(endField + " <= :" + endParam);
            params.put(endParam, endDate);
        }
        return this;
    }

    public StringBuilder buildQuery() {
        StringBuilder query = new StringBuilder();
        if (!conditions.isEmpty()) {
            query.append(String.join(" AND ", conditions));
        }
        return query;
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
