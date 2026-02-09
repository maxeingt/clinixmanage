package gt.com.xfactory.dto.response;

import gt.com.xfactory.dto.request.CommonPageRequest;
import gt.com.xfactory.utils.SortUtils;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PageResponse<T> {
    public List<T> content;
    public int currentPage;
    public int totalPages;
    public long totalItems;

    public PageResponse(List<T> content, int currentPage, int totalPages, long totalItems) {
        this.content = content;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
    }

    public static <E, D> PageResponse<D> toPageResponse(
            PanacheRepository<E> repository,
            StringBuilder query,
            CommonPageRequest pageRequest,
            Map<String, Object> params,
            Function<E, D> converter) {

        Sort panacheSort = SortUtils.parseSortParameters(pageRequest.getSort());
        var panacheQuery = repository.find(query.toString(), panacheSort, params);
        long totalItems = panacheQuery.count();
        int totalPages = (int) Math.ceil((double) totalItems / pageRequest.getSize());

        List<E> dataList = panacheQuery
                .page(pageRequest.getPage(), pageRequest.getSize())
                .list();

        List<D> content = dataList.stream()
                .map(converter)
                .collect(Collectors.toList());

        return new PageResponse<>(content, pageRequest.getPage(), totalPages, totalItems);
    }
}
