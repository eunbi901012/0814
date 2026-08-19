package kr.ac.knue.facultyevaluation.menupermission;

import static org.mockito.ArgumentMatchers.any;
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

@WebMvcTest(MenuPermissionManagementController.class)
@Import(GlobalExceptionHandler.class)
class MenuPermissionManagementContractTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    MenuPermissionManagementService menuPermissionManagementService;

    @Test
    void openapiFixtureIsAvailableOnClasspathForMenuPermissionContractCoverage() {
        org.assertj.core.api.Assertions.assertThat(new ClassPathResource("contracts/openapi.yaml").exists()).isTrue();
    }

    @Test
    void seedMigrationContainsMenuPermissionScreenAndR09PermissionFixture() throws Exception {
        ClassPathResource seed = new ClassPathResource("db/migration/V2__foundation_seed.sql");
        String content = StreamUtils.copyToString(seed.getInputStream(), StandardCharsets.UTF_8);

        org.assertj.core.api.Assertions.assertThat(content)
            .contains("'SCR-CMN-MENU-PERM'")
            .contains("'/admin/menu-permissions'")
            .contains("INSERT INTO menu_permission")
            .contains("'PERM-R09-' || menu_id");
    }

    @Test
    void listMenuPermissionsReturnsRoleOrganizationUserTargetMatrixAndServerPreview() throws Exception {
        MenuPermissionSearchResult result = new MenuPermissionSearchResult(List.of(
            new MenuPermissionSummary(
                "PERM-R09-M-SYS-USER",
                "ROLE",
                "R09",
                "M-SYS-USER",
                "시스템 관리",
                "사용자·조직 관리",
                "사용자 관리",
                "SCR-CMN-USER",
                "/admin/users",
                true,
                true,
                "ALLOW",
                LocalDateTime.parse("2026-01-01T09:00:00")
            ),
            new MenuPermissionSummary(
                "PERM-R09-M-SYS-MENUPERM",
                "ROLE",
                "R09",
                "M-SYS-MENUPERM",
                "시스템 관리",
                "역할·권한 관리",
                "메뉴 권한 관리",
                "SCR-CMN-MENU-PERM",
                "/admin/menu-permissions",
                true,
                true,
                "ALLOW",
                LocalDateTime.parse("2026-01-01T09:00:00")
            )
        ), 14, 0, 100, new PermissionPreview(
            List.of("/admin/users", "/admin/menu-permissions"),
            List.of("/admin/users", "/admin/menu-permissions"),
            List.of()
        ));
        when(menuPermissionManagementService.list(any(MenuPermissionSearchCriteria.class))).thenReturn(result);

        mockMvc.perform(get("/api/menu-permissions")
                .param("targetType", "ROLE")
                .param("targetId", "R09"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items[0].targetType").value("ROLE"))
            .andExpect(jsonPath("$.data.items[0].topMenuName").value("시스템 관리"))
            .andExpect(jsonPath("$.data.items[0].middleMenuName").value("사용자·조직 관리"))
            .andExpect(jsonPath("$.data.items[0].screenMenuName").value("사용자 관리"))
            .andExpect(jsonPath("$.data.items[0].canRead").value(true))
            .andExpect(jsonPath("$.data.items[0].canWrite").value(true))
            .andExpect(jsonPath("$.data.items[0].effect").value("ALLOW"))
            .andExpect(jsonPath("$.data.preview.visibleMenuUrls[0]").value("/admin/users"))
            .andExpect(jsonPath("$.data.total").value(14));
    }

    @Test
    void saveMenuPermissionsPersistsMatrixAndReturnsRefreshedPreview() throws Exception {
        MenuPermissionSearchResult result = new MenuPermissionSearchResult(List.of(
            new MenuPermissionSummary(
                "PERM-R01-M-SYS-USER",
                "ROLE",
                "R01",
                "M-SYS-USER",
                "시스템 관리",
                "사용자·조직 관리",
                "사용자 관리",
                "SCR-CMN-USER",
                "/admin/users",
                true,
                false,
                "ALLOW",
                LocalDateTime.parse("2026-01-02T09:00:00")
            ),
            new MenuPermissionSummary(
                "PERM-R01-M-SYS-MENUPERM",
                "ROLE",
                "R01",
                "M-SYS-MENUPERM",
                "시스템 관리",
                "역할·권한 관리",
                "메뉴 권한 관리",
                "SCR-CMN-MENU-PERM",
                "/admin/menu-permissions",
                false,
                false,
                "DENY",
                LocalDateTime.parse("2026-01-02T09:00:00")
            )
        ), 14, 0, 100, new PermissionPreview(
            List.of("/admin/users"),
            List.of(),
            List.of("/admin/menu-permissions")
        ));
        when(menuPermissionManagementService.save(any(SaveMenuPermissionRequest.class))).thenReturn(result);

        mockMvc.perform(put("/api/menu-permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetType\":\"ROLE\",\"targetId\":\"R01\",\"permissions\":[{\"menuId\":\"M-SYS-USER\",\"canRead\":true,\"canWrite\":false,\"effect\":\"ALLOW\"},{\"menuId\":\"M-SYS-MENUPERM\",\"canRead\":false,\"canWrite\":false,\"effect\":\"DENY\"}]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("메뉴 권한이 저장되었습니다."))
            .andExpect(jsonPath("$.data.items[0].targetId").value("R01"))
            .andExpect(jsonPath("$.data.items[0].canRead").value(true))
            .andExpect(jsonPath("$.data.items[1].effect").value("DENY"))
            .andExpect(jsonPath("$.data.preview.deniedMenuUrls[0]").value("/admin/menu-permissions"));
    }

    @Test
    void saveMenuPermissionsRejectsInvalidTargetTypeAndEffect() throws Exception {
        mockMvc.perform(put("/api/menu-permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetType\":\"GROUP\",\"targetId\":\"R01\",\"permissions\":[{\"menuId\":\"M-SYS-USER\",\"canRead\":true,\"canWrite\":false,\"effect\":\"BLOCK\"}]}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void saveMenuPermissionsRejectsWriteWithoutReadBecauseServerPermissionWouldConflict() throws Exception {
        when(menuPermissionManagementService.save(any(SaveMenuPermissionRequest.class)))
            .thenThrow(new IllegalArgumentException("변경허용은 조회허용 없이 설정할 수 없습니다."));

        mockMvc.perform(put("/api/menu-permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetType\":\"ROLE\",\"targetId\":\"R01\",\"permissions\":[{\"menuId\":\"M-SYS-USER\",\"canRead\":false,\"canWrite\":true,\"effect\":\"ALLOW\"}]}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
    }
}
