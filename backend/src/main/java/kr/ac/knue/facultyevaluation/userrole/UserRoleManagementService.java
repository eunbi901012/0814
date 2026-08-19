package kr.ac.knue.facultyevaluation.userrole;

import java.util.List;
import java.util.UUID;
import kr.ac.knue.facultyevaluation.userrole.mapper.UserRoleManagementMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRoleManagementService {

    private final UserRoleManagementMapper userRoleManagementMapper;

    public UserRoleManagementService(UserRoleManagementMapper userRoleManagementMapper) {
        this.userRoleManagementMapper = userRoleManagementMapper;
    }

    @Transactional(readOnly = true)
    public UserRoleSearchResult list(UserRoleSearchCriteria criteria) {
        List<UserRoleSummary> items = userRoleManagementMapper.list(criteria).stream()
            .map(UserRoleRow::toSummary)
            .toList();
        long total = userRoleManagementMapper.count(criteria);
        return new UserRoleSearchResult(items, total, criteria.safePage(), criteria.safeSize());
    }

    @Transactional
    public UserRoleSummary assign(AssignUserRoleRequest request) {
        ensureUserExists(request.userId(), "사용자를 찾을 수 없습니다: ");
        ensureUserExists(request.approverUserId(), "승인자를 찾을 수 없습니다: ");
        ensureRoleExists(request.roleCode());
        String userRoleId = "UR-" + UUID.randomUUID();
        userRoleManagementMapper.insertUserRole(
            userRoleId,
            request.userId(),
            request.roleCode(),
            request.assignmentType(),
            request.approverUserId(),
            request.effectiveStartDate(),
            request.effectiveEndDate()
        );
        return getUserRole(userRoleId);
    }

    @Transactional
    public UserRoleSummary change(String userRoleId, ChangeUserRoleRequest request) {
        ensureUserRoleExists(userRoleId);
        ensureUserExists(request.approverUserId(), "승인자를 찾을 수 없습니다: ");
        ensureRoleExists(request.roleCode());
        userRoleManagementMapper.updateUserRole(
            userRoleId,
            request.roleCode(),
            request.assignmentType(),
            request.approverUserId(),
            request.effectiveStartDate(),
            request.effectiveEndDate()
        );
        return getUserRole(userRoleId);
    }

    @Transactional
    public UserRoleSummary revoke(String userRoleId) {
        ensureUserRoleExists(userRoleId);
        userRoleManagementMapper.revokeUserRole(userRoleId);
        return getUserRole(userRoleId);
    }

    private void ensureUserExists(String userId, String messagePrefix) {
        if (!userRoleManagementMapper.existsUser(userId)) {
            throw new IllegalArgumentException(messagePrefix + userId);
        }
    }

    private void ensureRoleExists(String roleCode) {
        if (!userRoleManagementMapper.existsRole(roleCode)) {
            throw new IllegalArgumentException("존재하지 않는 역할코드입니다: " + roleCode);
        }
    }

    private void ensureUserRoleExists(String userRoleId) {
        if (!userRoleManagementMapper.existsUserRole(userRoleId)) {
            throw new IllegalArgumentException("사용자 역할을 찾을 수 없습니다: " + userRoleId);
        }
    }

    private UserRoleSummary getUserRole(String userRoleId) {
        UserRoleRow row = userRoleManagementMapper.findByUserRoleId(userRoleId);
        if (row == null) {
            throw new IllegalArgumentException("사용자 역할을 찾을 수 없습니다: " + userRoleId);
        }
        return row.toSummary();
    }
}
