package kr.ac.knue.facultyevaluation.organization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kr.ac.knue.facultyevaluation.organization.mapper.OrganizationManagementMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationManagementService {

    private final OrganizationManagementMapper organizationManagementMapper;

    public OrganizationManagementService(OrganizationManagementMapper organizationManagementMapper) {
        this.organizationManagementMapper = organizationManagementMapper;
    }

    @Transactional(readOnly = true)
    public OrganizationSearchResult list(OrganizationSearchCriteria criteria) {
        List<OrganizationRow> rows = organizationManagementMapper.list(criteria);
        Map<String, List<String>> childCodesByParent = new HashMap<>();
        Map<String, OrganizationRow> rowByCode = new HashMap<>();
        for (OrganizationRow row : rows) {
            rowByCode.put(row.orgCode(), row);
            if (row.parentOrgCode() != null) {
                childCodesByParent.computeIfAbsent(row.parentOrgCode(), ignored -> new ArrayList<>()).add(row.orgCode());
            }
        }
        List<OrganizationSummary> items = rows.stream()
            .map(row -> row.toSummary(resolveDepth(row, rowByCode), sortedChildren(childCodesByParent, row.orgCode())))
            .sorted(Comparator.comparingInt(OrganizationSummary::depth).thenComparing(OrganizationSummary::orgCode))
            .toList();
        long total = organizationManagementMapper.count(criteria);
        return new OrganizationSearchResult(items, total, criteria.safePage(), criteria.safeSize());
    }

    @Transactional
    public OrganizationSummary saveRelationship(String orgCode, SaveOrganizationRelationshipRequest request) {
        ensureOrganizationExists(orgCode);
        if (request.parentOrgCode() != null && !request.parentOrgCode().isBlank()) {
            if (orgCode.equals(request.parentOrgCode())) {
                throw new IllegalArgumentException("조직은 자기 자신을 상위조직으로 지정할 수 없습니다.");
            }
            ensureOrganizationExists(request.parentOrgCode());
        }
        String normalizedParentOrgCode = request.parentOrgCode() == null || request.parentOrgCode().isBlank()
            ? null
            : request.parentOrgCode();
        organizationManagementMapper.insertRelationshipHistory(
            "ORG-HIST-" + UUID.randomUUID(),
            orgCode,
            normalizedParentOrgCode,
            request.effectiveStartDate(),
            request.effectiveEndDate(),
            request.reason()
        );
        OrganizationRow updated = organizationManagementMapper.findByOrgCode(orgCode);
        return updated.toSummary(0, organizationManagementMapper.listChildOrgCodes(orgCode));
    }

    private void ensureOrganizationExists(String orgCode) {
        if (!organizationManagementMapper.existsOrganization(orgCode)) {
            throw new IllegalArgumentException("조직을 찾을 수 없습니다: " + orgCode);
        }
    }

    private int resolveDepth(OrganizationRow row, Map<String, OrganizationRow> rowByCode) {
        int depth = 0;
        String parentCode = row.parentOrgCode();
        while (parentCode != null && rowByCode.containsKey(parentCode)) {
            depth++;
            parentCode = rowByCode.get(parentCode).parentOrgCode();
        }
        return depth;
    }

    private List<String> sortedChildren(Map<String, List<String>> childCodesByParent, String orgCode) {
        return childCodesByParent.getOrDefault(orgCode, Collections.emptyList()).stream()
            .sorted()
            .toList();
    }
}
