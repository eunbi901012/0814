INSERT INTO organization (org_code, org_name, org_type, parent_org_code, use_yn)
VALUES
    ('KNUE', '한국교원대학교', 'UNIVERSITY', NULL, 'Y'),
    ('KNUE-COL-EDU', '교육학과군', 'COLLEGE', 'KNUE', 'Y'),
    ('KNUE-DEPT-COMMON', '공통기능학과', 'DEPARTMENT', 'KNUE-COL-EDU', 'Y')
ON CONFLICT (org_code) DO NOTHING;

INSERT INTO internal_account (user_id, login_id, password_hash, name, department_code, position_name, employment_status, system_use_yn)
VALUES
    ('admin', 'admin', 'sha256:8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', '시스템관리자', 'KNUE-DEPT-COMMON', '시스템관리자', 'ACTIVE', 'Y'),
    ('FAC-0001', 'fac0001', 'sha256:8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', '김교원', 'KNUE-DEPT-COMMON', '학과장', 'ACTIVE', 'Y')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO korus_faculty_snapshot (snapshot_id, employee_no, name, org_code, rank_name, retirement_date, last_synced_at, status)
VALUES
    ('SNAP-ADMIN', 'admin', '시스템관리자', 'KNUE-DEPT-COMMON', '관리자', NULL, CURRENT_TIMESTAMP, 'ACTIVE'),
    ('SNAP-FAC-0001', 'FAC-0001', '김교원', 'KNUE-DEPT-COMMON', '교수', NULL, CURRENT_TIMESTAMP, 'ACTIVE')
ON CONFLICT (snapshot_id) DO NOTHING;

INSERT INTO organization_relation_history (history_id, org_code, parent_org_code, effective_start_date, effective_end_date, reason)
VALUES
    ('ORG-HIST-KNUE-COL-EDU', 'KNUE-COL-EDU', 'KNUE', DATE '2026-01-01', NULL, '초기 조직 계층 seed'),
    ('ORG-HIST-KNUE-DEPT-COMMON', 'KNUE-DEPT-COMMON', 'KNUE-COL-EDU', DATE '2026-01-01', NULL, '초기 조직 계층 seed')
ON CONFLICT (history_id) DO NOTHING;

INSERT INTO role (role_code, role_name, purpose, grant_criteria, data_scope_default, use_yn)
VALUES
    ('R01', '교원', '본인 관련 업무를 수행하는 일반 사용자 역할', '교원 재직자', 'SELF', 'Y'),
    ('R02', '학과장', '소속 학과 교원 관련 업무 확인 역할', '학과장 보직자', 'DEPARTMENT', 'Y'),
    ('R03', '단과대학(원) 행정실', '단과대학 또는 대학원 행정 처리 역할', '행정실 담당자', 'COLLEGE', 'Y'),
    ('R04', '교수지원과', '기준정보와 평가 관련 행정 관리 역할', '교수지원과 담당자', 'UNIVERSITY', 'Y'),
    ('R05', '산학협력단', '연구비·간접비·지식재산 관련 자료 관리 역할', '산학협력단 담당자', 'RESEARCH', 'Y'),
    ('R06', '입학인재관리과', '입학·취업률 관련 자료 관리 역할', '입학인재관리과 담당자', 'UNIVERSITY', 'Y'),
    ('R07', '실적부서', '담당 실적 자료 관리 역할', '실적 담당 부서 사용자', 'DEPARTMENT', 'Y'),
    ('R08', '점수산출 감사자', '산출 과정과 근거를 조회하는 감사 역할', '감사 지정 사용자', 'AUDIT', 'Y'),
    ('R09', '시스템관리자', '사용자·조직·메뉴·권한·코드 관리를 수행하는 관리자 역할', '시스템 관리자', 'ALL', 'Y')
ON CONFLICT (role_code) DO NOTHING;

INSERT INTO user_role (user_role_id, user_id, role_code, assignment_type, approver_user_id, effective_start_date, effective_end_date, status)
VALUES
    ('UR-ADMIN-R09', 'admin', 'R09', 'MANUAL', 'admin', DATE '2026-01-01', NULL, 'ACTIVE'),
    ('UR-FAC-0001-R01', 'FAC-0001', 'R01', 'POSITION_BASED', 'admin', DATE '2026-01-01', NULL, 'ACTIVE')
ON CONFLICT (user_role_id) DO NOTHING;

