package kr.ac.knue.facultyevaluation.organization;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

@WebMvcTest(OrganizationManagementController.class)
@Import(GlobalExceptionHandler.class)
class OrganizationManagementContractTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    OrganizationManagementService organizationManagementService;

    @Test
    void openapiFixtureIsAvailableOnClasspathForOrganizationContractCoverage() {
        org.assertj.core.api.Assertions.assertThat(new ClassPathResource("contracts/openapi.yaml").exists()).isTrue();
    }

    @Test
    void seedMigrationContainsOrganizationScreenFixtureAndR09PermissionFixture() throws Exception {
        ClassPathResource seed = new ClassPathResource("db/migration/V2__foundation_seed.sql");
        String content = StreamUtils.copyToString(seed.getInputStream(), StandardCharsets.UTF_8);

        org.assertj.core.api.Assertions.assertThat(content)
            .contains("'KNUE-DEPT-COMMON'")
            .contains("'SCR-CMN-ORG'")
            .contains("'/admin/organizations'")
            .contains("'R09'");
    }

    @Test
    void listOrganizationsFiltersByOrgCodeAndTypeAndShowsHierarchyRelationships() throws Exception {
        OrganizationSearchResult result = new OrganizationSearchResult(List.of(
            new OrganizationSummary(
                "KNUE",
                "한국교원대학교",
                "UNIVERSITY",
                null,
                null,
                "Y",
                0,
                List.of("KNUE-COL-EDU"),
                LocalDateTime.parse("2026-01-01T09:00:00"),
                null
            ),
            new OrganizationSummary(
                "KNUE-COL-EDU",
                "교육학과군",
                "COLLEGE",
                "KNUE",
                "한국교원대학교",
                "Y",
                1,
                List.of("KNUE-DEPT-COMMON"),
                LocalDateTime.parse("2026-01-01T09:00:00"),
                null
            )
        ), 2, 0, 20);
        when(organizationManagementService.list(any(OrganizationSearchCriteria.class))).thenReturn(result);

        mockMvc.perform(get("/api/organizations")
                .param("orgCode", "KNUE")
                .param("orgType", "UNIVERSITY"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items[0].orgCode").value("KNUE"))
            .andExpect(jsonPath("$.data.items[0].orgType").value("UNIVERSITY"))
            .andExpect(jsonPath("$.data.items[0].depth").value(0))
            .andExpect(jsonPath("$.data.items[0].childOrgCodes[0]").value("KNUE-COL-EDU"))
            .andExpect(jsonPath("$.data.items[1].parentOrgCode").value("KNUE"));
    }

    @Test
    void saveOrganizationRelationshipStoresLocalCorrectionAndReturnsUpdatedHierarchy() throws Exception {
        OrganizationSummary updated = new OrganizationSummary(
            "KNUE-DEPT-COMMON",
            "공통기능학과",
            "DEPARTMENT",
            "KNUE",
            "한국교원대학교",
            "Y",
            1,
            List.of(),
            LocalDateTime.parse("2026-01-02T09:00:00"),
            new OrganizationRelationshipSummary(
                "KNUE",
                "한국교원대학교",
                LocalDate.parse("2026-03-01"),
                LocalDate.parse("2026-12-31"),
                "조직 개편 반영"
            )
        );
        when(organizationManagementService.saveRelationship(eq("KNUE-DEPT-COMMON"), any(SaveOrganizationRelationshipRequest.class)))
            .thenReturn(updated);

        mockMvc.perform(put("/api/organizations/KNUE-DEPT-COMMON/relationships")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"parentOrgCode\":\"KNUE\",\"effectiveStartDate\":\"2026-03-01\",\"effectiveEndDate\":\"2026-12-31\",\"reason\":\"조직 개편 반영\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.orgCode").value("KNUE-DEPT-COMMON"))
            .andExpect(jsonPath("$.data.parentOrgCode").value("KNUE"))
            .andExpect(jsonPath("$.data.currentRelationship.effectiveStartDate").value("2026-03-01"))
            .andExpect(jsonPath("$.data.currentRelationship.reason").value("조직 개편 반영"));
    }

    @Test
    void saveOrganizationRelationshipRejectsMissingEffectiveStartDate() throws Exception {
        mockMvc.perform(put("/api/organizations/KNUE-DEPT-COMMON/relationships")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"parentOrgCode\":\"KNUE\",\"reason\":\"시작일 누락\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void saveOrganizationRelationshipRejectsReversedEffectivePeriod() throws Exception {
        mockMvc.perform(put("/api/organizations/KNUE-DEPT-COMMON/relationships")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"parentOrgCode\":\"KNUE\",\"effectiveStartDate\":\"2026-12-31\",\"effectiveEndDate\":\"2026-03-01\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }
}
