package kr.ac.knue.facultyevaluation.menustructure;

import jakarta.validation.Valid;
import kr.ac.knue.facultyevaluation.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menus")
public class MenuStructureManagementController {

    private final MenuStructureManagementService menuStructureManagementService;

    public MenuStructureManagementController(MenuStructureManagementService menuStructureManagementService) {
        this.menuStructureManagementService = menuStructureManagementService;
    }

    @GetMapping("/tree")
    public ApiResponse<MenuTreeSearchResult> getMenuTree(
        @RequestParam(required = false) String selectedMenuId,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.ok(menuStructureManagementService.getTree(new MenuTreeSearchCriteria(selectedMenuId, page, size)));
    }

    @PutMapping("/{menuId}/parent")
    public ApiResponse<MenuTreeSearchResult> updateMenuParent(
        @PathVariable String menuId,
        @Valid @RequestBody UpdateMenuParentRequest request
    ) {
        return ApiResponse.ok(menuStructureManagementService.updateParent(menuId, request), "부모메뉴가 저장되었습니다.");
    }

    @PutMapping("/{menuId}/order")
    public ApiResponse<MenuTreeSearchResult> reorderMenu(
        @PathVariable String menuId,
        @Valid @RequestBody UpdateMenuOrderRequest request
    ) {
        return ApiResponse.ok(menuStructureManagementService.updateOrder(menuId, request), "표시순서가 저장되었습니다.");
    }
}
