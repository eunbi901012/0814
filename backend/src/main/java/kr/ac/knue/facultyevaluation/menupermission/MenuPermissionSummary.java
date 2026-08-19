package kr.ac.knue.facultyevaluation.menupermission;

import java.time.LocalDateTime;

public record MenuPermissionSummary(
    String permissionId,
    String targetType,
    String targetId,
    String menuId,
    String topMenuName,
    String middleMenuName,
    String screenMenuName,
    String screenId,
    String url,
    boolean canRead,
    boolean canWrite,
    String effect,
    LocalDateTime updatedAt
) {
}
