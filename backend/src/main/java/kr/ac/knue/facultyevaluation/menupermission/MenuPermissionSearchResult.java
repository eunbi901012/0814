package kr.ac.knue.facultyevaluation.menupermission;

import java.util.List;

public record MenuPermissionSearchResult(
    List<MenuPermissionSummary> items,
    long total,
    int page,
    int size,
    PermissionPreview preview
) {
}
