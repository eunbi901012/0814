package kr.ac.knue.facultyevaluation.role;

import java.util.List;

public record RoleSearchResult(
    List<RoleSummary> items,
    long total,
    int page,
    int size
) {
}
