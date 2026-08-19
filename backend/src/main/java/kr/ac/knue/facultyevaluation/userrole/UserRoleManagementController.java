package kr.ac.knue.facultyevaluation.userrole;

import jakarta.validation.Valid;
import java.time.LocalDate;
import kr.ac.knue.facultyevaluation.common.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user-roles")
public class UserRoleManagementController {

    private final UserRoleManagementService userRoleManagementService;

    public UserRoleManagementController(UserRoleManagementService userRoleManagementService) {
        this.userRoleManagementService = userRoleManagementService;
    }

    @GetMapping
    public ApiResponse<UserRoleSearchResult> listUserRoles(
        @RequestParam(required = false) String employeeNo,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String roleCode,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validOn,
        @RequestParam(required = false) String assignmentType,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.ok(userRoleManagementService.list(new UserRoleSearchCriteria(
            employeeNo,
            name,
            roleCode,
            validOn,
            assignmentType,
            status,
            page,
            size
        )));
    }

    @PostMapping
    public ApiResponse<UserRoleSummary> assignUserRole(@Valid @RequestBody AssignUserRoleRequest request) {
        return ApiResponse.ok(userRoleManagementService.assign(request), "사용자 역할이 부여되었습니다.");
    }

    @PatchMapping("/{userRoleId}")
    public ApiResponse<UserRoleSummary> changeUserRole(
        @PathVariable String userRoleId,
        @Valid @RequestBody ChangeUserRoleRequest request
    ) {
        return ApiResponse.ok(userRoleManagementService.change(userRoleId, request), "사용자 역할이 변경되었습니다.");
    }

    @DeleteMapping("/{userRoleId}")
    public ApiResponse<UserRoleSummary> revokeUserRole(@PathVariable String userRoleId) {
        return ApiResponse.ok(userRoleManagementService.revoke(userRoleId), "사용자 역할이 회수되었습니다.");
    }
}
