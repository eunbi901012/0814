package kr.ac.knue.facultyevaluation.menustructure;

import java.time.LocalDateTime;
import java.util.List;

public record MenuTreeRow(
    String menuId,
    String parentMenuId,
    String menuName,
    String screenId,
    String url,
    int displayOrder,
    String useYn,
    int depth,
    LocalDateTime updatedAt
) {
    public MenuTreeSummary toSummary(List<String> childMenuIds) {
        return new MenuTreeSummary(
            menuId,
            parentMenuId,
            menuName,
            screenId,
            url,
            displayOrder,
            useYn,
            depth,
            childMenuIds,
            updatedAt
        );
    }
}
