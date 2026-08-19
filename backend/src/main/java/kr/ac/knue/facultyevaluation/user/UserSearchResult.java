package kr.ac.knue.facultyevaluation.user;

import java.util.List;

public record UserSearchResult(List<UserSummary> items, long total, int page, int size) {
}
