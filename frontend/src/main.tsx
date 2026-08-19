import React, { FormEvent, useEffect, useMemo, useState } from "react";
import ReactDOM from "react-dom/client";
import "./styles.css";

type ScreenStatus = "loading" | "empty" | "error" | "permission" | "success";

export type AdminNavigationItem = {
  label: string;
  href: string;
};

export type AdminNavigationGroup = {
  label: string;
  items: AdminNavigationItem[];
};

const adminNavigationGroups: AdminNavigationGroup[] = [
  {
    label: "사용자·조직 관리",
    items: [
      { label: "사용자 관리", href: "/admin/users" },
      { label: "조직 관리", href: "/admin/organizations" },
    ],
  },
  {
    label: "역할·권한 관리",
    items: [
      { label: "역할 관리", href: "/admin/roles" },
      { label: "사용자 역할 관리", href: "/admin/user-roles" },
      { label: "메뉴 권한 관리", href: "/admin/menu-permissions" },
    ],
  },
  {
    label: "메뉴 관리",
    items: [
      { label: "메뉴 구조 관리", href: "/admin/menus/tree" },
      { label: "메뉴 정보 관리", href: "/admin/menus" },
    ],
  },
  {
    label: "공통코드 관리",
    items: [
      { label: "코드그룹 관리", href: "/admin/code-groups" },
      { label: "상세코드 관리", href: "/admin/code-details" },
    ],
  },
];

export function createVisibleAdminNavigation(
  visibleMenuUrls: string[],
): AdminNavigationGroup[] {
  const visibleUrlSet = new Set(visibleMenuUrls);
  return adminNavigationGroups
    .map((group) => ({
      ...group,
      items: group.items.filter((item) => visibleUrlSet.has(item.href)),
    }))
    .filter((group) => group.items.length > 0);
}

export type AdminUser = {
  userId: string;
  loginId: string;
  name: string;
  departmentCode: string | null;
  departmentName: string | null;
  rankName: string | null;
  employmentStatus: string | null;
  roles: string[];
  systemUseYn: "Y" | "N";
  positionName: string | null;
  retirementDate: string | null;
  lastSyncedAt: string | null;
};

export type AdminOrganization = {
  orgCode: string;
  orgName: string;
  orgType:
    | "UNIVERSITY"
    | "GRADUATE_SCHOOL"
    | "COLLEGE"
    | "DEPARTMENT"
    | "OFFICE";
  parentOrgCode: string | null;
  parentOrgName: string | null;
  useYn: "Y" | "N";
  depth: number;
  childOrgCodes: string[];
  updatedAt: string | null;
  currentRelationship: {
    parentOrgCode: string | null;
    parentOrgName: string | null;
    effectiveStartDate: string;
    effectiveEndDate: string | null;
    reason: string | null;
  } | null;
};

export type AdminRole = {
  roleCode: string;
  roleName: string;
  purpose: string;
  grantCriteria: string | null;
  dataScopeDefault: string | null;
  useYn: "Y" | "N";
  updatedAt: string | null;
};

export type AdminUserRole = {
  userRoleId: string;
  userId: string;
  userName: string;
  roleCode: string;
  roleName: string;
  effectiveStartDate: string;
  effectiveEndDate: string | null;
  approverUserId: string;
  approverName: string | null;
  assignmentType: "POSITION_BASED" | "MANUAL";
  status: "ACTIVE" | "REVOKED";
  updatedAt: string | null;
};

export type AdminMenuPermission = {
  permissionId: string | null;
  targetType: "ROLE" | "ORGANIZATION" | "USER";
  targetId: string;
  menuId: string;
  topMenuName: string;
  middleMenuName: string | null;
  screenMenuName: string | null;
  screenId: string | null;
  url: string | null;
  canRead: boolean;
  canWrite: boolean;
  effect: "ALLOW" | "DENY";
  updatedAt: string | null;
};

export type PermissionPreview = {
  visibleMenuUrls: string[];
  writableMenuUrls: string[];
  deniedMenuUrls: string[];
};

export type AdminMenuTree = {
  menuId: string;
  parentMenuId: string | null;
  menuName: string;
  screenId: string | null;
  url: string | null;
  displayOrder: number;
  useYn: "Y" | "N";
  depth: number;
  childMenuIds: string[];
  updatedAt: string | null;
};

export type AdminMenuInfo = {
  menuId: string;
  parentMenuId: string | null;
  menuName: string;
  screenId: string | null;
  url: string | null;
  icon: string | null;
  businessType: string | null;
  description: string | null;
  displayOrder: number;
  useYn: "Y" | "N";
  updatedAt: string | null;
};

export type AdminCodeGroup = {
  groupId: string;
  groupName: string;
  description: string | null;
  managingDepartment: string | null;
  useYn: "Y" | "N";
  updatedAt: string | null;
};

export type AdminCodeDetail = {
  groupId: string;
  codeValue: string;
  codeName: string;
  parentCodeValue: string | null;
  sortOrder: number;
  extraAttributes: string | null;
  useYn: "Y" | "N";
  validFrom: string | null;
  validTo: string | null;
  depth: number;
  updatedAt: string | null;
};

type UserSearchResponse = {
  items: AdminUser[];
  total: number;
  page: number;
  size: number;
};

type OrganizationSearchResponse = {
  items: AdminOrganization[];
  total: number;
  page: number;
  size: number;
};

type RoleSearchResponse = {
  items: AdminRole[];
  total: number;
  page: number;
  size: number;
};

type UserRoleSearchResponse = {
  items: AdminUserRole[];
  total: number;
  page: number;
  size: number;
};

type MenuPermissionSearchResponse = {
  items: AdminMenuPermission[];
  total: number;
  page: number;
  size: number;
  preview: PermissionPreview;
};

type MenuTreeSearchResponse = {
  items: AdminMenuTree[];
  total: number;
  page: number;
  size: number;
};

type MenuInformationSearchResponse = {
  items: AdminMenuInfo[];
  total: number;
  page: number;
  size: number;
};

type CodeGroupSearchResponse = {
  items: AdminCodeGroup[];
  total: number;
  page: number;
  size: number;
};

type CodeDetailSearchResponse = {
  items: AdminCodeDetail[];
  total: number;
  page: number;
  size: number;
};

type ApiResponse<T> = {
  success: true;
  data: T;
  message?: string | null;
};

type ApiErrorEnvelope = {
  success: false;
  error?: {
    code?: string;
    message?: string;
    fieldErrors?: { field: string; message: string }[];
    path?: string;
    timestamp?: string;
  };
};

type SearchFilters = {
  employeeNo: string;
  name: string;
  departmentCode: string;
  rankName: string;
  employmentStatus: string;
  roleCode: string;
  systemUseYn: string;
};

type RoleForm = {
  roleCodes: string[];
  assignmentType: "POSITION_BASED" | "MANUAL";
  approverUserId: string;
  effectiveStartDate: string;
  effectiveEndDate: string;
};

type OrganizationFilters = {
  orgCode: string;
  orgType: string;
};

type RoleFilters = {
  roleCode: string;
  roleName: string;
  useYn: string;
};

type UserRoleFilters = {
  employeeNo: string;
  name: string;
  roleCode: string;
  validOn: string;
  assignmentType: string;
};

type UserRoleForm = {
  userId: string;
  roleCode: string;
  assignmentType: "POSITION_BASED" | "MANUAL";
  approverUserId: string;
  effectiveStartDate: string;
  effectiveEndDate: string;
};

type MenuPermissionFilters = {
  targetType: "ROLE" | "ORGANIZATION" | "USER";
  targetId: string;
};

type MenuTreeForm = {
  parentMenuId: string;
  displayOrder: string;
};

type MenuInformationFilters = {
  menuName: string;
  screenId: string;
  url: string;
  businessType: string;
};

type MenuInformationForm = {
  menuId: string;
  parentMenuId: string;
  menuName: string;
  screenId: string;
  url: string;
  icon: string;
  businessType: string;
  description: string;
  displayOrder: string;
  useYn: "Y" | "N";
};

type CodeGroupFilters = {
  groupId: string;
  groupName: string;
  managingDepartment: string;
  useYn: string;
};

type CodeGroupForm = {
  groupId: string;
  groupName: string;
  description: string;
  managingDepartment: string;
  useYn: "Y" | "N";
};

type CodeDetailFilters = {
  groupId: string;
  filter: string;
  useYn: string;
  validOn: string;
};

type CodeDetailForm = {
  groupId: string;
  codeValue: string;
  codeName: string;
  parentCodeValue: string;
  sortOrder: string;
  extraAttributes: string;
  useYn: "Y" | "N";
  validFrom: string;
  validTo: string;
};

type RoleDetailForm = {
  roleName: string;
  purpose: string;
  grantCriteria: string;
  dataScopeDefault: string;
  useYn: "Y" | "N";
};

type OrganizationRelationshipForm = {
  parentOrgCode: string;
  effectiveStartDate: string;
  effectiveEndDate: string;
  reason: string;
};

export type AdminUsersViewModelInput = {
  path: string;
  users: AdminUser[];
  selectedUserId: string | null;
  status: ScreenStatus;
  message?: string;
};

export function createAdminUsersViewModel(input: AdminUsersViewModelInput) {
  const selectedUser =
    input.users.find((user) => user.userId === input.selectedUserId) ?? null;
  return {
    route: input.path,
    screenId: "SCR-CMN-USER",
    menuPath: "시스템 관리 > 사용자·조직 관리 > 사용자 관리",
    filters: ["교번", "성명", "소속", "직급", "재직상태", "역할", "사용여부"],
    columns: [
      "교번",
      "성명",
      "소속",
      "직급",
      "재직상태",
      "역할",
      "사용여부",
      "보직",
      "퇴직일자",
      "최종 동기화일시",
    ],
    actions: [
      "/api/users",
      "/api/users/{userId}/usage",
      "/api/users/{userId}/roles",
    ],
    users: input.users,
    selectedUser,
    status: input.status,
    message: input.message,
    permissionMessage:
      input.status === "permission" ? "시스템관리자 권한이 필요합니다." : null,
  };
}

export type AdminOrganizationsViewModelInput = {
  path: string;
  organizations: AdminOrganization[];
  selectedOrgCode: string | null;
  status: ScreenStatus;
  message?: string;
};

export function createAdminOrganizationsViewModel(
  input: AdminOrganizationsViewModelInput,
) {
  const selectedOrganization =
    input.organizations.find(
      (organization) => organization.orgCode === input.selectedOrgCode,
    ) ?? null;
  return {
    route: input.path,
    screenId: "SCR-CMN-ORG",
    menuPath: "시스템 관리 > 사용자·조직 관리 > 조직 관리",
    filters: ["조직코드", "조직구분"],
    sourceColumns: ["조직코드", "조직명", "조직구분", "상위조직", "사용여부"],
    actions: [
      "/api/organizations",
      "/api/organizations/{orgCode}/relationships",
    ],
    treeRows: input.organizations,
    selectedOrganization,
    status: input.status,
    message: input.message,
    permissionMessage:
      input.status === "permission" ? "시스템관리자 권한이 필요합니다." : null,
  };
}

export type AdminRolesViewModelInput = {
  path: string;
  roles: AdminRole[];
  selectedRoleCode: string | null;
  status: ScreenStatus;
  message?: string;
};

export function createAdminRolesViewModel(input: AdminRolesViewModelInput) {
  const selectedRole =
    input.roles.find((role) => role.roleCode === input.selectedRoleCode) ??
    null;
  return {
    route: input.path,
    screenId: "SCR-CMN-ROLE",
    menuPath: "시스템 관리 > 역할·권한 관리 > 역할 관리",
    filters: ["역할코드", "역할명", "사용여부"],
    columns: ["역할코드", "역할명", "목적", "사용여부"],
    detailFields: [
      "role_code readonly",
      "역할명",
      "목적",
      "부여 기준",
      "데이터 범위 기본값",
      "사용여부",
    ],
    actions: ["/api/roles", "/api/roles/{roleCode}"],
    roles: input.roles,
    selectedRole,
    status: input.status,
    message: input.message,
    permissionMessage:
      input.status === "permission" ? "시스템관리자 권한이 필요합니다." : null,
  };
}

export type AdminUserRolesViewModelInput = {
  path: string;
  userRoles: AdminUserRole[];
  selectedUserRoleId: string | null;
  status: ScreenStatus;
  message?: string;
};

export function createAdminUserRolesViewModel(
  input: AdminUserRolesViewModelInput,
) {
  const selectedUserRole =
    input.userRoles.find(
      (userRole) => userRole.userRoleId === input.selectedUserRoleId,
    ) ?? null;
  return {
    route: input.path,
    screenId: "SCR-CMN-USER-ROLE",
    menuPath: "시스템 관리 > 역할·권한 관리 > 사용자 역할 관리",
    filters: ["교번", "성명", "역할", "유효기간", "부여구분"],
    columns: [
      "교번",
      "성명",
      "현재 역할",
      "시작일",
      "종료일",
      "승인자",
      "부여구분",
      "상태",
    ],
    detailFields: ["역할", "시작일", "종료일", "승인자", "부여구분", "상태"],
    actions: ["/api/user-roles", "/api/user-roles/{userRoleId}"],
    userRoles: input.userRoles,
    selectedUserRole,
    status: input.status,
    message: input.message,
    permissionMessage:
      input.status === "permission" ? "시스템관리자 권한이 필요합니다." : null,
  };
}

export type AdminMenuPermissionsViewModelInput = {
  path: string;
  permissions: AdminMenuPermission[];
  preview: PermissionPreview;
  status: ScreenStatus;
  message?: string;
};

export function createAdminMenuPermissionsViewModel(
  input: AdminMenuPermissionsViewModelInput,
) {
  return {
    route: input.path,
    screenId: "SCR-CMN-MENU-PERM",
    menuPath: "시스템 관리 > 역할·권한 관리 > 메뉴 권한 관리",
    filters: ["대상유형", "대상ID"],
    columns: ["대메뉴", "중메뉴", "화면", "조회허용", "변경허용", "effect"],
    actions: ["/api/menu-permissions"],
    permissions: input.permissions,
    preview: input.preview,
    status: input.status,
    message: input.message,
    permissionMessage:
      input.status === "permission" ? "시스템관리자 권한이 필요합니다." : null,
  };
}

export type AdminMenuTreeViewModelInput = {
  path: string;
  menus: AdminMenuTree[];
  selectedMenuId: string | null;
  status: ScreenStatus;
  message?: string;
};

export function createAdminMenuTreeViewModel(
  input: AdminMenuTreeViewModelInput,
) {
  const selectedMenu =
    input.menus.find((menu) => menu.menuId === input.selectedMenuId) ?? null;
  return {
    route: input.path,
    screenId: "SCR-CMN-MENU-TREE",
    menuPath: "시스템 관리 > 메뉴 관리 > 메뉴 구조 관리",
    treeLabels: input.menus.map((menu) => menu.menuName),
    detailFields: ["menu_id readonly", "현재 부모메뉴", "부모메뉴", "표시순서"],
    actions: [
      "/api/menus/tree",
      "/api/menus/{menuId}/parent",
      "/api/menus/{menuId}/order",
    ],
    menus: input.menus,
    selectedMenu,
    status: input.status,
    message: input.message,
    permissionMessage:
      input.status === "permission" ? "시스템관리자 권한이 필요합니다." : null,
  };
}

export type AdminMenusViewModelInput = {
  path: string;
  menus: AdminMenuInfo[];
  selectedMenuId: string | null;
  status: ScreenStatus;
  message?: string;
};

export function createAdminMenusViewModel(input: AdminMenusViewModelInput) {
  const selectedMenu =
    input.menus.find((menu) => menu.menuId === input.selectedMenuId) ?? null;
  return {
    route: input.path,
    screenId: "SCR-CMN-MENU-INFO",
    menuPath: "시스템 관리 > 메뉴 관리 > 메뉴 정보 관리",
    filters: ["메뉴명", "화면ID", "URL", "업무구분"],
    columns: [
      "메뉴명",
      "화면ID",
      "URL",
      "아이콘",
      "업무구분",
      "설명",
      "사용여부",
    ],
    detailFields: [
      "menu_id",
      "부모메뉴",
      "메뉴명",
      "화면ID",
      "URL",
      "아이콘",
      "업무구분",
      "설명",
      "표시순서",
      "사용여부",
    ],
    actions: ["/api/menus", "/api/menus/{menuId}"],
    menus: input.menus,
    selectedMenu,
    status: input.status,
    message: input.message,
    permissionMessage:
      input.status === "permission" ? "시스템관리자 권한이 필요합니다." : null,
  };
}

