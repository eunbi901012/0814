package kr.ac.knue.facultyevaluation.organization;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OrganizationRow(
    String orgCode,
    String orgName,
    String orgType,
    String parentOrgCode,
    String parentOrgName,
    String useYn,
    LocalDateTime updatedAt,
    LocalDate relationshipEffectiveStartDate,
    LocalDate relationshipEffectiveEndDate,
    String relationshipReason
) {
    public OrganizationSummary toSummary(int depth, List<String> childOrgCodes) {
        OrganizationRelationshipSummary currentRelationship = relationshipEffectiveStartDate == null
            ? null
            : new OrganizationRelationshipSummary(parentOrgCode, parentOrgName, relationshipEffectiveStartDate,
                relationshipEffectiveEndDate, relationshipReason);
        return new OrganizationSummary(orgCode, orgName, orgType, parentOrgCode, parentOrgName, useYn, depth,
            childOrgCodes, updatedAt, currentRelationship);
    }
}