INSERT INTO menu (menu_id, parent_menu_id, menu_name, screen_id, url, icon, business_type, description, display_order, use_yn)
VALUES
    ('M-SYS', NULL, '시스템 관리', NULL, NULL, 'settings', 'SYSTEM', '시스템 관리 대메뉴', 1, 'Y'),
    ('M-SYS-USERORG', 'M-SYS', '사용자·조직 관리', NULL, NULL, 'users', 'SYSTEM', '사용자와 조직 관리 중메뉴', 1, 'Y'),
    ('M-SYS-ROLEAUTH', 'M-SYS', '역할·권한 관리', NULL, NULL, 'shield', 'SYSTEM', '역할과 권한 관리 중메뉴', 2, 'Y'),
    ('M-SYS-MENU', 'M-SYS', '메뉴 관리', NULL, NULL, 'menu', 'SYSTEM', '메뉴 구조와 실행정보 관리 중메뉴', 3, 'Y'),
    ('M-SYS-CODE', 'M-SYS', '공통코드 관리', NULL, NULL, 'code', 'SYSTEM', '공통코드 관리 중메뉴', 4, 'Y'),
    ('M-SYS-USER', 'M-SYS-USERORG', '사용자 관리', 'SCR-CMN-USER', '/admin/users', 'user', 'SYSTEM', '사용자 검색과 사용여부·역할 관리', 1, 'Y'),
    ('M-SYS-ORG', 'M-SYS-USERORG', '조직 관리', 'SCR-CMN-ORG', '/admin/organizations', 'org', 'SYSTEM', '조직 계층과 관계 보정 관리', 2, 'Y'),
    ('M-SYS-ROLE', 'M-SYS-ROLEAUTH', '역할 관리', 'SCR-CMN-ROLE', '/admin/roles', 'role', 'SYSTEM', '역할 목적과 부여 기준 관리', 1, 'Y'),
    ('M-SYS-USERROLE', 'M-SYS-ROLEAUTH', '사용자 역할 관리', 'SCR-CMN-USER-ROLE', '/admin/user-roles', 'user-role', 'SYSTEM', '사용자 역할 유효기간 관리', 2, 'Y'),
    ('M-SYS-MENUPERM', 'M-SYS-ROLEAUTH', '메뉴 권한 관리', 'SCR-CMN-MENU-PERM', '/admin/menu-permissions', 'permission', 'SYSTEM', '메뉴 접근 권한 관리', 3, 'Y'),
    ('M-SYS-MENUTREE', 'M-SYS-MENU', '메뉴 구조 관리', 'SCR-CMN-MENU-TREE', '/admin/menus/tree', 'tree', 'SYSTEM', '메뉴 부모와 표시순서 관리', 1, 'Y'),
    ('M-SYS-MENUINFO', 'M-SYS-MENU', '메뉴 정보 관리', 'SCR-CMN-MENU-INFO', '/admin/menus', 'menu-info', 'SYSTEM', '메뉴 실행정보 관리', 2, 'Y'),
    ('M-SYS-CODEGROUP', 'M-SYS-CODE', '코드그룹 관리', 'SCR-CMN-CODE-GROUP', '/admin/code-groups', 'code-group', 'SYSTEM', '공통코드 그룹 관리', 1, 'Y'),
    ('M-SYS-CODEDETAIL', 'M-SYS-CODE', '상세코드 관리', 'SCR-CMN-CODE-DETAIL', '/admin/code-details', 'code-detail', 'SYSTEM', '상세코드 관리', 2, 'Y')
ON CONFLICT (menu_id) DO NOTHING;

INSERT INTO menu_permission (permission_id, target_type, target_id, menu_id, can_read, can_write, effect)
SELECT 'PERM-R09-' || menu_id, 'ROLE', 'R09', menu_id, true, true, 'ALLOW'
FROM menu
ON CONFLICT (target_type, target_id, menu_id) DO NOTHING;

INSERT INTO code_group (group_id, group_name, description, managing_department, use_yn)
VALUES
    ('EMPLOYMENT_STATUS', '재직상태', '사용자 검색과 KORUS snapshot 검증에 필요한 재직 상태', '교수지원과', 'Y'),
    ('USE_YN', '사용여부', '공통 사용 여부 코드', '시스템관리', 'Y')
ON CONFLICT (group_id) DO NOTHING;

INSERT INTO code_detail (group_id, code_value, code_name, parent_code_value, sort_order, extra_attributes, use_yn, valid_from, valid_to)
VALUES
    ('EMPLOYMENT_STATUS', 'ACTIVE', '재직', NULL, 1, '{"source":"local-seed"}'::jsonb, 'Y', DATE '2026-01-01', NULL),
    ('EMPLOYMENT_STATUS', 'LEAVE', '휴직', NULL, 2, '{"source":"local-seed"}'::jsonb, 'Y', DATE '2026-01-01', NULL),
    ('EMPLOYMENT_STATUS', 'RETIRED', '퇴직', NULL, 3, '{"source":"local-seed"}'::jsonb, 'Y', DATE '2026-01-01', NULL),
    ('USE_YN', 'Y', '사용', NULL, 1, '{"boolean":true}'::jsonb, 'Y', DATE '2026-01-01', NULL),
    ('USE_YN', 'N', '미사용', NULL, 2, '{"boolean":false}'::jsonb, 'Y', DATE '2026-01-01', NULL)
ON CONFLICT (group_id, code_value) DO NOTHING;
