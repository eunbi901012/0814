package kr.ac.knue.facultyevaluation.menustructure;

import java.util.List;

public record MenuTreeSearchResult(
    List<MenuTreeSummary> items,
    long total,
    int page,
    int size
) {
}
