SELECT app_id, app_label, app_version, app_universal_id, app_status, app_manifest_url, app_store_url, app_created_at, app_updated_at, info_data, type_data
FROM
(
	SELECT APP.id AS app_id, APP.label AS app_label, APP.version AS app_version, APP.app_universal_id AS app_universal_id, APP.status AS app_status, APP.manifest_url AS app_manifest_url, APP.store_url AS app_store_url, APP.created_at AS app_created_at, APP.updated_at AS app_updated_at,
	CONCAT_WS('||', APP.id, APP.label, APP.version, APP.app_universal_id, APP.status, APP.manifest_url, APP.store_url, APP.created_at, APP.updated_at) AS info_data, 'APPLICATION' AS type_data
	FROM $schema.APPLICATION AS APP
	UNION ALL
	SELECT APP.id AS app_id, APP.label AS app_label, APP.version AS app_version, APP.app_universal_id AS app_universal_id, APP.status AS app_status, APP.manifest_url AS app_manifest_url, APP.store_url AS app_store_url, APP.created_at AS app_created_at, APP.updated_at AS app_updated_at,
	CONCAT_WS('||', ORG.id, ORG.label, ORG.queryable, ORG_APP.status, ORG_APP.license_file_fs_id, ORG_APP.license_file_id, ORG.created_at, ORG.updated_at) AS info_data, 'ORGANIZATION' AS type_data
	FROM $schema.APPLICATION AS APP
	INNER JOIN $schema.ORGANIZATION_APPLICATION AS ORG_APP ON APP.id = ORG_APP.application_id
	INNER JOIN $schema.ORGANIZATION AS ORG ON ORG_APP.organization_id = ORG.id
	UNION ALL
	SELECT APP.id AS app_id, APP.label AS app_label, APP.version AS app_version, APP.app_universal_id AS app_universal_id, APP.status AS app_status, APP.manifest_url AS app_manifest_url, APP.store_url AS app_store_url, APP.created_at AS app_created_at, APP.updated_at AS app_updated_at,
	CONCAT_WS('||', ORG.id, ORGTYPE.id, ORGTYPE.label_text_id, ORGTYPE.created_at, ORGTYPE.updated_at) AS info_data, 'ORGTYPE' AS type_data
	FROM $schema.APPLICATION AS APP
	INNER JOIN $schema.ORGANIZATION_APPLICATION AS ORG_APP ON APP.id = ORG_APP.application_id
	INNER JOIN $schema.ORGANIZATION AS ORG ON ORG_APP.organization_id = ORG.id
	INNER JOIN $schema.ORGANIZATIONTYPE AS ORGTYPE ON ORG.organizationtype_id = ORGTYPE.id
	UNION ALL
	SELECT APP.id AS app_id, APP.label AS app_label, APP.version AS app_version, APP.app_universal_id AS app_universal_id, APP.status AS app_status, APP.manifest_url AS app_manifest_url, APP.store_url AS app_store_url, APP.created_at AS app_created_at, APP.updated_at AS app_updated_at,
	CONCAT_WS('||', ORG.id, ORGTYPE.id, TEXT.content, LANG.code, TEXT.language_id, LANG.label) AS info_data, 'ORGTYPE_LABEL_LANG_VARIANT' AS type_data
	FROM $schema.APPLICATION AS APP
	INNER JOIN $schema.ORGANIZATION_APPLICATION AS ORG_APP ON APP.id = ORG_APP.application_id
	INNER JOIN $schema.ORGANIZATION AS ORG ON ORG_APP.organization_id = ORG.id
	INNER JOIN $schema.ORGANIZATIONTYPE AS ORGTYPE ON ORG.organizationtype_id = ORGTYPE.id
	INNER JOIN $schema.TEXT AS TEXT ON TEXT.id = ORGTYPE.label_text_id
	INNER JOIN $schema.LANGUAGE AS LANG ON TEXT.language_id = LANG.id
	UNION ALL
	SELECT APP.id AS app_id, APP.label AS app_label, APP.version AS app_version, APP.app_universal_id AS app_universal_id, APP.status AS app_status, APP.manifest_url AS app_manifest_url, APP.store_url AS app_store_url, APP.created_at AS app_created_at, APP.updated_at AS app_updated_at,
	CONCAT_WS('||', PERMISSION.id, PERMISSION.key, PERMISSION.label_text_id, PERMISSION.description_text_id, PERMISSION.editable, PERMISSION.application_id, PERMISSION.created_at, PERMISSION.updated_at) AS info_data, 'PERMISSION' AS type_data
	FROM $schema.APPLICATION AS APP
	INNER JOIN $schema.PERMISSION AS PERMISSION ON APP.id = PERMISSION.application_id
	UNION ALL
	SELECT APP.id AS app_id, APP.label AS app_label, APP.version AS app_version, APP.app_universal_id AS app_universal_id, APP.status AS app_status, APP.manifest_url AS app_manifest_url, APP.store_url AS app_store_url, APP.created_at AS app_created_at, APP.updated_at AS app_updated_at,
	CONCAT_WS('||', PERMISSION.id, content, code, language_id, LANG.label, TEXT.id) AS info_data, 'PERMISSION_LABEL_LANG_VARIANT' AS type_data
	FROM $schema.APPLICATION AS APP
	INNER JOIN $schema.PERMISSION AS PERMISSION ON APP.id = PERMISSION.application_id
	INNER JOIN $schema.TEXT AS TEXT ON TEXT.id = PERMISSION.label_text_id
	INNER JOIN $schema.LANGUAGE AS LANG ON TEXT.language_id = LANG.id
	UNION ALL
	SELECT APP.id AS app_id, APP.label AS app_label, APP.version AS app_version, APP.app_universal_id AS app_universal_id, APP.status AS app_status, APP.manifest_url AS app_manifest_url, APP.store_url AS app_store_url, APP.created_at AS app_created_at, APP.updated_at AS app_updated_at,
	CONCAT_WS('||', PERMISSION.id, content, code, language_id, LANG.label, TEXT.id) AS info_data, 'PERMISSION_DESC_LANG_VARIANT_DESCRIPTION' AS type_data
	FROM $schema.APPLICATION AS APP
	INNER JOIN $schema.PERMISSION AS PERMISSION ON APP.id = PERMISSION.application_id
	INNER JOIN $schema.TEXT AS TEXT ON TEXT.id = PERMISSION.description_text_id
	INNER JOIN $schema.LANGUAGE AS LANG ON TEXT.language_id = LANG.id
)