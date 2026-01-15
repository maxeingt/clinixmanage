package gt.com.xfactory.utils;

import io.quarkus.panache.common.Sort;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import java.util.ArrayList;
import java.util.List;

public class SortUtils {

    public static Sort parseSortParameters(List<String> sortParams) {
        if (sortParams == null || sortParams.isEmpty()) {
            return Sort.by("id", Sort.Direction.Ascending);
        }

        Sort sorting = null;
        for (String sortParam : sortParams) {
            String[] parts = splitSortParameter(sortParam);
            String field = parts[0];
            Sort.Direction direction = parseDirection(parts[1]);

            if (sorting == null) {
                sorting = Sort.by(field, direction);
            } else {
                sorting = sorting.and(field, direction);
            }
        }

        return sorting != null ? sorting : Sort.by("id", Sort.Direction.Ascending);
    }

    private static String[] splitSortParameter(String sortParam) {
        if (sortParam == null || sortParam.trim().isEmpty()) {
            return new String[]{"id", "asc"};
        }

        String[] parts = sortParam.trim().split("\\.");
        if (parts.length != 2) {
            return new String[]{sortParam.trim(), "asc"};
        }
        return new String[]{parts[0], parts[1]};
    }

    private static Sort.Direction parseDirection(String direction) {
        return "desc".equalsIgnoreCase(direction)
            ? Sort.Direction.Descending
            : Sort.Direction.Ascending;
    }

    public static List<Order> toCriteriaOrders(CriteriaBuilder cb, Path<?> path, List<String> sortParams, String defaultField) {
        List<Order> orders = new ArrayList<>();
        if (sortParams == null || sortParams.isEmpty()) {
            orders.add(cb.asc(path.get(defaultField)));
            return orders;
        }
        for (String sortParam : sortParams) {
            String[] parts = splitSortParameter(sortParam);
            String field = parts[0];
            boolean desc = "desc".equalsIgnoreCase(parts[1]);
            orders.add(desc ? cb.desc(path.get(field)) : cb.asc(path.get(field)));
        }
        return orders;
    }
}