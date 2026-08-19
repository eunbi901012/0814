package kr.ac.knue.facultyevaluation.userrole.mapper;

import java.time.LocalDate;
import java.util.List;
import kr.ac.knue.facultyevaluation.userrole.UserRoleRow;
import kr.ac.knue.facultyevaluation.userrole.UserRoleSearchCriteria;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserRoleManagementMapper {

    String BASE_SELECT = """
        SELECT ur.user_role_id AS "userRoleId",
               ur.user_id AS "userId",
               ia.name AS "userName",
               ur.role_code AS "roleCode",
               r.role_name AS "roleName",
               ur.effective_start_date AS "effectiveStartDate",
               ur.effective_end_date AS "effectiveEndDate",
               ur.approver_user_id AS "approverUserId",
               approver.name AS "approverName",
               ur.assignment_type AS "assignmentType",
               ur.status,
               ur.updated_at AS "updatedAt"
        FROM user_role ur
        JOIN internal_account ia ON ia.user_id = ur.user_id
        JOIN role r ON r.role_code = ur.role_code
        LEFT JOIN internal_account approver ON approver.user_id = ur.approver_user_id
        """;

    String DYNAMIC_WHERE = """
        <where>
            <if test="criteria.employeeNo != null and criteria.employeeNo != ''">
                ur.user_id = #{criteria.employeeNo}
            </if>
            <if test="criteria.name != null and criteria.name != ''">
                AND ia.name ILIKE CONCAT('%', #{criteria.name}, '%')
            </if>
            <if test="criteria.roleCode != null and criteria.roleCode != ''">
                AND ur.role_code = #{criteria.roleCode}
            </if>
            <if test="criteria.assignmentType != null and criteria.assignmentType != ''">
                AND ur.assignment_type = #{criteria.assignmentType}
            </if>
            <if test="criteria.status != null and criteria.status != ''">
                AND ur.status = #{criteria.status}
            </if>
            <if test="criteria.validOn != null">
                AND ur.effective_start_date &lt;= #{criteria.validOn}
                AND (ur.effective_end_date IS NULL OR ur.effective_end_date &gt;= #{criteria.validOn})
            </if>
        </where>
        """;

    @Select("""
        <script>
        """ + BASE_SELECT + DYNAMIC_WHERE + """
        ORDER BY ur.updated_at DESC, ur.user_id, ur.role_code
        LIMIT #{criteria.safeSize} OFFSET #{criteria.offset}
        </script>
        """)
    List<UserRoleRow> list(@Param("criteria") UserRoleSearchCriteria criteria);

    @Select("""
        <script>
        SELECT COUNT(*)
        FROM user_role ur
        JOIN internal_account ia ON ia.user_id = ur.user_id
        JOIN role r ON r.role_code = ur.role_code
        """ + DYNAMIC_WHERE + """
        </script>
        """)
    long count(@Param("criteria") UserRoleSearchCriteria criteria);

    @Select("""
        SELECT EXISTS (SELECT 1 FROM internal_account WHERE user_id = #{userId})
        """)
    boolean existsUser(@Param("userId") String userId);

    @Select("""
        SELECT EXISTS (SELECT 1 FROM role WHERE role_code = #{roleCode} AND use_yn = 'Y')
        """)
    boolean existsRole(@Param("roleCode") String roleCode);

    @Select("""
        SELECT EXISTS (SELECT 1 FROM user_role WHERE user_role_id = #{userRoleId})
        """)
    boolean existsUserRole(@Param("userRoleId") String userRoleId);

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

    @Update("""
        UPDATE user_role
        SET role_code = #{roleCode},
            assignment_type = #{assignmentType},
            approver_user_id = #{approverUserId},
            effective_start_date = #{effectiveStartDate},
            effective_end_date = #{effectiveEndDate},
            status = 'ACTIVE',
            updated_at = CURRENT_TIMESTAMP
        WHERE user_role_id = #{userRoleId}
        """)
    int updateUserRole(
        @Param("userRoleId") String userRoleId,
        @Param("roleCode") String roleCode,
        @Param("assignmentType") String assignmentType,
        @Param("approverUserId") String approverUserId,
        @Param("effectiveStartDate") LocalDate effectiveStartDate,
        @Param("effectiveEndDate") LocalDate effectiveEndDate
    );

    @Update("""
        UPDATE user_role
        SET status = 'REVOKED', updated_at = CURRENT_TIMESTAMP
        WHERE user_role_id = #{userRoleId}
        """)
    int revokeUserRole(@Param("userRoleId") String userRoleId);

    @Select("""
        """ + BASE_SELECT + """
        WHERE ur.user_role_id = #{userRoleId}
        """)
    UserRoleRow findByUserRoleId(@Param("userRoleId") String userRoleId);
}
