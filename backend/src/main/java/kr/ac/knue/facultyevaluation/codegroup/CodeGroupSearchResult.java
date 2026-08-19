package kr.ac.knue.facultyevaluation.codegroup;

import java.util.List;

public record CodeGroupSearchResult(
    List<CodeGroupSummary> items,
    long total,
    int page,
    int size
) {
}
