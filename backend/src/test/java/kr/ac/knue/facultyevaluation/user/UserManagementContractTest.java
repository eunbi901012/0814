package kr.ac.knue.facultyevaluation.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@WebMvcTest(UserManagementController.class)
@Import(GlobalExceptionHandler.class)
class UserManagementContractTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserManagementService userManagementService;

    @Test
    void openapiFixtureIsAvailableOnClasspathForUserManagementContractCoverage() {
        org.assertj.core.api.Assertions.assertThat(new ClassPathResource("contracts/openapi.yaml").exists()).isTrue();
    }

    @Test
    void searchUsersFiltersBySourceBackedConditionsAndShowsRolePositionRetirementAndSyncColumns() throws Exception {
        UserSearchResult result = new UserSearchResult(List.of(new UserSummary(
            "FAC-0001",
            "fac0001",
            "김교원",
            "KNUE-DEPT-COMMON",
            "공통기능학과",
            "교수",
            "ACTIVE",
            List.of("R01"),
            "Y",
            "학과장",
            null,
            LocalDateTime.parse("2026-01-01T09:00:00")
        )), 1, 0, 20);
        when(userManagementService.search(any(UserSearchCriteria.class))).thenReturn(result);

        mockMvc.perform(get("/api/users")
                .param("employeeNo", "FAC-0001")
                .param("name", "김교원")
                .param("departmentCode", "KNUE-DEPT-COMMON")
                .param("rankName", "교수")
                .param("employmentStatus", "ACTIVE")
                .param("roleCode", "R01")
                .param("systemUseYn", "Y"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items[0].userId").value("FAC-0001"))
            .andExpect(jsonPath("$.data.items[0].rankName").value("교수"))
            .andExpect(jsonPath("$.data.items[0].roles[0]").value("R01"))
            .andExpect(jsonPath("$.data.items[0].positionName").value("학과장"))
            .andExpect(jsonPath("$.data.items[0].lastSyncedAt").value("2026-01-01T09:00:00"));
    }

    @Test
    void updateUserUsagePersistsUseYnAndReturnsUpdatedUser() throws Exception {
        UserSummary updated = new UserSummary("FAC-0001", "fac0001", "김교원", "KNUE-DEPT-COMMON", "공통기능학과",
            "교수", "ACTIVE", List.of("R01"), "N", "학과장", null, LocalDateTime.parse("2026-01-01T09:00:00"));
        when(userManagementService.updateUsage(eq("FAC-0001"), any(UpdateUserUsageRequest.class))).thenReturn(updated);

        mockMvc.perform(patch("/api/users/FAC-0001/usage")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"systemUseYn\":\"N\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.systemUseYn").value("N"));
    }

    @Test
    void updateUserUsageRejectsInvalidUseYn() throws Exception {
        mockMvc.perform(patch("/api/users/FAC-0001/usage")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"systemUseYn\":\"X\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void updateUserBusinessRolesStoresRolesValidityAndApprover() throws Exception {
        UserSummary updated = new UserSummary("FAC-0001", "fac0001", "김교원", "KNUE-DEPT-COMMON", "공통기능학과",
            "교수", "ACTIVE", List.of("R01", "R02"), "Y", "학과장", null, LocalDateTime.parse("2026-01-01T09:00:00"));
        when(userManagementService.updateBusinessRoles(eq("FAC-0001"), any(UpdateUserRolesRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/users/FAC-0001/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleCodes\":[\"R01\",\"R02\"],\"assignmentType\":\"MANUAL\",\"approverUserId\":\"admin\",\"effectiveStartDate\":\"2026-01-01\",\"effectiveEndDate\":\"2026-12-31\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.roles[1]").value("R02"));
    }

    @Test
    void updateUserBusinessRolesRejectsReversedValidityPeriod() throws Exception {
        mockMvc.perform(put("/api/users/FAC-0001/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleCodes\":[\"R01\"],\"assignmentType\":\"MANUAL\",\"approverUserId\":\"admin\",\"effectiveStartDate\":\"2026-12-31\",\"effectiveEndDate\":\"2026-01-01\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }
}
