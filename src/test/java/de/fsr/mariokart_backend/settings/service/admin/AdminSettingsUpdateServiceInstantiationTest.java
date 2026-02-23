package de.fsr.mariokart_backend.settings.service.admin;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@Tag("unit")
class AdminSettingsUpdateServiceInstantiationTest {

    @Test
    void canInstantiateWithMockDependencies() throws Exception {
        Constructor<?> constructor = Arrays.stream(AdminSettingsUpdateService.class.getDeclaredConstructors())
                .max(Comparator.comparingInt(Constructor::getParameterCount))
                .orElseThrow();

        constructor.setAccessible(true);

        Object[] args = Arrays.stream(constructor.getParameterTypes())
                .map(Mockito::mock)
                .toArray();

        Object service = constructor.newInstance(args);

        assertThat(service).isNotNull();
    }
}
