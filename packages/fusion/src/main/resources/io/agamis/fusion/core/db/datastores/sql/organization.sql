SELECT org_id, org_label, org_queryable, org_created_at, org_updated_at, info_data, type_data
FROM
(
	SELECT ORG.id AS org_id, ORG.label AS org_label, queryable AS org_queryable, ORG.created_at AS org_created_at, ORG.updated_at AS org_updated_at,
	CONCAT_WS('||',	ORG.id, ORG.label, queryable, ORG.created_at, ORG.updated_at) AS info_data, 'ORGANIZATION' AS type_data
	FROM $schema.ORGANIZATION AS ORG
	UNION ALL
	SELECT ORG.id AS org_id, ORG.label AS org_label, queryable AS org_queryable, ORG.created_at AS org_created_at, ORG.updated_at AS org_updated_at,
	CONCAT_WS('||',	PROFILE.id, lastname, firstname, CONCAT_WS(';', EMAIL.id, EMAIL.address), last_login, is_active, user_id, PROFILE.organization_id , PROFILE.created_at , PROFILE.updated_at) AS info_data, 'PROFILE' AS type_data 
	FROM $schema.ORGANIZATION as ORG
	INNER JOIN $schema.PROFILE AS PROFILE ON PROFILE.organization_id = ORG.id
	INNER JOIN $schema.PROFILE_EMAIL AS PROFILE_EMAIL ON PROFILE_EMAIL.profile_id = PROFILE.id
	INNER JOIN $schema.EMAIL AS EMAIL ON PROFILE_EMAIL.email_id = EMAIL.id
	WHERE PROFILE_EMAIL.is_main = TRUE
	UNION ALL
	SELECT ORG.id AS org_id, ORG.label AS org_label, queryable AS org_queryable, ORG.created_at AS org_created_at, ORG.updated_at AS org_updated_at,
	CONCAT_WS('||',FS.id, rootdir_id, FS.label, shared, FS_ORG.is_default, FS.created_at, FS.updated_at) AS info_data, 'FS' AS type_data
	FROM $schema.ORGANIZATION as ORG
	INNER JOIN $schema.FILESYSTEM_ORGANIZATION AS FS_ORG ON FS_ORG.organization_id = ORG.id
	INNER JOIN $schema.FILESYSTEM AS FS ON FS_ORG.filesystem_id = FS.id
	UNION ALL
	SELECT ORG.id AS org_id, ORG.label AS org_label, queryable AS org_queryable, ORG.created_at AS org_created_at, ORG.updated_at AS org_updated_at,
	CONCAT_WS('||', ORG.id, ORGTYPE.id, ORGTYPE.label_text_id, ORGTYPE.created_at, ORGTYPE.updated_at) AS info_data, 'ORGTYPE' AS type_data
	FROM $schema.ORGANIZATION AS ORG
	INNER JOIN $schema.ORGANIZATIONTYPE AS ORGTYPE ON ORG.organizationtype_id = ORGTYPE.id
	UNION ALL
	SELECT ORG.id AS org_id, ORG.label AS org_label, queryable AS org_queryable, ORG.created_at AS org_created_at, ORG.updated_at AS org_updated_at,
	CONCAT_WS('||', ORG.id, ORGTYPE.id, TEXT.content, LANG.code, TEXT.language_id, LANG.label) AS info_data, 'ORGTYPE_LABEL_LANG_VARIANT' AS type_data
	FROM $schema.ORGANIZATION AS ORG
	INNER JOIN $schema.ORGANIZATIONTYPE AS ORGTYPE ON ORG.organizationtype_id = ORGTYPE.id
	INNER JOIN $schema.TEXT AS TEXT ON TEXT.id = ORGTYPE.label_text_id
	INNER JOIN $schema.LANGUAGE AS LANG ON TEXT.language_id = LANG.id
)