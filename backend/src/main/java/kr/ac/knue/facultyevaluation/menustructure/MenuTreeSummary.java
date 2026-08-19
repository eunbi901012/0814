package kr.ac.knue.facultyevaluation.menustructure;

import java.time.LocalDateTime;
import java.util.List;

public record MenuTreeSummary(
    String menuId,
    String parentMenuId,
    String menuName,
    String screenId,
    String url,
    int displayOrder,
    String useYn,
    int depth,
    List<String> childMenuIds,
    LocalDateTime updatedAt
) {
}
