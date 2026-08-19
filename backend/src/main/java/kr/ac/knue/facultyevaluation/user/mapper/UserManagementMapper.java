package kr.ac.knue.facultyevaluation.user.mapper;

import java.time.LocalDate;
import java.util.List;
import kr.ac.knue.facultyevaluation.user.UserRow;
import kr.ac.knue.facultyevaluation.user.UserSearchCriteria;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserManagementMapper {

    String BASE_SELECT = """
        SELECT ia.user_id AS "userId", ia.login_id AS "loginId", ia.name,
               ia.department_code AS "departmentCode", org.org_name AS "departmentName",
               kfs.rank_name AS "rankName", COALESCE(kfs.status, ia.employment_status) AS "employmentStatus",
               COALESCE(string_agg(DISTINCT ur.role_code, ',' ORDER BY ur.role_code), '') AS roles,
               ia.system_use_yn AS "systemUseYn", ia.position_name AS "positionName",
               kfs.retirement_date AS "retirementDate", kfs.last_synced_at AS "lastSyncedAt"
        FROM internal_account ia
        LEFT JOIN korus_faculty_snapshot kfs ON kfs.employee_no = ia.user_id
        LEFT JOIN organization org ON org.org_code = ia.department_code
        LEFT JOIN user_role ur ON ur.user_id = ia.user_id AND ur.status = 'ACTIVE'
        """;

    String DYNAMIC_WHERE = """
        <where>
            <if test="criteria.employeeNo != null and criteria.employeeNo != ''">
                ia.user_id = #{criteria.employeeNo}
            </if>
            <if test="criteria.name != null and criteria.name != ''">
                AND ia.name ILIKE CONCAT('%', #{criteria.name}, '%')
            </if>
            <if test="criteria.departmentCode != null and criteria.departmentCode != ''">
                AND ia.department_code = #{criteria.departmentCode}
            </if>
            <if test="criteria.rankName != null and criteria.rankName != ''">
                AND kfs.rank_name = #{criteria.rankName}
            </if>
            <if test="criteria.employmentStatus != null and criteria.employmentStatus != ''">
                AND COALESCE(kfs.status, ia.employment_status) = #{criteria.employmentStatus}
            </if>
            <if test="criteria.roleCode != null and criteria.roleCode != ''">
                AND EXISTS (
                    SELECT 1 FROM user_role filter_ur
                    WHERE filter_ur.user_id = ia.user_id
                      AND filter_ur.status = 'ACTIVE'
                      AND filter_ur.role_code = #{criteria.roleCode}
                )
            </if>
            <if test="criteria.systemUseYn != null and criteria.systemUseYn != ''">
                AND ia.system_use_yn = #{criteria.systemUseYn}
            </if>
        </where>
        """;

    @Select("""
        <script>
        """ + BASE_SELECT + DYNAMIC_WHERE + """
        GROUP BY ia.user_id, ia.login_id, ia.name, ia.department_code, org.org_name, kfs.rank_name,
                 kfs.status, ia.employment_status, ia.system_use_yn, ia.position_name,
                 kfs.retirement_date, kfs.last_synced_at
        ORDER BY ia.user_id
        LIMIT #{criteria.safeSize} OFFSET #{criteria.offset}
        </script>
        """)
    List<UserRow> search(@Param("criteria") UserSearchCriteria criteria);

    @Select("""
        <script>
        SELECT COUNT(*)
        FROM internal_account ia
        LEFT JOIN korus_faculty_snapshot kfs ON kfs.employee_no = ia.user_id
        """ + DYNAMIC_WHERE + """
        </script>
        """)
    long count(@Param("criteria") UserSearchCriteria criteria);

    @Select("""
        SELECT EXISTS (SELECT 1 FROM internal_account WHERE user_id = #{userId})
        """)
    boolean existsUser(@Param("userId") String userId);

    @Select("""
        SELECT EXISTS (SELECT 1 FROM role WHERE role_code = #{roleCode} AND use_yn = 'Y')
        """)
    boolean existsRole(@Param("roleCode") String roleCode);

    @Update("""
        UPDATE internal_account
        SET system_use_yn = #{systemUseYn}, updated_at = CURRENT_TIMESTAMP
        WHERE user_id = #{userId}
        """)
    int updateUsage(@Param("userId") String userId, @Param("systemUseYn") String systemUseYn);

    @Update("""
        UPDATE user_role
        SET status = 'REVOKED', updated_at = CURRENT_TIMESTAMP
        WHERE user_id = #{userId} AND status = 'ACTIVE'
        """)
    int revokeActiveRoles(@Param("userId") String userId);

    @Insert("""
        INSERT INTO user_role (user_role_id, user_id, role_code, assignment_type, approver_user_id,
                               effective_start_date, effective_end_date, status, created_at, updated_at)
        VALUES (#{userRoleId}, #{userId}, #{roleCode}, #{assignmentType}, #{approverUserId},
                #{effectiveStartDate}, #{effectiveEndDate}, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """)
    int insertUserRole(
        @Param("userRoleId") String userRoleId,
        @Param("userId") String userId,
        @Param("roleCode") String roleCode,
        @Param("assignmentType") String assignmentType,
        @Param("approverUserId") String approverUserId,
        @Param("effectiveStartDate") LocalDate effectiveStartDate,
        @Param("effectiveEndDate") LocalDate effectiveEndDate
    );

    @Select("""
        <script>
        """ + BASE_SELECT + """
        WHERE ia.user_id = #{userId}
        GROUP BY ia.user_id, ia.login_id, ia.name, ia.department_code, org.org_name, kfs.rank_name,
                 kfs.status, ia.employment_status, ia.system_use_yn, ia.position_name,
                 kfs.retirement_date, kfs.last_synced_at
        </script>
        """)
    UserRow findByUserId(@Param("userId") String userId);
}
