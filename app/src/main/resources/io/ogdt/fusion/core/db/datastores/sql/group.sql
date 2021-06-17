SELECT group_id, group_name, group_created_at, group_updated_at, info_data, type_data
FROM
(
	SELECT "GROUP".id AS group_id, "GROUP".name AS group_name, "GROUP".created_at AS group_created_at, "GROUP".updated_at AS group_updated_at,
	CONCAT_WS('||',	"GROUP".id, "GROUP".name, "GROUP".created_at, "GROUP".updated_at) AS info_data, 'GROUP' AS type_data
	FROM $schema."GROUP"
	UNION ALL
	SELECT "GROUP".id AS group_id, "GROUP".name AS group_name, "GROUP".created_at AS group_created_at, "GROUP".updated_at AS group_updated_at,
	CONCAT_WS('||',	PROFILE.id, lastname, firstname, CONCAT_WS(';', EMAIL.id, EMAIL.address), last_login, is_active, PROFILE.created_at , PROFILE.updated_at) AS info_data, 'PROFILE' AS type_data 
	FROM $schema."GROUP" AS "GROUP"
	INNER JOIN $schema.PROFILE_GROUP AS PROFILE_GROUP ON PROFILE_GROUP.group_id = "GROUP".id
	INNER JOIN $schema.PROFILE AS PROFILE ON PROFILE_GROUP.profile_id = PROFILE.id
	INNER JOIN $schema.PROFILE_EMAIL AS PROFILE_EMAIL ON PROFILE_EMAIL.profile_id = PROFILE.id
	INNER JOIN $schema.EMAIL AS EMAIL ON PROFILE_EMAIL.email_id = EMAIL.id
	WHERE PROFILE_EMAIL.is_main = TRUE
	UNION ALL
	SELECT "GROUP".id AS group_id, "GROUP".name AS group_name, "GROUP".created_at AS group_created_at, "GROUP".updated_at AS group_updated_at,
	CONCAT_WS('||', ORG.id, ORG.label, ORG.queryable, ORG.created_at, ORG.updated_at) AS info_data, 'ORGANIZATION' AS type_data
	FROM $schema."GROUP" AS "GROUP"
	INNER JOIN $schema.ORGANIZATION AS ORG ON "GROUP".organization_id = ORG.id
	UNION ALL
	SELECT "GROUP".id AS group_id, "GROUP".name AS group_name, "GROUP".created_at AS group_created_at, "GROUP".updated_at AS group_updated_at,
	CONCAT_WS('||', ORG.id, ORGTYPE.id, ORGTYPE.label_text_id, ORGTYPE.created_at, ORGTYPE.updated_at) AS info_data, 'ORGTYPE' AS type_data
	FROM $schema."GROUP" AS "GROUP"
	INNER JOIN $schema.ORGANIZATION AS ORG ON "GROUP".organization_id = ORG.id
	INNER JOIN $schema.ORGANIZATIONTYPE AS ORGTYPE ON ORG.organizationtype_id = ORGTYPE.id
	UNION ALL
	SELECT "GROUP".id AS group_id, "GROUP".name AS group_name, "GROUP".created_at AS group_created_at, "GROUP".updated_at AS group_updated_at,
	CONCAT_WS('||', ORG.id, ORGTYPE.id, TEXT.content, LANG.code, TEXT.language_id, LANG.label) AS info_data, 'ORGTYPE_LANG_VARIANT' AS type_data
	FROM $schema."GROUP" AS "GROUP"
	INNER JOIN $schema.ORGANIZATION AS ORG ON "GROUP".organization_id = ORG.id
	INNER JOIN $schema.ORGANIZATIONTYPE AS ORGTYPE ON ORG.organizationtype_id = ORGTYPE.id
	INNER JOIN $schema.TEXT AS TEXT ON TEXT.id = ORGTYPE.label_text_id
	INNER JOIN $schema.LANGUAGE AS LANG ON TEXT.language_id = LANG.id
	UNION ALL
	SELECT "GROUP".id AS group_id, "GROUP".name AS group_name, "GROUP".created_at AS group_created_at, "GROUP".updated_at AS group_updated_at,
	CONCAT_WS('||', PERMISSION.id, PERMISSION.key, PERMISSION.label_text_id, PERMISSION.description_text_id, PERMISSION.editable, PERMISSION.application_id, PERMISSION.created_at, PERMISSION.updated_at) AS info_data, 'PERMISSION' AS type_data
	FROM $schema."GROUP" AS "GROUP"
	INNER JOIN $schema.GROUP_PERMISSION AS GROUP_PERMISSION ON GROUP_PERMISSION.group_id = "GROUP".id
	INNER JOIN $schema.PERMISSION AS PERMISSION ON GROUP_PERMISSION.permission_id = PERMISSION.id
	UNION ALL
	SELECT "GROUP".id AS group_id, "GROUP".name AS group_name, "GROUP".created_at AS group_created_at, "GROUP".updated_at AS group_updated_at,
	CONCAT_WS('||', PERMISSION.id, content, code, language_id, LANG.label, TEXT.id) AS info_data, 'PERMISSION_LABEL_LANG_VARIANT' AS type_data
	FROM $schema."GROUP" AS "GROUP"
	INNER JOIN $schema.GROUP_PERMISSION AS GROUP_PERMISSION ON GROUP_PERMISSION.group_id = "GROUP".id
	INNER JOIN $schema.PERMISSION AS PERMISSION ON GROUP_PERMISSION.permission_id = PERMISSION.id
	INNER JOIN $schema.TEXT AS TEXT ON TEXT.id = PERMISSION.label_text_id
	INNER JOIN $schema.LANGUAGE AS LANG ON TEXT.language_id = LANG.id
	UNION ALL
	SELECT "GROUP".id AS group_id, "GROUP".name AS group_name, "GROUP".created_at AS group_created_at, "GROUP".updated_at AS group_updated_at,
	CONCAT_WS('||', PERMISSION.id, content, code, language_id, LANG.label, TEXT.id) AS info_data, 'PERMISSION_LABEL_LANG_VARIANT' AS type_data
	FROM $schema."GROUP" AS "GROUP"
	INNER JOIN $schema.GROUP_PERMISSION AS GROUP_PERMISSION ON GROUP_PERMISSION.group_id = "GROUP".id
	INNER JOIN $schema.PERMISSION AS PERMISSION ON GROUP_PERMISSION.permission_id = PERMISSION.id
	INNER JOIN $schema.TEXT AS TEXT ON TEXT.id = PERMISSION.description_text_id
	INNER JOIN $schema.LANGUAGE AS LANG ON TEXT.language_id = LANG.id
)