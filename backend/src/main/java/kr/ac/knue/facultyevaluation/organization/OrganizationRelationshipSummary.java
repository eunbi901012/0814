package kr.ac.knue.facultyevaluation.organization;

import java.time.LocalDate;

public record OrganizationRelationshipSummary(
    String parentOrgCode,
    String parentOrgName,
    LocalDate effectiveStartDate,
    LocalDate effectiveEndDate,
    String reason
) {
}
