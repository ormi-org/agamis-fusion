SELECT profile_id, profile_lastname, profile_firstname, profile_last_login, profile_is_active, profile_created_at, profile_updated_at, info_data, type_data
FROM
(
	SELECT PROFILE.id AS profile_id, lastname AS profile_lastname, firstname AS profile_firstname, last_login AS profile_last_login, is_active AS profile_is_active, PROFILE.created_at AS profile_created_at, PROFILE.updated_at AS profile_updated_at,
	CONCAT_WS('||', PROFILE.id, lastname, firstname, last_login, is_active, user_id, PROFILE.organization_id , PROFILE.created_at , PROFILE.updated_at) AS info_data, 'PROFILE' AS type_data
	FROM $schema.PROFILE AS PROFILE
	INNER JOIN $schema.PROFILE_EMAIL AS PROFILE_EMAIL ON PROFILE_EMAIL.profile_id = PROFILE.id
	INNER JOIN $schema.EMAIL AS EMAIL ON PROFILE_EMAIL.email_id = EMAIL.id
	WHERE PROFILE_EMAIL.is_main = TRUE
	UNION ALL
	SELECT PROFILE.id AS profile_id, lastname AS profile_lastname, firstname AS profile_firstname, last_login AS profile_last_login, is_active AS profile_is_active, PROFILE.created_at AS profile_created_at, PROFILE.updated_at AS profile_updated_at,
	CONCAT_WS('||', USER.id, username, password, USER.created_at, USER.updated_at) AS info_data, 'USER' AS type_data
	FROM $schema.PROFILE AS PROFILE
	INNER JOIN $schema.USER as USER ON USER.id = PROFILE.user_id
	UNION ALL
	SELECT PROFILE.id AS profile_id, lastname AS profile_lastname, firstname AS profile_firstname, last_login AS profile_last_login, is_active AS profile_is_active, PROFILE.created_at AS profile_created_at, PROFILE.updated_at AS profile_updated_at, 
	CONCAT_WS('||', ORG.id, ORG.label, ORG.queryable, ORG.created_at, ORG.updated_at) AS info_data, 'ORGANIZATION' AS type_data
	FROM $schema.PROFILE AS PROFILE
	INNER JOIN $schema.ORGANIZATION AS ORG ON PROFILE.organization_id = ORG.id
	UNION ALL
	SELECT PROFILE.id AS profile_id, lastname AS profile_lastname, firstname AS profile_firstname, last_login AS profile_last_login, is_active AS profile_is_active, PROFILE.created_at AS profile_created_at, PROFILE.updated_at AS profile_updated_at,
	CONCAT_WS('||', ORG.id, ORGTYPE.id, ORGTYPE.label_text_id, ORGTYPE.created_at, ORGTYPE.updated_at) AS info_data, 'ORGTYPE' AS type_data
	FROM $schema.PROFILE AS PROFILE
	INNER JOIN $schema.ORGANIZATION AS ORG ON PROFILE.organization_id = ORG.id
	INNER JOIN $schema.ORGANIZATIONTYPE AS ORGTYPE ON ORG.organizationtype_id = ORGTYPE.id
	UNION ALL
	SELECT PROFILE.id AS profile_id, lastname AS profile_lastname, firstname AS profile_firstname, last_login AS profile_last_login, is_active AS profile_is_active, PROFILE.created_at AS profile_created_at, PROFILE.updated_at AS profile_updated_at,
	CONCAT_WS('||', ORG.id, ORGTYPE.id, TEXT.content, LANG.code, TEXT.language_id, LANG.label, TEXT.id) AS info_data, 'ORGTYPE_LANG_VARIANT' AS type_data
	FROM $schema.PROFILE AS PROFILE
	INNER JOIN $schema.ORGANIZATION AS ORG ON PROFILE.organization_id = ORG.id
	INNER JOIN $schema.ORGANIZATIONTYPE AS ORGTYPE ON ORG.organizationtype_id = ORGTYPE.id
	INNER JOIN $schema.TEXT AS TEXT ON TEXT.id = ORGTYPE.label_text_id
	INNER JOIN $schema.LANGUAGE AS LANG ON TEXT.language_id = LANG.id
	UNION ALL
	SELECT PROFILE.id AS profile_id, lastname AS profile_lastname, firstname AS profile_firstname, last_login AS profile_last_login, is_active AS profile_is_active, PROFILE.created_at AS profile_created_at, PROFILE.updated_at AS profile_updated_at,
	CONCAT_WS('||', EMAIL.id, EMAIL.address, PROFILE_EMAIL.is_main) AS info_data, 'EMAIL' AS type_data
	FROM $schema.PROFILE AS PROFILE
	INNER JOIN $schema.PROFILE_EMAIL AS PROFILE_EMAIL ON PROFILE_EMAIL.profile_id = PROFILE.id
	INNER JOIN $schema.EMAIL AS EMAIL ON PROFILE_EMAIL.email_id = EMAIL.id
	UNION ALL
	SELECT PROFILE.id AS profile_id, lastname AS profile_lastname, firstname AS profile_firstname, last_login AS profile_last_login, is_active AS profile_is_active, PROFILE.created_at AS profile_created_at, PROFILE.updated_at AS profile_updated_at,
	CONCAT_WS('||', "GROUP".id, "GROUP".name, "GROUP".created_at, "GROUP".updated_at) AS info_data, 'GROUP' AS type_data
	FROM $schema.PROFILE AS PROFILE
	INNER JOIN $schema.PROFILE_GROUP AS PROFILE_GROUP ON PROFILE.id = PROFILE_GROUP.profile_id
	INNER JOIN $schema."GROUP" AS "GROUP" ON "GROUP".id = PROFILE_GROUP.group_id
	UNION ALL
	SELECT PROFILE.id AS profile_id, lastname AS profile_lastname, firstname AS profile_firstname, last_login AS profile_last_login, is_active AS profile_is_active, PROFILE.created_at AS profile_created_at, PROFILE.updated_at AS profile_updated_at,
	CONCAT_WS('||', PERMISSION.id, PERMISSION.key, PERMISSION.label_text_id, PERMISSION.description_text_id, PERMISSION.editable, PERMISSION.application_id, PERMISSION.created_at, PERMISSION.updated_at) AS info_data, 'PERMISSION' AS type_data
	FROM $schema.PROFILE AS PROFILE
	INNER JOIN $schema.PROFILE_PERMISSION AS PROFILE_PERMISSION ON PROFILE_PERMISSION.profile_id = PROFILE.id
	INNER JOIN $schema.PERMISSION AS PERMISSION ON PROFILE_PERMISSION.permission_id = PERMISSION.id
	UNION ALL
	SELECT PROFILE.id AS profile_id, lastname AS profile_lastname, firstname AS profile_firstname, last_login AS profile_last_login, is_active AS profile_is_active, PROFILE.created_at AS profile_created_at, PROFILE.updated_at AS profile_updated_at,
	CONCAT_WS('||', PERMISSION.id, content, code, language_id, LANG.label, TEXT.id) AS info_data, 'PERMISSION_LABEL_LANG_VARIANT' AS type_data
	FROM $schema.PROFILE AS PROFILE
	INNER JOIN $schema.PROFILE_PERMISSION AS PROFILE_PERMISSION ON PROFILE_PERMISSION.profile_id = PROFILE.id
	INNER JOIN $schema.PERMISSION AS PERMISSION ON PROFILE_PERMISSION.permission_id = PERMISSION.id
	INNER JOIN $schema.TEXT AS TEXT ON TEXT.id = PERMISSION.label_text_id
	INNER JOIN $schema.LANGUAGE AS LANG ON TEXT.language_id = LANG.id
	UNION ALL
	SELECT PROFILE.id AS profile_id, lastname AS profile_lastname, firstname AS profile_firstname, last_login AS profile_last_login, is_active AS profile_is_active, PROFILE.created_at AS profile_created_at, PROFILE.updated_at AS profile_updated_at,
	CONCAT_WS('||', PERMISSION.id, content, code, language_id, LANG.label, TEXT.id) AS info_data, 'PERMISSION_DESC_LANG_VARIANT' AS type_data
	FROM $schema.PROFILE AS PROFILE
	INNER JOIN $schema.PROFILE_PERMISSION AS PROFILE_PERMISSION ON PROFILE_PERMISSION.profile_id = PROFILE.id
	INNER JOIN $schema.PERMISSION AS PERMISSION ON PROFILE_PERMISSION.permission_id = PERMISSION.id
	INNER JOIN $schema.TEXT AS TEXT ON TEXT.id = PERMISSION.description_text_id
	INNER JOIN $schema.LANGUAGE AS LANG ON TEXT.language_id = LANG.id
)