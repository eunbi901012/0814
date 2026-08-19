package kr.ac.knue.facultyevaluation.menuinfo;

import java.time.LocalDateTime;

public record MenuInformationRow(
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
    public MenuInformationSummary toSummary() {
        return new MenuInformationSummary(
            menuId,
            parentMenuId,
            menuName,
            screenId,
            url,
            icon,
            businessType,
            description,
            displayOrder,
            useYn,
            updatedAt
        );
    }
}
