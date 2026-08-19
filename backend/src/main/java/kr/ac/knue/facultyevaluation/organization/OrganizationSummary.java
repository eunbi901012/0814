package kr.ac.knue.facultyevaluation.organization;

import java.time.LocalDateTime;
import java.util.List;

public record OrganizationSummary(
    String orgCode,
    String orgName,
    String orgType,
    String parentOrgCode,
    String parentOrgName,
    String useYn,
    int depth,
    List<String> childOrgCodes,
    LocalDateTime updatedAt,
    OrganizationRelationshipSummary currentRelationship
) {
}
