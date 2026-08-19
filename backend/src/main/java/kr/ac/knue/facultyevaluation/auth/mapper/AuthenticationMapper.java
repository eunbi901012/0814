package kr.ac.knue.facultyevaluation.auth.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import kr.ac.knue.facultyevaluation.auth.StoredAccount;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AuthenticationMapper {

    @Select("""
        SELECT user_id AS "userId", login_id AS "loginId", password_hash AS "passwordHash",
               name, department_code AS "departmentCode", system_use_yn AS "systemUseYn"
        FROM internal_account
        WHERE login_id = #{loginId}
        """)
    Optional<StoredAccount> findByLoginId(@Param("loginId") String loginId);

    @Select("""
        SELECT r.role_code
        FROM user_role ur
        JOIN role r ON r.role_code = ur.role_code
        WHERE ur.user_id = #{userId}
          AND ur.status = 'ACTIVE'
          AND r.use_yn = 'Y'
          AND ur.effective_start_date <= CURRENT_DATE
          AND (ur.effective_end_date IS NULL OR ur.effective_end_date >= CURRENT_DATE)
        ORDER BY r.role_code
        """)
    List<String> findActiveRoleCodes(@Param("userId") String userId);

    @Insert("""
        INSERT INTO session (session_id, user_id, expires_at, status)
        VALUES (#{sessionId}, #{userId}, #{expiresAt}, 'ACTIVE')
        """)
    void insertSession(@Param("sessionId") String sessionId, @Param("userId") String userId, @Param("expiresAt") LocalDateTime expiresAt);

    @Select("""
        SELECT ia.user_id AS "userId", ia.login_id AS "loginId", ia.password_hash AS "passwordHash",
               ia.name, ia.department_code AS "departmentCode", ia.system_use_yn AS "systemUseYn"
        FROM session s
        JOIN internal_account ia ON ia.user_id = s.user_id
        WHERE s.session_id = #{sessionId}
          AND s.status = 'ACTIVE'
          AND s.expires_at > #{now}
          AND ia.system_use_yn = 'Y'
        """)
    Optional<StoredAccount> findActiveSessionAccount(@Param("sessionId") String sessionId, @Param("now") LocalDateTime now);

    @Update("UPDATE session SET status = 'EXPIRED' WHERE session_id = #{sessionId}")
    int expireSession(@Param("sessionId") String sessionId);
}
