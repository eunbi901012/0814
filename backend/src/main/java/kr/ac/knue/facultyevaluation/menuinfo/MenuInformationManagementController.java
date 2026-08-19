package kr.ac.knue.facultyevaluation.menuinfo;

import jakarta.validation.Valid;
import kr.ac.knue.facultyevaluation.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menus")
public class MenuInformationManagementController {

    private final MenuInformationManagementService menuInformationManagementService;

    public MenuInformationManagementController(MenuInformationManagementService menuInformationManagementService) {
        this.menuInformationManagementService = menuInformationManagementService;
    }

    @GetMapping
    public ApiResponse<MenuInformationSearchResult> listMenus(
        @RequestParam(required = false) String menuName,
        @RequestParam(required = false) String screenId,
        @RequestParam(required = false) String url,
        @RequestParam(required = false) String businessType,
        @RequestParam(required = false) String useYn,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.ok(menuInformationManagementService.list(new MenuInformationSearchCriteria(menuName, screenId, url, businessType, useYn, page, size)));
    }

    @PostMapping
    public ApiResponse<MenuInformationSummary> createMenu(@Valid @RequestBody CreateMenuRequest request) {
        return ApiResponse.ok(menuInformationManagementService.create(request), "메뉴 실행정보가 등록되었습니다.");
    }

    @PutMapping("/{menuId}")
    public ApiResponse<MenuInformationSummary> updateMenu(
        @PathVariable String menuId,
        @Valid @RequestBody UpdateMenuRequest request
    ) {
        return ApiResponse.ok(menuInformationManagementService.update(menuId, request), "메뉴 실행정보가 저장되었습니다.");
    }
}
