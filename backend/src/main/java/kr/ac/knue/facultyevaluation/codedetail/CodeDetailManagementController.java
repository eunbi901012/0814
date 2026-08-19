package kr.ac.knue.facultyevaluation.codedetail;

import jakarta.validation.Valid;
import java.time.LocalDate;
import kr.ac.knue.facultyevaluation.common.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/code-details")
public class CodeDetailManagementController {

    private final CodeDetailManagementService codeDetailManagementService;

    public CodeDetailManagementController(CodeDetailManagementService codeDetailManagementService) {
        this.codeDetailManagementService = codeDetailManagementService;
    }

    @GetMapping
    public ApiResponse<CodeDetailSearchResult> listCodeDetails(
        @RequestParam(required = false) String groupId,
        @RequestParam(required = false) String filter,
        @RequestParam(required = false) String useYn,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validOn,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.ok(codeDetailManagementService.list(new CodeDetailSearchCriteria(groupId, filter, useYn, validOn, page, size)));
    }

    @PostMapping
    public ApiResponse<CodeDetailSummary> createCodeDetail(@Valid @RequestBody CreateCodeDetailRequest request) {
        return ApiResponse.ok(codeDetailManagementService.create(request), "상세코드가 등록되었습니다.");
    }

    @PutMapping("/{groupId}/{codeValue}")
    public ApiResponse<CodeDetailSummary> updateCodeDetail(
        @PathVariable String groupId,
        @PathVariable String codeValue,
        @Valid @RequestBody UpdateCodeDetailRequest request
    ) {
        return ApiResponse.ok(codeDetailManagementService.update(groupId, codeValue, request), "상세코드가 저장되었습니다.");
    }
}
