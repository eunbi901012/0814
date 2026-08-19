package kr.ac.knue.facultyevaluation.codegroup;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import kr.ac.knue.facultyevaluation.common.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;

@WebMvcTest(CodeGroupManagementController.class)
@Import(GlobalExceptionHandler.class)
class CodeGroupManagementContractTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CodeGroupManagementService codeGroupManagementService;

    @Test
    void openapiFixtureIsAvailableOnClasspathForCodeGroupContractCoverage() {
        org.assertj.core.api.Assertions.assertThat(new ClassPathResource("contracts/openapi.yaml").exists()).isTrue();
    }

    @Test
    void seedMigrationContainsCodeGroupScreenR09PermissionAndMinimumCodeFixtures() throws Exception {
        ClassPathResource seed = new ClassPathResource("db/migration/V2__foundation_seed.sql");
        String content = StreamUtils.copyToString(seed.getInputStream(), StandardCharsets.UTF_8);

        org.assertj.core.api.Assertions.assertThat(content)
            .contains("'SCR-CMN-CODE-GROUP'")
            .contains("'/admin/code-groups'")
            .contains("'코드그룹 관리'")
            .contains("'PERM-R09-' || menu_id")
            .contains("INSERT INTO code_group")
            .contains("'EMPLOYMENT_STATUS'")
            .contains("'USE_YN'");
    }

    @Test
    void listCodeGroupsReturnsSearchableGroupManagementFieldsAndDetailNavigationContract() throws Exception {
        when(codeGroupManagementService.list(any(CodeGroupSearchCriteria.class))).thenReturn(new CodeGroupSearchResult(List.of(
            new CodeGroupSummary("EMPLOYMENT_STATUS", "재직상태", "사용자 검색과 KORUS snapshot 검증에 필요한 재직 상태", "교수지원과", "Y", LocalDateTime.parse("2026-01-01T09:00:00"))
        ), 1, 0, 20));

        mockMvc.perform(get("/api/code-groups")
                .param("groupId", "EMPLOYMENT")
                .param("groupName", "재직")
                .param("managingDepartment", "교수지원과")
                .param("useYn", "Y"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items[0].groupId").value("EMPLOYMENT_STATUS"))
            .andExpect(jsonPath("$.data.items[0].groupName").value("재직상태"))
            .andExpect(jsonPath("$.data.items[0].description").value("사용자 검색과 KORUS snapshot 검증에 필요한 재직 상태"))
            .andExpect(jsonPath("$.data.items[0].managingDepartment").value("교수지원과"))
            .andExpect(jsonPath("$.data.items[0].useYn").value("Y"))
            .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void createCodeGroupRegistersGroupNameDescriptionManagingDepartmentAndUseYn() throws Exception {
        when(codeGroupManagementService.create(any(CreateCodeGroupRequest.class))).thenReturn(
            new CodeGroupSummary("EVALUATION_STATUS", "평가상태", "평가 진행 상태 코드 그룹", "교수지원과", "Y", LocalDateTime.parse("2026-01-02T09:00:00"))
        );

        mockMvc.perform(post("/api/code-groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"EVALUATION_STATUS\",\"groupName\":\"평가상태\",\"description\":\"평가 진행 상태 코드 그룹\",\"managingDepartment\":\"교수지원과\",\"useYn\":\"Y\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("코드그룹이 등록되었습니다."))
            .andExpect(jsonPath("$.data.groupId").value("EVALUATION_STATUS"))
            .andExpect(jsonPath("$.data.groupName").value("평가상태"))
            .andExpect(jsonPath("$.data.managingDepartment").value("교수지원과"));
    }

    @Test
    void updateCodeGroupPersistsEditableFieldsAndKeepsGroupIdReadonly() throws Exception {
        when(codeGroupManagementService.update(eq("USE_YN"), any(UpdateCodeGroupRequest.class))).thenReturn(
            new CodeGroupSummary("USE_YN", "사용여부", "시스템 공통 사용 여부", "시스템관리팀", "Y", LocalDateTime.parse("2026-01-03T09:00:00"))
        );

        mockMvc.perform(put("/api/code-groups/USE_YN")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"USE_YN\",\"groupName\":\"사용여부\",\"description\":\"시스템 공통 사용 여부\",\"managingDepartment\":\"시스템관리팀\",\"useYn\":\"Y\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("코드그룹이 저장되었습니다."))
            .andExpect(jsonPath("$.data.groupId").value("USE_YN"))
            .andExpect(jsonPath("$.data.description").value("시스템 공통 사용 여부"))
            .andExpect(jsonPath("$.data.managingDepartment").value("시스템관리팀"));
    }

    @Test
    void updateCodeGroupRejectsChangedGroupIdBecauseGroupIdIsReadonly() throws Exception {
        when(codeGroupManagementService.update(eq("USE_YN"), any(UpdateCodeGroupRequest.class)))
            .thenThrow(new IllegalArgumentException("그룹ID는 변경할 수 없습니다."));

        mockMvc.perform(put("/api/code-groups/USE_YN")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"OTHER_GROUP\",\"groupName\":\"사용여부\",\"description\":\"시스템 공통 사용 여부\",\"managingDepartment\":\"시스템관리팀\",\"useYn\":\"Y\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
    }

    @Test
    void createCodeGroupRejectsMissingRequiredGroupFields() throws Exception {
        mockMvc.perform(post("/api/code-groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"\",\"groupName\":\"\",\"description\":\"설명\",\"managingDepartment\":\"교수지원과\",\"useYn\":\"Y\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void updateCodeGroupRejectsInvalidUseYn() throws Exception {
        mockMvc.perform(put("/api/code-groups/USE_YN")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"USE_YN\",\"groupName\":\"사용여부\",\"description\":\"시스템 공통 사용 여부\",\"managingDepartment\":\"시스템관리팀\",\"useYn\":\"X\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
}
