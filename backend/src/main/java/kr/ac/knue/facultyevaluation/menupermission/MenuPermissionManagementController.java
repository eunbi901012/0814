package kr.ac.knue.facultyevaluation.menupermission;

import jakarta.validation.Valid;
import kr.ac.knue.facultyevaluation.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menu-permissions")
public class MenuPermissionManagementController {

    private final MenuPermissionManagementService menuPermissionManagementService;

    public MenuPermissionManagementController(MenuPermissionManagementService menuPermissionManagementService) {
        this.menuPermissionManagementService = menuPermissionManagementService;
    }

    @GetMapping
    public ApiResponse<MenuPermissionSearchResult> listMenuPermissions(
        @RequestParam String targetType,
        @RequestParam String targetId,
        @RequestParam(required = false) String menuId,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.ok(menuPermissionManagementService.list(new MenuPermissionSearchCriteria(
            targetType,
            targetId,
            menuId,
            page,
            size
        )));
    }

    @PutMapping
    public ApiResponse<MenuPermissionSearchResult> saveMenuPermissions(@Valid @RequestBody SaveMenuPermissionRequest request) {
        return ApiResponse.ok(menuPermissionManagementService.save(request), "메뉴 권한이 저장되었습니다.");
    }
}