export type AdminCodeGroupsViewModelInput = {
  path: string;
  codeGroups: AdminCodeGroup[];
  selectedGroupId: string | null;
  status: ScreenStatus;
  message?: string;
};

export function createAdminCodeGroupsViewModel(
  input: AdminCodeGroupsViewModelInput,
) {
  const selectedCodeGroup =
    input.codeGroups.find(
      (codeGroup) => codeGroup.groupId === input.selectedGroupId,
    ) ?? null;
  const encodedGroupId = selectedCodeGroup
    ? encodeURIComponent(selectedCodeGroup.groupId)
    : null;
  return {
    route: input.path,
    screenId: "SCR-CMN-CODE-GROUP",
    menuPath: "시스템 관리 > 공통코드 관리 > 코드그룹 관리",
    filters: ["그룹ID", "명칭", "관리부서", "사용여부"],
    columns: ["그룹ID", "명칭", "설명", "관리부서", "사용여부"],
    detailFields: [
      "group_id readonly on edit",
      "명칭",
      "설명",
      "관리부서",
      "사용여부",
    ],
    actions: [
      "/api/code-groups",
      "/api/code-groups/{groupId}",
      "/admin/code-details?groupId={groupId}",
    ],
    codeGroups: input.codeGroups,
    selectedCodeGroup,
    detailNavigationUrl: encodedGroupId
      ? `/admin/code-details?groupId=${encodedGroupId}`
      : null,
    status: input.status,
    message: input.message,
    permissionMessage:
      input.status === "permission" ? "시스템관리자 권한이 필요합니다." : null,
  };
}

export type AdminCodeDetailsViewModelInput = {
  path: string;
  codeDetails: AdminCodeDetail[];
  selectedCodeKey: string | null;
  status: ScreenStatus;
  message?: string;
};

function codeDetailKey(
  codeDetail: Pick<AdminCodeDetail, "groupId" | "codeValue">,
) {
  return `${codeDetail.groupId}::${codeDetail.codeValue}`;
}

export function createAdminCodeDetailsViewModel(
  input: AdminCodeDetailsViewModelInput,
) {
  const selectedCodeDetail =
    input.codeDetails.find(
      (codeDetail) => codeDetailKey(codeDetail) === input.selectedCodeKey,
    ) ?? null;
  return {
    route: input.path,
    screenId: "SCR-CMN-CODE-DETAIL",
    menuPath: "시스템 관리 > 공통코드 관리 > 상세코드 관리",
    filters: ["그룹ID", "코드값/코드명", "사용여부", "유효일자"],
    columns: [
      "그룹ID",
      "코드값",
      "코드명",
      "상위코드",
      "정렬순서",
      "추가속성",
      "사용여부",
      "유효기간",
    ],
    detailFields: [
      "group_id readonly on edit",
      "code_value readonly on edit",
      "코드명",
      "상위코드",
      "정렬순서",
      "추가속성",
      "사용여부",
      "유효 시작일",
      "유효 종료일",
    ],
    actions: [
      "/api/code-details",
      "/api/code-details/{groupId}/{codeValue}",
      "/admin/code-groups",
    ],
    codeDetails: input.codeDetails,
    selectedCodeDetail,
    status: input.status,
    message: input.message,
    permissionMessage:
      input.status === "permission" ? "시스템관리자 권한이 필요합니다." : null,
  };
}

const emptyFilters: SearchFilters = {
  employeeNo: "",
  name: "",
  departmentCode: "",
  rankName: "",
  employmentStatus: "",
  roleCode: "",
  systemUseYn: "",
};

const roleOptions = [
  "R01",
  "R02",
  "R03",
  "R04",
  "R05",
  "R06",
  "R07",
  "R08",
  "R09",
];

const orgTypeOptions = [
  { value: "", label: "전체" },
  { value: "UNIVERSITY", label: "대학" },
  { value: "GRADUATE_SCHOOL", label: "대학원" },
  { value: "COLLEGE", label: "단과대학" },
  { value: "DEPARTMENT", label: "학과" },
  { value: "OFFICE", label: "부서" },
];

function buildUserQuery(filters: SearchFilters) {
  const params = new URLSearchParams();
  Object.entries(filters).forEach(([key, value]) => {
    if (value.trim() !== "") {
      params.set(key, value.trim());
    }
  });
  const query = params.toString();
  return query ? `/api/users?${query}` : "/api/users";
}

function buildOrganizationQuery(filters: OrganizationFilters) {
  const params = new URLSearchParams();
  if (filters.orgCode.trim() !== "") {
    params.set("orgCode", filters.orgCode.trim());
  }
  if (filters.orgType.trim() !== "") {
    params.set("orgType", filters.orgType.trim());
  }
  const query = params.toString();
  return query ? `/api/organizations?${query}` : "/api/organizations";
}

function buildRoleQuery(filters: RoleFilters) {
  const params = new URLSearchParams();
  if (filters.roleCode.trim() !== "") {
    params.set("roleCode", filters.roleCode.trim());
  }
  if (filters.roleName.trim() !== "") {
    params.set("roleName", filters.roleName.trim());
  }
  if (filters.useYn.trim() !== "") {
    params.set("useYn", filters.useYn.trim());
  }
  const query = params.toString();
  return query ? `/api/roles?${query}` : "/api/roles";
}

function buildUserRoleQuery(filters: UserRoleFilters) {
  const params = new URLSearchParams();
  if (filters.employeeNo.trim() !== "") {
    params.set("employeeNo", filters.employeeNo.trim());
  }
  if (filters.name.trim() !== "") {
    params.set("name", filters.name.trim());
  }
  if (filters.roleCode.trim() !== "") {
    params.set("roleCode", filters.roleCode.trim());
  }
  if (filters.validOn.trim() !== "") {
    params.set("validOn", filters.validOn.trim());
  }
  if (filters.assignmentType.trim() !== "") {
    params.set("assignmentType", filters.assignmentType.trim());
  }
  const query = params.toString();
  return query ? `/api/user-roles?${query}` : "/api/user-roles";
}

function buildMenuPermissionQuery(filters: MenuPermissionFilters) {
  const params = new URLSearchParams();
  params.set("targetType", filters.targetType);
  params.set("targetId", filters.targetId.trim());
  return `/api/menu-permissions?${params.toString()}`;
}

function buildMenuInformationQuery(filters: MenuInformationFilters) {
  const params = new URLSearchParams();
  if (filters.menuName.trim() !== "") {
    params.set("menuName", filters.menuName.trim());
  }
  if (filters.screenId.trim() !== "") {
    params.set("screenId", filters.screenId.trim());
  }
  if (filters.url.trim() !== "") {
    params.set("url", filters.url.trim());
  }
  if (filters.businessType.trim() !== "") {
    params.set("businessType", filters.businessType.trim());
  }
  const query = params.toString();
  return query ? `/api/menus?${query}` : "/api/menus";
}

function buildCodeGroupQuery(filters: CodeGroupFilters) {
  const params = new URLSearchParams();
  if (filters.groupId.trim() !== "") {
    params.set("groupId", filters.groupId.trim());
  }
  if (filters.groupName.trim() !== "") {
    params.set("groupName", filters.groupName.trim());
  }
  if (filters.managingDepartment.trim() !== "") {
    params.set("managingDepartment", filters.managingDepartment.trim());
  }
  if (filters.useYn.trim() !== "") {
    params.set("useYn", filters.useYn.trim());
  }
  const query = params.toString();
  return query ? `/api/code-groups?${query}` : "/api/code-groups";
}

function buildCodeDetailQuery(filters: CodeDetailFilters) {
  const params = new URLSearchParams();
  if (filters.groupId.trim() !== "") {
    params.set("groupId", filters.groupId.trim());
  }
  if (filters.filter.trim() !== "") {
    params.set("filter", filters.filter.trim());
  }
  if (filters.useYn.trim() !== "") {
    params.set("useYn", filters.useYn.trim());
  }
  if (filters.validOn.trim() !== "") {
    params.set("validOn", filters.validOn.trim());
  }
  const query = params.toString();
  return query ? `/api/code-details?${query}` : "/api/code-details";
}

async function requestJson<T>(
  path: string,
  init?: RequestInit,
): Promise<ApiResponse<T>> {
  const headers = new Headers(init?.headers);
  headers.set("Content-Type", "application/json");
  const response = await fetch(path, {
    ...init,
    credentials: "include",
    headers,
  });
  const body = (await response.json()) as ApiResponse<T> | ApiErrorEnvelope;
  if (!response.ok || !body.success) {
    const fieldErrors =
      !body.success && body.error?.fieldErrors?.length
        ? ` (${body.error.fieldErrors.map((fieldError) => `${fieldError.field}: ${fieldError.message}`).join(", ")})`
        : "";
    const message =
      !body.success && body.error?.message
        ? `${body.error.message}${fieldErrors}`
        : "요청 처리 중 오류가 발생했습니다.";
    throw new Error(message);
  }
  return body as ApiResponse<T>;
}

