package kr.ac.knue.facultyevaluation.menupermission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SaveMenuPermissionItem(
    @NotBlank(message = "메뉴는 필수입니다.")
    String menuId,

    @NotNull(message = "조회허용은 필수입니다.")
    Boolean canRead,

    @NotNull(message = "변경허용은 필수입니다.")
    Boolean canWrite,

    @NotBlank(message = "effect는 필수입니다.")
    @Pattern(regexp = "ALLOW|DENY", message = "effect는 ALLOW 또는 DENY이어야 합니다.")
    String effect
) {
}
