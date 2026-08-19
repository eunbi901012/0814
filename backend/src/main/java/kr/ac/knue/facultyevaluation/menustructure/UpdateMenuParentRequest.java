package kr.ac.knue.facultyevaluation.menustructure;

import jakarta.validation.constraints.Size;

public record UpdateMenuParentRequest(
    @Size(max = 40, message = "부모메뉴 ID는 40자 이하여야 합니다.")
    String parentMenuId
) {
}
