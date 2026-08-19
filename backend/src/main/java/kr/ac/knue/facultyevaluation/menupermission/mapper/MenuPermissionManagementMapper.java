package kr.ac.knue.facultyevaluation.menupermission.mapper;

import java.util.List;
import kr.ac.knue.facultyevaluation.menupermission.MenuPermissionRow;
import kr.ac.knue.facultyevaluation.menupermission.MenuPermissionSearchCriteria;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MenuPermissionManagementMapper {

    String BASE_SELECT = """
        SELECT mp.permission_id AS "permissionId",
               COALESCE(mp.target_type, #{criteria.targetType}) AS "targetType",
               COALESCE(mp.target_id, #{criteria.targetId}) AS "targetId",
               m.menu_id AS "menuId",
               CASE
                   WHEN grand.menu_id IS NOT NULL THEN grand.menu_name
                   WHEN parent.menu_id IS NOT NULL THEN parent.menu_name
                   ELSE m.menu_name
               END AS "topMenuName",
               CASE
                   WHEN grand.menu_id IS NOT NULL THEN parent.menu_name
                   WHEN parent.menu_id IS NOT NULL AND m.screen_id IS NULL THEN m.menu_name
                   WHEN parent.menu_id IS NOT NULL THEN parent.menu_name
                   ELSE NULL
               END AS "middleMenuName",
               CASE WHEN m.screen_id IS NOT NULL THEN m.menu_name ELSE NULL END AS "screenMenuName",
               m.screen_id AS "screenId",
               m.url,
               COALESCE(mp.can_read, false) AS "canRead",
               COALESCE(mp.can_write, false) AS "canWrite",
               COALESCE(mp.effect, 'DENY') AS "effect",
               mp.updated_at AS "updatedAt"
        FROM menu m
        LEFT JOIN menu parent ON parent.menu_id = m.parent_menu_id
        LEFT JOIN menu grand ON grand.menu_id = parent.parent_menu_id
        LEFT JOIN menu_permission mp
          ON mp.menu_id = m.menu_id
         AND mp.target_type = #{criteria.targetType}
         AND mp.target_id = #{criteria.targetId}
        """;

    String DYNAMIC_WHERE = """
        <where>
            m.use_yn = 'Y'
            <if test="criteria.menuId != null and criteria.menuId != ''">
                AND m.menu_id = #{criteria.menuId}
            </if>
        </where>
        """;

    @Select("""
        <script>
        """ + BASE_SELECT + DYNAMIC_WHERE + """
        ORDER BY COALESCE(grand.display_order, parent.display_order, m.display_order),
                 COALESCE(parent.display_order, m.display_order),
                 m.display_order,
                 m.menu_id
        LIMIT #{criteria.safeSize} OFFSET #{criteria.offset}
        </script>
        """)
    List<MenuPermissionRow> list(@Param("criteria") MenuPermissionSearchCriteria criteria);

    @Select("""
        <script>
        SELECT COUNT(*)
        FROM menu m
        """ + DYNAMIC_WHERE + """
        </script>
        """)
    long count(@Param("criteria") MenuPermissionSearchCriteria criteria);

    @Select("SELECT EXISTS (SELECT 1 FROM role WHERE role_code = #{roleCode} AND use_yn = 'Y')")
    boolean existsRole(@Param("roleCode") String roleCode);

    @Select("SELECT EXISTS (SELECT 1 FROM organization WHERE org_code = #{orgCode} AND use_yn = 'Y')")
    boolean existsOrganization(@Param("orgCode") String orgCode);

    @Select("SELECT EXISTS (SELECT 1 FROM internal_account WHERE user_id = #{userId} AND system_use_yn = 'Y')")
    boolean existsUser(@Param("userId") String userId);

    @Select("SELECT EXISTS (SELECT 1 FROM menu WHERE menu_id = #{menuId} AND use_yn = 'Y')")
    boolean existsMenu(@Param("menuId") String menuId);

    @Select("""
        SELECT permission_id
        FROM menu_permission
        WHERE target_type = #{targetType}
          AND target_id = #{targetId}
          AND menu_id = #{menuId}
        """)
    String findPermissionId(
        @Param("targetType") String targetType,
        @Param("targetId") String targetId,
        @Param("menuId") String menuId
    );

    @Insert("""
        INSERT INTO menu_permission (permission_id, target_type, target_id, menu_id, can_read, can_write, effect, created_at, updated_at)
        VALUES (#{permissionId}, #{targetType}, #{targetId}, #{menuId}, #{canRead}, #{canWrite}, #{effect}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        ON CONFLICT (target_type, target_id, menu_id)
        DO UPDATE SET can_read = EXCLUDED.can_read,
                      can_write = EXCLUDED.can_write,
                      effect = EXCLUDED.effect,
                      updated_at = CURRENT_TIMESTAMP
        """)
    int upsertPermission(
        @Param("permissionId") String permissionId,
        @Param("targetType") String targetType,
        @Param("targetId") String targetId,
        @Param("menuId") String menuId,
        @Param("canRead") boolean canRead,
        @Param("canWrite") boolean canWrite,
        @Param("effect") String effect
    );

    @Select("""
        <script>
        SELECT mp.permission_id AS "permissionId",
               mp.target_type AS "targetType",
               mp.target_id AS "targetId",
               m.menu_id AS "menuId",
               NULL AS "topMenuName",
               NULL AS "middleMenuName",
               m.menu_name AS "screenMenuName",
               m.screen_id AS "screenId",
               m.url,
               mp.can_read AS "canRead",
               mp.can_write AS "canWrite",
               mp.effect,
               mp.updated_at AS "updatedAt"
        FROM menu_permission mp
        JOIN menu m ON m.menu_id = mp.menu_id
        WHERE m.url = #{menuUrl}
          AND m.use_yn = 'Y'
          AND (
              (mp.target_type = 'USER' AND mp.target_id = #{userId})
              OR (mp.target_type = 'ORGANIZATION' AND mp.target_id = #{departmentCode})
              <if test="roleCodes != null and roleCodes.size() > 0">
              OR (mp.target_type = 'ROLE' AND mp.target_id IN
                  <foreach collection="roleCodes" item="roleCode" open="(" separator="," close=")">
                      #{roleCode}
                  </foreach>)
              </if>
          )
        ORDER BY CASE
                     WHEN mp.effect = 'DENY' AND mp.target_type = 'USER' THEN 1
                     WHEN mp.effect = 'DENY' AND mp.target_type = 'ORGANIZATION' THEN 2
                     WHEN mp.effect = 'DENY' AND mp.target_type = 'ROLE' THEN 3
                     WHEN mp.target_type = 'USER' THEN 4
                     WHEN mp.target_type = 'ORGANIZATION' THEN 5
                     ELSE 6
                 END
        LIMIT 1
        </script>
        """)
    MenuPermissionRow findEffectivePermission(
        @Param("userId") String userId,
        @Param("departmentCode") String departmentCode,
        @Param("roleCodes") List<String> roleCodes,
        @Param("menuUrl") String menuUrl
    );
}