function AdminUsersScreen() {
  const [filters, setFilters] = useState<SearchFilters>(emptyFilters);
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [selectedUserId, setSelectedUserId] = useState<string | null>(null);
  const [status, setStatus] = useState<ScreenStatus>("loading");
  const [message, setMessage] = useState("");
  const [usageValue, setUsageValue] = useState<"Y" | "N">("Y");
  const [roleForm, setRoleForm] = useState<RoleForm>({
    roleCodes: [],
    assignmentType: "MANUAL",
    approverUserId: "",
    effectiveStartDate: "",
    effectiveEndDate: "",
  });

  const viewModel = useMemo(
    () =>
      createAdminUsersViewModel({
        path: "/admin/users",
        users,
        selectedUserId,
        status,
        message,
      }),
    [message, selectedUserId, status, users],
  );

  const selectedUser = viewModel.selectedUser;

  async function loadUsers(nextFilters = filters, successMessage = "") {
    setStatus("loading");
    setMessage("");
    try {
      const response = await requestJson<UserSearchResponse>(
        buildUserQuery(nextFilters),
      );
      setUsers(response.data.items);
      setStatus(response.data.items.length === 0 ? "empty" : "success");
      setMessage(successMessage);
      if (response.data.items.length > 0) {
        const nextSelected =
          response.data.items.find((user) => user.userId === selectedUserId) ??
          response.data.items[0];
        selectUser(nextSelected);
      } else {
        setSelectedUserId(null);
      }
    } catch (error) {
      const errorMessage =
        error instanceof Error
          ? error.message
          : "요청 처리 중 오류가 발생했습니다.";
      setStatus(errorMessage.includes("권한") ? "permission" : "error");
      setMessage(errorMessage);
    }
  }

  function selectUser(user: AdminUser) {
    setSelectedUserId(user.userId);
    setUsageValue(user.systemUseYn);
    setRoleForm((current) => ({
      ...current,
      roleCodes: user.roles,
    }));
  }

  useEffect(() => {
    void loadUsers(emptyFilters);
  }, []);

  async function handleSearch(event: FormEvent) {
    event.preventDefault();
    await loadUsers(filters);
  }

  async function saveUsage() {
    if (!selectedUser) {
      setStatus("empty");
      setMessage("사용자를 먼저 선택해 주세요.");
      return;
    }
    try {
      await requestJson<AdminUser>(
        `/api/users/${encodeURIComponent(selectedUser.userId)}/usage`,
        {
          method: "PATCH",
          body: JSON.stringify({ systemUseYn: usageValue }),
        },
      );
      await loadUsers(filters, "사용여부가 저장되었습니다.");
    } catch (error) {
      setStatus("error");
      setMessage(
        error instanceof Error
          ? error.message
          : "사용여부 저장에 실패했습니다.",
      );
    }
  }

  async function saveRoles() {
    if (!selectedUser) {
      setStatus("empty");
      setMessage("사용자를 먼저 선택해 주세요.");
      return;
    }
    if (
      roleForm.roleCodes.length === 0 ||
      roleForm.approverUserId.trim() === "" ||
      roleForm.effectiveStartDate === ""
    ) {
      setStatus("error");
      setMessage("업무 역할, 승인자, 유효 시작일은 필수입니다.");
      return;
    }
    try {
      await requestJson<AdminUser>(
        `/api/users/${encodeURIComponent(selectedUser.userId)}/roles`,
        {
          method: "PUT",
          body: JSON.stringify({
            roleCodes: roleForm.roleCodes,
            assignmentType: roleForm.assignmentType,
            approverUserId: roleForm.approverUserId,
            effectiveStartDate: roleForm.effectiveStartDate,
            effectiveEndDate: roleForm.effectiveEndDate || null,
          }),
        },
      );
      await loadUsers(filters, "업무 역할이 저장되었습니다.");
    } catch (error) {
      setStatus("error");
      setMessage(
        error instanceof Error
          ? error.message
          : "업무 역할 저장에 실패했습니다.",
      );
    }
  }

  function toggleRole(roleCode: string) {
    setRoleForm((current) => ({
      ...current,
      roleCodes: current.roleCodes.includes(roleCode)
        ? current.roleCodes.filter((value) => value !== roleCode)
        : [...current.roleCodes, roleCode],
    }));
  }

  if (window.location.pathname !== "/admin/users") {
    return (
      <main className="min-h-screen bg-slate-50 p-8 text-slate-900">
        <section className="mx-auto max-w-4xl rounded-2xl border border-amber-200 bg-amber-50 p-8">
          <p className="text-sm font-semibold text-amber-700">permission</p>
          <h1 className="mt-2 text-2xl font-bold">권한 없음</h1>
          <p className="mt-3 text-amber-900">시스템관리자 권한이 필요합니다.</p>
        </section>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-slate-100 text-slate-900">
      <div className="grid min-h-screen grid-cols-[280px_1fr]">
        <aside className="border-r border-slate-200 bg-slate-950 p-6 text-white">
          <p className="text-xs font-semibold uppercase tracking-widest text-blue-300">
            KNUE
          </p>
          <h1 className="mt-2 text-xl font-bold">교수업적평가시스템</h1>
          <nav className="mt-8 space-y-4 text-sm">
            <div>
              <p className="font-semibold text-slate-300">시스템 관리</p>
              <a
                className="mt-2 block rounded-lg bg-blue-600 px-3 py-2 font-semibold"
                href="/admin/users"
              >
                사용자 관리
              </a>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/organizations"
              >
                조직 관리
              </a>
            </div>
          </nav>
        </aside>
        <section className="p-8">
          <header className="mb-6 flex items-center justify-between">
            <div>
              <p className="text-sm text-slate-500">{viewModel.menuPath}</p>
              <h2 className="mt-1 text-3xl font-bold">사용자 관리</h2>
            </div>
            <span className="rounded-full bg-slate-900 px-4 py-2 text-sm font-semibold text-white">
              SCR-CMN-USER
            </span>
          </header>

          <form
            className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"
            onSubmit={handleSearch}
          >
            <div className="grid grid-cols-7 gap-3">
              {viewModel.filters.map((label) => {
                const keyMap: Record<string, keyof SearchFilters> = {
                  교번: "employeeNo",
                  성명: "name",
                  소속: "departmentCode",
                  직급: "rankName",
                  재직상태: "employmentStatus",
                  역할: "roleCode",
                  사용여부: "systemUseYn",
                };
                const key = keyMap[label];
                return (
                  <label
                    className="text-sm font-medium text-slate-700"
                    key={label}
                  >
                    {label}
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      value={filters[key]}
                      onChange={(event) =>
                        setFilters((current) => ({
                          ...current,
                          [key]: event.target.value,
                        }))
                      }
                    />
                  </label>
                );
              })}
            </div>
            <button
              className="mt-4 rounded-lg bg-blue-600 px-5 py-2 font-semibold text-white"
              type="submit"
            >
              검색
            </button>
          </form>

          <div className="mt-4 min-h-8 text-sm font-medium">
            {status === "loading" && (
              <p className="text-blue-700">
                loading: 사용자 목록을 조회하는 중입니다.
              </p>
            )}
            {status === "empty" && (
              <p className="text-slate-600">
                empty: 조건에 맞는 데이터가 없습니다.
              </p>
            )}
            {status === "error" && (
              <p className="text-red-700">error: {message}</p>
            )}
            {status === "permission" && (
              <p className="text-amber-700">
                permission: 시스템관리자 권한이 필요합니다.
              </p>
            )}
            {status === "success" && message && (
              <p className="text-green-700">success: {message}</p>
            )}
          </div>

          <div className="mt-4 grid grid-cols-[1.5fr_0.8fr] gap-5">
            <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
              <table className="w-full border-collapse text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                  <tr>
                    {viewModel.columns.map((column) => (
                      <th className="px-3 py-3" key={column}>
                        {column}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {users.map((user) => (
                    <tr
                      className={`cursor-pointer border-t border-slate-100 ${selectedUserId === user.userId ? "bg-blue-50" : "hover:bg-slate-50"}`}
                      key={user.userId}
                      onClick={() => selectUser(user)}
                    >
                      <td className="px-3 py-3 font-semibold">{user.userId}</td>
                      <td className="px-3 py-3">{user.name}</td>
                      <td className="px-3 py-3">
                        {user.departmentName ?? user.departmentCode ?? "-"}
                      </td>
                      <td className="px-3 py-3">{user.rankName ?? "-"}</td>
                      <td className="px-3 py-3">
                        {user.employmentStatus ?? "-"}
                      </td>
                      <td className="px-3 py-3">
                        {user.roles.join(", ") || "-"}
                      </td>
                      <td className="px-3 py-3">{user.systemUseYn}</td>
                      <td className="px-3 py-3">{user.positionName ?? "-"}</td>
                      <td className="px-3 py-3">
                        {user.retirementDate ?? "-"}
                      </td>
                      <td className="px-3 py-3">{user.lastSyncedAt ?? "-"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </section>

            <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
              <h3 className="text-lg font-bold">상세/편집</h3>
              {selectedUser ? (
                <div className="mt-4 space-y-4">
                  <div className="rounded-xl bg-slate-50 p-4 text-sm">
                    <p>
                      <b>교번</b> {selectedUser.userId}
                    </p>
                    <p>
                      <b>성명</b> {selectedUser.name}
                    </p>
                    <p>
                      <b>KORUS 원천</b> 직급·퇴직일자·최종 동기화일시는 조회
                      전용입니다.
                    </p>
                  </div>
                  <label className="block text-sm font-medium">
                    시스템 사용여부
                    <select
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      value={usageValue}
                      onChange={(event) =>
                        setUsageValue(event.target.value as "Y" | "N")
                      }
                    >
                      <option value="Y">Y</option>
                      <option value="N">N</option>
                    </select>
                  </label>
                  <button
                    className="w-full rounded-lg bg-slate-900 px-4 py-2 font-semibold text-white"
                    type="button"
                    onClick={() => void saveUsage()}
                  >
                    사용여부 저장
                  </button>

                  <fieldset className="space-y-2">
                    <legend className="text-sm font-semibold">업무 역할</legend>
                    <div className="grid grid-cols-3 gap-2">
                      {roleOptions.map((roleCode) => (
                        <label
                          className="rounded-lg border border-slate-200 px-2 py-1 text-sm"
                          key={roleCode}
                        >
                          <input
                            className="mr-1"
                            type="checkbox"
                            checked={roleForm.roleCodes.includes(roleCode)}
                            onChange={() => toggleRole(roleCode)}
                          />
                          {roleCode}
                        </label>
                      ))}
                    </div>
                  </fieldset>
                  <div className="grid grid-cols-2 gap-3">
                    <label className="text-sm font-medium">
                      유효 시작일
                      <input
                        className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                        type="date"
                        value={roleForm.effectiveStartDate}
                        onChange={(event) =>
                          setRoleForm((current) => ({
                            ...current,
                            effectiveStartDate: event.target.value,
                          }))
                        }
                      />
                    </label>
                    <label className="text-sm font-medium">
                      종료일
                      <input
                        className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                        type="date"
                        value={roleForm.effectiveEndDate}
                        onChange={(event) =>
                          setRoleForm((current) => ({
                            ...current,
                            effectiveEndDate: event.target.value,
                          }))
                        }
                      />
                    </label>
                  </div>
                  <label className="block text-sm font-medium">
                    승인자
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      value={roleForm.approverUserId}
                      onChange={(event) =>
                        setRoleForm((current) => ({
                          ...current,
                          approverUserId: event.target.value,
                        }))
                      }
                    />
                  </label>
                  <button
                    className="w-full rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white"
                    type="button"
                    onClick={() => void saveRoles()}
                  >
                    업무 역할 저장
                  </button>
                  <button
                    className="w-full rounded-lg border border-slate-300 px-4 py-2 font-semibold"
                    type="button"
                    onClick={() => setSelectedUserId(null)}
                  >
                    취소
                  </button>
                </div>
              ) : (
                <p className="mt-4 text-sm text-slate-600">
                  목록에서 사용자를 선택해 주세요.
                </p>
              )}
            </section>
          </div>
        </section>
      </div>
    </main>
  );
}

function AdminOrganizationsScreen() {
  const [filters, setFilters] = useState<OrganizationFilters>({
    orgCode: "",
    orgType: "",
  });
  const [organizations, setOrganizations] = useState<AdminOrganization[]>([]);
  const [selectedOrgCode, setSelectedOrgCode] = useState<string | null>(null);
  const [status, setStatus] = useState<ScreenStatus>("loading");
  const [message, setMessage] = useState("");
  const [relationshipForm, setRelationshipForm] =
    useState<OrganizationRelationshipForm>({
      parentOrgCode: "",
      effectiveStartDate: "",
      effectiveEndDate: "",
      reason: "",
    });

  const viewModel = useMemo(
    () =>
      createAdminOrganizationsViewModel({
        path: "/admin/organizations",
        organizations,
        selectedOrgCode,
        status,
        message,
      }),
    [message, organizations, selectedOrgCode, status],
  );

  const selectedOrganization = viewModel.selectedOrganization;

  async function loadOrganizations(nextFilters = filters, successMessage = "") {
    setStatus("loading");
    setMessage("");
    try {
      const response = await requestJson<OrganizationSearchResponse>(
        buildOrganizationQuery(nextFilters),
      );
      setOrganizations(response.data.items);
      setStatus(response.data.items.length === 0 ? "empty" : "success");
      setMessage(successMessage);
      if (response.data.items.length > 0) {
        selectOrganization(
          response.data.items.find(
            (organization) => organization.orgCode === selectedOrgCode,
          ) ?? response.data.items[0],
        );
      } else {
        setSelectedOrgCode(null);
      }
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : "조직 조회에 실패했습니다.";
      setStatus(errorMessage.includes("권한") ? "permission" : "error");
      setMessage(errorMessage);
    }
  }

  function selectOrganization(organization: AdminOrganization) {
    setSelectedOrgCode(organization.orgCode);
    setRelationshipForm({
      parentOrgCode:
        organization.currentRelationship?.parentOrgCode ??
        organization.parentOrgCode ??
        "",
      effectiveStartDate:
        organization.currentRelationship?.effectiveStartDate ?? "",
      effectiveEndDate:
        organization.currentRelationship?.effectiveEndDate ?? "",
      reason: organization.currentRelationship?.reason ?? "",
    });
  }

  useEffect(() => {
    void loadOrganizations({ orgCode: "", orgType: "" });
  }, []);

  async function handleOrganizationSearch(event: FormEvent) {
    event.preventDefault();
    await loadOrganizations(filters);
  }

  async function saveOrganizationRelationship() {
    if (!selectedOrganization) {
      setStatus("empty");
      setMessage("조직을 먼저 선택해 주세요.");
      return;
    }
    if (relationshipForm.effectiveStartDate === "") {
      setStatus("error");
      setMessage("적용 시작일은 필수입니다.");
      return;
    }
    try {
      await requestJson<AdminOrganization>(
        `/api/organizations/${encodeURIComponent(selectedOrganization.orgCode)}/relationships`,
        {
          method: "PUT",
          body: JSON.stringify({
            parentOrgCode: relationshipForm.parentOrgCode || null,
            effectiveStartDate: relationshipForm.effectiveStartDate,
            effectiveEndDate: relationshipForm.effectiveEndDate || null,
            reason: relationshipForm.reason || null,
          }),
        },
      );
      await loadOrganizations(filters, "관계/기간이 저장되었습니다.");
    } catch (error) {
      setStatus("error");
      setMessage(
        error instanceof Error
          ? error.message
          : "관계/기간 저장에 실패했습니다.",
      );
    }
  }

  return (
    <main className="min-h-screen bg-slate-100 text-slate-900">
      <div className="grid min-h-screen grid-cols-[280px_1fr]">
        <aside className="border-r border-slate-200 bg-slate-950 p-6 text-white">
          <p className="text-xs font-semibold uppercase tracking-widest text-blue-300">
            KNUE
          </p>
          <h1 className="mt-2 text-xl font-bold">교수업적평가시스템</h1>
          <nav className="mt-8 space-y-4 text-sm">
            <div>
              <p className="font-semibold text-slate-300">시스템 관리</p>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/users"
              >
                사용자 관리
              </a>
              <a
                className="mt-2 block rounded-lg bg-blue-600 px-3 py-2 font-semibold"
                href="/admin/organizations"
              >
                조직 관리
              </a>
            </div>
          </nav>
        </aside>
        <section className="p-8">
          <header className="mb-6 flex items-center justify-between">
            <div>
              <p className="text-sm text-slate-500">{viewModel.menuPath}</p>
              <h2 className="mt-1 text-3xl font-bold">조직 관리</h2>
            </div>
            <span className="rounded-full bg-slate-900 px-4 py-2 text-sm font-semibold text-white">
              SCR-CMN-ORG
            </span>
          </header>

          <form
            className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"
            onSubmit={handleOrganizationSearch}
          >
            <div className="grid grid-cols-3 gap-3">
              <label className="text-sm font-medium text-slate-700">
                조직코드
                <input
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={filters.orgCode}
                  onChange={(event) =>
                    setFilters((current) => ({
                      ...current,
                      orgCode: event.target.value,
                    }))
                  }
                />
              </label>
              <label className="text-sm font-medium text-slate-700">
                조직구분
                <select
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={filters.orgType}
                  onChange={(event) =>
                    setFilters((current) => ({
                      ...current,
                      orgType: event.target.value,
                    }))
                  }
                >
                  {orgTypeOptions.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>
              <div className="flex items-end">
                <button
                  className="rounded-lg bg-blue-600 px-5 py-2 font-semibold text-white"
                  type="submit"
                >
                  검색
                </button>
              </div>
            </div>
          </form>

          <div className="mt-4 min-h-8 text-sm font-medium">
            {status === "loading" && (
              <p className="text-blue-700">
                loading: 조직 계층을 조회하는 중입니다.
              </p>
            )}
            {status === "empty" && (
              <p className="text-slate-600">
                empty: 조건에 맞는 데이터가 없습니다.
              </p>
            )}
            {status === "error" && (
              <p className="text-red-700">error: {message}</p>
            )}
            {status === "permission" && (
              <p className="text-amber-700">
                permission: 시스템관리자 권한이 필요합니다.
              </p>
            )}
            {status === "success" && message && (
              <p className="text-green-700">success: {message}</p>
            )}
          </div>

          <div className="mt-4 grid grid-cols-[1fr_1fr] gap-5">
            <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
              <h3 className="text-lg font-bold">좌측: 조직 계층 트리</h3>
              <div className="mt-4 space-y-2 text-sm">
                {viewModel.treeRows.map((organization) => (
                  <button
                    className={`block w-full rounded-lg border px-3 py-2 text-left ${selectedOrgCode === organization.orgCode ? "border-blue-500 bg-blue-50" : "border-slate-200 hover:bg-slate-50"}`}
                    key={organization.orgCode}
                    style={{ paddingLeft: `${12 + organization.depth * 24}px` }}
                    type="button"
                    onClick={() => selectOrganization(organization)}
                  >
                    <span className="font-semibold">
                      {organization.orgName}
                    </span>
                    <span className="ml-2 text-slate-500">
                      {organization.orgCode} · {organization.orgType}
                    </span>
                  </button>
                ))}
              </div>
            </section>

            <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
              <h3 className="text-lg font-bold">우측: 선택 조직 원천 정보</h3>
              {selectedOrganization ? (
                <div className="mt-4 space-y-4">
                  <div className="rounded-xl bg-slate-50 p-4 text-sm">
                    <p>
                      <b>조직코드</b> {selectedOrganization.orgCode}
                    </p>
                    <p>
                      <b>조직명</b> {selectedOrganization.orgName}
                    </p>
                    <p>
                      <b>조직구분</b> {selectedOrganization.orgType}
                    </p>
                    <p>
                      <b>상위조직</b>{" "}
                      {selectedOrganization.parentOrgName ??
                        selectedOrganization.parentOrgCode ??
                        "-"}
                    </p>
                    <p>
                      <b>하위조직</b>{" "}
                      {selectedOrganization.childOrgCodes.join(", ") || "-"}
                    </p>
                    <p>
                      <b>조회 전용</b> KORUS 원천 조직 정보는 직접 수정하지 않고
                      아래 로컬 보정 이력만 저장합니다.
                    </p>
                  </div>
                  <div className="grid grid-cols-2 gap-3">
                    <label className="text-sm font-medium">
                      조직코드 readonly
                      <input
                        className="mt-1 w-full rounded-lg border border-slate-300 bg-slate-100 px-3 py-2"
                        readOnly
                        value={selectedOrganization.orgCode}
                      />
                    </label>
                    <label className="text-sm font-medium">
                      상위조직
                      <input
                        className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                        value={relationshipForm.parentOrgCode}
                        onChange={(event) =>
                          setRelationshipForm((current) => ({
                            ...current,
                            parentOrgCode: event.target.value,
                          }))
                        }
                      />
                    </label>
                    <label className="text-sm font-medium">
                      적용 시작일
                      <input
                        className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                        type="date"
                        value={relationshipForm.effectiveStartDate}
                        onChange={(event) =>
                          setRelationshipForm((current) => ({
                            ...current,
                            effectiveStartDate: event.target.value,
                          }))
                        }
                      />
                    </label>
                    <label className="text-sm font-medium">
                      종료일
                      <input
                        className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                        type="date"
                        value={relationshipForm.effectiveEndDate}
                        onChange={(event) =>
                          setRelationshipForm((current) => ({
                            ...current,
                            effectiveEndDate: event.target.value,
                          }))
                        }
                      />
                    </label>
                  </div>
                  <label className="block text-sm font-medium">
                    사유
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      value={relationshipForm.reason}
                      onChange={(event) =>
                        setRelationshipForm((current) => ({
                          ...current,
                          reason: event.target.value,
                        }))
                      }
                    />
                  </label>
                  <button
                    className="w-full rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white"
                    type="button"
                    onClick={() => void saveOrganizationRelationship()}
                  >
                    관계/기간 저장
                  </button>
                  <button
                    className="w-full rounded-lg border border-slate-300 px-4 py-2 font-semibold"
                    type="button"
                    onClick={() => setSelectedOrgCode(null)}
                  >
                    취소
                  </button>
                </div>
              ) : (
                <p className="mt-4 text-sm text-slate-600">
                  조직 트리에서 조직을 선택해 주세요.
                </p>
              )}
            </section>
          </div>
        </section>
      </div>
    </main>
  );
}

function AdminRolesScreen() {
  const [filters, setFilters] = useState<RoleFilters>({
    roleCode: "",
    roleName: "",
    useYn: "",
  });
  const [roles, setRoles] = useState<AdminRole[]>([]);
  const [selectedRoleCode, setSelectedRoleCode] = useState<string | null>(null);
  const [status, setStatus] = useState<ScreenStatus>("loading");
  const [message, setMessage] = useState("");
  const [detailForm, setDetailForm] = useState<RoleDetailForm>({
    roleName: "",
    purpose: "",
    grantCriteria: "",
    dataScopeDefault: "",
    useYn: "Y",
  });

  const viewModel = useMemo(
    () =>
      createAdminRolesViewModel({
        path: "/admin/roles",
        roles,
        selectedRoleCode,
        status,
        message,
      }),
    [message, roles, selectedRoleCode, status],
  );

  const selectedRole = viewModel.selectedRole;

  async function loadRoles(nextFilters = filters, successMessage = "") {
    setStatus("loading");
    setMessage("");
    try {
      const response = await requestJson<RoleSearchResponse>(
        buildRoleQuery(nextFilters),
      );
      setRoles(response.data.items);
      setStatus(response.data.items.length === 0 ? "empty" : "success");
      setMessage(successMessage);
      if (response.data.items.length > 0) {
        selectRole(
          response.data.items.find(
            (role) => role.roleCode === selectedRoleCode,
          ) ?? response.data.items[0],
        );
      } else {
        setSelectedRoleCode(null);
      }
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : "역할 조회에 실패했습니다.";
      setStatus(errorMessage.includes("권한") ? "permission" : "error");
      setMessage(errorMessage);
    }
  }

  function selectRole(role: AdminRole) {
    setSelectedRoleCode(role.roleCode);
    setDetailForm({
      roleName: role.roleName,
      purpose: role.purpose,
      grantCriteria: role.grantCriteria ?? "",
      dataScopeDefault: role.dataScopeDefault ?? "",
      useYn: role.useYn,
    });
  }

  useEffect(() => {
    void loadRoles({ roleCode: "", roleName: "", useYn: "" });
  }, []);

  async function handleRoleSearch(event: FormEvent) {
    event.preventDefault();
    await loadRoles(filters);
  }

  async function saveRole() {
    if (!selectedRole) {
      setStatus("empty");
      setMessage("역할을 먼저 선택해 주세요.");
      return;
    }
    if (detailForm.roleName.trim() === "" || detailForm.purpose.trim() === "") {
      setStatus("error");
      setMessage("역할명과 목적은 필수입니다.");
      return;
    }
    try {
      await requestJson<AdminRole>(
        `/api/roles/${encodeURIComponent(selectedRole.roleCode)}`,
        {
          method: "PUT",
          body: JSON.stringify({
            roleCode: selectedRole.roleCode,
            roleName: detailForm.roleName,
            purpose: detailForm.purpose,
            grantCriteria: detailForm.grantCriteria || null,
            dataScopeDefault: detailForm.dataScopeDefault || null,
            useYn: detailForm.useYn,
          }),
        },
      );
      await loadRoles(filters, "역할 기준이 저장되었습니다.");
    } catch (error) {
      setStatus("error");
      setMessage(
        error instanceof Error
          ? error.message
          : "역할 기준 저장에 실패했습니다.",
      );
    }
  }

  return (
    <main className="min-h-screen bg-slate-100 text-slate-900">
      <div className="grid min-h-screen grid-cols-[280px_1fr]">
        <aside className="border-r border-slate-200 bg-slate-950 p-6 text-white">
          <p className="text-xs font-semibold uppercase tracking-widest text-blue-300">
            KNUE
          </p>
          <h1 className="mt-2 text-xl font-bold">교수업적평가시스템</h1>
          <nav className="mt-8 space-y-4 text-sm">
            <div>
              <p className="font-semibold text-slate-300">시스템 관리</p>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/users"
              >
                사용자 관리
              </a>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/organizations"
              >
                조직 관리
              </a>
              <p className="mt-4 font-semibold text-slate-300">
                역할·권한 관리
              </p>
              <a
                className="mt-2 block rounded-lg bg-blue-600 px-3 py-2 font-semibold"
                href="/admin/roles"
              >
                역할 관리
              </a>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/user-roles"
              >
                사용자 역할 관리
              </a>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/menu-permissions"
              >
                메뉴 권한 관리
              </a>
            </div>
          </nav>
        </aside>
        <section className="p-8">
          <header className="mb-6 flex items-center justify-between">
            <div>
              <p className="text-sm text-slate-500">{viewModel.menuPath}</p>
              <h2 className="mt-1 text-3xl font-bold">역할 관리</h2>
            </div>
            <span className="rounded-full bg-slate-900 px-4 py-2 text-sm font-semibold text-white">
              SCR-CMN-ROLE
            </span>
          </header>

          <form
            className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"
            onSubmit={handleRoleSearch}
          >
            <div className="grid grid-cols-4 gap-3">
              <label className="text-sm font-medium text-slate-700">
                역할코드
                <select
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={filters.roleCode}
                  onChange={(event) =>
                    setFilters((current) => ({
                      ...current,
                      roleCode: event.target.value,
                    }))
                  }
                >
                  <option value="">전체</option>
                  {roleOptions.map((roleCode) => (
                    <option key={roleCode} value={roleCode}>
                      {roleCode}
                    </option>
                  ))}
                </select>
              </label>
              <label className="text-sm font-medium text-slate-700">
                역할명
                <input
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={filters.roleName}
                  onChange={(event) =>
                    setFilters((current) => ({
                      ...current,
                      roleName: event.target.value,
                    }))
                  }
                />
              </label>
              <label className="text-sm font-medium text-slate-700">
                사용여부
                <select
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={filters.useYn}
                  onChange={(event) =>
                    setFilters((current) => ({
                      ...current,
                      useYn: event.target.value,
                    }))
                  }
                >
                  <option value="">전체</option>
                  <option value="Y">Y</option>
                  <option value="N">N</option>
                </select>
              </label>
              <div className="flex items-end">
                <button
                  className="rounded-lg bg-blue-600 px-5 py-2 font-semibold text-white"
                  type="submit"
                >
                  검색
                </button>
              </div>
            </div>
          </form>

          <div className="mt-4 min-h-8 text-sm font-medium">
            {status === "loading" && (
              <p className="text-blue-700">
                loading: 역할 목록을 조회하는 중입니다.
              </p>
            )}
            {status === "empty" && (
              <p className="text-slate-600">
                empty: 조건에 맞는 데이터가 없습니다.
              </p>
            )}
            {status === "error" && (
              <p className="text-red-700">error: {message}</p>
            )}
            {status === "permission" && (
              <p className="text-amber-700">
                permission: 시스템관리자 권한이 필요합니다.
              </p>
            )}
            {status === "success" && message && (
              <p className="text-green-700">success: {message}</p>
            )}
          </div>

          <div className="mt-4 grid grid-cols-[1.1fr_0.9fr] gap-5">
            <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
              <table className="w-full border-collapse text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                  <tr>
                    {viewModel.columns.map((column) => (
                      <th className="px-4 py-3" key={column}>
                        {column}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {roles.map((role) => (
                    <tr
                      className={`cursor-pointer border-t border-slate-100 ${selectedRoleCode === role.roleCode ? "bg-blue-50" : "hover:bg-slate-50"}`}
                      key={role.roleCode}
                      onClick={() => selectRole(role)}
                    >
                      <td className="px-4 py-3 font-semibold">
                        {role.roleCode}
                      </td>
                      <td className="px-4 py-3">{role.roleName}</td>
                      <td className="px-4 py-3">{role.purpose}</td>
                      <td className="px-4 py-3">{role.useYn}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </section>

            <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
              <h3 className="text-lg font-bold">선택 역할 상세 패널</h3>
              {selectedRole ? (
                <div className="mt-4 space-y-4">
                  <label className="block text-sm font-medium">
                    role_code readonly
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 bg-slate-100 px-3 py-2"
                      readOnly
                      value={selectedRole.roleCode}
                    />
                  </label>
                  <label className="block text-sm font-medium">
                    역할명
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      value={detailForm.roleName}
                      onChange={(event) =>
                        setDetailForm((current) => ({
                          ...current,
                          roleName: event.target.value,
                        }))
                      }
                    />
                  </label>
                  <label className="block text-sm font-medium">
                    목적
                    <textarea
                      className="mt-1 min-h-24 w-full rounded-lg border border-slate-300 px-3 py-2"
                      value={detailForm.purpose}
                      onChange={(event) =>
                        setDetailForm((current) => ({
                          ...current,
                          purpose: event.target.value,
                        }))
                      }
                    />
                  </label>
                  <div className="grid grid-cols-2 gap-3">
                    <label className="text-sm font-medium">
                      부여 기준
                      <input
                        className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                        value={detailForm.grantCriteria}
                        onChange={(event) =>
                          setDetailForm((current) => ({
                            ...current,
                            grantCriteria: event.target.value,
                          }))
                        }
                      />
                    </label>
                    <label className="text-sm font-medium">
                      데이터 범위 기본값
                      <input
                        className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                        value={detailForm.dataScopeDefault}
                        onChange={(event) =>
                          setDetailForm((current) => ({
                            ...current,
                            dataScopeDefault: event.target.value,
                          }))
                        }
                      />
                    </label>
                  </div>
                  <label className="block text-sm font-medium">
                    사용여부
                    <select
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      value={detailForm.useYn}
                      onChange={(event) =>
                        setDetailForm((current) => ({
                          ...current,
                          useYn: event.target.value as "Y" | "N",
                        }))
                      }
                    >
                      <option value="Y">Y</option>
                      <option value="N">N</option>
                    </select>
                  </label>
                  <p className="rounded-xl bg-slate-50 p-3 text-sm text-slate-600">
                    검증: role_code 변경 입력은 거부하고 역할명·목적·부여
                    기준·데이터 범위 기본값만 저장 대상입니다.
                  </p>
                  <button
                    className="w-full rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white"
                    type="button"
                    onClick={() => void saveRole()}
                  >
                    저장
                  </button>
                  <button
                    className="w-full rounded-lg border border-slate-300 px-4 py-2 font-semibold"
                    type="button"
                    onClick={() => setSelectedRoleCode(null)}
                  >
                    취소
                  </button>
                </div>
              ) : (
                <p className="mt-4 text-sm text-slate-600">
                  역할 목록에서 역할을 선택해 주세요.
                </p>
              )}
            </section>
          </div>
        </section>
      </div>
    </main>
  );
}

function AdminUserRolesScreen() {
  const [filters, setFilters] = useState<UserRoleFilters>({
    employeeNo: "",
    name: "",
    roleCode: "",
    validOn: "",
    assignmentType: "",
  });
  const [userRoles, setUserRoles] = useState<AdminUserRole[]>([]);
  const [selectedUserRoleId, setSelectedUserRoleId] = useState<string | null>(
    null,
  );
  const [status, setStatus] = useState<ScreenStatus>("loading");
  const [message, setMessage] = useState("");
  const [form, setForm] = useState<UserRoleForm>({
    userId: "",
    roleCode: "R01",
    assignmentType: "MANUAL",
    approverUserId: "",
    effectiveStartDate: "",
    effectiveEndDate: "",
  });

  const viewModel = useMemo(
    () =>
      createAdminUserRolesViewModel({
        path: "/admin/user-roles",
        userRoles,
        selectedUserRoleId,
        status,
        message,
      }),
    [message, selectedUserRoleId, status, userRoles],
  );

  const selectedUserRole = viewModel.selectedUserRole;

  async function loadUserRoles(nextFilters = filters, successMessage = "") {
    setStatus("loading");
    setMessage("");
    try {
      const response = await requestJson<UserRoleSearchResponse>(
        buildUserRoleQuery(nextFilters),
      );
      setUserRoles(response.data.items);
      setStatus(response.data.items.length === 0 ? "empty" : "success");
      setMessage(successMessage);
      if (response.data.items.length > 0) {
        selectUserRole(
          response.data.items.find(
            (userRole) => userRole.userRoleId === selectedUserRoleId,
          ) ?? response.data.items[0],
        );
      } else {
        setSelectedUserRoleId(null);
      }
    } catch (error) {
      const errorMessage =
        error instanceof Error
          ? error.message
          : "사용자 역할 조회에 실패했습니다.";
      setStatus(errorMessage.includes("권한") ? "permission" : "error");
      setMessage(errorMessage);
    }
  }

  function selectUserRole(userRole: AdminUserRole) {
    setSelectedUserRoleId(userRole.userRoleId);
    setForm({
      userId: userRole.userId,
      roleCode: userRole.roleCode,
      assignmentType: userRole.assignmentType,
      approverUserId: userRole.approverUserId,
      effectiveStartDate: userRole.effectiveStartDate,
      effectiveEndDate: userRole.effectiveEndDate ?? "",
    });
  }

  useEffect(() => {
    void loadUserRoles({
      employeeNo: "",
      name: "",
      roleCode: "",
      validOn: "",
      assignmentType: "",
    });
  }, []);

  async function handleUserRoleSearch(event: FormEvent) {
    event.preventDefault();
    await loadUserRoles(filters);
  }

  function validateUserRoleForm() {
    if (
      form.userId.trim() === "" ||
      form.roleCode.trim() === "" ||
      form.approverUserId.trim() === "" ||
      form.effectiveStartDate === ""
    ) {
      setStatus("error");
      setMessage("사용자, 역할, 승인자, 유효 시작일은 필수입니다.");
      return false;
    }
    if (
      form.effectiveEndDate !== "" &&
      form.effectiveStartDate > form.effectiveEndDate
    ) {
      setStatus("error");
      setMessage("유효 시작일은 종료일보다 늦을 수 없습니다.");
      return false;
    }
    return true;
  }

  async function saveUserRole() {
    if (!validateUserRoleForm()) {
      return;
    }
    const body = JSON.stringify({
      userId: form.userId,
      roleCode: form.roleCode,
      assignmentType: form.assignmentType,
      approverUserId: form.approverUserId,
      effectiveStartDate: form.effectiveStartDate,
      effectiveEndDate: form.effectiveEndDate || null,
    });
    try {
      if (selectedUserRole) {
        await requestJson<AdminUserRole>(
          `/api/user-roles/${encodeURIComponent(selectedUserRole.userRoleId)}`,
          {
            method: "PATCH",
            body: JSON.stringify({
              roleCode: form.roleCode,
              assignmentType: form.assignmentType,
              approverUserId: form.approverUserId,
              effectiveStartDate: form.effectiveStartDate,
              effectiveEndDate: form.effectiveEndDate || null,
            }),
          },
        );
      } else {
        await requestJson<AdminUserRole>("/api/user-roles", {
          method: "POST",
          body,
        });
      }
      await loadUserRoles(filters, "사용자 역할이 저장되었습니다.");
    } catch (error) {
      setStatus("error");
      setMessage(
        error instanceof Error
          ? error.message
          : "사용자 역할 저장에 실패했습니다.",
      );
    }
  }

  async function revokeUserRole() {
    if (!selectedUserRole) {
      setStatus("empty");
      setMessage("회수할 사용자 역할을 먼저 선택해 주세요.");
      return;
    }
    if (!window.confirm("선택한 사용자 역할을 회수하시겠습니까?")) {
      return;
    }
    try {
      await requestJson<AdminUserRole>(
        `/api/user-roles/${encodeURIComponent(selectedUserRole.userRoleId)}`,
        { method: "DELETE" },
      );
      await loadUserRoles(filters, "사용자 역할이 회수되었습니다.");
    } catch (error) {
      setStatus("error");
      setMessage(
        error instanceof Error
          ? error.message
          : "사용자 역할 회수에 실패했습니다.",
      );
    }
  }

  function resetForm() {
    setSelectedUserRoleId(null);
    setForm({
      userId: "",
      roleCode: "R01",
      assignmentType: "MANUAL",
      approverUserId: "",
      effectiveStartDate: "",
      effectiveEndDate: "",
    });
  }

  return (
    <main className="min-h-screen bg-slate-100 text-slate-900">
      <div className="grid min-h-screen grid-cols-[280px_1fr]">
        <aside className="border-r border-slate-200 bg-slate-950 p-6 text-white">
          <p className="text-xs font-semibold uppercase tracking-widest text-blue-300">
            KNUE
          </p>
          <h1 className="mt-2 text-xl font-bold">교수업적평가시스템</h1>
          <nav className="mt-8 space-y-4 text-sm">
            <div>
              <p className="font-semibold text-slate-300">시스템 관리</p>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/users"
              >
                사용자 관리
              </a>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/organizations"
              >
                조직 관리
              </a>
              <p className="mt-4 font-semibold text-slate-300">
                역할·권한 관리
              </p>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/roles"
              >
                역할 관리
              </a>
              <a
                className="mt-2 block rounded-lg bg-blue-600 px-3 py-2 font-semibold"
                href="/admin/user-roles"
              >
                사용자 역할 관리
              </a>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/menu-permissions"
              >
                메뉴 권한 관리
              </a>
            </div>
          </nav>
        </aside>
        <section className="p-8">
          <header className="mb-6 flex items-center justify-between">
            <div>
              <p className="text-sm text-slate-500">{viewModel.menuPath}</p>
              <h2 className="mt-1 text-3xl font-bold">사용자 역할 관리</h2>
            </div>
            <span className="rounded-full bg-slate-900 px-4 py-2 text-sm font-semibold text-white">
              SCR-CMN-USER-ROLE
            </span>
          </header>

          <form
            className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"
            onSubmit={handleUserRoleSearch}
          >
            <div className="grid grid-cols-6 gap-3">
              <label className="text-sm font-medium text-slate-700">
                교번
                <input
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={filters.employeeNo}
                  onChange={(event) =>
                    setFilters((current) => ({
                      ...current,
                      employeeNo: event.target.value,
                    }))
                  }
                />
              </label>
              <label className="text-sm font-medium text-slate-700">
                성명
                <input
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={filters.name}
                  onChange={(event) =>
                    setFilters((current) => ({
                      ...current,
                      name: event.target.value,
                    }))
                  }
                />
              </label>
              <label className="text-sm font-medium text-slate-700">
                역할
                <select
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={filters.roleCode}
                  onChange={(event) =>
                    setFilters((current) => ({
                      ...current,
                      roleCode: event.target.value,
                    }))
                  }
                >
                  <option value="">전체</option>
                  {roleOptions.map((roleCode) => (
                    <option key={roleCode} value={roleCode}>
                      {roleCode}
                    </option>
                  ))}
                </select>
              </label>
              <label className="text-sm font-medium text-slate-700">
                유효기간
                <input
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                  type="date"
                  value={filters.validOn}
                  onChange={(event) =>
                    setFilters((current) => ({
                      ...current,
                      validOn: event.target.value,
                    }))
                  }
                />
              </label>
              <label className="text-sm font-medium text-slate-700">
                부여구분
                <select
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={filters.assignmentType}
                  onChange={(event) =>
                    setFilters((current) => ({
                      ...current,
                      assignmentType: event.target.value,
                    }))
                  }
                >
                  <option value="">전체</option>
                  <option value="POSITION_BASED">보직기반</option>
                  <option value="MANUAL">수동</option>
                </select>
              </label>
              <div className="flex items-end">
                <button
                  className="rounded-lg bg-blue-600 px-5 py-2 font-semibold text-white"
                  type="submit"
                >
                  검색
                </button>
              </div>
            </div>
          </form>

          <div className="mt-4 min-h-8 text-sm font-medium">
            {status === "loading" && (
              <p className="text-blue-700">
                loading: 사용자 역할 목록을 조회하는 중입니다.
              </p>
            )}
            {status === "empty" && (
              <p className="text-slate-600">
                empty: 조건에 맞는 데이터가 없습니다.
              </p>
            )}
            {status === "error" && (
              <p className="text-red-700">error: {message}</p>
            )}
            {status === "permission" && (
              <p className="text-amber-700">
                permission: 시스템관리자 권한이 필요합니다.
              </p>
            )}
            {status === "success" && message && (
              <p className="text-green-700">success: {message}</p>
            )}
          </div>

          <div className="mt-4 grid grid-cols-[1.2fr_0.8fr] gap-5">
            <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
              <table className="w-full border-collapse text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                  <tr>
                    {viewModel.columns.map((column) => (
                      <th className="px-3 py-3" key={column}>
                        {column}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {userRoles.map((userRole) => (
                    <tr
                      className={`cursor-pointer border-t border-slate-100 ${selectedUserRoleId === userRole.userRoleId ? "bg-blue-50" : "hover:bg-slate-50"}`}
                      key={userRole.userRoleId}
                      onClick={() => selectUserRole(userRole)}
                    >
                      <td className="px-3 py-3 font-semibold">
                        {userRole.userId}
                      </td>
                      <td className="px-3 py-3">{userRole.userName}</td>
                      <td className="px-3 py-3">
                        {userRole.roleCode} · {userRole.roleName}
                      </td>
                      <td className="px-3 py-3">
                        {userRole.effectiveStartDate}
                      </td>
                      <td className="px-3 py-3">
                        {userRole.effectiveEndDate ?? "-"}
                      </td>
                      <td className="px-3 py-3">
                        {userRole.approverName ?? userRole.approverUserId}
                      </td>
                      <td className="px-3 py-3">{userRole.assignmentType}</td>
                      <td className="px-3 py-3">{userRole.status}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </section>

            <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
              <h3 className="text-lg font-bold">사용자 역할 상세/등록</h3>
              <div className="mt-4 space-y-4">
                <label className="block text-sm font-medium">
                  교번
                  <input
                    className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                    value={form.userId}
                    onChange={(event) =>
                      setForm((current) => ({
                        ...current,
                        userId: event.target.value,
                      }))
                    }
                  />
                </label>
                <label className="block text-sm font-medium">
                  역할
                  <select
                    className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                    value={form.roleCode}
                    onChange={(event) =>
                      setForm((current) => ({
                        ...current,
                        roleCode: event.target.value,
                      }))
                    }
                  >
                    {roleOptions.map((roleCode) => (
                      <option key={roleCode} value={roleCode}>
                        {roleCode}
                      </option>
                    ))}
                  </select>
                </label>
                <div className="grid grid-cols-2 gap-3">
                  <label className="text-sm font-medium">
                    시작일
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      type="date"
                      value={form.effectiveStartDate}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          effectiveStartDate: event.target.value,
                        }))
                      }
                    />
                  </label>
                  <label className="text-sm font-medium">
                    종료일
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      type="date"
                      value={form.effectiveEndDate}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          effectiveEndDate: event.target.value,
                        }))
                      }
                    />
                  </label>
                </div>
                <label className="block text-sm font-medium">
                  승인자
                  <input
                    className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                    value={form.approverUserId}
                    onChange={(event) =>
                      setForm((current) => ({
                        ...current,
                        approverUserId: event.target.value,
                      }))
                    }
                  />
                </label>
                <label className="block text-sm font-medium">
                  부여구분
                  <select
                    className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                    value={form.assignmentType}
                    onChange={(event) =>
                      setForm((current) => ({
                        ...current,
                        assignmentType: event.target.value as
                          | "POSITION_BASED"
                          | "MANUAL",
                      }))
                    }
                  >
                    <option value="POSITION_BASED">보직기반</option>
                    <option value="MANUAL">수동</option>
                  </select>
                </label>
                <p className="rounded-xl bg-slate-50 p-3 text-sm text-slate-600">
                  회수는 물리삭제하지 않고 REVOKED 상태로 기록합니다.
                </p>
                <button
                  className="w-full rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white"
                  type="button"
                  onClick={() => void saveUserRole()}
                >
                  {selectedUserRole ? "변경 저장" : "역할 부여"}
                </button>
                <button
                  className="w-full rounded-lg bg-red-600 px-4 py-2 font-semibold text-white"
                  type="button"
                  onClick={() => void revokeUserRole()}
                  disabled={!selectedUserRole}
                >
                  역할 회수
                </button>
                <button
                  className="w-full rounded-lg border border-slate-300 px-4 py-2 font-semibold"
                  type="button"
                  onClick={resetForm}
                >
                  신규/취소
                </button>
              </div>
            </section>
          </div>
        </section>
      </div>
    </main>
  );
}

function AdminMenuPermissionsScreen() {
  const [filters, setFilters] = useState<MenuPermissionFilters>({
    targetType: "ROLE",
    targetId: "R09",
  });
  const [permissions, setPermissions] = useState<AdminMenuPermission[]>([]);
  const [preview, setPreview] = useState<PermissionPreview>({
    visibleMenuUrls: [],
    writableMenuUrls: [],
    deniedMenuUrls: [],
  });
  const [status, setStatus] = useState<ScreenStatus>("loading");
  const [message, setMessage] = useState("");

  const viewModel = useMemo(
    () =>
      createAdminMenuPermissionsViewModel({
        path: "/admin/menu-permissions",
        permissions,
        preview,
        status,
        message,
      }),
    [message, permissions, preview, status],
  );

  async function loadMenuPermissions(
    nextFilters = filters,
    successMessage = "",
  ) {
    if (nextFilters.targetId.trim() === "") {
      setStatus("error");
      setMessage("대상 ID는 필수입니다.");
      return;
    }
    setStatus("loading");
    setMessage("");
    try {
      const response = await requestJson<MenuPermissionSearchResponse>(
        buildMenuPermissionQuery(nextFilters),
      );
      setPermissions(response.data.items);
      setPreview(response.data.preview);
      setStatus(response.data.items.length === 0 ? "empty" : "success");
      setMessage(successMessage);
    } catch (error) {
      const errorMessage =
        error instanceof Error
          ? error.message
          : "메뉴 권한 조회에 실패했습니다.";
      setStatus(errorMessage.includes("권한") ? "permission" : "error");
      setMessage(errorMessage);
    }
  }

  useEffect(() => {
    void loadMenuPermissions({ targetType: "ROLE", targetId: "R09" });
  }, []);

  async function handleMenuPermissionSearch(event: FormEvent) {
    event.preventDefault();
    await loadMenuPermissions(filters);
  }

  function updatePermission(
    menuId: string,
    patch: Partial<
      Pick<AdminMenuPermission, "canRead" | "canWrite" | "effect">
    >,
  ) {
    setPermissions((current) =>
      current.map((permission) => {
        if (permission.menuId !== menuId) {
          return permission;
        }
        const next = { ...permission, ...patch };
        if (patch.effect === "DENY") {
          next.canRead = false;
          next.canWrite = false;
        }
        if (patch.canRead === false) {
          next.canWrite = false;
        }
        return next;
      }),
    );
  }

  async function saveMenuPermissions() {
    if (permissions.length === 0) {
      setStatus("empty");
      setMessage("저장할 메뉴 권한이 없습니다.");
      return;
    }
    try {
      const response = await requestJson<MenuPermissionSearchResponse>(
        "/api/menu-permissions",
        {
          method: "PUT",
          body: JSON.stringify({
            targetType: filters.targetType,
            targetId: filters.targetId,
            permissions: permissions.map((permission) => ({
              menuId: permission.menuId,
              canRead: permission.canRead,
              canWrite: permission.canWrite,
              effect: permission.effect,
            })),
          }),
        },
      );
      setPermissions(response.data.items);
      setPreview(response.data.preview);
      setStatus("success");
      setMessage("메뉴 권한이 저장되었습니다.");
    } catch (error) {
      setStatus("error");
      setMessage(
        error instanceof Error
          ? error.message
          : "메뉴 권한 저장에 실패했습니다.",
      );
    }
  }

  return (
    <main className="min-h-screen bg-slate-100 text-slate-900">
      <div className="grid min-h-screen grid-cols-[280px_1fr]">
        <aside className="border-r border-slate-200 bg-slate-950 p-6 text-white">
          <p className="text-xs font-semibold uppercase tracking-widest text-blue-300">
            KNUE
          </p>
          <h1 className="mt-2 text-xl font-bold">교수업적평가시스템</h1>
          <nav className="mt-8 space-y-4 text-sm">
            <div>
              <p className="font-semibold text-slate-300">시스템 관리</p>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/users"
              >
                사용자 관리
              </a>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/organizations"
              >
                조직 관리
              </a>
              <p className="mt-4 font-semibold text-slate-300">
                역할·권한 관리
              </p>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/roles"
              >
                역할 관리
              </a>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/user-roles"
              >
                사용자 역할 관리
              </a>
              <a
                className="mt-2 block rounded-lg bg-blue-600 px-3 py-2 font-semibold"
                href="/admin/menu-permissions"
              >
                메뉴 권한 관리
              </a>
            </div>
          </nav>
        </aside>
        <section className="p-8">
          <header className="mb-6 flex items-center justify-between">
            <div>
              <p className="text-sm text-slate-500">{viewModel.menuPath}</p>
              <h2 className="mt-1 text-3xl font-bold">메뉴 권한 관리</h2>
            </div>
            <span className="rounded-full bg-slate-900 px-4 py-2 text-sm font-semibold text-white">
              SCR-CMN-MENU-PERM
            </span>
          </header>

          <form
            className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"
            onSubmit={handleMenuPermissionSearch}
          >
            <div className="grid grid-cols-4 gap-3">
              <label className="text-sm font-medium text-slate-700">
                대상유형
                <select
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={filters.targetType}
                  onChange={(event) =>
                    setFilters((current) => ({
                      ...current,
                      targetType: event.target
                        .value as MenuPermissionFilters["targetType"],
                    }))
                  }
                >
                  <option value="ROLE">역할</option>
                  <option value="ORGANIZATION">조직</option>
                  <option value="USER">사용자</option>
                </select>
              </label>
              <label className="text-sm font-medium text-slate-700">
                대상ID
                <input
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={filters.targetId}
                  onChange={(event) =>
                    setFilters((current) => ({
                      ...current,
                      targetId: event.target.value,
                    }))
                  }
                />
              </label>
              <div className="flex items-end">
                <button
                  className="rounded-lg bg-blue-600 px-5 py-2 font-semibold text-white"
                  type="submit"
                >
                  조회
                </button>
              </div>
            </div>
          </form>

          <div className="mt-4 min-h-8 text-sm font-medium">
            {status === "loading" && (
              <p className="text-blue-700">
                loading: 메뉴 권한 matrix를 조회하는 중입니다.
              </p>
            )}
            {status === "empty" && (
              <p className="text-slate-600">
                empty: 조건에 맞는 데이터가 없습니다.
              </p>
            )}
            {status === "error" && (
              <p className="text-red-700">error: {message}</p>
            )}
            {status === "permission" && (
              <p className="text-amber-700">
                permission: 시스템관리자 권한이 필요합니다.
              </p>
            )}
            {status === "success" && message && (
              <p className="text-green-700">success: {message}</p>
            )}
          </div>

          <div className="mt-4 grid grid-cols-[1.2fr_0.8fr] gap-5">
            <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
              <table className="w-full border-collapse text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                  <tr>
                    {viewModel.columns.map((column) => (
                      <th className="px-3 py-3" key={column}>
                        {column}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {permissions.map((permission) => (
                    <tr
                      className="border-t border-slate-100"
                      key={permission.menuId}
                    >
                      <td className="px-3 py-3 font-semibold">
                        {permission.topMenuName}
                      </td>
                      <td className="px-3 py-3">
                        {permission.middleMenuName ?? "-"}
                      </td>
                      <td className="px-3 py-3">
                        {permission.screenMenuName ??
                          permission.screenId ??
                          "-"}
                      </td>
                      <td className="px-3 py-3">
                        <input
                          aria-label={`${permission.menuId} 조회허용`}
                          type="checkbox"
                          checked={permission.canRead}
                          onChange={(event) =>
                            updatePermission(permission.menuId, {
                              canRead: event.target.checked,
                            })
                          }
                        />
                      </td>
                      <td className="px-3 py-3">
                        <input
                          aria-label={`${permission.menuId} 변경허용`}
                          type="checkbox"
                          checked={permission.canWrite}
                          disabled={
                            !permission.canRead || permission.effect === "DENY"
                          }
                          onChange={(event) =>
                            updatePermission(permission.menuId, {
                              canWrite: event.target.checked,
                            })
                          }
                        />
                      </td>
                      <td className="px-3 py-3">
                        <select
                          className="rounded-lg border border-slate-300 px-2 py-1"
                          value={permission.effect}
                          onChange={(event) =>
                            updatePermission(permission.menuId, {
                              effect: event.target
                                .value as AdminMenuPermission["effect"],
                            })
                          }
                        >
                          <option value="ALLOW">ALLOW</option>
                          <option value="DENY">DENY</option>
                        </select>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </section>

            <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
              <h3 className="text-lg font-bold">서버 접근통제 미리보기</h3>
              <p className="mt-2 text-sm text-slate-600">
                저장한 조회허용·변경허용·effect 값이 메뉴 노출과 서버 접근통제에
                같은 방향으로 적용됩니다.
              </p>
              <div className="mt-4 space-y-4 text-sm">
                <div className="rounded-xl bg-green-50 p-3">
                  <p className="font-semibold text-green-800">노출 메뉴</p>
                  <p>{preview.visibleMenuUrls.join(", ") || "-"}</p>
                </div>
                <div className="rounded-xl bg-blue-50 p-3">
                  <p className="font-semibold text-blue-800">변경 가능 메뉴</p>
                  <p>{preview.writableMenuUrls.join(", ") || "-"}</p>
                </div>
                <div className="rounded-xl bg-red-50 p-3">
                  <p className="font-semibold text-red-800">차단 메뉴</p>
                  <p>{preview.deniedMenuUrls.join(", ") || "-"}</p>
                </div>
              </div>
              <button
                className="mt-5 w-full rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white"
                type="button"
                onClick={() => void saveMenuPermissions()}
              >
                권한 저장
              </button>
              <button
                className="mt-2 w-full rounded-lg border border-slate-300 px-4 py-2 font-semibold"
                type="button"
                onClick={() => void loadMenuPermissions(filters)}
              >
                취소
              </button>
            </section>
          </div>
        </section>
      </div>
    </main>
  );
}

function AdminMenuTreeScreen() {
  const [menus, setMenus] = useState<AdminMenuTree[]>([]);
  const [selectedMenuId, setSelectedMenuId] = useState<string | null>(null);
  const [status, setStatus] = useState<ScreenStatus>("loading");
  const [message, setMessage] = useState("");
  const [form, setForm] = useState<MenuTreeForm>({
    parentMenuId: "",
    displayOrder: "1",
  });

  const viewModel = useMemo(
    () =>
      createAdminMenuTreeViewModel({
        path: "/admin/menus/tree",
        menus,
        selectedMenuId,
        status,
        message,
      }),
    [menus, selectedMenuId, status, message],
  );

  const selectedMenu = viewModel.selectedMenu;

  async function loadMenuTree(
    nextSelectedMenuId = selectedMenuId,
    successMessage = "",
  ) {
    setStatus("loading");
    setMessage("");
    try {
      const query = nextSelectedMenuId
        ? `?selectedMenuId=${encodeURIComponent(nextSelectedMenuId)}`
        : "";
      const response = await requestJson<MenuTreeSearchResponse>(
        `/api/menus/tree${query}`,
      );
      setMenus(response.data.items);
      setStatus(response.data.items.length === 0 ? "empty" : "success");
      setMessage(successMessage);
      const nextSelected =
        response.data.items.find(
          (menu) => menu.menuId === nextSelectedMenuId,
        ) ??
        response.data.items[0] ??
        null;
      if (nextSelected) {
        selectMenu(nextSelected);
      } else {
        setSelectedMenuId(null);
      }
    } catch (error) {
      const errorMessage =
        error instanceof Error
          ? error.message
          : "메뉴 계층 조회에 실패했습니다.";
      setStatus(errorMessage.includes("권한") ? "permission" : "error");
      setMessage(errorMessage);
    }
  }

  function selectMenu(menu: AdminMenuTree) {
    setSelectedMenuId(menu.menuId);
    setForm({
      parentMenuId: menu.parentMenuId ?? "",
      displayOrder: String(menu.displayOrder),
    });
  }

  useEffect(() => {
    void loadMenuTree(null);
  }, []);

  async function saveParent() {
    if (!selectedMenu) {
      setStatus("empty");
      setMessage("메뉴 노드를 먼저 선택해 주세요.");
      return;
    }
    try {
      await requestJson<MenuTreeSearchResponse>(
        `/api/menus/${encodeURIComponent(selectedMenu.menuId)}/parent`,
        {
          method: "PUT",
          body: JSON.stringify({
            parentMenuId: form.parentMenuId.trim() || null,
          }),
        },
      );
      await loadMenuTree(selectedMenu.menuId, "부모메뉴가 저장되었습니다.");
    } catch (error) {
      setStatus("error");
      setMessage(
        error instanceof Error
          ? error.message
          : "부모메뉴 저장에 실패했습니다.",
      );
    }
  }

  async function saveOrder() {
    if (!selectedMenu) {
      setStatus("empty");
      setMessage("메뉴 노드를 먼저 선택해 주세요.");
      return;
    }
    const displayOrder = Number(form.displayOrder);
    if (!Number.isInteger(displayOrder) || displayOrder < 1) {
      setStatus("error");
      setMessage("표시순서는 1 이상의 숫자여야 합니다.");
      return;
    }
    try {
      await requestJson<MenuTreeSearchResponse>(
        `/api/menus/${encodeURIComponent(selectedMenu.menuId)}/order`,
        {
          method: "PUT",
          body: JSON.stringify({ displayOrder }),
        },
      );
      await loadMenuTree(selectedMenu.menuId, "표시순서가 저장되었습니다.");
    } catch (error) {
      setStatus("error");
      setMessage(
        error instanceof Error
          ? error.message
          : "표시순서 저장에 실패했습니다.",
      );
    }
  }

  const parentName = selectedMenu?.parentMenuId
    ? (menus.find((menu) => menu.menuId === selectedMenu.parentMenuId)
        ?.menuName ?? selectedMenu.parentMenuId)
    : "최상위";

  return (
    <main className="min-h-screen bg-slate-100 text-slate-900">
      <div className="grid min-h-screen grid-cols-[280px_1fr]">
        <aside className="border-r border-slate-200 bg-slate-950 p-6 text-white">
          <p className="text-xs font-semibold uppercase tracking-widest text-blue-300">
            KNUE
          </p>
          <h1 className="mt-2 text-xl font-bold">교수업적평가시스템</h1>
          <nav className="mt-8 space-y-4 text-sm">
            <div>
              <p className="font-semibold text-slate-300">시스템 관리</p>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/users"
              >
                사용자 관리
              </a>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/organizations"
              >
                조직 관리
              </a>
              <p className="mt-4 font-semibold text-slate-300">
                역할·권한 관리
              </p>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/roles"
              >
                역할 관리
              </a>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/user-roles"
              >
                사용자 역할 관리
              </a>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/menu-permissions"
              >
                메뉴 권한 관리
              </a>
              <p className="mt-4 font-semibold text-slate-300">메뉴 관리</p>
              <a
                className="mt-2 block rounded-lg bg-blue-600 px-3 py-2 font-semibold"
                href="/admin/menus/tree"
              >
                메뉴 구조 관리
              </a>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/menus"
              >
                메뉴 정보 관리
              </a>
            </div>
          </nav>
        </aside>
        <section className="p-8">
          <header className="mb-6 flex items-center justify-between">
            <div>
              <p className="text-sm text-slate-500">{viewModel.menuPath}</p>
              <h2 className="mt-1 text-3xl font-bold">메뉴 구조 관리</h2>
            </div>
            <span className="rounded-full bg-slate-900 px-4 py-2 text-sm font-semibold text-white">
              SCR-CMN-MENU-TREE
            </span>
          </header>

          <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
            <p className="text-sm text-slate-600">
              시스템 관리 메뉴 트리에서 대메뉴·중메뉴·소메뉴를 확인하고 선택
              노드의 부모메뉴와 표시순서를 저장합니다.
            </p>
            <button
              className="mt-4 rounded-lg bg-blue-600 px-5 py-2 font-semibold text-white"
              type="button"
              onClick={() => void loadMenuTree(selectedMenuId)}
            >
              계층 조회
            </button>
          </div>

          <div className="mt-4 min-h-8 text-sm font-medium">
            {status === "loading" && (
              <p className="text-blue-700">
                loading: 메뉴 계층을 조회하는 중입니다.
              </p>
            )}
            {status === "empty" && (
              <p className="text-slate-600">
                empty: 조건에 맞는 데이터가 없습니다.
              </p>
            )}
            {status === "error" && (
              <p className="text-red-700">error: {message}</p>
            )}
            {status === "permission" && (
              <p className="text-amber-700">
                permission: 시스템관리자 권한이 필요합니다.
              </p>
            )}
            {status === "success" && message && (
              <p className="text-green-700">success: {message}</p>
            )}
          </div>

          <div className="mt-4 grid grid-cols-[1fr_0.85fr] gap-5">
            <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
              <h3 className="text-lg font-bold">메뉴 계층 트리</h3>
              <div className="mt-4 space-y-2 text-sm">
                {menus.map((menu) => (
                  <button
                    className={`block w-full rounded-lg border px-3 py-2 text-left ${selectedMenuId === menu.menuId ? "border-blue-500 bg-blue-50" : "border-slate-200 hover:bg-slate-50"}`}
                    key={menu.menuId}
                    style={{ paddingLeft: `${12 + menu.depth * 24}px` }}
                    type="button"
                    onClick={() => selectMenu(menu)}
                  >
                    <span className="font-semibold">{menu.menuName}</span>
                    <span className="ml-2 text-slate-500">
                      {menu.menuId} · order {menu.displayOrder}
                    </span>
                    {menu.url && (
                      <span className="ml-2 text-blue-700">{menu.url}</span>
                    )}
                  </button>
                ))}
              </div>
            </section>

            <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
              <h3 className="text-lg font-bold">선택 노드 편집 영역</h3>
              {selectedMenu ? (
                <div className="mt-4 space-y-4">
                  <div className="rounded-xl bg-slate-50 p-4 text-sm">
                    <p>
                      <b>menu_id readonly</b> {selectedMenu.menuId}
                    </p>
                    <p>
                      <b>메뉴명</b> {selectedMenu.menuName}
                    </p>
                    <p>
                      <b>현재 부모메뉴</b> {parentName}
                    </p>
                    <p>
                      <b>자식 메뉴</b>{" "}
                      {selectedMenu.childMenuIds.join(", ") || "-"}
                    </p>
                  </div>
                  <label className="block text-sm font-medium">
                    부모메뉴
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      value={form.parentMenuId}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          parentMenuId: event.target.value,
                        }))
                      }
                    />
                  </label>
                  <label className="block text-sm font-medium">
                    표시순서
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      type="number"
                      min="1"
                      value={form.displayOrder}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          displayOrder: event.target.value,
                        }))
                      }
                    />
                  </label>
                  <p className="rounded-xl bg-slate-50 p-3 text-sm text-slate-600">
                    순환 구조는 서버에서 차단하며, 표시순서는 같은 부모 아래
                    노드 정렬에 사용됩니다.
                  </p>
                  <button
                    className="w-full rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white"
                    type="button"
                    onClick={() => void saveParent()}
                  >
                    부모 저장
                  </button>
                  <button
                    className="w-full rounded-lg bg-slate-900 px-4 py-2 font-semibold text-white"
                    type="button"
                    onClick={() => void saveOrder()}
                  >
                    순서 저장
                  </button>
                  <button
                    className="w-full rounded-lg border border-slate-300 px-4 py-2 font-semibold"
                    type="button"
                    onClick={() => selectedMenu && selectMenu(selectedMenu)}
                  >
                    취소
                  </button>
                </div>
              ) : (
                <p className="mt-4 text-sm text-slate-600">
                  메뉴 트리에서 노드를 선택해 주세요.
                </p>
              )}
            </section>
          </div>
        </section>
      </div>
    </main>
  );
}

function AdminMenusScreen() {
  const [filters, setFilters] = useState<MenuInformationFilters>({
    menuName: "",
    screenId: "",
    url: "",
    businessType: "",
  });
  const [menus, setMenus] = useState<AdminMenuInfo[]>([]);
  const [selectedMenuId, setSelectedMenuId] = useState<string | null>(null);
  const [status, setStatus] = useState<ScreenStatus>("loading");
  const [message, setMessage] = useState("");
  const [form, setForm] = useState<MenuInformationForm>({
    menuId: "",
    parentMenuId: "",
    menuName: "",
    screenId: "",
    url: "",
    icon: "",
    businessType: "SYSTEM",
    description: "",
    displayOrder: "1",
    useYn: "Y",
  });

  const viewModel = useMemo(
    () =>
      createAdminMenusViewModel({
        path: "/admin/menus",
        menus,
        selectedMenuId,
        status,
        message,
      }),
    [menus, selectedMenuId, status, message],
  );

  const selectedMenu = viewModel.selectedMenu;

  async function loadMenus(nextFilters = filters, successMessage = "") {
    setStatus("loading");
    setMessage("");
    try {
      const response = await requestJson<MenuInformationSearchResponse>(
        buildMenuInformationQuery(nextFilters),
      );
      setMenus(response.data.items);
      setStatus(response.data.items.length === 0 ? "empty" : "success");
      setMessage(successMessage);
      if (response.data.items.length > 0) {
        selectMenuInfo(
          response.data.items.find((menu) => menu.menuId === selectedMenuId) ??
            response.data.items[0],
        );
      } else {
        resetMenuInfoForm();
      }
    } catch (error) {
      const errorMessage =
        error instanceof Error
          ? error.message
          : "메뉴 실행정보 조회에 실패했습니다.";
      setStatus(errorMessage.includes("권한") ? "permission" : "error");
      setMessage(errorMessage);
    }
  }

  function selectMenuInfo(menu: AdminMenuInfo) {
    setSelectedMenuId(menu.menuId);
    setForm({
      menuId: menu.menuId,
      parentMenuId: menu.parentMenuId ?? "",
      menuName: menu.menuName,
      screenId: menu.screenId ?? "",
      url: menu.url ?? "",
      icon: menu.icon ?? "",
      businessType: menu.businessType ?? "",
      description: menu.description ?? "",
      displayOrder: String(menu.displayOrder),
      useYn: menu.useYn,
    });
  }

  function resetMenuInfoForm() {
    setSelectedMenuId(null);
    setForm({
      menuId: "",
      parentMenuId: "",
      menuName: "",
      screenId: "",
      url: "",
      icon: "",
      businessType: "SYSTEM",
      description: "",
      displayOrder: "1",
      useYn: "Y",
    });
  }

  useEffect(() => {
    void loadMenus({ menuName: "", screenId: "", url: "", businessType: "" });
  }, []);

  async function handleMenuInformationSearch(event: FormEvent) {
    event.preventDefault();
    await loadMenus(filters);
  }

  function validateMenuInformationForm() {
    const displayOrder = Number(form.displayOrder);
    if (form.menuId.trim() === "" || form.menuName.trim() === "") {
      setStatus("error");
      setMessage("menu_id와 메뉴명은 필수입니다.");
      return null;
    }
    if (!Number.isInteger(displayOrder) || displayOrder < 1) {
      setStatus("error");
      setMessage("표시순서는 1 이상의 숫자여야 합니다.");
      return null;
    }
    if ((form.screenId.trim() === "") !== (form.url.trim() === "")) {
      setStatus("error");
      setMessage("화면ID와 URL은 함께 입력해야 합니다.");
      return null;
    }
    if (form.url.trim() !== "" && !form.url.trim().startsWith("/admin/")) {
      setStatus("error");
      setMessage("URL은 /admin/ 경로로 시작해야 합니다.");
      return null;
    }
    return {
      parentMenuId: form.parentMenuId.trim() || null,
      menuName: form.menuName.trim(),
      screenId: form.screenId.trim() || null,
      url: form.url.trim() || null,
      icon: form.icon.trim() || null,
      businessType: form.businessType.trim() || null,
      description: form.description.trim() || null,
      displayOrder,
      useYn: form.useYn,
    };
  }

  async function saveMenuInformation() {
    const payload = validateMenuInformationForm();
    if (!payload) {
      return;
    }
    try {
      if (selectedMenu) {
        await requestJson<AdminMenuInfo>(
          `/api/menus/${encodeURIComponent(selectedMenu.menuId)}`,
          {
            method: "PUT",
            body: JSON.stringify(payload),
          },
        );
      } else {
        await requestJson<AdminMenuInfo>("/api/menus", {
          method: "POST",
          body: JSON.stringify({ menuId: form.menuId.trim(), ...payload }),
        });
      }
      await loadMenus(
        filters,
        selectedMenu
          ? "메뉴 실행정보가 저장되었습니다."
          : "메뉴 실행정보가 등록되었습니다.",
      );
    } catch (error) {
      setStatus("error");
      setMessage(
        error instanceof Error
          ? error.message
          : "메뉴 실행정보 저장에 실패했습니다.",
      );
    }
  }

  return (
    <main className="min-h-screen bg-slate-100 text-slate-900">
      <div className="grid min-h-screen grid-cols-[280px_1fr]">
        <aside className="border-r border-slate-200 bg-slate-950 p-6 text-white">
          <p className="text-xs font-semibold uppercase tracking-widest text-blue-300">
            KNUE
          </p>
          <h1 className="mt-2 text-xl font-bold">교수업적평가시스템</h1>
          <nav className="mt-8 space-y-4 text-sm">
            <div>
              <p className="font-semibold text-slate-300">시스템 관리</p>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/users"
              >
                사용자 관리
              </a>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/organizations"
              >
                조직 관리
              </a>
              <p className="mt-4 font-semibold text-slate-300">
                역할·권한 관리
              </p>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/roles"
              >
                역할 관리
              </a>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/user-roles"
              >
                사용자 역할 관리
              </a>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/menu-permissions"
              >
                메뉴 권한 관리
              </a>
              <p className="mt-4 font-semibold text-slate-300">메뉴 관리</p>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/menus/tree"
              >
                메뉴 구조 관리
              </a>
              <a
                className="mt-2 block rounded-lg bg-blue-600 px-3 py-2 font-semibold"
                href="/admin/menus"
              >
                메뉴 정보 관리
              </a>
            </div>
          </nav>
        </aside>
        <section className="p-8">
          <header className="mb-6 flex items-center justify-between">
            <div>
              <p className="text-sm text-slate-500">{viewModel.menuPath}</p>
              <h2 className="mt-1 text-3xl font-bold">메뉴 정보 관리</h2>
            </div>
            <span className="rounded-full bg-slate-900 px-4 py-2 text-sm font-semibold text-white">
              SCR-CMN-MENU-INFO
            </span>
          </header>

          <form
            className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"
            onSubmit={handleMenuInformationSearch}
          >
            <div className="grid grid-cols-5 gap-3">
              <label className="text-sm font-medium text-slate-700">
                메뉴명
                <input
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={filters.menuName}
                  onChange={(event) =>
                    setFilters((current) => ({
                      ...current,
                      menuName: event.target.value,
                    }))
                  }
                />
              </label>
              <label className="text-sm font-medium text-slate-700">
                화면ID
                <input
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={filters.screenId}
                  onChange={(event) =>
                    setFilters((current) => ({
                      ...current,
                      screenId: event.target.value,
                    }))
                  }
                />
              </label>
              <label className="text-sm font-medium text-slate-700">
                URL
                <input
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={filters.url}
                  onChange={(event) =>
                    setFilters((current) => ({
                      ...current,
                      url: event.target.value,
                    }))
                  }
                />
              </label>
              <label className="text-sm font-medium text-slate-700">
                업무구분
                <input
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={filters.businessType}
                  onChange={(event) =>
                    setFilters((current) => ({
                      ...current,
                      businessType: event.target.value,
                    }))
                  }
                />
              </label>
              <div className="flex items-end">
                <button
                  className="rounded-lg bg-blue-600 px-5 py-2 font-semibold text-white"
                  type="submit"
                >
                  검색
                </button>
              </div>
            </div>
          </form>

          <div className="mt-4 min-h-8 text-sm font-medium">
            {status === "loading" && (
              <p className="text-blue-700">
                loading: 메뉴 실행정보를 조회하는 중입니다.
              </p>
            )}
            {status === "empty" && (
              <p className="text-slate-600">
                empty: 조건에 맞는 데이터가 없습니다.
              </p>
            )}
            {status === "error" && (
              <p className="text-red-700">error: {message}</p>
            )}
            {status === "permission" && (
              <p className="text-amber-700">
                permission: 시스템관리자 권한이 필요합니다.
              </p>
            )}
            {status === "success" && message && (
              <p className="text-green-700">success: {message}</p>
            )}
          </div>

          <div className="mt-4 grid grid-cols-[1.15fr_0.85fr] gap-5">
            <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
              <table className="w-full border-collapse text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                  <tr>
                    {viewModel.columns.map((column) => (
                      <th className="px-3 py-3" key={column}>
                        {column}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {menus.map((menu) => (
                    <tr
                      className={`cursor-pointer border-t border-slate-100 ${selectedMenuId === menu.menuId ? "bg-blue-50" : "hover:bg-slate-50"}`}
                      key={menu.menuId}
                      onClick={() => selectMenuInfo(menu)}
                    >
                      <td className="px-3 py-3 font-semibold">
                        {menu.menuName}
                        <span className="ml-2 text-slate-500">
                          {menu.menuId}
                        </span>
                      </td>
                      <td className="px-3 py-3">{menu.screenId ?? "-"}</td>
                      <td className="px-3 py-3">{menu.url ?? "-"}</td>
                      <td className="px-3 py-3">{menu.icon ?? "-"}</td>
                      <td className="px-3 py-3">{menu.businessType ?? "-"}</td>
                      <td className="px-3 py-3">{menu.description ?? "-"}</td>
                      <td className="px-3 py-3">{menu.useYn}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </section>

            <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
              <h3 className="text-lg font-bold">메뉴 실행정보 상세/등록</h3>
              <div className="mt-4 space-y-4">
                <div className="grid grid-cols-2 gap-3">
                  <label className="text-sm font-medium">
                    menu_id
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      readOnly={Boolean(selectedMenu)}
                      value={form.menuId}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          menuId: event.target.value,
                        }))
                      }
                    />
                  </label>
                  <label className="text-sm font-medium">
                    부모메뉴
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      value={form.parentMenuId}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          parentMenuId: event.target.value,
                        }))
                      }
                    />
                  </label>
                </div>
                <label className="block text-sm font-medium">
                  메뉴명
                  <input
                    className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                    value={form.menuName}
                    onChange={(event) =>
                      setForm((current) => ({
                        ...current,
                        menuName: event.target.value,
                      }))
                    }
                  />
                </label>
                <div className="grid grid-cols-2 gap-3">
                  <label className="text-sm font-medium">
                    화면ID
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      value={form.screenId}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          screenId: event.target.value,
                        }))
                      }
                    />
                  </label>
                  <label className="text-sm font-medium">
                    URL
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      value={form.url}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          url: event.target.value,
                        }))
                      }
                    />
                  </label>
                  <label className="text-sm font-medium">
                    아이콘
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      value={form.icon}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          icon: event.target.value,
                        }))
                      }
                    />
                  </label>
                  <label className="text-sm font-medium">
                    업무구분
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      value={form.businessType}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          businessType: event.target.value,
                        }))
                      }
                    />
                  </label>
                  <label className="text-sm font-medium">
                    표시순서
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      min="1"
                      type="number"
                      value={form.displayOrder}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          displayOrder: event.target.value,
                        }))
                      }
                    />
                  </label>
                  <label className="text-sm font-medium">
                    사용여부
                    <select
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      value={form.useYn}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          useYn: event.target.value as "Y" | "N",
                        }))
                      }
                    >
                      <option value="Y">Y</option>
                      <option value="N">N</option>
                    </select>
                  </label>
                </div>
                <label className="block text-sm font-medium">
                  설명
                  <textarea
                    className="mt-1 min-h-24 w-full rounded-lg border border-slate-300 px-3 py-2"
                    value={form.description}
                    onChange={(event) =>
                      setForm((current) => ({
                        ...current,
                        description: event.target.value,
                      }))
                    }
                  />
                </label>
                <p className="rounded-xl bg-slate-50 p-3 text-sm text-slate-600">
                  화면ID와 URL 연결은 서버 저장 후 메뉴 권한과 화면 노출에
                  사용됩니다. 사용 중지 메뉴는 물리삭제하지 않고 N으로
                  관리합니다.
                </p>
                <button
                  className="w-full rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white"
                  type="button"
                  onClick={() => void saveMenuInformation()}
                >
                  {selectedMenu ? "저장" : "등록"}
                </button>
                <button
                  className="w-full rounded-lg border border-slate-300 px-4 py-2 font-semibold"
                  type="button"
                  onClick={resetMenuInfoForm}
                >
                  신규/취소
                </button>
              </div>
            </section>
          </div>
        </section>
      </div>
    </main>
  );
}

