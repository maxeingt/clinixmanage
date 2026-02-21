package gt.com.xfactory.utils;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class QueryUtilsTest {

    @Test
    void addLikeCondition_withNonBlankValue_addsLowerLikeCondition() {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        QueryUtils.addLikeCondition("maria", "firstName", "firstName", conditions, params);
        assertEquals(1, conditions.size());
        String cond = conditions.get(0);
        assertTrue(cond.contains("LOWER(firstName)"), "Debe envolver el campo en LOWER()");
        assertTrue(cond.contains("LIKE LOWER(CONCAT"), "Debe usar LIKE LOWER(CONCAT(...))");
        assertTrue(cond.contains(":firstName"), "Debe referenciar el parámetro por nombre");
        assertEquals("maria", params.get("firstName"));
    }

    @Test
    void addLikeCondition_withCustomParamName_usesCustomParam() {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        QueryUtils.addLikeCondition("test@test.com", "email", "mail", conditions, params);
        assertEquals(1, conditions.size());
        assertTrue(conditions.get(0).contains(":mail"));
        assertFalse(conditions.get(0).contains(":email"));
        assertEquals("test@test.com", params.get("mail"));
    }

    @Test
    void addLikeCondition_withBlankValue_doesNotAddCondition() {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        QueryUtils.addLikeCondition("   ", "firstName", "firstName", conditions, params);
        assertTrue(conditions.isEmpty());
        assertTrue(params.isEmpty());
    }

    @Test
    void addLikeCondition_withEmptyString_doesNotAddCondition() {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        QueryUtils.addLikeCondition("", "firstName", "firstName", conditions, params);
        assertTrue(conditions.isEmpty());
        assertTrue(params.isEmpty());
    }

    @Test
    void addLikeCondition_withNullValue_doesNotAddCondition() {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        QueryUtils.addLikeCondition(null, "firstName", "firstName", conditions, params);
        assertTrue(conditions.isEmpty());
        assertTrue(params.isEmpty());
    }

    @Test
    void addLikeCondition_doesNotAffectExistingConditions() {
        List<String> conditions = new ArrayList<>();
        conditions.add("existing = :existing");
        Map<String, Object> params = new HashMap<>();
        params.put("existing", "value");

        QueryUtils.addLikeCondition(null, "firstName", "firstName", conditions, params);

        assertEquals(1, conditions.size(), "No debe agregar condiciones si el valor es null");
        assertEquals(1, params.size(), "No debe agregar parámetros si el valor es null");
    }
}
