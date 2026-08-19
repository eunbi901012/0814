package kr.ac.knue.facultyevaluation.menustructure;

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

@WebMvcTest(MenuStructureManagementController.class)
@Import(GlobalExceptionHandler.class)
class MenuStructureManagementContractTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    MenuStructureManagementService menuStructureManagementService;

    @Test
    void openapiFixtureIsAvailableOnClasspathForMenuTreeContractCoverage() {
        org.assertj.core.api.Assertions.assertThat(new ClassPathResource("contracts/openapi.yaml").exists()).isTrue();
    }

    @Test
    void seedMigrationContainsMenuStructureScreenAndR09PermissionFixture() throws Exception {
        ClassPathResource seed = new ClassPathResource("db/migration/V2__foundation_seed.sql");
        String content = StreamUtils.copyToString(seed.getInputStream(), StandardCharsets.UTF_8);

        org.assertj.core.api.Assertions.assertThat(content)
            .contains("'SCR-CMN-MENU-TREE'")
            .contains("'/admin/menus/tree'")
            .contains("'메뉴 구조 관리'")
            .contains("'PERM-R09-' || menu_id");
    }

    @Test
    void getMenuTreeReturnsThreeLevelMenuHierarchyWithSelectedNodeContext() throws Exception {
        when(menuStructureManagementService.getTree(any(MenuTreeSearchCriteria.class))).thenReturn(new MenuTreeSearchResult(List.of(
            new MenuTreeSummary("M-SYS", null, "시스템 관리", null, null, 1, "Y", 0, List.of("M-SYS-USERORG", "M-SYS-ROLEAUTH", "M-SYS-MENU"), LocalDateTime.parse("2026-01-01T09:00:00")),
            new MenuTreeSummary("M-SYS-MENU", "M-SYS", "메뉴 관리", null, null, 3, "Y", 1, List.of("M-SYS-MENUTREE", "M-SYS-MENUINFO"), LocalDateTime.parse("2026-01-01T09:00:00")),
            new MenuTreeSummary("M-SYS-MENUTREE", "M-SYS-MENU", "메뉴 구조 관리", "SCR-CMN-MENU-TREE", "/admin/menus/tree", 1, "Y", 2, List.of(), LocalDateTime.parse("2026-01-01T09:00:00"))
        ), 3, 0, 100));

        mockMvc.perform(get("/api/menus/tree").param("selectedMenuId", "M-SYS-MENUTREE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items[0].menuName").value("시스템 관리"))
            .andExpect(jsonPath("$.data.items[0].depth").value(0))
            .andExpect(jsonPath("$.data.items[1].menuName").value("메뉴 관리"))
            .andExpect(jsonPath("$.data.items[1].childMenuIds[0]").value("M-SYS-MENUTREE"))
            .andExpect(jsonPath("$.data.items[2].screenId").value("SCR-CMN-MENU-TREE"))
            .andExpect(jsonPath("$.data.items[2].url").value("/admin/menus/tree"));
    }

    @Test
    void updateMenuParentMovesNodeUnderNewParentAndReturnsRefreshedTree() throws Exception {
        when(menuStructureManagementService.updateParent(eq("M-SYS-MENUTREE"), any(UpdateMenuParentRequest.class))).thenReturn(new MenuTreeSearchResult(List.of(
            new MenuTreeSummary("M-SYS-ROLEAUTH", "M-SYS", "역할·권한 관리", null, null, 2, "Y", 1, List.of("M-SYS-MENUTREE"), LocalDateTime.parse("2026-01-02T09:00:00")),
            new MenuTreeSummary("M-SYS-MENUTREE", "M-SYS-ROLEAUTH", "메뉴 구조 관리", "SCR-CMN-MENU-TREE", "/admin/menus/tree", 4, "Y", 2, List.of(), LocalDateTime.parse("2026-01-02T09:00:00"))
        ), 2, 0, 100));

        mockMvc.perform(put("/api/menus/M-SYS-MENUTREE/parent")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"parentMenuId\":\"M-SYS-ROLEAUTH\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("부모메뉴가 저장되었습니다."))
            .andExpect(jsonPath("$.data.items[1].parentMenuId").value("M-SYS-ROLEAUTH"));
    }

    @Test
    void reorderMenuPersistsDisplayOrderInsideSameHierarchy() throws Exception {
        when(menuStructureManagementService.updateOrder(eq("M-SYS-MENUTREE"), any(UpdateMenuOrderRequest.class))).thenReturn(new MenuTreeSearchResult(List.of(
            new MenuTreeSummary("M-SYS-MENUINFO", "M-SYS-MENU", "메뉴 정보 관리", "SCR-CMN-MENU-INFO", "/admin/menus", 1, "Y", 2, List.of(), LocalDateTime.parse("2026-01-02T09:00:00")),
            new MenuTreeSummary("M-SYS-MENUTREE", "M-SYS-MENU", "메뉴 구조 관리", "SCR-CMN-MENU-TREE", "/admin/menus/tree", 2, "Y", 2, List.of(), LocalDateTime.parse("2026-01-02T09:00:00"))
        ), 2, 0, 100));

        mockMvc.perform(put("/api/menus/M-SYS-MENUTREE/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayOrder\":2}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("표시순서가 저장되었습니다."))
            .andExpect(jsonPath("$.data.items[1].displayOrder").value(2));
    }

    @Test
    void updateMenuParentRejectsCycleBecauseMenuCannotMoveUnderDescendant() throws Exception {
        when(menuStructureManagementService.updateParent(eq("M-SYS"), any(UpdateMenuParentRequest.class)))
            .thenThrow(new IllegalArgumentException("하위 메뉴를 부모메뉴로 지정할 수 없습니다."));

        mockMvc.perform(put("/api/menus/M-SYS/parent")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"parentMenuId\":\"M-SYS-MENUTREE\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
    }

    @Test
    void reorderMenuRejectsMissingDisplayOrder() throws Exception {
        mockMvc.perform(put("/api/menus/M-SYS-MENUTREE/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
}
