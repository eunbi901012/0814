package kr.ac.knue.facultyevaluation.menupermission;

import java.util.List;

public record PermissionPreview(
    List<String> visibleMenuUrls,
    List<String> writableMenuUrls,
    List<String> deniedMenuUrls
) {
}
