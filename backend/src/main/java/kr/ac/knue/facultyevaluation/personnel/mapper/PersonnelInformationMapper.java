package kr.ac.knue.facultyevaluation.personnel.mapper;

import java.util.List;
import kr.ac.knue.facultyevaluation.personnel.PersonnelSearchCriteria;
import kr.ac.knue.facultyevaluation.personnel.PersonnelSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PersonnelInformationMapper {

    @Select("""
        <script>
        SELECT snapshot_id AS "snapshotId", employee_no AS "employeeNo", name, org_code AS "orgCode",
               rank_name AS "rankName", retirement_date AS "retirementDate",
               last_synced_at AS "lastSyncedAt", status
        FROM korus_faculty_snapshot
        WHERE 1 = 1
        <if test="criteria.employeeNo != null and criteria.employeeNo != ''">
            AND employee_no = #{criteria.employeeNo}
        </if>
        <if test="criteria.name != null and criteria.name != ''">
            AND name ILIKE CONCAT('%', #{criteria.name}, '%')
        </if>
        <if test="criteria.orgCode != null and criteria.orgCode != ''">
            AND org_code = #{criteria.orgCode}
        </if>
        <if test="criteria.rankName != null and criteria.rankName != ''">
            AND rank_name = #{criteria.rankName}
        </if>
        <if test="criteria.status != null and criteria.status != ''">
            AND status = #{criteria.status}
        </if>
        ORDER BY employee_no
        </script>
        """)
    List<PersonnelSnapshot> searchReadonly(@Param("criteria") PersonnelSearchCriteria criteria);
}
