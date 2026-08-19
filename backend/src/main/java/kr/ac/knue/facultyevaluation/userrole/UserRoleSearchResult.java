package kr.ac.knue.facultyevaluation.userrole;

import java.util.List;

public record UserRoleSearchResult(List<UserRoleSummary> items, long total, int page, int size) {
}
