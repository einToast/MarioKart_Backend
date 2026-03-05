package de.fsr.mariokart_backend.healthcheck.controller.pub;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.testsupport.ContractSchemaSupport;

@WebMvcTest(PublicHealthcheckController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("contract")
class PublicHealthcheckControllerContractTest extends AbstractWebMvcSliceTest {

    private static final String SCHEMA = "contracts/public/healthcheck/public-healthcheck-controller.schema.json";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getHealthcheckMatchesContract() throws Exception {
        MvcResult result = mockMvc.perform(get("/public/healthcheck"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(
                SCHEMA,
                "get_root_success",
                result.getResponse().getContentAsString());
    }
}