function AdminCodeGroupsScreen() {
  const [filters, setFilters] = useState<CodeGroupFilters>({
    groupId: "",
    groupName: "",
    managingDepartment: "",
    useYn: "",
  });
  const [codeGroups, setCodeGroups] = useState<AdminCodeGroup[]>([]);
  const [selectedGroupId, setSelectedGroupId] = useState<string | null>(null);
  const [status, setStatus] = useState<ScreenStatus>("loading");
  const [message, setMessage] = useState("");
  const [form, setForm] = useState<CodeGroupForm>({
    groupId: "",
    groupName: "",
    description: "",
    managingDepartment: "",
    useYn: "Y",
  });

  const viewModel = useMemo(
    () =>
      createAdminCodeGroupsViewModel({
        path: "/admin/code-groups",
        codeGroups,
        selectedGroupId,
        status,
        message,
      }),
    [codeGroups, selectedGroupId, status, message],
  );

  const selectedCodeGroup = viewModel.selectedCodeGroup;

  async function loadCodeGroups(nextFilters = filters, successMessage = "") {
    setStatus("loading");
    setMessage("");
    try {
      const response = await requestJson<CodeGroupSearchResponse>(
        buildCodeGroupQuery(nextFilters),
      );
      setCodeGroups(response.data.items);
      setStatus(response.data.items.length === 0 ? "empty" : "success");
      setMessage(successMessage);
      if (response.data.items.length > 0) {
        selectCodeGroup(
          response.data.items.find(
            (codeGroup) => codeGroup.groupId === selectedGroupId,
          ) ?? response.data.items[0],
        );
      } else {
        resetCodeGroupForm();
      }
    } catch (error) {
      const errorMessage =
        error instanceof Error
          ? error.message
          : "코드그룹 조회에 실패했습니다.";
      setStatus(errorMessage.includes("권한") ? "permission" : "error");
      setMessage(errorMessage);
    }
  }

  function selectCodeGroup(codeGroup: AdminCodeGroup) {
    setSelectedGroupId(codeGroup.groupId);
    setForm({
      groupId: codeGroup.groupId,
      groupName: codeGroup.groupName,
      description: codeGroup.description ?? "",
      managingDepartment: codeGroup.managingDepartment ?? "",
      useYn: codeGroup.useYn,
    });
  }

  function resetCodeGroupForm() {
    setSelectedGroupId(null);
    setForm({
      groupId: "",
      groupName: "",
      description: "",
      managingDepartment: "",
      useYn: "Y",
    });
  }

  useEffect(() => {
    void loadCodeGroups({
      groupId: "",
      groupName: "",
      managingDepartment: "",
      useYn: "",
    });
  }, []);

  async function handleCodeGroupSearch(event: FormEvent) {
    event.preventDefault();
    await loadCodeGroups(filters);
  }

  function validateCodeGroupForm() {
    if (form.groupId.trim() === "" || form.groupName.trim() === "") {
      setStatus("error");
      setMessage("그룹ID와 명칭은 필수입니다.");
      return null;
    }
    return {
      groupName: form.groupName.trim(),
      description: form.description.trim() || null,
      managingDepartment: form.managingDepartment.trim() || null,
      useYn: form.useYn,
    };
  }

  async function saveCodeGroup() {
    const payload = validateCodeGroupForm();
    if (!payload) {
      return;
    }
    try {
      if (selectedCodeGroup) {
        await requestJson<AdminCodeGroup>(
          `/api/code-groups/${encodeURIComponent(selectedCodeGroup.groupId)}`,
          {
            method: "PUT",
            body: JSON.stringify({
              groupId: selectedCodeGroup.groupId,
              ...payload,
            }),
          },
        );
      } else {
        await requestJson<AdminCodeGroup>("/api/code-groups", {
          method: "POST",
          body: JSON.stringify({ groupId: form.groupId.trim(), ...payload }),
        });
      }
      await loadCodeGroups(
        filters,
        selectedCodeGroup
          ? "코드그룹이 저장되었습니다."
          : "코드그룹이 등록되었습니다.",
      );
    } catch (error) {
      setStatus("error");
      setMessage(
        error instanceof Error
          ? error.message
          : "코드그룹 저장에 실패했습니다.",
      );
    }
  }

  function moveToCodeDetails() {
    if (!viewModel.detailNavigationUrl) {
      setStatus("empty");
      setMessage("상세코드로 이동할 코드그룹을 먼저 선택해 주세요.");
      return;
    }
    window.location.href = viewModel.detailNavigationUrl;
  }

  return (
    <main className="min-h-screen bg-slate-100 text-slate-900">
      <div className="grid min-h-screen grid-cols-[280px_1fr]">
        <aside className="border-r border-slate-200 bg-slate-950 p-6 text-white">
          <p className="text-xs font-semibold uppercase tracking-widest text-blue-300">
            KNUE
          </p>
          <h1 className="mt-2 text-xl font-bold">교수업적평가시스템</h1>
          <nav className="mt-8 space-y-4 text-sm">
            <div>
              <p className="font-semibold text-slate-300">시스템 관리</p>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/users"
              >
                사용자 관리
              </a>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/organizations"
              >
                조직 관리
              </a>
              <p className="mt-4 font-semibold text-slate-300">
                역할·권한 관리
              </p>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/roles"
              >
                역할 관리
              </a>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/user-roles"
              >
                사용자 역할 관리
              </a>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/menu-permissions"
              >
                메뉴 권한 관리
              </a>
              <p className="mt-4 font-semibold text-slate-300">메뉴 관리</p>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/menus/tree"
              >
                메뉴 구조 관리
              </a>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/menus"
              >
                메뉴 정보 관리
              </a>
              <p className="mt-4 font-semibold text-slate-300">공통코드 관리</p>
              <a
                className="mt-2 block rounded-lg bg-blue-600 px-3 py-2 font-semibold"
                href="/admin/code-groups"
              >
                코드그룹 관리
              </a>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/code-details"
              >
                상세코드 관리
              </a>
            </div>
          </nav>
        </aside>
        <section className="p-8">
          <header className="mb-6 flex items-center justify-between">
            <div>
              <p className="text-sm text-slate-500">{viewModel.menuPath}</p>
              <h2 className="mt-1 text-3xl font-bold">코드그룹 관리</h2>
            </div>
            <span className="rounded-full bg-slate-900 px-4 py-2 text-sm font-semibold text-white">
              SCR-CMN-CODE-GROUP
            </span>
          </header>

          <form
            className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"
            onSubmit={handleCodeGroupSearch}
          >
            <div className="grid grid-cols-5 gap-3">
              <label className="text-sm font-medium text-slate-700">
                그룹ID
                <input
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={filters.groupId}
                  onChange={(event) =>
                    setFilters((current) => ({
                      ...current,
                      groupId: event.target.value,
                    }))
                  }
                />
              </label>
              <label className="text-sm font-medium text-slate-700">
                명칭
                <input
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={filters.groupName}
                  onChange={(event) =>
                    setFilters((current) => ({
                      ...current,
                      groupName: event.target.value,
                    }))
                  }
                />
              </label>
              <label className="text-sm font-medium text-slate-700">
                관리부서
                <input
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={filters.managingDepartment}
                  onChange={(event) =>
                    setFilters((current) => ({
                      ...current,
                      managingDepartment: event.target.value,
                    }))
                  }
                />
              </label>
              <label className="text-sm font-medium text-slate-700">
                사용여부
                <select
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={filters.useYn}
                  onChange={(event) =>
                    setFilters((current) => ({
                      ...current,
                      useYn: event.target.value,
                    }))
                  }
                >
                  <option value="">전체</option>
                  <option value="Y">Y</option>
                  <option value="N">N</option>
                </select>
              </label>
              <div className="flex items-end">
                <button
                  className="rounded-lg bg-blue-600 px-5 py-2 font-semibold text-white"
                  type="submit"
                >
                  검색
                </button>
              </div>
            </div>
          </form>

          <div className="mt-4 min-h-8 text-sm font-medium">
            {status === "loading" && (
              <p className="text-blue-700">
                loading: 코드그룹 목록을 조회하는 중입니다.
              </p>
            )}
            {status === "empty" && (
              <p className="text-slate-600">
                empty: 조건에 맞는 데이터가 없습니다.
              </p>
            )}
            {status === "error" && (
              <p className="text-red-700">error: {message}</p>
            )}
            {status === "permission" && (
              <p className="text-amber-700">
                permission: 시스템관리자 권한이 필요합니다.
              </p>
            )}
            {status === "success" && message && (
              <p className="text-green-700">success: {message}</p>
            )}
          </div>

          <div className="mt-4 grid grid-cols-[1.1fr_0.9fr] gap-5">
            <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
              <table className="w-full border-collapse text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                  <tr>
                    {viewModel.columns.map((column) => (
                      <th className="px-4 py-3" key={column}>
                        {column}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {codeGroups.map((codeGroup) => (
                    <tr
                      className={`cursor-pointer border-t border-slate-100 ${selectedGroupId === codeGroup.groupId ? "bg-blue-50" : "hover:bg-slate-50"}`}
                      key={codeGroup.groupId}
                      onClick={() => selectCodeGroup(codeGroup)}
                    >
                      <td className="px-4 py-3 font-semibold">
                        {codeGroup.groupId}
                      </td>
                      <td className="px-4 py-3">{codeGroup.groupName}</td>
                      <td className="px-4 py-3">
                        {codeGroup.description ?? "-"}
                      </td>
                      <td className="px-4 py-3">
                        {codeGroup.managingDepartment ?? "-"}
                      </td>
                      <td className="px-4 py-3">{codeGroup.useYn}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </section>

            <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
              <h3 className="text-lg font-bold">그룹 등록/수정 폼</h3>
              <div className="mt-4 space-y-4">
                <div className="grid grid-cols-2 gap-3">
                  <label className="text-sm font-medium">
                    group_id readonly on edit
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      readOnly={Boolean(selectedCodeGroup)}
                      value={form.groupId}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          groupId: event.target.value,
                        }))
                      }
                    />
                  </label>
                  <label className="text-sm font-medium">
                    명칭
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      value={form.groupName}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          groupName: event.target.value,
                        }))
                      }
                    />
                  </label>
                  <label className="text-sm font-medium">
                    관리부서
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      value={form.managingDepartment}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          managingDepartment: event.target.value,
                        }))
                      }
                    />
                  </label>
                  <label className="text-sm font-medium">
                    사용여부
                    <select
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      value={form.useYn}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          useYn: event.target.value as "Y" | "N",
                        }))
                      }
                    >
                      <option value="Y">Y</option>
                      <option value="N">N</option>
                    </select>
                  </label>
                </div>
                <label className="block text-sm font-medium">
                  설명
                  <textarea
                    className="mt-1 min-h-24 w-full rounded-lg border border-slate-300 px-3 py-2"
                    value={form.description}
                    onChange={(event) =>
                      setForm((current) => ({
                        ...current,
                        description: event.target.value,
                      }))
                    }
                  />
                </label>
                <p className="rounded-xl bg-slate-50 p-3 text-sm text-slate-600">
                  사용 중인 그룹은 물리삭제하지 않고 사용여부 N으로 관리합니다.
                  상세코드 이동은 선택한 groupId를 query state로 전달합니다.
                </p>
                <button
                  className="w-full rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white"
                  type="button"
                  onClick={() => void saveCodeGroup()}
                >
                  {selectedCodeGroup ? "그룹 수정" : "그룹 등록"}
                </button>
                <button
                  className="w-full rounded-lg bg-slate-900 px-4 py-2 font-semibold text-white"
                  type="button"
                  onClick={moveToCodeDetails}
                >
                  상세코드로 이동
                </button>
                <button
                  className="w-full rounded-lg border border-slate-300 px-4 py-2 font-semibold"
                  type="button"
                  onClick={resetCodeGroupForm}
                >
                  신규/취소
                </button>
              </div>
            </section>
          </div>
        </section>
      </div>
    </main>
  );
}

