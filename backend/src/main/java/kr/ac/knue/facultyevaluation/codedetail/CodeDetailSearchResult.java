package kr.ac.knue.facultyevaluation.codedetail;

import java.util.List;

public record CodeDetailSearchResult(
    List<CodeDetailSummary> items,
    long total,
    int page,
    int size
) {
}
