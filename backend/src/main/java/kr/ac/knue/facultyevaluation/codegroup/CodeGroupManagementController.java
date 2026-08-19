package kr.ac.knue.facultyevaluation.codegroup;

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
@RequestMapping("/api/code-groups")
public class CodeGroupManagementController {

    private final CodeGroupManagementService codeGroupManagementService;

    public CodeGroupManagementController(CodeGroupManagementService codeGroupManagementService) {
        this.codeGroupManagementService = codeGroupManagementService;
    }

    @GetMapping
    public ApiResponse<CodeGroupSearchResult> listCodeGroups(
        @RequestParam(required = false) String groupId,
        @RequestParam(required = false) String groupName,
        @RequestParam(required = false) String managingDepartment,
        @RequestParam(required = false) String useYn,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.ok(codeGroupManagementService.list(new CodeGroupSearchCriteria(groupId, groupName, managingDepartment, useYn, page, size)));
    }

    @PostMapping
    public ApiResponse<CodeGroupSummary> createCodeGroup(@Valid @RequestBody CreateCodeGroupRequest request) {
        return ApiResponse.ok(codeGroupManagementService.create(request), "코드그룹이 등록되었습니다.");
    }

    @PutMapping("/{groupId}")
    public ApiResponse<CodeGroupSummary> updateCodeGroup(
        @PathVariable String groupId,
        @Valid @RequestBody UpdateCodeGroupRequest request
    ) {
        return ApiResponse.ok(codeGroupManagementService.update(groupId, request), "코드그룹이 저장되었습니다.");
    }
}
