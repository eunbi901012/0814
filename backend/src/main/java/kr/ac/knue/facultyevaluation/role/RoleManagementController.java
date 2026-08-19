package kr.ac.knue.facultyevaluation.role;

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
@RequestMapping("/api/roles")
public class RoleManagementController {

    private final RoleManagementService roleManagementService;

    public RoleManagementController(RoleManagementService roleManagementService) {
        this.roleManagementService = roleManagementService;
    }

    @GetMapping
    public ApiResponse<RoleSearchResult> listRoles(
        @RequestParam(required = false) String roleCode,
        @RequestParam(required = false) String roleName,
        @RequestParam(required = false) String useYn,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.ok(roleManagementService.list(new RoleSearchCriteria(roleCode, roleName, useYn, page, size)));
    }

    @PutMapping("/{roleCode}")
    public ApiResponse<RoleSummary> updateRole(
        @PathVariable String roleCode,
        @Valid @RequestBody UpdateRoleRequest request
    ) {
        return ApiResponse.ok(roleManagementService.updateRole(roleCode, request), "역할 기준이 저장되었습니다.");
    }
}
