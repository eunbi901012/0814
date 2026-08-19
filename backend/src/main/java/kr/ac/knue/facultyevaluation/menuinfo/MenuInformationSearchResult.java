package kr.ac.knue.facultyevaluation.menuinfo;

import java.util.List;

public record MenuInformationSearchResult(
    List<MenuInformationSummary> items,
    long total,
    int page,
    int size
) {
}
