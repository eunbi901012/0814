package kr.ac.knue.facultyevaluation.role.mapper;

import java.util.List;
import kr.ac.knue.facultyevaluation.role.RoleRow;
import kr.ac.knue.facultyevaluation.role.RoleSearchCriteria;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RoleManagementMapper {

    String BASE_SELECT = """
        SELECT role_code AS "roleCode",
               role_name AS "roleName",
               purpose,
               grant_criteria AS "grantCriteria",
               data_scope_default AS "dataScopeDefault",
               use_yn AS "useYn",
               updated_at AS "updatedAt"
        FROM role
        """;

    String DYNAMIC_WHERE = """
        <where>
            <if test="criteria.roleCode != null and criteria.roleCode != ''">
                role_code = #{criteria.roleCode}
            </if>
            <if test="criteria.roleName != null and criteria.roleName != ''">
                AND role_name ILIKE CONCAT('%', #{criteria.roleName}, '%')
            </if>
            <if test="criteria.useYn != null and criteria.useYn != ''">
                AND use_yn = #{criteria.useYn}
            </if>
        </where>
        """;

    @Select("""
        <script>
        """ + BASE_SELECT + DYNAMIC_WHERE + """
        ORDER BY role_code
        LIMIT #{criteria.safeSize} OFFSET #{criteria.offset}
        </script>
        """)
    List<RoleRow> list(@Param("criteria") RoleSearchCriteria criteria);

    @Select("""
        <script>
        SELECT COUNT(*)
        FROM role
        """ + DYNAMIC_WHERE + """
        </script>
        """)
    long count(@Param("criteria") RoleSearchCriteria criteria);

    @Select("""
        SELECT EXISTS (SELECT 1 FROM role WHERE role_code = #{roleCode})
        """)
    boolean existsRole(@Param("roleCode") String roleCode);

    @Select("""
        """ + BASE_SELECT + """
        WHERE role_code = #{roleCode}
        """)
    RoleRow findByRoleCode(@Param("roleCode") String roleCode);

    @Update("""
        UPDATE role
        SET role_name = #{roleName},
            purpose = #{purpose},
            grant_criteria = #{grantCriteria},
            data_scope_default = #{dataScopeDefault},
            use_yn = #{useYn},
            updated_at = CURRENT_TIMESTAMP
        WHERE role_code = #{roleCode}
        """)
    int updateRole(
        @Param("roleCode") String roleCode,
        @Param("roleName") String roleName,
        @Param("purpose") String purpose,
        @Param("grantCriteria") String grantCriteria,
        @Param("dataScopeDefault") String dataScopeDefault,
        @Param("useYn") String useYn
    );
}
