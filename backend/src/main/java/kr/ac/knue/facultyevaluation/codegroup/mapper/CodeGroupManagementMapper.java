package kr.ac.knue.facultyevaluation.codegroup.mapper;

import java.util.List;
import kr.ac.knue.facultyevaluation.codegroup.CodeGroupRow;
import kr.ac.knue.facultyevaluation.codegroup.CodeGroupSearchCriteria;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CodeGroupManagementMapper {

    String BASE_SELECT = """
        SELECT group_id AS "groupId",
               group_name AS "groupName",
               description,
               managing_department AS "managingDepartment",
               use_yn AS "useYn",
               updated_at AS "updatedAt"
        FROM code_group
        """;

    String DYNAMIC_WHERE = """
        <where>
            <if test="criteria.groupId != null and criteria.groupId != ''">
                group_id ILIKE CONCAT('%', #{criteria.groupId}, '%')
            </if>
            <if test="criteria.groupName != null and criteria.groupName != ''">
                AND group_name ILIKE CONCAT('%', #{criteria.groupName}, '%')
            </if>
            <if test="criteria.managingDepartment != null and criteria.managingDepartment != ''">
                AND managing_department ILIKE CONCAT('%', #{criteria.managingDepartment}, '%')
            </if>
            <if test="criteria.useYn != null and criteria.useYn != ''">
                AND use_yn = #{criteria.useYn}
            </if>
        </where>
        """;

    @Select("""
        <script>
        """ + BASE_SELECT + DYNAMIC_WHERE + """
        ORDER BY group_id
        LIMIT #{criteria.safeSize} OFFSET #{criteria.offset}
        </script>
        """)
    List<CodeGroupRow> list(@Param("criteria") CodeGroupSearchCriteria criteria);

    @Select("""
        <script>
        SELECT COUNT(*)
        FROM code_group
        """ + DYNAMIC_WHERE + """
        </script>
        """)
    long count(@Param("criteria") CodeGroupSearchCriteria criteria);

    @Select("SELECT EXISTS (SELECT 1 FROM code_group WHERE group_id = #{groupId})")
    boolean existsGroup(@Param("groupId") String groupId);

    @Select("""
        """ + BASE_SELECT + """
        WHERE group_id = #{groupId}
        """)
    CodeGroupRow findByGroupId(@Param("groupId") String groupId);

    @Insert("""
        INSERT INTO code_group (group_id, group_name, description, managing_department, use_yn, created_at, updated_at)
        VALUES (#{groupId}, #{groupName}, #{description}, #{managingDepartment}, #{useYn}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """)
    int insertCodeGroup(
        @Param("groupId") String groupId,
        @Param("groupName") String groupName,
        @Param("description") String description,
        @Param("managingDepartment") String managingDepartment,
        @Param("useYn") String useYn
    );

    @Update("""
        UPDATE code_group
        SET group_name = #{groupName},
            description = #{description},
            managing_department = #{managingDepartment},
            use_yn = #{useYn},
            updated_at = CURRENT_TIMESTAMP
        WHERE group_id = #{groupId}
        """)
    int updateCodeGroup(
        @Param("groupId") String groupId,
        @Param("groupName") String groupName,
        @Param("description") String description,
        @Param("managingDepartment") String managingDepartment,
        @Param("useYn") String useYn
    );
}
