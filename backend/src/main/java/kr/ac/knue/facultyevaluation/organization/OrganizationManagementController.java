package kr.ac.knue.facultyevaluation.organization;

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
@RequestMapping("/api/organizations")
public class OrganizationManagementController {

    private final OrganizationManagementService organizationManagementService;

    public OrganizationManagementController(OrganizationManagementService organizationManagementService) {
        this.organizationManagementService = organizationManagementService;
    }

    @GetMapping
    public ApiResponse<OrganizationSearchResult> listOrganizations(
        @RequestParam(required = false) String orgCode,
        @RequestParam(required = false) String orgType,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.ok(organizationManagementService.list(new OrganizationSearchCriteria(orgCode, orgType, page, size)));
    }

    @PutMapping("/{orgCode}/relationships")
    public ApiResponse<OrganizationSummary> saveOrganizationRelationship(
        @PathVariable String orgCode,
        @Valid @RequestBody SaveOrganizationRelationshipRequest request
    ) {
        return ApiResponse.ok(organizationManagementService.saveRelationship(orgCode, request), "관계/기간이 저장되었습니다.");
    }
}
