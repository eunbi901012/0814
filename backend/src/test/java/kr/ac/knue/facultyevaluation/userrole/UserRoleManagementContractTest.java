package kr.ac.knue.facultyevaluation.userrole;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

@WebMvcTest(UserRoleManagementController.class)
@Import(GlobalExceptionHandler.class)
class UserRoleManagementContractTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserRoleManagementService userRoleManagementService;

    @Test
    void openapiFixtureIsAvailableOnClasspathForUserRoleContractCoverage() {
        org.assertj.core.api.Assertions.assertThat(new ClassPathResource("contracts/openapi.yaml").exists()).isTrue();
    }

    @Test
    void seedMigrationContainsUserRoleFixtureAndUserRoleScreenMenuFixture() throws Exception {
        ClassPathResource seed = new ClassPathResource("db/migration/V2__foundation_seed.sql");
        String content = StreamUtils.copyToString(seed.getInputStream(), StandardCharsets.UTF_8);

        org.assertj.core.api.Assertions.assertThat(content)
            .contains("'UR-ADMIN-R09'")
            .contains("'UR-FAC-0001-R01'")
            .contains("'SCR-CMN-USER-ROLE'")
            .contains("'/admin/user-roles'")
            .contains("'POSITION_BASED'")
            .contains("'MANUAL'");
    }

    @Test
    void listUserRolesShowsCurrentRolesEffectivePeriodApproverAssignmentTypeAndStatus() throws Exception {
        UserRoleSearchResult result = new UserRoleSearchResult(List.of(
            new UserRoleSummary(
                "UR-FAC-0001-R01",
                "FAC-0001",
                "김교원",
                "R01",
                "교원",
                LocalDate.parse("2026-01-01"),
                null,
                "admin",
                "시스템관리자",
                "POSITION_BASED",
                "ACTIVE",
                LocalDateTime.parse("2026-01-01T09:00:00")
            )
        ), 1, 0, 20);
        when(userRoleManagementService.list(any(UserRoleSearchCriteria.class))).thenReturn(result);

        mockMvc.perform(get("/api/user-roles")
                .param("employeeNo", "FAC-0001")
                .param("name", "김교원")
                .param("roleCode", "R01")
                .param("validOn", "2026-01-10")
                .param("assignmentType", "POSITION_BASED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items[0].userId").value("FAC-0001"))
            .andExpect(jsonPath("$.data.items[0].roleCode").value("R01"))
            .andExpect(jsonPath("$.data.items[0].effectiveStartDate").value("2026-01-01"))
            .andExpect(jsonPath("$.data.items[0].approverUserId").value("admin"))
            .andExpect(jsonPath("$.data.items[0].assignmentType").value("POSITION_BASED"))
            .andExpect(jsonPath("$.data.items[0].status").value("ACTIVE"));
    }

    @Test
    void assignUserRolePersistsNewRoleAndReturnsVisibleRow() throws Exception {
        UserRoleSummary assigned = userRoleSummary("UR-NEW-R02", "R02", "학과장", "MANUAL", "ACTIVE");
        when(userRoleManagementService.assign(any(AssignUserRoleRequest.class))).thenReturn(assigned);

        mockMvc.perform(post("/api/user-roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"FAC-0001\",\"roleCode\":\"R02\",\"assignmentType\":\"MANUAL\",\"approverUserId\":\"admin\",\"effectiveStartDate\":\"2026-02-01\",\"effectiveEndDate\":\"2026-12-31\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userRoleId").value("UR-NEW-R02"))
            .andExpect(jsonPath("$.data.roleCode").value("R02"))
            .andExpect(jsonPath("$.data.assignmentType").value("MANUAL"))
            .andExpect(jsonPath("$.data.approverUserId").value("admin"));
    }

    @Test
    void changeUserRolePersistsRoleEffectivePeriodAndApprover() throws Exception {
        UserRoleSummary changed = userRoleSummary("UR-FAC-0001-R01", "R02", "학과장", "MANUAL", "ACTIVE");
        when(userRoleManagementService.change(eq("UR-FAC-0001-R01"), any(ChangeUserRoleRequest.class))).thenReturn(changed);

        mockMvc.perform(patch("/api/user-roles/UR-FAC-0001-R01")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleCode\":\"R02\",\"assignmentType\":\"MANUAL\",\"approverUserId\":\"admin\",\"effectiveStartDate\":\"2026-02-01\",\"effectiveEndDate\":\"2026-12-31\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.roleCode").value("R02"))
            .andExpect(jsonPath("$.data.effectiveStartDate").value("2026-02-01"))
            .andExpect(jsonPath("$.data.effectiveEndDate").value("2026-12-31"));
    }

    @Test
    void revokeUserRoleMarksRoleAsRevokedWithoutPhysicalDelete() throws Exception {
        UserRoleSummary revoked = userRoleSummary("UR-FAC-0001-R01", "R01", "교원", "POSITION_BASED", "REVOKED");
        when(userRoleManagementService.revoke("UR-FAC-0001-R01")).thenReturn(revoked);

        mockMvc.perform(delete("/api/user-roles/UR-FAC-0001-R01"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userRoleId").value("UR-FAC-0001-R01"))
            .andExpect(jsonPath("$.data.status").value("REVOKED"));
    }

    @Test
    void assignUserRoleRejectsInvalidDateRange() throws Exception {
        mockMvc.perform(post("/api/user-roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"FAC-0001\",\"roleCode\":\"R02\",\"assignmentType\":\"MANUAL\",\"approverUserId\":\"admin\",\"effectiveStartDate\":\"2026-12-31\",\"effectiveEndDate\":\"2026-02-01\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    private UserRoleSummary userRoleSummary(String userRoleId, String roleCode, String roleName, String assignmentType, String status) {
        return new UserRoleSummary(
            userRoleId,
            "FAC-0001",
            "김교원",
            roleCode,
            roleName,
            LocalDate.parse("2026-02-01"),
            LocalDate.parse("2026-12-31"),
            "admin",
            "시스템관리자",
            assignmentType,
            status,
            LocalDateTime.parse("2026-02-01T09:00:00")
        );
    }
}
