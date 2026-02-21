package backbone.dto;

import backbone.models.Project;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PaginationMapperTest {

    @Test
    void fromShouldMapFirstPageMetadataCorrectly() {
        Page<Project> page = new PageImpl<>(
                List.of(new Project(), new Project()),
                PageRequest.of(0, 2),
                5
        );

        PaginationMeta meta = PaginationMapper.from(page);

        assertThat(meta.getPage()).isEqualTo(0);
        assertThat(meta.getSize()).isEqualTo(2);
        assertThat(meta.getTotalElements()).isEqualTo(5);
        assertThat(meta.getTotalPages()).isEqualTo(3);
        assertThat(meta.isHasNext()).isTrue();
        assertThat(meta.isHasPrevious()).isFalse();
    }

    @Test
    void fromShouldMapMiddlePageMetadataCorrectly() {
        Page<Project> page = new PageImpl<>(
                List.of(new Project(), new Project()),
                PageRequest.of(1, 2),
                6
        );

        PaginationMeta meta = PaginationMapper.from(page);

        assertThat(meta.getPage()).isEqualTo(1);
        assertThat(meta.getSize()).isEqualTo(2);
        assertThat(meta.getTotalElements()).isEqualTo(6);
        assertThat(meta.getTotalPages()).isEqualTo(3);
        assertThat(meta.isHasNext()).isTrue();
        assertThat(meta.isHasPrevious()).isTrue();
    }

    @Test
    void fromShouldMapLastPageMetadataCorrectly() {
        Page<Project> page = new PageImpl<>(
                List.of(new Project()),
                PageRequest.of(2, 2),
                5
        );

        PaginationMeta meta = PaginationMapper.from(page);

        assertThat(meta.getPage()).isEqualTo(2);
        assertThat(meta.getSize()).isEqualTo(2);
        assertThat(meta.getTotalElements()).isEqualTo(5);
        assertThat(meta.getTotalPages()).isEqualTo(3);
        assertThat(meta.isHasNext()).isFalse();
        assertThat(meta.isHasPrevious()).isTrue();
    }
}
