SELECT fs_id, fs_rootdir_id, fs_label, fs_shared, fs_created_at, fs_updated_at, info_data, type_data
FROM
(
	SELECT FS.id AS fs_id, FS.rootdir_id AS fs_rootdir_id, FS.label AS fs_label, shared AS fs_shared, FS.created_at AS fs_created_at, FS.updated_at AS fs_updated_at,
	CONCAT_WS('||', FS.id, FS.rootdir_id, FS.label, shared, FS.created_at, FS.updated_at) AS info_data, 'FS' AS type_data
	FROM $schema.FILESYSTEM AS FS
	UNION ALL 
	SELECT FS.id AS fs_id, FS.rootdir_id AS fs_rootdir_id, FS.label AS fs_label, shared AS fs_shared, FS.created_at AS fs_created_at, FS.updated_at AS fs_updated_at,
	CONCAT_WS('||', ORG.id, ORG.label, ORG.queryable, ORG.created_at, ORG.updated_at) AS info_data, 'ORGANIZATION' AS type_data
	FROM $schema.FILESYSTEM AS FS
	INNER JOIN $schema.FILESYSTEM_ORGANIZATION as FS_ORG ON FS_ORG.filesystem_id = FS.id
	INNER JOIN $schema.ORGANIZATION AS ORG ON FS_ORG.organization_id = ORG.id
	UNION ALL
	SELECT FS.id AS fs_id, FS.rootdir_id AS fs_rootdir_id, FS.label AS fs_label, shared AS fs_shared, FS.created_at AS fs_created_at, FS.updated_at AS fs_updated_at,
	CONCAT_WS('||', ORG.id, ORGTYPE.id, ORGTYPE.label_text_id, ORGTYPE.created_at, ORGTYPE.updated_at) AS info_data, 'ORGTYPE' AS type_data
	FROM $schema.FILESYSTEM AS FS
	INNER JOIN $schema.FILESYSTEM_ORGANIZATION as FS_ORG ON FS_ORG.filesystem_id = FS.id
	INNER JOIN $schema.ORGANIZATION AS ORG ON FS_ORG.organization_id = ORG.id
	INNER JOIN $schema.ORGANIZATIONTYPE AS ORGTYPE ON ORG.organizationtype_id = ORGTYPE.id
	UNION ALL
	SELECT FS.id AS fs_id, FS.rootdir_id AS fs_rootdir_id, FS.label AS fs_label, shared AS fs_shared, FS.created_at AS fs_created_at, FS.updated_at AS fs_updated_at,
	CONCAT_WS('||', ORG.id, ORGTYPE.id, TEXT.content, LANG.code, TEXT.language_id, LANG.label) AS info_data, 'ORGTYPE_LANG_VARIANT' AS type_data
	FROM $schema.FILESYSTEM AS FS
	INNER JOIN $schema.FILESYSTEM_ORGANIZATION as FS_ORG ON FS_ORG.filesystem_id = FS.id
	INNER JOIN $schema.ORGANIZATION AS ORG ON FS_ORG.organization_id = ORG.id
	INNER JOIN $schema.ORGANIZATIONTYPE AS ORGTYPE ON ORG.organizationtype_id = ORGTYPE.id
	INNER JOIN $schema.TEXT AS TEXT ON TEXT.id = ORGTYPE.label_text_id
	INNER JOIN $schema.LANGUAGE AS LANG ON TEXT.language_id = LANG.id
)