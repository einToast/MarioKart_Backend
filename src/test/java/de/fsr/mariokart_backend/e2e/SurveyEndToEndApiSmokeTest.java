package de.fsr.mariokart_backend.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import de.fsr.mariokart_backend.e2e.testsupport.AbstractEndToEndApiSmokeTest;
import de.fsr.mariokart_backend.registration.model.Team;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
@TestPropertySource(properties = {
        "app.user.secret-key=MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI="
})
class SurveyEndToEndApiSmokeTest extends AbstractEndToEndApiSmokeTest {

    @Test
    void surveyLifecycleSmoke() throws Exception {
        Team team = createTeamDirect("Survey Team", "Mario");
        MockCookie adminCookie = loginAsAdmin();

        MvcResult questionResult = mockMvc.perform(post("/admin/survey")
                        .cookie(adminCookie)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"questionText":"Best track?","questionType":"MULTIPLE_CHOICE","options":["Rainbow Road","Moo Moo Meadows","Bowser Castle"],"active":true,"visible":true,"live":false,"finalTeamsOnly":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionText").value("Best track?"))
                .andReturn();

        long questionId = jsonFieldAsLong(questionResult, "id");

        mockMvc.perform(get("/public/survey/visible"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].questionText").value("Best track?"));

        mockMvc.perform(post("/public/survey/answer")
                        .cookie(new MockCookie("user", "{\"teamId\":%d}".formatted(team.getId())))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"questionId":%d,"answerType":"MULTIPLE_CHOICE","multipleChoiceSelectedOption":1}
                                """.formatted(questionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionId").value(questionId))
                .andExpect(jsonPath("$.multipleChoiceSelectedOption").value(1));

        mockMvc.perform(get("/admin/survey").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].questionText").value("Best track?"));

        mockMvc.perform(get("/admin/survey/{id}/statistics", questionId).cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1]").value(1));

        mockMvc.perform(get("/admin/survey/{id}/answers/count", questionId).cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        mockMvc.perform(put("/admin/survey/{id}", questionId)
                        .cookie(adminCookie)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"questionText":"Best track results","questionType":"MULTIPLE_CHOICE","options":["Rainbow Road","Moo Moo Meadows","Bowser Castle"],"active":false,"visible":true,"live":false,"finalTeamsOnly":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionText").value("Best track results"))
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(get("/public/survey/{id}/statistics", questionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1]").value(1));

        mockMvc.perform(delete("/admin/survey/{id}", questionId).cookie(adminCookie))
                .andExpect(status().isOk());

        assertThat(questionRepository.count()).isZero();
    }

    @Test
    void adminSurveyReadAnswersControllerSmoke() throws Exception {
        Team team = createTeamDirect("Free Text Team", "Mario");
        MockCookie adminCookie = loginAsAdmin();

        MvcResult questionResult = mockMvc.perform(post("/admin/survey")
                        .cookie(adminCookie)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"questionText":"Feedback?","questionType":"FREE_TEXT","options":[],"active":true,"visible":true,"live":false,"finalTeamsOnly":false}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        long questionId = jsonFieldAsLong(questionResult, "id");

        mockMvc.perform(post("/public/survey/answer")
                        .cookie(new MockCookie("user", "{\"teamId\":%d}".formatted(team.getId())))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"questionId":%d,"answerType":"FREE_TEXT","freeTextAnswer":"Great tournament"}
                                """.formatted(questionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.freeTextAnswer").value("Great tournament"));

        mockMvc.perform(get("/admin/survey/{id}/answers", questionId).cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].freeTextAnswer").value("Great tournament"));

        mockMvc.perform(delete("/admin/survey").cookie(adminCookie))
                .andExpect(status().isOk());

        assertThat(questionRepository.count()).isZero();
    }
}
