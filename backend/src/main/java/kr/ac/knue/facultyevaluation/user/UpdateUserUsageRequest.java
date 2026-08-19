package kr.ac.knue.facultyevaluation.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateUserUsageRequest(
    @NotBlank(message = "사용여부는 필수입니다.")
    @Pattern(regexp = "Y|N", message = "사용여부는 Y 또는 N이어야 합니다.")
    String systemUseYn
) {
}
