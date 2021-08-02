SELECT orgtype_id, orgtype_label_text_id, orgtype_created_at, orgtype_updated_at, info_data, type_data
FROM
(
	SELECT ORGTYPE.id AS orgtype_id, ORGTYPE.label_text_id AS orgtype_label_text_id, ORGTYPE.created_at AS orgtype_created_at, ORGTYPE.updated_at AS orgtype_updated_at,
	CONCAT_WS('||', ORGTYPE.id, ORGTYPE.label_text_id, ORGTYPE.created_at, ORGTYPE.updated_at) AS info_data, 'ORGTYPE' AS type_data
	FROM $schema.ORGANIZATIONTYPE AS ORGTYPE
	UNION ALL
	SELECT ORGTYPE.id AS orgtype_id, ORGTYPE.label_text_id AS orgtype_label_text_id, ORGTYPE.created_at AS orgtype_created_at, ORGTYPE.updated_at AS orgtype_updated_at,
	CONCAT_WS('||', ORGTYPE.id, TEXT.content, LANG.code, TEXT.language_id, LANG.label) AS info_data, 'ORGTYPE_LABEL_LANG_VARIANT' AS type_data
	FROM $schema.ORGANIZATIONTYPE AS ORGTYPE
	INNER JOIN $schema.TEXT AS TEXT ON TEXT.id = ORGTYPE.label_text_id
	INNER JOIN $schema.LANGUAGE AS LANG ON TEXT.language_id = LANG.id
	UNION ALL
	SELECT ORGTYPE.id AS orgtype_id, ORGTYPE.label_text_id AS orgtype_label_text_id, ORGTYPE.created_at AS orgtype_created_at, ORGTYPE.updated_at AS orgtype_updated_at,
	CONCAT_WS('||', ORG.id, ORG.label, ORG.queryable, ORG.organizationtype_id, ORG.created_at, ORG.updated_at) AS info_data, 'ORGANIZATION' AS type_data
	FROM $schema.ORGANIZATIONTYPE AS ORGTYPE
	INNER JOIN $schema.ORGANIZATION AS ORG ON ORG.organizationtype_id = ORGTYPE.id
)