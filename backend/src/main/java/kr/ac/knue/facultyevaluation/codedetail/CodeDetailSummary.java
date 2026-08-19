package kr.ac.knue.facultyevaluation.codedetail;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CodeDetailSummary(
    String groupId,
    String codeValue,
    String codeName,
    String parentCodeValue,
    Integer sortOrder,
    String extraAttributes,
    String useYn,
    LocalDate validFrom,
    LocalDate validTo,
    Integer depth,
    LocalDateTime updatedAt
) {
}
