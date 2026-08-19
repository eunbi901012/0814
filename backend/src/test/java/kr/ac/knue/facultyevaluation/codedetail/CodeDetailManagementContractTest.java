package kr.ac.knue.facultyevaluation.codedetail;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import kr.ac.knue.facultyevaluation.common.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;

@WebMvcTest(CodeDetailManagementController.class)
@Import(GlobalExceptionHandler.class)
class CodeDetailManagementContractTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CodeDetailManagementService codeDetailManagementService;

    @Test
    void openapiFixtureIsAvailableOnClasspathForCodeDetailContractCoverage() {
        org.assertj.core.api.Assertions.assertThat(new ClassPathResource("contracts/openapi.yaml").exists()).isTrue();
    }

    @Test
    void seedMigrationContainsCodeDetailScreenPermissionAndMinimumDetailFixtures() throws Exception {
        ClassPathResource seed = new ClassPathResource("db/migration/V2__foundation_seed.sql");
        String content = StreamUtils.copyToString(seed.getInputStream(), StandardCharsets.UTF_8);

        org.assertj.core.api.Assertions.assertThat(content)
            .contains("'SCR-CMN-CODE-DETAIL'")
            .contains("'/admin/code-details'")
            .contains("'상세코드 관리'")
            .contains("INSERT INTO code_detail")
            .contains("'EMPLOYMENT_STATUS', 'ACTIVE'")
            .contains("'USE_YN', 'Y'");
    }

    @Test
    void listCodeDetailsReturnsGroupScopedHierarchyAndAttributes() throws Exception {
        when(codeDetailManagementService.list(any(CodeDetailSearchCriteria.class))).thenReturn(new CodeDetailSearchResult(List.of(
            new CodeDetailSummary("EMPLOYMENT_STATUS", "ACTIVE", "재직", null, 1, "{\"source\":\"local-seed\"}", "Y", LocalDate.parse("2026-01-01"), null, 0, LocalDateTime.parse("2026-01-01T09:00:00")),
            new CodeDetailSummary("EMPLOYMENT_STATUS", "LEAVE", "휴직", "ACTIVE", 2, "{\"parent\":\"ACTIVE\"}", "Y", LocalDate.parse("2026-01-01"), null, 1, LocalDateTime.parse("2026-01-01T09:10:00"))
        ), 2, 0, 20));

        mockMvc.perform(get("/api/code-details")
                .param("groupId", "EMPLOYMENT_STATUS")
                .param("filter", "재직"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items[0].groupId").value("EMPLOYMENT_STATUS"))
            .andExpect(jsonPath("$.data.items[0].codeValue").value("ACTIVE"))
            .andExpect(jsonPath("$.data.items[0].codeName").value("재직"))
            .andExpect(jsonPath("$.data.items[0].sortOrder").value(1))
            .andExpect(jsonPath("$.data.items[0].extraAttributes").value("{\"source\":\"local-seed\"}"))
            .andExpect(jsonPath("$.data.items[1].parentCodeValue").value("ACTIVE"))
            .andExpect(jsonPath("$.data.items[1].depth").value(1))
            .andExpect(jsonPath("$.data.total").value(2));
    }

    @Test
    void createCodeDetailRegistersCodeValueNameParentOrderAttributesUseYnAndPeriod() throws Exception {
        when(codeDetailManagementService.create(any(CreateCodeDetailRequest.class))).thenReturn(
            new CodeDetailSummary("EMPLOYMENT_STATUS", "SABBATICAL", "연구년", "ACTIVE", 4, "{\"mapping\":\"sabbatical\"}", "Y", LocalDate.parse("2026-03-01"), null, 1, LocalDateTime.parse("2026-03-01T09:00:00"))
        );

        mockMvc.perform(post("/api/code-details")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"EMPLOYMENT_STATUS\",\"codeValue\":\"SABBATICAL\",\"codeName\":\"연구년\",\"parentCodeValue\":\"ACTIVE\",\"sortOrder\":4,\"extraAttributes\":\"{\\\"mapping\\\":\\\"sabbatical\\\"}\",\"useYn\":\"Y\",\"validFrom\":\"2026-03-01\",\"validTo\":null}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("상세코드가 등록되었습니다."))
            .andExpect(jsonPath("$.data.groupId").value("EMPLOYMENT_STATUS"))
            .andExpect(jsonPath("$.data.codeValue").value("SABBATICAL"))
            .andExpect(jsonPath("$.data.parentCodeValue").value("ACTIVE"));
    }

    @Test
    void updateCodeDetailPersistsEditableFieldsAndKeepsIdentityReadonly() throws Exception {
        when(codeDetailManagementService.update(eq("USE_YN"), eq("Y"), any(UpdateCodeDetailRequest.class))).thenReturn(
            new CodeDetailSummary("USE_YN", "Y", "사용함", null, 1, "{\"boolean\":true,\"label\":\"enabled\"}", "Y", LocalDate.parse("2026-01-01"), null, 0, LocalDateTime.parse("2026-03-02T09:00:00"))
        );

        mockMvc.perform(put("/api/code-details/USE_YN/Y")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"USE_YN\",\"codeValue\":\"Y\",\"codeName\":\"사용함\",\"parentCodeValue\":null,\"sortOrder\":1,\"extraAttributes\":\"{\\\"boolean\\\":true,\\\"label\\\":\\\"enabled\\\"}\",\"useYn\":\"Y\",\"validFrom\":\"2026-01-01\",\"validTo\":null}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("상세코드가 저장되었습니다."))
            .andExpect(jsonPath("$.data.groupId").value("USE_YN"))
            .andExpect(jsonPath("$.data.codeValue").value("Y"))
            .andExpect(jsonPath("$.data.codeName").value("사용함"));
    }

    @Test
    void updateCodeDetailRejectsChangedIdentityBecauseGroupIdAndCodeValueAreReadonly() throws Exception {
        when(codeDetailManagementService.update(eq("USE_YN"), eq("Y"), any(UpdateCodeDetailRequest.class)))
            .thenThrow(new IllegalArgumentException("그룹ID와 코드값은 변경할 수 없습니다."));

        mockMvc.perform(put("/api/code-details/USE_YN/Y")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"OTHER\",\"codeValue\":\"N\",\"codeName\":\"사용\",\"sortOrder\":1,\"useYn\":\"Y\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
    }

    @Test
    void createCodeDetailRejectsMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/code-details")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"\",\"codeValue\":\"\",\"codeName\":\"\",\"sortOrder\":0,\"useYn\":\"Y\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void createCodeDetailRejectsInvalidPeriodRange() throws Exception {
        when(codeDetailManagementService.create(any(CreateCodeDetailRequest.class)))
            .thenThrow(new IllegalArgumentException("유효 시작일은 종료일보다 늦을 수 없습니다."));

        mockMvc.perform(post("/api/code-details")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"EMPLOYMENT_STATUS\",\"codeValue\":\"BAD_PERIOD\",\"codeName\":\"기간오류\",\"sortOrder\":9,\"useYn\":\"Y\",\"validFrom\":\"2026-12-31\",\"validTo\":\"2026-01-01\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
    }
}
