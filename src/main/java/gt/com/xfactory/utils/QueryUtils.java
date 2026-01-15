package gt.com.xfactory.utils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import jakarta.persistence.EntityManager;

public class QueryUtils {

    public static void addLikeCondition(String value, String fieldName, String paramName,
                                      List<String> conditions, Map<String, Object> params) {
        if (StringUtils.isNotBlank(value)) {
            conditions.add(String.format("LOWER(%s) LIKE LOWER(CONCAT('%%', :%s, '%%'))", fieldName, paramName));
            params.put(paramName, value);
        }
    }

    /**
     * Agrega un filtro LIKE (case-insensitive) a un predicado base para un campo específico.
     * @param cb CriteriaBuilder
     * @param path Path raíz (ej: root o join)
     * @param basePredicate predicado base
     * @param field nombre del campo
     * @param value valor a filtrar (LIKE)
     * @return Predicate combinado
     */
    public static Predicate addLikePredicate(
            CriteriaBuilder cb,
            Path<?> path,
            Predicate basePredicate,
            String field,
            String value
    ) {
        if (StringUtils.isNotBlank(value)) {
            return cb.and(basePredicate,
                cb.like(cb.lower(path.get(field)), "%" + value.toLowerCase() + "%")
            );
        }
        return basePredicate;
    }

    /**
     * Agrega un filtro EQUAL a un predicado base para un campo específico.
     * @param cb CriteriaBuilder
     * @param path Path raíz (ej: root o join)
     * @param basePredicate predicado base
     * @param field nombre del campo
     * @param value valor a comparar (EQUAL)
     * @return Predicate combinado
     */
    public static <T> Predicate addEqualPredicate(
            CriteriaBuilder cb,
            Path<?> path,
            Predicate basePredicate,
            String field,
            T value
    ) {
        if (value != null) {
            return cb.and(basePredicate, cb.equal(path.get(field), value));
        }
        return basePredicate;
    }

    /**
     * Obtiene el conteo total de registros para una consulta paginada
     * @param em EntityManager
     * @param root Root de la entidad
     * @param joinPath Path opcional para el join
     * @param predicate Predicado con las condiciones
     * @return Total de registros
     */
    public static Long getTotal(
            EntityManager em,
            Root<?> root,
            Join<?, ?> joinPath,
            Predicate predicate
    ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<?> countRoot = countQuery.from(root.getJavaType());
        Join<?, ?> countJoin = null;

        if (joinPath != null) {
            countJoin = countRoot.join(joinPath.getAttribute().getName());
            countQuery.select(cb.countDistinct(countJoin));
        } else {
            countQuery.select(cb.countDistinct(countRoot));
        }

        countQuery.where(predicate);
        return em.createQuery(countQuery).getSingleResult();
    }
}
