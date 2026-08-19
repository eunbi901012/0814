package kr.ac.knue.facultyevaluation.menuinfo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

@WebMvcTest(MenuInformationManagementController.class)
@Import(GlobalExceptionHandler.class)
class MenuInformationManagementContractTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    MenuInformationManagementService menuInformationManagementService;

    @Test
    void openapiFixtureIsAvailableOnClasspathForMenuInformationContractCoverage() {
        org.assertj.core.api.Assertions.assertThat(new ClassPathResource("contracts/openapi.yaml").exists()).isTrue();
    }

    @Test
    void seedMigrationContainsMenuInformationScreenAndR09PermissionFixture() throws Exception {
        ClassPathResource seed = new ClassPathResource("db/migration/V2__foundation_seed.sql");
        String content = StreamUtils.copyToString(seed.getInputStream(), StandardCharsets.UTF_8);

        org.assertj.core.api.Assertions.assertThat(content)
            .contains("'SCR-CMN-MENU-INFO'")
            .contains("'/admin/menus'")
            .contains("'메뉴 정보 관리'")
            .contains("'PERM-R09-' || menu_id");
    }

    @Test
    void listMenusReturnsExecutionInformationColumnsAndScreenUrlLinkage() throws Exception {
        when(menuInformationManagementService.list(any(MenuInformationSearchCriteria.class))).thenReturn(new MenuInformationSearchResult(List.of(
            new MenuInformationSummary("M-SYS-MENUINFO", "M-SYS-MENU", "메뉴 정보 관리", "SCR-CMN-MENU-INFO", "/admin/menus", "menu-info", "SYSTEM", "메뉴 실행정보 관리", 2, "Y", LocalDateTime.parse("2026-01-01T09:00:00"))
        ), 1, 0, 20));

        mockMvc.perform(get("/api/menus")
                .param("menuName", "메뉴")
                .param("screenId", "SCR-CMN-MENU-INFO")
                .param("url", "/admin/menus")
                .param("businessType", "SYSTEM"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items[0].menuName").value("메뉴 정보 관리"))
            .andExpect(jsonPath("$.data.items[0].screenId").value("SCR-CMN-MENU-INFO"))
            .andExpect(jsonPath("$.data.items[0].url").value("/admin/menus"))
            .andExpect(jsonPath("$.data.items[0].icon").value("menu-info"))
            .andExpect(jsonPath("$.data.items[0].businessType").value("SYSTEM"))
            .andExpect(jsonPath("$.data.items[0].description").value("메뉴 실행정보 관리"));
    }

    @Test
    void createMenuRegistersExecutionInformationAndReturnsCreatedRow() throws Exception {
        when(menuInformationManagementService.create(any(CreateMenuRequest.class))).thenReturn(
            new MenuInformationSummary("M-SYS-NEWINFO", "M-SYS-MENU", "신규 메뉴", "SCR-CMN-NEW", "/admin/new-menu", "new", "SYSTEM", "신규 실행정보", 9, "Y", LocalDateTime.parse("2026-01-02T09:00:00"))
        );

        mockMvc.perform(post("/api/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuId\":\"M-SYS-NEWINFO\",\"parentMenuId\":\"M-SYS-MENU\",\"menuName\":\"신규 메뉴\",\"screenId\":\"SCR-CMN-NEW\",\"url\":\"/admin/new-menu\",\"icon\":\"new\",\"businessType\":\"SYSTEM\",\"description\":\"신규 실행정보\",\"displayOrder\":9,\"useYn\":\"Y\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("메뉴 실행정보가 등록되었습니다."))
            .andExpect(jsonPath("$.data.menuId").value("M-SYS-NEWINFO"))
            .andExpect(jsonPath("$.data.screenId").value("SCR-CMN-NEW"))
            .andExpect(jsonPath("$.data.url").value("/admin/new-menu"));
    }

    @Test
    void updateMenuPersistsExecutionInformationAndKeepsScreenUrlConnectionVisible() throws Exception {
        when(menuInformationManagementService.update(eq("M-SYS-MENUINFO"), any(UpdateMenuRequest.class))).thenReturn(
            new MenuInformationSummary("M-SYS-MENUINFO", "M-SYS-MENU", "메뉴 정보 관리", "SCR-CMN-MENU-INFO", "/admin/menus", "menu-info-updated", "SYSTEM", "수정된 설명", 2, "Y", LocalDateTime.parse("2026-01-03T09:00:00"))
        );

        mockMvc.perform(put("/api/menus/M-SYS-MENUINFO")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"parentMenuId\":\"M-SYS-MENU\",\"menuName\":\"메뉴 정보 관리\",\"screenId\":\"SCR-CMN-MENU-INFO\",\"url\":\"/admin/menus\",\"icon\":\"menu-info-updated\",\"businessType\":\"SYSTEM\",\"description\":\"수정된 설명\",\"displayOrder\":2,\"useYn\":\"Y\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("메뉴 실행정보가 저장되었습니다."))
            .andExpect(jsonPath("$.data.menuId").value("M-SYS-MENUINFO"))
            .andExpect(jsonPath("$.data.icon").value("menu-info-updated"))
            .andExpect(jsonPath("$.data.description").value("수정된 설명"));
    }

    @Test
    void createMenuRejectsMissingRequiredExecutionInformation() throws Exception {
        mockMvc.perform(post("/api/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuId\":\"\",\"menuName\":\"\",\"screenId\":\"SCR-CMN-NEW\",\"url\":\"/admin/new-menu\",\"displayOrder\":1,\"useYn\":\"Y\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void updateMenuRejectsUrlOutsideAdminRouteContract() throws Exception {
        when(menuInformationManagementService.update(eq("M-SYS-MENUINFO"), any(UpdateMenuRequest.class)))
            .thenThrow(new IllegalArgumentException("URL은 /admin/ 경로로 시작해야 합니다."));

        mockMvc.perform(put("/api/menus/M-SYS-MENUINFO")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"parentMenuId\":\"M-SYS-MENU\",\"menuName\":\"메뉴 정보 관리\",\"screenId\":\"SCR-CMN-MENU-INFO\",\"url\":\"/external/menus\",\"icon\":\"menu-info\",\"businessType\":\"SYSTEM\",\"description\":\"메뉴 실행정보 관리\",\"displayOrder\":2,\"useYn\":\"Y\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
    }
}
