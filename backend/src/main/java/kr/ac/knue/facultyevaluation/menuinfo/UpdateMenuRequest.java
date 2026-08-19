package kr.ac.knue.facultyevaluation.menuinfo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateMenuRequest(
    @Size(max = 40, message = "부모메뉴ID는 40자 이하여야 합니다.")
    String parentMenuId,

    @NotBlank(message = "메뉴명은 필수입니다.")
    @Size(max = 100, message = "메뉴명은 100자 이하여야 합니다.")
    String menuName,

    @Size(max = 80, message = "화면ID는 80자 이하여야 합니다.")
    String screenId,

    @Size(max = 200, message = "URL은 200자 이하여야 합니다.")
    String url,

    @Size(max = 80, message = "아이콘은 80자 이하여야 합니다.")
    String icon,

    @Size(max = 80, message = "업무구분은 80자 이하여야 합니다.")
    String businessType,

    @Size(max = 500, message = "설명은 500자 이하여야 합니다.")
    String description,

    @Min(value = 1, message = "표시순서는 1 이상이어야 합니다.")
    int displayOrder,

    @NotBlank(message = "사용여부는 필수입니다.")
    @Pattern(regexp = "Y|N", message = "사용여부는 Y 또는 N이어야 합니다.")
    String useYn
) {
}
