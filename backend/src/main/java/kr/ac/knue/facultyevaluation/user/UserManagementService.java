package kr.ac.knue.facultyevaluation.user;

import java.util.List;
import java.util.UUID;
import kr.ac.knue.facultyevaluation.user.mapper.UserManagementMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserManagementService {

    private final UserManagementMapper userManagementMapper;

    public UserManagementService(UserManagementMapper userManagementMapper) {
        this.userManagementMapper = userManagementMapper;
    }

    @Transactional(readOnly = true)
    public UserSearchResult search(UserSearchCriteria criteria) {
        List<UserSummary> items = userManagementMapper.search(criteria).stream()
            .map(UserRow::toSummary)
            .toList();
        long total = userManagementMapper.count(criteria);
        return new UserSearchResult(items, total, criteria.safePage(), criteria.safeSize());
    }

    @Transactional
    public UserSummary updateUsage(String userId, UpdateUserUsageRequest request) {
        ensureUserExists(userId);
        userManagementMapper.updateUsage(userId, request.systemUseYn());
        return getUser(userId);
    }

    @Transactional
    public UserSummary updateBusinessRoles(String userId, UpdateUserRolesRequest request) {
        ensureUserExists(userId);
        ensureUserExists(request.approverUserId());
        for (String roleCode : request.roleCodes()) {
            if (!userManagementMapper.existsRole(roleCode)) {
                throw new IllegalArgumentException("존재하지 않는 역할코드입니다: " + roleCode);
            }
        }
        userManagementMapper.revokeActiveRoles(userId);
        for (String roleCode : request.roleCodes()) {
            userManagementMapper.insertUserRole(
                "UR-" + UUID.randomUUID(),
                userId,
                roleCode,
                request.assignmentType(),
                request.approverUserId(),
                request.effectiveStartDate(),
                request.effectiveEndDate()
            );
        }
        return getUser(userId);
    }

    private void ensureUserExists(String userId) {
        if (!userManagementMapper.existsUser(userId)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId);
        }
    }

    private UserSummary getUser(String userId) {
        UserRow userRow = userManagementMapper.findByUserId(userId);
        if (userRow == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId);
        }
        return userRow.toSummary();
    }
}
