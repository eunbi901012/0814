package kr.ac.knue.facultyevaluation.organization;

import java.util.List;

public record OrganizationSearchResult(
    List<OrganizationSummary> items,
    long total,
    int page,
    int size
) {
}
