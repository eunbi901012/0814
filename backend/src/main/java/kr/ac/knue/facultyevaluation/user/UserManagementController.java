package kr.ac.knue.facultyevaluation.user;

import jakarta.validation.Valid;
import kr.ac.knue.facultyevaluation.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserManagementController {

    private final UserManagementService userManagementService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    public ApiResponse<UserSearchResult> searchUsers(
        @RequestParam(required = false) String employeeNo,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String departmentCode,
        @RequestParam(required = false) String rankName,
        @RequestParam(required = false) String employmentStatus,
        @RequestParam(required = false) String roleCode,
        @RequestParam(required = false) String systemUseYn,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size
    ) {
        UserSearchCriteria criteria = new UserSearchCriteria(employeeNo, name, departmentCode, rankName, employmentStatus,
            roleCode, systemUseYn, page, size);
        return ApiResponse.ok(userManagementService.search(criteria));
    }

    @PatchMapping("/{userId}/usage")
    public ApiResponse<UserSummary> updateUserUsage(
        @PathVariable String userId,
        @Valid @RequestBody UpdateUserUsageRequest request
    ) {
        return ApiResponse.ok(userManagementService.updateUsage(userId, request), "사용여부가 저장되었습니다.");
    }

    @PutMapping("/{userId}/roles")
    public ApiResponse<UserSummary> updateUserBusinessRoles(
        @PathVariable String userId,
        @Valid @RequestBody UpdateUserRolesRequest request
    ) {
        return ApiResponse.ok(userManagementService.updateBusinessRoles(userId, request), "업무 역할이 저장되었습니다.");
    }
}
