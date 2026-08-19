package kr.ac.knue.facultyevaluation.codegroup;

import java.util.List;
import kr.ac.knue.facultyevaluation.codegroup.mapper.CodeGroupManagementMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeGroupManagementService {

    private final CodeGroupManagementMapper codeGroupManagementMapper;

    public CodeGroupManagementService(CodeGroupManagementMapper codeGroupManagementMapper) {
        this.codeGroupManagementMapper = codeGroupManagementMapper;
    }

    @Transactional(readOnly = true)
    public CodeGroupSearchResult list(CodeGroupSearchCriteria criteria) {
        List<CodeGroupSummary> items = codeGroupManagementMapper.list(criteria).stream()
            .map(CodeGroupRow::toSummary)
            .toList();
        long total = codeGroupManagementMapper.count(criteria);
        return new CodeGroupSearchResult(items, total, criteria.safePage(), criteria.safeSize());
    }

    @Transactional
    public CodeGroupSummary create(CreateCodeGroupRequest request) {
        String groupId = normalizeRequired(request.groupId(), "그룹ID는 필수입니다.");
        validateGroupIdAvailable(groupId);
        codeGroupManagementMapper.insertCodeGroup(
            groupId,
            normalizeRequired(request.groupName(), "명칭은 필수입니다."),
            normalizeBlank(request.description()),
            normalizeBlank(request.managingDepartment()),
            request.useYn()
        );
        return getCodeGroup(groupId);
    }

    @Transactional
    public CodeGroupSummary update(String groupId, UpdateCodeGroupRequest request) {
        String normalizedGroupId = normalizeRequired(groupId, "그룹ID는 필수입니다.");
        if (request.groupId() != null && !request.groupId().isBlank() && !normalizedGroupId.equals(request.groupId().trim())) {
            throw new IllegalArgumentException("그룹ID는 변경할 수 없습니다.");
        }
        validateGroupExists(normalizedGroupId);
        codeGroupManagementMapper.updateCodeGroup(
            normalizedGroupId,
            normalizeRequired(request.groupName(), "명칭은 필수입니다."),
            normalizeBlank(request.description()),
            normalizeBlank(request.managingDepartment()),
            request.useYn()
        );
        return getCodeGroup(normalizedGroupId);
    }

    private CodeGroupSummary getCodeGroup(String groupId) {
        CodeGroupRow codeGroupRow = codeGroupManagementMapper.findByGroupId(groupId);
        if (codeGroupRow == null) {
            throw new IllegalArgumentException("코드그룹을 찾을 수 없습니다: " + groupId);
        }
        return codeGroupRow.toSummary();
    }

    private void validateGroupIdAvailable(String groupId) {
        if (codeGroupManagementMapper.existsGroup(groupId)) {
            throw new IllegalArgumentException("이미 등록된 그룹ID입니다: " + groupId);
        }
    }

    private void validateGroupExists(String groupId) {
        if (!codeGroupManagementMapper.existsGroup(groupId)) {
            throw new IllegalArgumentException("코드그룹을 찾을 수 없습니다: " + groupId);
        }
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
