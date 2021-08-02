SELECT permission_id, permission_key, permission_editable, permission_label_text_id, permission_description_text_id, permission_created_at, permission_updated_at, info_data, type_data
FROM
(
	SELECT PERMISSION.id AS permission_id, PERMISSION.key AS permission_key, PERMISSION.editable AS permission_editable, PERMISSION.label_text_id AS permission_label_text_id, PERMISSION.description_text_id AS permission_description_text_id, PERMISSION.created_at AS permission_created_at, PERMISSION.updated_at AS permission_updated_at,
	CONCAT_WS('||', PERMISSION.id, PERMISSION.key, PERMISSION.editable, PERMISSION.label_text_id, PERMISSION.description_text_id, PERMISSION.created_at, PERMISSION.updated_at) AS info_data, 'PERMISSION' AS type_data
	FROM $schema.PERMISSION
	UNION ALL
	SELECT PERMISSION.id AS permission_id, PERMISSION.key AS permission_key, PERMISSION.editable AS permission_editable, PERMISSION.label_text_id AS permission_label_text_id, PERMISSION.description_text_id AS permission_description_text_id, PERMISSION.created_at AS permission_created_at, PERMISSION.updated_at AS permission_updated_at,
	CONCAT_WS('||', PROFILE.id, lastname, firstname, CONCAT_WS(';', EMAIL.id, EMAIL.address), last_login, is_active, PROFILE.created_at , PROFILE.updated_at) AS info_data, 'PROFILE' AS type_data
	FROM $schema.PERMISSION AS PERMISSION
	INNER JOIN $schema.PROFILE_PERMISSION AS PROF_PER ON PROF_PER.permission_id = PERMISSION.id
	INNER JOIN $schema.PROFILE AS PROFILE ON PROFILE.id = PROF_PER.profile_id
	INNER JOIN $schema.PROFILE_EMAIL AS PROFILE_EMAIL ON PROFILE_EMAIL.profile_id = PROFILE.id
	INNER JOIN $schema.EMAIL AS EMAIL ON PROFILE_EMAIL.email_id = EMAIL.id
	WHERE PROFILE_EMAIL.is_main = TRUE
	UNION ALL
	SELECT PERMISSION.id AS permission_id, PERMISSION.key AS permission_key, PERMISSION.editable AS permission_editable, PERMISSION.label_text_id AS permission_label_text_id, PERMISSION.description_text_id AS permission_description_text_id, PERMISSION.created_at AS permission_created_at, PERMISSION.updated_at AS permission_updated_at,
	CONCAT_WS('||', "GROUP".id, "GROUP".name, "GROUP".created_at, "GROUP".updated_at) AS info_data, 'GROUP' AS type_data
	FROM $schema.PERMISSION AS PERMISSION
	INNER JOIN $schema.GROUP_PERMISSION AS GROUP_PER ON GROUP_PER.permission_id = PERMISSION.id
	INNER JOIN $schema."GROUP" AS "GROUP" ON "GROUP".id = GROUP_PER.group_id
	UNION ALL
	SELECT PERMISSION.id AS permission_id, PERMISSION.key AS permission_key, PERMISSION.editable AS permission_editable, PERMISSION.label_text_id AS permission_label_text_id, PERMISSION.description_text_id AS permission_description_text_id, PERMISSION.created_at AS permission_created_at, PERMISSION.updated_at AS permission_updated_at,
	CONCAT_WS('||', APP.id, APP.label, APP.version, APP.app_universal_id, APP.status, APP.manifest_url, APP.store_url, APP.created_at, APP.updated_at) AS info_data, 'APPLICATION' AS type_data
	FROM $schema.PERMISSION AS PERMISSION
	INNER JOIN $schema.APPLICATION AS APP ON APP.id = PERMISSION.application_id	
	UNION ALL
	SELECT PERMISSION.id AS permission_id, PERMISSION.key AS permission_key, PERMISSION.editable AS permission_editable, PERMISSION.label_text_id AS permission_label_text_id, PERMISSION.description_text_id AS permission_description_text_id, PERMISSION.created_at AS permission_created_at, PERMISSION.updated_at AS permission_updated_at,
	CONCAT_WS('||', PERMISSION.id, content, code, language_id, label, TEXT.id) AS info_data, 'PERMISSION_LABEL_LANG_VARIANT' AS type_data			
	FROM $schema.PERMISSION AS PERMISSION
	INNER JOIN $schema.TEXT AS TEXT ON TEXT.id = PERMISSION.label_text_id
	INNER JOIN $schema.LANGUAGE AS LANG ON TEXT.language_id = LANG.id
	UNION ALL
	SELECT PERMISSION.id AS permission_id, PERMISSION.key AS permission_key, PERMISSION.editable AS permission_editable, PERMISSION.label_text_id AS permission_label_text_id, PERMISSION.description_text_id AS permission_description_text_id, PERMISSION.created_at AS permission_created_at, PERMISSION.updated_at AS permission_updated_at,
	CONCAT_WS('||', PERMISSION.id, content, code, language_id, label, TEXT.id) AS info_data, 'PERMISSION_DESC_LANG_VARIANT' AS type_data
	FROM $schema.PERMISSION AS PERMISSION
	INNER JOIN $schema.TEXT AS TEXT ON TEXT.id = PERMISSION.description_text_id
	INNER JOIN $schema.LANGUAGE AS LANG ON TEXT.language_id = LANG.id
)