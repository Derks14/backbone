package backbone.dto;

import org.springframework.data.domain.Page;

public class PaginationMapper {

    public static <T> PaginationMeta from(Page<T> page) {
        return PaginationMeta.builder()
                .size(page.getSize())
                .page(page.getNumber())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();

    }
}
