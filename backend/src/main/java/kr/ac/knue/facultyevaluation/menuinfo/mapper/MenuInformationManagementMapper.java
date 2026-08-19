package kr.ac.knue.facultyevaluation.menuinfo.mapper;

import java.util.List;
import kr.ac.knue.facultyevaluation.menuinfo.MenuInformationRow;
import kr.ac.knue.facultyevaluation.menuinfo.MenuInformationSearchCriteria;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MenuInformationManagementMapper {

    String BASE_SELECT = """
        SELECT menu_id AS "menuId",
               parent_menu_id AS "parentMenuId",
               menu_name AS "menuName",
               screen_id AS "screenId",
               url,
               icon,
               business_type AS "businessType",
               description,
               display_order AS "displayOrder",
               use_yn AS "useYn",
               updated_at AS "updatedAt"
        FROM menu
        """;

    String DYNAMIC_WHERE = """
        <where>
            <if test="criteria.menuName != null and criteria.menuName != ''">
                menu_name ILIKE CONCAT('%', #{criteria.menuName}, '%')
            </if>
            <if test="criteria.screenId != null and criteria.screenId != ''">
                AND screen_id ILIKE CONCAT('%', #{criteria.screenId}, '%')
            </if>
            <if test="criteria.url != null and criteria.url != ''">
                AND url ILIKE CONCAT('%', #{criteria.url}, '%')
            </if>
            <if test="criteria.businessType != null and criteria.businessType != ''">
                AND business_type = #{criteria.businessType}
            </if>
            <if test="criteria.useYn != null and criteria.useYn != ''">
                AND use_yn = #{criteria.useYn}
            </if>
        </where>
        """;

    @Select("""
        <script>
        """ + BASE_SELECT + DYNAMIC_WHERE + """
        ORDER BY parent_menu_id NULLS FIRST, display_order, menu_id
        LIMIT #{criteria.safeSize} OFFSET #{criteria.offset}
        </script>
        """)
    List<MenuInformationRow> list(@Param("criteria") MenuInformationSearchCriteria criteria);

    @Select("""
        <script>
        SELECT COUNT(*)
        FROM menu
        """ + DYNAMIC_WHERE + """
        </script>
        """)
    long count(@Param("criteria") MenuInformationSearchCriteria criteria);

    @Select("SELECT EXISTS (SELECT 1 FROM menu WHERE menu_id = #{menuId})")
    boolean existsMenu(@Param("menuId") String menuId);

    @Select("SELECT EXISTS (SELECT 1 FROM menu WHERE screen_id = #{screenId} AND menu_id &lt;&gt; #{menuId})")
    boolean existsScreenIdForOtherMenu(@Param("screenId") String screenId, @Param("menuId") String menuId);

    @Select("SELECT EXISTS (SELECT 1 FROM menu WHERE url = #{url} AND menu_id &lt;&gt; #{menuId})")
    boolean existsUrlForOtherMenu(@Param("url") String url, @Param("menuId") String menuId);

    @Select("""
        """ + BASE_SELECT + """
        WHERE menu_id = #{menuId}
        """)
    MenuInformationRow findByMenuId(@Param("menuId") String menuId);

    @Select("""
        WITH RECURSIVE descendants AS (
            SELECT menu_id
            FROM menu
            WHERE parent_menu_id = #{menuId}
            UNION ALL
            SELECT child.menu_id
            FROM menu child
            JOIN descendants parent ON parent.menu_id = child.parent_menu_id
        )
        SELECT EXISTS (SELECT 1 FROM descendants WHERE menu_id = #{candidateParentMenuId})
        """)
    boolean isDescendant(
        @Param("menuId") String menuId,
        @Param("candidateParentMenuId") String candidateParentMenuId
    );

    @Insert("""
        INSERT INTO menu (menu_id, parent_menu_id, menu_name, screen_id, url, icon, business_type, description, display_order, use_yn, created_at, updated_at)
        VALUES (#{menuId}, #{parentMenuId}, #{menuName}, #{screenId}, #{url}, #{icon}, #{businessType}, #{description}, #{displayOrder}, #{useYn}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """)
    int insertMenu(
        @Param("menuId") String menuId,
        @Param("parentMenuId") String parentMenuId,
        @Param("menuName") String menuName,
        @Param("screenId") String screenId,
        @Param("url") String url,
        @Param("icon") String icon,
        @Param("businessType") String businessType,
        @Param("description") String description,
        @Param("displayOrder") int displayOrder,
        @Param("useYn") String useYn
    );

    @Update("""
        UPDATE menu
        SET parent_menu_id = #{parentMenuId},
            menu_name = #{menuName},
            screen_id = #{screenId},
            url = #{url},
            icon = #{icon},
            business_type = #{businessType},
            description = #{description},
            display_order = #{displayOrder},
            use_yn = #{useYn},
            updated_at = CURRENT_TIMESTAMP
        WHERE menu_id = #{menuId}
        """)
    int updateMenu(
        @Param("menuId") String menuId,
        @Param("parentMenuId") String parentMenuId,
        @Param("menuName") String menuName,
        @Param("screenId") String screenId,
        @Param("url") String url,
        @Param("icon") String icon,
        @Param("businessType") String businessType,
        @Param("description") String description,
        @Param("displayOrder") int displayOrder,
        @Param("useYn") String useYn
    );
}
