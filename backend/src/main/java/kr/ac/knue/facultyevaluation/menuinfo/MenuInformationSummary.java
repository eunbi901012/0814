package kr.ac.knue.facultyevaluation.menuinfo;

import java.time.LocalDateTime;

public record MenuInformationSummary(
    String menuId,
    String parentMenuId,
    String menuName,
    String screenId,
    String url,
    String icon,
    String businessType,
    String description,
    int displayOrder,
    String useYn,
    LocalDateTime updatedAt
) {
}
