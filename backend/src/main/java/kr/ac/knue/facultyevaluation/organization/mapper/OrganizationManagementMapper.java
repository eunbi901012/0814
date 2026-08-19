package kr.ac.knue.facultyevaluation.organization.mapper;

import java.time.LocalDate;
import java.util.List;
import kr.ac.knue.facultyevaluation.organization.OrganizationRow;
import kr.ac.knue.facultyevaluation.organization.OrganizationSearchCriteria;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrganizationManagementMapper {

    String LATEST_RELATION = """
        WITH latest_relation AS (
            SELECT DISTINCT ON (orh.org_code)
                   orh.org_code,
                   orh.parent_org_code,
                   parent.org_name AS parent_org_name,
                   orh.effective_start_date,
                   orh.effective_end_date,
                   orh.reason
            FROM organization_relation_history orh
            LEFT JOIN organization parent ON parent.org_code = orh.parent_org_code
            ORDER BY orh.org_code, orh.effective_start_date DESC, orh.created_at DESC
        )
        """;

    String BASE_SELECT = """
        SELECT org.org_code AS "orgCode",
               org.org_name AS "orgName",
               org.org_type AS "orgType",
               COALESCE(lr.parent_org_code, org.parent_org_code) AS "parentOrgCode",
               COALESCE(lr.parent_org_name, source_parent.org_name) AS "parentOrgName",
               org.use_yn AS "useYn",
               org.updated_at AS "updatedAt",
               lr.effective_start_date AS "relationshipEffectiveStartDate",
               lr.effective_end_date AS "relationshipEffectiveEndDate",
               lr.reason AS "relationshipReason"
        FROM organization org
        LEFT JOIN latest_relation lr ON lr.org_code = org.org_code
        LEFT JOIN organization source_parent ON source_parent.org_code = org.parent_org_code
        """;

    String DYNAMIC_WHERE = """
        <where>
            <if test="criteria.orgCode != null and criteria.orgCode != ''">
                org.org_code ILIKE CONCAT('%', #{criteria.orgCode}, '%')
            </if>
            <if test="criteria.orgType != null and criteria.orgType != ''">
                AND org.org_type = #{criteria.orgType}
            </if>
        </where>
        """;

    @Select("""
        <script>
        """ + LATEST_RELATION + BASE_SELECT + DYNAMIC_WHERE + """
        ORDER BY COALESCE(lr.parent_org_code, org.parent_org_code) NULLS FIRST, org.org_code
        LIMIT #{criteria.safeSize} OFFSET #{criteria.offset}
        </script>
        """)
    List<OrganizationRow> list(@Param("criteria") OrganizationSearchCriteria criteria);

    @Select("""
        <script>
        """ + LATEST_RELATION + """
        SELECT COUNT(*)
        FROM organization org
        LEFT JOIN latest_relation lr ON lr.org_code = org.org_code
        """ + DYNAMIC_WHERE + """
        </script>
        """)
    long count(@Param("criteria") OrganizationSearchCriteria criteria);

    @Select("""
        """ + LATEST_RELATION + BASE_SELECT + """
        WHERE org.org_code = #{orgCode}
        """)
    OrganizationRow findByOrgCode(@Param("orgCode") String orgCode);

    @Select("""
        """ + LATEST_RELATION + """
        SELECT org.org_code
        FROM organization org
        LEFT JOIN latest_relation lr ON lr.org_code = org.org_code
        WHERE COALESCE(lr.parent_org_code, org.parent_org_code) = #{parentOrgCode}
        ORDER BY org.org_code
        """)
    List<String> listChildOrgCodes(@Param("parentOrgCode") String parentOrgCode);

    @Select("""
        SELECT EXISTS (SELECT 1 FROM organization WHERE org_code = #{orgCode})
        """)
    boolean existsOrganization(@Param("orgCode") String orgCode);

    @Insert("""
        INSERT INTO organization_relation_history (history_id, org_code, parent_org_code,
                                                   effective_start_date, effective_end_date, reason, created_at)
        VALUES (#{historyId}, #{orgCode}, #{parentOrgCode}, #{effectiveStartDate}, #{effectiveEndDate},
                #{reason}, CURRENT_TIMESTAMP)
        """)
    int insertRelationshipHistory(
        @Param("historyId") String historyId,
        @Param("orgCode") String orgCode,
        @Param("parentOrgCode") String parentOrgCode,
        @Param("effectiveStartDate") LocalDate effectiveStartDate,
        @Param("effectiveEndDate") LocalDate effectiveEndDate,
        @Param("reason") String reason
    );
}
