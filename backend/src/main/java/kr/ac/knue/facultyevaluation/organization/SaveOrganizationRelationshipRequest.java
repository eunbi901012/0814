package kr.ac.knue.facultyevaluation.organization;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record SaveOrganizationRelationshipRequest(
    String parentOrgCode,
    @NotNull(message = "적용 시작일은 필수입니다.")
    LocalDate effectiveStartDate,
    LocalDate effectiveEndDate,
    String reason
) {
    @AssertTrue(message = "적용 시작일은 종료일보다 늦을 수 없습니다.")
    public boolean isValidPeriod() {
        return effectiveStartDate == null || effectiveEndDate == null || !effectiveStartDate.isAfter(effectiveEndDate);
    }
}
