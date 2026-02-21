package backbone;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.assertj.core.api.Assertions.assertThat;

class BackboneApplicationTests {

    @Test
    void applicationShouldDeclareSpringBootApplicationAnnotation() {
        assertThat(Application.class.isAnnotationPresent(SpringBootApplication.class)).isTrue();
    }
}
