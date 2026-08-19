package kr.ac.knue.facultyevaluation.codedetail.mapper;

import java.time.LocalDate;
import java.util.List;
import kr.ac.knue.facultyevaluation.codedetail.CodeDetailRow;
import kr.ac.knue.facultyevaluation.codedetail.CodeDetailSearchCriteria;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CodeDetailManagementMapper {

    String HIERARCHY_SELECT = """
        WITH RECURSIVE detail_tree AS (
            SELECT group_id,
                   code_value,
                   code_name,
                   parent_code_value,
                   sort_order,
                   extra_attributes::text AS "extraAttributes",
                   use_yn,
                   valid_from,
                   valid_to,
                   updated_at,
                   0 AS depth
            FROM code_detail
            WHERE parent_code_value IS NULL
            UNION ALL
            SELECT child.group_id,
                   child.code_value,
                   child.code_name,
                   child.parent_code_value,
                   child.sort_order,
                   child.extra_attributes::text AS "extraAttributes",
                   child.use_yn,
                   child.valid_from,
                   child.valid_to,
                   child.updated_at,
                   parent.depth + 1 AS depth
            FROM code_detail child
            JOIN detail_tree parent
              ON parent.group_id = child.group_id
             AND parent.code_value = child.parent_code_value
        )
        SELECT group_id AS "groupId",
               code_value AS "codeValue",
               code_name AS "codeName",
               parent_code_value AS "parentCodeValue",
               sort_order AS "sortOrder",
               "extraAttributes",
               use_yn AS "useYn",
               valid_from AS "validFrom",
               valid_to AS "validTo",
               depth,
               updated_at AS "updatedAt"
        FROM detail_tree
        """;

    String DYNAMIC_WHERE = """
        <where>
            <if test="criteria.groupId != null and criteria.groupId != ''">
                group_id = #{criteria.groupId}
            </if>
            <if test="criteria.filter != null and criteria.filter != ''">
                AND (code_value ILIKE CONCAT('%', #{criteria.filter}, '%') OR code_name ILIKE CONCAT('%', #{criteria.filter}, '%'))
            </if>
            <if test="criteria.useYn != null and criteria.useYn != ''">
                AND use_yn = #{criteria.useYn}
            </if>
            <if test="criteria.validOn != null">
                AND (valid_from IS NULL OR valid_from &lt;= #{criteria.validOn})
                AND (valid_to IS NULL OR valid_to &gt;= #{criteria.validOn})
            </if>
        </where>
        """;

    @Select("""
        <script>
        """ + HIERARCHY_SELECT + DYNAMIC_WHERE + """
        ORDER BY group_id, depth, sort_order, code_value
        LIMIT #{criteria.safeSize} OFFSET #{criteria.offset}
        </script>
        """)
    List<CodeDetailRow> list(@Param("criteria") CodeDetailSearchCriteria criteria);

    @Select("""
        <script>
        SELECT COUNT(*)
        FROM code_detail
        """ + DYNAMIC_WHERE + """
        </script>
        """)
    long count(@Param("criteria") CodeDetailSearchCriteria criteria);

    @Select("SELECT EXISTS (SELECT 1 FROM code_group WHERE group_id = #{groupId})")
    boolean existsGroup(@Param("groupId") String groupId);

    @Select("SELECT EXISTS (SELECT 1 FROM code_detail WHERE group_id = #{groupId} AND code_value = #{codeValue})")
    boolean existsCode(@Param("groupId") String groupId, @Param("codeValue") String codeValue);

    @Select("""
        SELECT group_id AS "groupId",
               code_value AS "codeValue",
               code_name AS "codeName",
               parent_code_value AS "parentCodeValue",
               sort_order AS "sortOrder",
               extra_attributes::text AS "extraAttributes",
               use_yn AS "useYn",
               valid_from AS "validFrom",
               valid_to AS "validTo",
               CASE WHEN parent_code_value IS NULL THEN 0 ELSE 1 END AS depth,
               updated_at AS "updatedAt"
        FROM code_detail
        WHERE group_id = #{groupId}
          AND code_value = #{codeValue}
        """)
    CodeDetailRow findById(@Param("groupId") String groupId, @Param("codeValue") String codeValue);

    @Insert("""
        INSERT INTO code_detail (group_id, code_value, code_name, parent_code_value, sort_order, extra_attributes, use_yn, valid_from, valid_to, created_at, updated_at)
        VALUES (#{groupId}, #{codeValue}, #{codeName}, #{parentCodeValue}, #{sortOrder}, CAST(#{extraAttributes} AS jsonb), #{useYn}, #{validFrom}, #{validTo}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """)
    int insertCodeDetail(
        @Param("groupId") String groupId,
        @Param("codeValue") String codeValue,
        @Param("codeName") String codeName,
        @Param("parentCodeValue") String parentCodeValue,
        @Param("sortOrder") Integer sortOrder,
        @Param("extraAttributes") String extraAttributes,
        @Param("useYn") String useYn,
        @Param("validFrom") LocalDate validFrom,
        @Param("validTo") LocalDate validTo
    );

    @Update("""
        UPDATE code_detail
        SET code_name = #{codeName},
            parent_code_value = #{parentCodeValue},
            sort_order = #{sortOrder},
            extra_attributes = CAST(#{extraAttributes} AS jsonb),
            use_yn = #{useYn},
            valid_from = #{validFrom},
            valid_to = #{validTo},
            updated_at = CURRENT_TIMESTAMP
        WHERE group_id = #{groupId}
          AND code_value = #{codeValue}
        """)
    int updateCodeDetail(
        @Param("groupId") String groupId,
        @Param("codeValue") String codeValue,
        @Param("codeName") String codeName,
        @Param("parentCodeValue") String parentCodeValue,
        @Param("sortOrder") Integer sortOrder,
        @Param("extraAttributes") String extraAttributes,
        @Param("useYn") String useYn,
        @Param("validFrom") LocalDate validFrom,
        @Param("validTo") LocalDate validTo
    );
}
