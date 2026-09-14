package de.fsr.mariokart_backend.testsupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fsr.mariokart_backend.security.JwtTokenFilter;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public abstract class AbstractWebMvcSliceTest {

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    protected JwtTokenFilter jwtTokenFilter;

    @MockitoBean
    protected CacheManager cacheManager;
}
