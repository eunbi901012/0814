package kr.ac.knue.facultyevaluation.role;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
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

@WebMvcTest(RoleManagementController.class)
@Import(GlobalExceptionHandler.class)
class RoleManagementContractTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RoleManagementService roleManagementService;

    @Test
    void openapiFixtureIsAvailableOnClasspathForRoleContractCoverage() {
        org.assertj.core.api.Assertions.assertThat(new ClassPathResource("contracts/openapi.yaml").exists()).isTrue();
    }

    @Test
    void seedMigrationContainsR01ToR09RoleFixturesAndRoleScreenPermissionFixture() throws Exception {
        ClassPathResource seed = new ClassPathResource("db/migration/V2__foundation_seed.sql");
        String content = StreamUtils.copyToString(seed.getInputStream(), StandardCharsets.UTF_8);

        org.assertj.core.api.Assertions.assertThat(content)
            .contains("'R01'")
            .contains("'R09'")
            .contains("'SCR-CMN-ROLE'")
            .contains("'/admin/roles'")
            .contains("'PERM-R09-'");
    }

    @Test
    void listRolesShowsR01ToR09RoleCodesPurposeAndManagementFields() throws Exception {
        RoleSearchResult result = new RoleSearchResult(List.of(
            new RoleSummary(
                "R01",
                "교원",
                "본인 관련 업무를 수행하는 일반 사용자 역할",
                "교원 재직자",
                "SELF",
                "Y",
                LocalDateTime.parse("2026-01-01T09:00:00")
            ),
            new RoleSummary(
                "R09",
                "시스템관리자",
                "사용자·조직·메뉴·권한·코드 관리를 수행하는 관리자 역할",
                "시스템 관리자",
                "ALL",
                "Y",
                LocalDateTime.parse("2026-01-01T09:00:00")
            )
        ), 9, 0, 20);
        when(roleManagementService.list(any(RoleSearchCriteria.class))).thenReturn(result);

        mockMvc.perform(get("/api/roles")
                .param("roleCode", "R09")
                .param("roleName", "시스템관리자")
                .param("useYn", "Y"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items[0].roleCode").value("R01"))
            .andExpect(jsonPath("$.data.items[0].purpose").value("본인 관련 업무를 수행하는 일반 사용자 역할"))
            .andExpect(jsonPath("$.data.items[1].roleCode").value("R09"))
            .andExpect(jsonPath("$.data.items[1].grantCriteria").value("시스템 관리자"))
            .andExpect(jsonPath("$.data.total").value(9));
    }

    @Test
    void updateRolePersistsNamePurposeGrantCriteriaDataScopeAndKeepsRoleCodeReadonly() throws Exception {
        RoleSummary updated = new RoleSummary(
            "R09",
            "시스템관리자",
            "공통 관리 기준정보 운영",
            "보안 승인된 관리자",
            "ALL",
            "Y",
            LocalDateTime.parse("2026-01-02T09:00:00")
        );
        when(roleManagementService.updateRole(eq("R09"), any(UpdateRoleRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/roles/R09")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleCode\":\"R09\",\"roleName\":\"시스템관리자\",\"purpose\":\"공통 관리 기준정보 운영\",\"grantCriteria\":\"보안 승인된 관리자\",\"dataScopeDefault\":\"ALL\",\"useYn\":\"Y\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.roleCode").value("R09"))
            .andExpect(jsonPath("$.data.purpose").value("공통 관리 기준정보 운영"))
            .andExpect(jsonPath("$.data.grantCriteria").value("보안 승인된 관리자"));
    }

    @Test
    void updateRoleRejectsChangedRoleCodeBecauseRoleCodeIsReadonly() throws Exception {
        when(roleManagementService.updateRole(eq("R09"), any(UpdateRoleRequest.class)))
            .thenThrow(new IllegalArgumentException("역할코드는 변경할 수 없습니다."));

        mockMvc.perform(put("/api/roles/R09")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleCode\":\"R01\",\"roleName\":\"시스템관리자\",\"purpose\":\"공통 관리 기준정보 운영\",\"grantCriteria\":\"보안 승인된 관리자\",\"dataScopeDefault\":\"ALL\",\"useYn\":\"Y\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
    }

    @Test
    void updateRoleRejectsInvalidUseYn() throws Exception {
        mockMvc.perform(put("/api/roles/R09")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleCode\":\"R09\",\"roleName\":\"시스템관리자\",\"purpose\":\"공통 관리 기준정보 운영\",\"grantCriteria\":\"보안 승인된 관리자\",\"dataScopeDefault\":\"ALL\",\"useYn\":\"X\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
}