function AdminCodeDetailsScreen() {
  const initialGroupId =
    new URLSearchParams(window.location.search).get("groupId") ?? "";
  const [filters, setFilters] = useState<CodeDetailFilters>({
    groupId: initialGroupId,
    filter: "",
    useYn: "",
    validOn: "",
  });
  const [codeDetails, setCodeDetails] = useState<AdminCodeDetail[]>([]);
  const [selectedCodeKey, setSelectedCodeKey] = useState<string | null>(null);
  const [status, setStatus] = useState<ScreenStatus>("loading");
  const [message, setMessage] = useState("");
  const [form, setForm] = useState<CodeDetailForm>({
    groupId: initialGroupId,
    codeValue: "",
    codeName: "",
    parentCodeValue: "",
    sortOrder: "1",
    extraAttributes: "{}",
    useYn: "Y",
    validFrom: "",
    validTo: "",
  });

  const viewModel = useMemo(
    () =>
      createAdminCodeDetailsViewModel({
        path: `${window.location.pathname}${window.location.search}`,
        codeDetails,
        selectedCodeKey,
        status,
        message,
      }),
    [codeDetails, selectedCodeKey, status, message],
  );

  const selectedCodeDetail = viewModel.selectedCodeDetail;

  async function loadCodeDetails(nextFilters = filters, successMessage = "") {
    setStatus("loading");
    setMessage("");
    try {
      const response = await requestJson<CodeDetailSearchResponse>(
        buildCodeDetailQuery(nextFilters),
      );
      setCodeDetails(response.data.items);
      setStatus(response.data.items.length === 0 ? "empty" : "success");
      setMessage(successMessage);
      if (response.data.items.length > 0) {
        selectCodeDetail(
          response.data.items.find(
            (codeDetail) => codeDetailKey(codeDetail) === selectedCodeKey,
          ) ?? response.data.items[0],
        );
      } else {
        resetCodeDetailForm(nextFilters.groupId);
      }
    } catch (error) {
      const errorMessage =
        error instanceof Error
          ? error.message
          : "상세코드 조회에 실패했습니다.";
      setStatus(errorMessage.includes("권한") ? "permission" : "error");
      setMessage(errorMessage);
    }
  }

  function selectCodeDetail(codeDetail: AdminCodeDetail) {
    setSelectedCodeKey(codeDetailKey(codeDetail));
    setForm({
      groupId: codeDetail.groupId,
      codeValue: codeDetail.codeValue,
      codeName: codeDetail.codeName,
      parentCodeValue: codeDetail.parentCodeValue ?? "",
      sortOrder: String(codeDetail.sortOrder),
      extraAttributes: codeDetail.extraAttributes ?? "{}",
      useYn: codeDetail.useYn,
      validFrom: codeDetail.validFrom ?? "",
      validTo: codeDetail.validTo ?? "",
    });
  }

  function resetCodeDetailForm(nextGroupId = filters.groupId) {
    setSelectedCodeKey(null);
    setForm({
      groupId: nextGroupId,
      codeValue: "",
      codeName: "",
      parentCodeValue: "",
      sortOrder: "1",
      extraAttributes: "{}",
      useYn: "Y",
      validFrom: "",
      validTo: "",
    });
  }

  useEffect(() => {
    void loadCodeDetails({
      groupId: initialGroupId,
      filter: "",
      useYn: "",
      validOn: "",
    });
  }, []);

  async function handleCodeDetailSearch(event: FormEvent) {
    event.preventDefault();
    await loadCodeDetails(filters);
  }

  function validateCodeDetailForm() {
    const sortOrder = Number(form.sortOrder);
    if (
      form.groupId.trim() === "" ||
      form.codeValue.trim() === "" ||
      form.codeName.trim() === ""
    ) {
      setStatus("error");
      setMessage("그룹ID, 코드값, 코드명은 필수입니다.");
      return null;
    }
    if (!Number.isInteger(sortOrder) || sortOrder < 1) {
      setStatus("error");
      setMessage("정렬순서는 1 이상의 숫자여야 합니다.");
      return null;
    }
    if (
      form.validFrom !== "" &&
      form.validTo !== "" &&
      form.validFrom > form.validTo
    ) {
      setStatus("error");
      setMessage("유효 시작일은 종료일보다 늦을 수 없습니다.");
      return null;
    }
    try {
      JSON.parse(form.extraAttributes.trim() || "{}");
    } catch {
      setStatus("error");
      setMessage("추가속성은 유효한 JSON이어야 합니다.");
      return null;
    }
    return {
      codeName: form.codeName.trim(),
      parentCodeValue: form.parentCodeValue.trim() || null,
      sortOrder,
      extraAttributes: form.extraAttributes.trim() || "{}",
      useYn: form.useYn,
      validFrom: form.validFrom || null,
      validTo: form.validTo || null,
    };
  }

  async function saveCodeDetail() {
    const payload = validateCodeDetailForm();
    if (!payload) {
      return;
    }
    try {
      if (selectedCodeDetail) {
        await requestJson<AdminCodeDetail>(
          `/api/code-details/${encodeURIComponent(selectedCodeDetail.groupId)}/${encodeURIComponent(selectedCodeDetail.codeValue)}`,
          {
            method: "PUT",
            body: JSON.stringify({
              groupId: selectedCodeDetail.groupId,
              codeValue: selectedCodeDetail.codeValue,
              ...payload,
            }),
          },
        );
      } else {
        await requestJson<AdminCodeDetail>("/api/code-details", {
          method: "POST",
          body: JSON.stringify({
            groupId: form.groupId.trim(),
            codeValue: form.codeValue.trim(),
            ...payload,
          }),
        });
      }
      const nextFilters = { ...filters, groupId: form.groupId.trim() };
      setFilters(nextFilters);
      await loadCodeDetails(
        nextFilters,
        selectedCodeDetail
          ? "상세코드가 저장되었습니다."
          : "상세코드가 등록되었습니다.",
      );
    } catch (error) {
      setStatus("error");
      setMessage(
        error instanceof Error
          ? error.message
          : "상세코드 저장에 실패했습니다.",
      );
    }
  }

  return (
    <main className="min-h-screen bg-slate-100 text-slate-900">
      <div className="grid min-h-screen grid-cols-[280px_1fr]">
        <aside className="border-r border-slate-200 bg-slate-950 p-6 text-white">
          <p className="text-xs font-semibold uppercase tracking-widest text-blue-300">
            KNUE
          </p>
          <h1 className="mt-2 text-xl font-bold">교수업적평가시스템</h1>
          <nav className="mt-8 space-y-4 text-sm">
            <div>
              <p className="font-semibold text-slate-300">시스템 관리</p>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/users"
              >
                사용자 관리
              </a>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/organizations"
              >
                조직 관리
              </a>
              <p className="mt-4 font-semibold text-slate-300">
                역할·권한 관리
              </p>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/roles"
              >
                역할 관리
              </a>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/user-roles"
              >
                사용자 역할 관리
              </a>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/menu-permissions"
              >
                메뉴 권한 관리
              </a>
              <p className="mt-4 font-semibold text-slate-300">메뉴 관리</p>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/menus/tree"
              >
                메뉴 구조 관리
              </a>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/menus"
              >
                메뉴 정보 관리
              </a>
              <p className="mt-4 font-semibold text-slate-300">공통코드 관리</p>
              <a
                className="mt-2 block rounded-lg px-3 py-2 font-semibold text-slate-200"
                href="/admin/code-groups"
              >
                코드그룹 관리
              </a>
              <a
                className="mt-2 block rounded-lg bg-blue-600 px-3 py-2 font-semibold"
                href="/admin/code-details"
              >
                상세코드 관리
              </a>
            </div>
          </nav>
        </aside>
        <section className="p-8">
          <header className="mb-6 flex items-center justify-between">
            <div>
              <p className="text-sm text-slate-500">{viewModel.menuPath}</p>
              <h2 className="mt-1 text-3xl font-bold">상세코드 관리</h2>
            </div>
            <span className="rounded-full bg-slate-900 px-4 py-2 text-sm font-semibold text-white">
              SCR-CMN-CODE-DETAIL
            </span>
          </header>

          <form
            className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"
            onSubmit={handleCodeDetailSearch}
          >
            <div className="grid grid-cols-5 gap-3">
              <label className="text-sm font-medium text-slate-700">
                그룹ID
                <input
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={filters.groupId}
                  onChange={(event) =>
                    setFilters((current) => ({
                      ...current,
                      groupId: event.target.value,
                    }))
                  }
                />
              </label>
              <label className="text-sm font-medium text-slate-700">
                코드값/코드명
                <input
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={filters.filter}
                  onChange={(event) =>
                    setFilters((current) => ({
                      ...current,
                      filter: event.target.value,
                    }))
                  }
                />
              </label>
              <label className="text-sm font-medium text-slate-700">
                사용여부
                <select
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                  value={filters.useYn}
                  onChange={(event) =>
                    setFilters((current) => ({
                      ...current,
                      useYn: event.target.value,
                    }))
                  }
                >
                  <option value="">전체</option>
                  <option value="Y">Y</option>
                  <option value="N">N</option>
                </select>
              </label>
              <label className="text-sm font-medium text-slate-700">
                유효일자
                <input
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                  type="date"
                  value={filters.validOn}
                  onChange={(event) =>
                    setFilters((current) => ({
                      ...current,
                      validOn: event.target.value,
                    }))
                  }
                />
              </label>
              <div className="flex items-end">
                <button
                  className="rounded-lg bg-blue-600 px-5 py-2 font-semibold text-white"
                  type="submit"
                >
                  검색
                </button>
              </div>
            </div>
          </form>

          <div className="mt-4 min-h-8 text-sm font-medium">
            {status === "loading" && (
              <p className="text-blue-700">
                loading: 상세코드 목록을 조회하는 중입니다.
              </p>
            )}
            {status === "empty" && (
              <p className="text-slate-600">
                empty: 조건에 맞는 데이터가 없습니다.
              </p>
            )}
            {status === "error" && (
              <p className="text-red-700">error: {message}</p>
            )}
            {status === "permission" && (
              <p className="text-amber-700">
                permission: 시스템관리자 권한이 필요합니다.
              </p>
            )}
            {status === "success" && message && (
              <p className="text-green-700">success: {message}</p>
            )}
          </div>

          <div className="mt-4 grid grid-cols-[1.25fr_0.9fr] gap-5">
            <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
              <table className="w-full border-collapse text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                  <tr>
                    {viewModel.columns.map((column) => (
                      <th className="px-3 py-3" key={column}>
                        {column}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {codeDetails.map((codeDetail) => (
                    <tr
                      className={`cursor-pointer border-t border-slate-100 ${selectedCodeKey === codeDetailKey(codeDetail) ? "bg-blue-50" : "hover:bg-slate-50"}`}
                      key={codeDetailKey(codeDetail)}
                      onClick={() => selectCodeDetail(codeDetail)}
                    >
                      <td className="px-3 py-3 font-semibold">
                        {codeDetail.groupId}
                      </td>
                      <td className="px-3 py-3">
                        <span
                          style={{ paddingLeft: `${codeDetail.depth * 16}px` }}
                        >
                          {codeDetail.codeValue}
                        </span>
                      </td>
                      <td className="px-3 py-3">{codeDetail.codeName}</td>
                      <td className="px-3 py-3">
                        {codeDetail.parentCodeValue ?? "-"}
                      </td>
                      <td className="px-3 py-3">{codeDetail.sortOrder}</td>
                      <td className="px-3 py-3 font-mono text-xs">
                        {codeDetail.extraAttributes ?? "{}"}
                      </td>
                      <td className="px-3 py-3">{codeDetail.useYn}</td>
                      <td className="px-3 py-3">
                        {codeDetail.validFrom ?? "-"} ~{" "}
                        {codeDetail.validTo ?? "-"}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </section>

            <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
              <h3 className="text-lg font-bold">상세코드 등록/수정 폼</h3>
              <div className="mt-4 space-y-4">
                <div className="grid grid-cols-2 gap-3">
                  <label className="text-sm font-medium">
                    group_id readonly on edit
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      readOnly={Boolean(selectedCodeDetail)}
                      value={form.groupId}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          groupId: event.target.value,
                        }))
                      }
                    />
                  </label>
                  <label className="text-sm font-medium">
                    code_value readonly on edit
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      readOnly={Boolean(selectedCodeDetail)}
                      value={form.codeValue}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          codeValue: event.target.value,
                        }))
                      }
                    />
                  </label>
                  <label className="text-sm font-medium">
                    코드명
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      value={form.codeName}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          codeName: event.target.value,
                        }))
                      }
                    />
                  </label>
                  <label className="text-sm font-medium">
                    상위코드
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      value={form.parentCodeValue}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          parentCodeValue: event.target.value,
                        }))
                      }
                    />
                  </label>
                  <label className="text-sm font-medium">
                    정렬순서
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      min="1"
                      type="number"
                      value={form.sortOrder}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          sortOrder: event.target.value,
                        }))
                      }
                    />
                  </label>
                  <label className="text-sm font-medium">
                    사용여부
                    <select
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      value={form.useYn}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          useYn: event.target.value as "Y" | "N",
                        }))
                      }
                    >
                      <option value="Y">Y</option>
                      <option value="N">N</option>
                    </select>
                  </label>
                  <label className="text-sm font-medium">
                    유효 시작일
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      type="date"
                      value={form.validFrom}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          validFrom: event.target.value,
                        }))
                      }
                    />
                  </label>
                  <label className="text-sm font-medium">
                    유효 종료일
                    <input
                      className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
                      type="date"
                      value={form.validTo}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          validTo: event.target.value,
                        }))
                      }
                    />
                  </label>
                </div>
                <label className="block text-sm font-medium">
                  추가속성 JSON
                  <textarea
                    className="mt-1 min-h-24 w-full rounded-lg border border-slate-300 px-3 py-2 font-mono text-xs"
                    value={form.extraAttributes}
                    onChange={(event) =>
                      setForm((current) => ({
                        ...current,
                        extraAttributes: event.target.value,
                      }))
                    }
                  />
                </label>
                <p className="rounded-xl bg-slate-50 p-3 text-sm text-slate-600">
                  사용 중인 코드는 물리삭제하지 않고 사용여부 N 또는 유효
                  종료일로 관리합니다. group_id와 code_value는 수정 모드에서
                  변경할 수 없습니다.
                </p>
                <button
                  className="w-full rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white"
                  type="button"
                  onClick={() => void saveCodeDetail()}
                >
                  {selectedCodeDetail ? "상세코드 수정" : "상세코드 등록"}
                </button>
                <a
                  className="block w-full rounded-lg bg-slate-900 px-4 py-2 text-center font-semibold text-white"
                  href="/admin/code-groups"
                >
                  코드그룹으로 돌아가기
                </a>
                <button
                  className="w-full rounded-lg border border-slate-300 px-4 py-2 font-semibold"
                  type="button"
                  onClick={() => resetCodeDetailForm()}
                >
                  신규/취소
                </button>
              </div>
            </section>
          </div>
        </section>
      </div>
    </main>
  );
}

function App() {
  if (window.location.pathname === "/admin/code-details") {
    return <AdminCodeDetailsScreen />;
  }
  if (window.location.pathname === "/admin/code-groups") {
    return <AdminCodeGroupsScreen />;
  }
  if (window.location.pathname === "/admin/menus") {
    return <AdminMenusScreen />;
  }
  if (window.location.pathname === "/admin/menus/tree") {
    return <AdminMenuTreeScreen />;
  }
  if (window.location.pathname === "/admin/organizations") {
    return <AdminOrganizationsScreen />;
  }
  if (window.location.pathname === "/admin/roles") {
    return <AdminRolesScreen />;
  }
  if (window.location.pathname === "/admin/user-roles") {
    return <AdminUserRolesScreen />;
  }
  if (window.location.pathname === "/admin/menu-permissions") {
    return <AdminMenuPermissionsScreen />;
  }
  return <AdminUsersScreen />;
}

const rootElement =
  typeof document === "undefined" ? null : document.getElementById("root");

if (rootElement) {
  ReactDOM.createRoot(rootElement).render(
    <React.StrictMode>
      <App />
    </React.StrictMode>,
  );
}
