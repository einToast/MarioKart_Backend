package de.fsr.mariokart_backend;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import de.fsr.mariokart_backend.security.JwtTokenFilter;

@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
class MarioKartBackendApplicationTests {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private JwtTokenFilter jwtTokenFilter;

    @Autowired
    private MarioKartStartupRunner startupRunner;

	@Test
	void contextLoads() {
        assertThat(cacheManager).isNotNull();
        assertThat(securityFilterChain).isNotNull();
        assertThat(jwtTokenFilter).isNotNull();
        assertThat(startupRunner).isNotNull();
	}

}
