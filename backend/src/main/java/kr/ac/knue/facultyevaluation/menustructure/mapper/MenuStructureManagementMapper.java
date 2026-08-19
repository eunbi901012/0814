package kr.ac.knue.facultyevaluation.menustructure.mapper;

import java.util.List;
import kr.ac.knue.facultyevaluation.menustructure.MenuTreeRow;
import kr.ac.knue.facultyevaluation.menustructure.MenuTreeSearchCriteria;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MenuStructureManagementMapper {

    @Select("""
        <script>
        WITH RECURSIVE menu_tree AS (
            SELECT m.menu_id,
                   m.parent_menu_id,
                   m.menu_name,
                   m.screen_id,
                   m.url,
                   m.display_order,
                   m.use_yn,
                   m.updated_at,
                   0 AS depth,
                   LPAD(m.display_order::text, 5, '0') || ':' || m.menu_id AS sort_path
            FROM menu m
            WHERE m.parent_menu_id IS NULL
              AND m.use_yn = 'Y'
            UNION ALL
            SELECT child.menu_id,
                   child.parent_menu_id,
                   child.menu_name,
                   child.screen_id,
                   child.url,
                   child.display_order,
                   child.use_yn,
                   child.updated_at,
                   parent.depth + 1 AS depth,
                   parent.sort_path || '/' || LPAD(child.display_order::text, 5, '0') || ':' || child.menu_id AS sort_path
            FROM menu child
            JOIN menu_tree parent ON parent.menu_id = child.parent_menu_id
            WHERE child.use_yn = 'Y'
        )
        SELECT menu_id AS "menuId",
               parent_menu_id AS "parentMenuId",
               menu_name AS "menuName",
               screen_id AS "screenId",
               url,
               display_order AS "displayOrder",
               use_yn AS "useYn",
               depth,
               updated_at AS "updatedAt"
        FROM menu_tree
        <where>
            <if test="criteria.selectedMenuId != null and criteria.selectedMenuId != ''">
                menu_id = #{criteria.selectedMenuId}
                OR parent_menu_id = #{criteria.selectedMenuId}
                OR menu_id IN (
                    WITH RECURSIVE ancestors AS (
                        SELECT parent_menu_id
                        FROM menu
                        WHERE menu_id = #{criteria.selectedMenuId}
                          AND parent_menu_id IS NOT NULL
                        UNION ALL
                        SELECT m.parent_menu_id
                        FROM menu m
                        JOIN ancestors a ON a.parent_menu_id = m.menu_id
                        WHERE m.parent_menu_id IS NOT NULL
                    )
                    SELECT parent_menu_id FROM ancestors
                )
                OR menu_id IN (
                    WITH RECURSIVE descendants AS (
                        SELECT menu_id
                        FROM menu
                        WHERE parent_menu_id = #{criteria.selectedMenuId}
                        UNION ALL
                        SELECT m.menu_id
                        FROM menu m
                        JOIN descendants d ON m.parent_menu_id = d.menu_id
                    )
                    SELECT menu_id FROM descendants
                )
            </if>
        </where>
        ORDER BY sort_path
        LIMIT #{criteria.safeSize} OFFSET #{criteria.offset}
        </script>
        """)
    List<MenuTreeRow> listTree(@Param("criteria") MenuTreeSearchCriteria criteria);

    @Select("SELECT COUNT(*) FROM menu WHERE use_yn = 'Y'")
    long countActiveMenus();

    @Select("SELECT EXISTS (SELECT 1 FROM menu WHERE menu_id = #{menuId} AND use_yn = 'Y')")
    boolean existsMenu(@Param("menuId") String menuId);

    @Select("""
        WITH RECURSIVE descendants AS (
            SELECT menu_id
            FROM menu
            WHERE parent_menu_id = #{menuId}
              AND use_yn = 'Y'
            UNION ALL
            SELECT child.menu_id
            FROM menu child
            JOIN descendants parent ON parent.menu_id = child.parent_menu_id
            WHERE child.use_yn = 'Y'
        )
        SELECT EXISTS (SELECT 1 FROM descendants WHERE menu_id = #{candidateParentMenuId})
        """)
    boolean isDescendant(
        @Param("menuId") String menuId,
        @Param("candidateParentMenuId") String candidateParentMenuId
    );

    @Update("""
        UPDATE menu
        SET parent_menu_id = #{parentMenuId},
            updated_at = CURRENT_TIMESTAMP
        WHERE menu_id = #{menuId}
        """)
    int updateParent(@Param("menuId") String menuId, @Param("parentMenuId") String parentMenuId);

    @Update("""
        UPDATE menu
        SET display_order = #{displayOrder},
            updated_at = CURRENT_TIMESTAMP
        WHERE menu_id = #{menuId}
        """)
    int updateDisplayOrder(@Param("menuId") String menuId, @Param("displayOrder") int displayOrder);
}
