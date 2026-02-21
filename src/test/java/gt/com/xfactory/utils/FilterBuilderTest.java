package gt.com.xfactory.utils;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FilterBuilderTest {

    // ========== addLike ==========

    @Test
    void addLike_withNullValue_doesNotAddCondition() {
        FilterBuilder fb = FilterBuilder.create();
        fb.addLike(null, "firstName");
        assertEquals("", fb.buildQuery().toString());
        assertTrue(fb.getParams().isEmpty());
    }

    @Test
    void addLike_withBlankValue_doesNotAddCondition() {
        FilterBuilder fb = FilterBuilder.create();
        fb.addLike("   ", "firstName");
        assertEquals("", fb.buildQuery().toString());
        assertTrue(fb.getParams().isEmpty());
    }

    @Test
    void addLike_withValue_addsLikeCondition() {
        FilterBuilder fb = FilterBuilder.create();
        fb.addLike("maria", "firstName");
        String query = fb.buildQuery().toString();
        assertTrue(query.contains("firstName"), "Debe contener el nombre del campo");
        assertTrue(query.contains("LIKE"), "Debe contener LIKE");
        assertTrue(query.contains(":firstName"), "Debe referenciar el parámetro");
        assertEquals("maria", fb.getParams().get("firstName"));
    }

    @Test
    void addLike_withCustomParamName_usesCustomParam() {
        FilterBuilder fb = FilterBuilder.create();
        fb.addLike("test@test.com", "email", "mail");
        String query = fb.buildQuery().toString();
        assertTrue(query.contains(":mail"), "Debe usar el paramName personalizado");
        assertFalse(query.contains(":email"), "No debe usar el nombre del campo como param");
        assertEquals("test@test.com", fb.getParams().get("mail"));
    }

    // ========== addEquals ==========

    @Test
    void addEquals_withNullValue_doesNotAddCondition() {
        FilterBuilder fb = FilterBuilder.create();
        fb.addEquals((String) null, "gender");
        assertEquals("", fb.buildQuery().toString());
        assertTrue(fb.getParams().isEmpty());
    }

    @Test
    void addEquals_withValue_addsEqualsCondition() {
        FilterBuilder fb = FilterBuilder.create();
        fb.addEquals("MALE", "gender");
        String query = fb.buildQuery().toString();
        assertTrue(query.contains("gender = :gender"));
        assertEquals("MALE", fb.getParams().get("gender"));
    }

    @Test
    void addEquals_withCustomParamName_usesCustomParam() {
        FilterBuilder fb = FilterBuilder.create();
        fb.addEquals(42, "age", "ageParam");
        String query = fb.buildQuery().toString();
        assertTrue(query.contains("age = :ageParam"));
        assertEquals(42, fb.getParams().get("ageParam"));
    }

    // ========== addNameSearch ==========

    @Test
    void addNameSearch_withBlankQ_doesNotAddCondition() {
        FilterBuilder fb = FilterBuilder.create();
        fb.addNameSearch("  ", "firstName", "lastName");
        assertEquals("", fb.buildQuery().toString());
        assertTrue(fb.getParams().isEmpty());
    }

    @Test
    void addNameSearch_withNullQ_doesNotAddCondition() {
        FilterBuilder fb = FilterBuilder.create();
        fb.addNameSearch(null, "firstName", "lastName");
        assertEquals("", fb.buildQuery().toString());
        assertTrue(fb.getParams().isEmpty());
    }

    @Test
    void addNameSearch_withOneToken_addsSingleGroupWithOrBetweenFields() {
        FilterBuilder fb = FilterBuilder.create();
        fb.addNameSearch("maria", "firstName", "lastName");
        String query = fb.buildQuery().toString();
        assertTrue(query.contains("firstName"), "Debe buscar en firstName");
        assertTrue(query.contains("lastName"), "Debe buscar en lastName");
        assertTrue(query.contains("OR"), "Debe usar OR entre campos");
        assertEquals("maria", fb.getParams().get("name0"));
    }

    @Test
    void addNameSearch_withTwoTokens_addsTwoGroupsWithAnd() {
        FilterBuilder fb = FilterBuilder.create();
        fb.addNameSearch("maria perez", "firstName", "lastName");
        String query = fb.buildQuery().toString();
        assertTrue(query.contains("AND"), "Debe unir los tokens con AND");
        assertEquals("maria", fb.getParams().get("name0"));
        assertEquals("perez", fb.getParams().get("name1"));
    }

    // ========== buildNameTokenCondition ==========

    @Test
    void buildNameTokenCondition_withOneToken_buildsSingleGroup() {
        Map<String, Object> params = new HashMap<>();
        String condition = FilterBuilder.buildNameTokenCondition("mario", "firstName", "lastName", params, "tok");
        assertTrue(condition.contains(":tok0"));
        assertFalse(condition.contains(":tok1"), "Solo debe haber un token");
        assertEquals("mario", params.get("tok0"));
    }

    @Test
    void buildNameTokenCondition_withTwoTokens_buildsTwoGroups() {
        Map<String, Object> params = new HashMap<>();
        String condition = FilterBuilder.buildNameTokenCondition("ana maria", "firstName", "lastName", params, "tok");
        assertTrue(condition.contains(":tok0"));
        assertTrue(condition.contains(":tok1"));
        assertEquals("ana", params.get("tok0"));
        assertEquals("maria", params.get("tok1"));
    }

    @Test
    void buildNameTokenCondition_withThreeTokens_buildsThreeGroups() {
        Map<String, Object> params = new HashMap<>();
        String condition = FilterBuilder.buildNameTokenCondition("a b c", "p.firstName", "p.lastName", params, "tok");
        assertTrue(condition.contains(":tok0"));
        assertTrue(condition.contains(":tok1"));
        assertTrue(condition.contains(":tok2"));
        assertEquals("a", params.get("tok0"));
        assertEquals("b", params.get("tok1"));
        assertEquals("c", params.get("tok2"));
    }

    @Test
    void buildNameTokenCondition_differentPrefixes_doNotCollide() {
        Map<String, Object> params = new HashMap<>();
        FilterBuilder.buildNameTokenCondition("ana", "firstName", "lastName", params, "prefix1");
        FilterBuilder.buildNameTokenCondition("juan", "firstName", "lastName", params, "prefix2");
        assertEquals("ana", params.get("prefix10"));
        assertEquals("juan", params.get("prefix20"));
        assertEquals(2, params.size(), "Prefijos distintos no deben sobrescribirse entre sí");
    }

    // ========== addDateRange ==========

    @Test
    void addDateRange_bothNull_doesNotAddConditions() {
        FilterBuilder fb = FilterBuilder.create();
        fb.addDateRange(null, "createdAt", "startDate", null, "createdAt", "endDate");
        assertEquals("", fb.buildQuery().toString());
        assertTrue(fb.getParams().isEmpty());
    }

    @Test
    void addDateRange_onlyStartDate_addsOnlyStartCondition() {
        FilterBuilder fb = FilterBuilder.create();
        fb.addDateRange("2024-01-01", "createdAt", "startDate", null, "createdAt", "endDate");
        String query = fb.buildQuery().toString();
        assertTrue(query.contains("createdAt >= :startDate"));
        assertFalse(query.contains("endDate"), "No debe agregar condición de endDate si es null");
    }

    @Test
    void addDateRange_onlyEndDate_addsOnlyEndCondition() {
        FilterBuilder fb = FilterBuilder.create();
        fb.addDateRange(null, "createdAt", "startDate", "2024-12-31", "createdAt", "endDate");
        String query = fb.buildQuery().toString();
        assertFalse(query.contains("startDate"), "No debe agregar condición de startDate si es null");
        assertTrue(query.contains("createdAt <= :endDate"));
    }

    @Test
    void addDateRange_bothDates_addsBothConditions() {
        FilterBuilder fb = FilterBuilder.create();
        fb.addDateRange("2024-01-01", "createdAt", "startDate", "2024-12-31", "createdAt", "endDate");
        String query = fb.buildQuery().toString();
        assertTrue(query.contains("createdAt >= :startDate"));
        assertTrue(query.contains("createdAt <= :endDate"));
        assertTrue(query.contains("AND"), "Ambas condiciones se unen con AND");
    }

    // ========== buildQuery ==========

    @Test
    void buildQuery_withNoConditions_returnsEmptyString() {
        FilterBuilder fb = FilterBuilder.create();
        assertEquals("", fb.buildQuery().toString());
    }

    @Test
    void buildQuery_withOneCondition_noLeadingAnd() {
        FilterBuilder fb = FilterBuilder.create();
        fb.addEquals("value", "field");
        String query = fb.buildQuery().toString();
        assertFalse(query.startsWith("AND"), "No debe empezar con AND");
        assertTrue(query.contains("field = :field"));
    }

    @Test
    void buildQuery_withMultipleConditions_joinsWithAnd() {
        FilterBuilder fb = FilterBuilder.create();
        fb.addEquals("v1", "field1")
          .addEquals("v2", "field2")
          .addEquals("v3", "field3");
        String query = fb.buildQuery().toString();
        assertTrue(query.contains("field1 = :field1"));
        assertTrue(query.contains("field2 = :field2"));
        assertTrue(query.contains("field3 = :field3"));
        // Dos ANDs para tres condiciones
        assertEquals(2, countOccurrences(query, "AND"));
    }

    // ========== getParams ==========

    @Test
    void getParams_reflectsAllAddedParams() {
        FilterBuilder fb = FilterBuilder.create();
        fb.addEquals("v1", "f1")
          .addLike("v2", "f2", "p2");
        Map<String, Object> params = fb.getParams();
        assertEquals("v1", params.get("f1"));
        assertEquals("v2", params.get("p2"));
        assertEquals(2, params.size());
    }

    @Test
    void getParams_emptyWhenNoConditions() {
        FilterBuilder fb = FilterBuilder.create();
        assertTrue(fb.getParams().isEmpty());
    }

    // ========== helpers ==========

    private int countOccurrences(String text, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }
}
