SELECT user_id, user_username, user_password, user_created_at, user_updated_at, info_data, type_data
FROM
(
	SELECT USER.id AS user_id, USER.username AS user_username, USER.password AS user_password, USER.created_at AS user_created_at, USER.updated_at AS user_updated_at,
	CONCAT_WS('||', USER.id, USER.username, USER.password, USER.created_at, USER.updated_at) AS info_data, 'USER' AS type_data
	FROM $schema.USER AS USER
	UNION ALL
	SELECT USER.id AS user_id, USER.username AS user_username, USER.password AS user_password, USER.created_at AS user_created_at, USER.updated_at AS user_updated_at,
	CONCAT_WS('||', PROFILE.id, PROFILE.lastname, PROFILE.firstname, CONCAT_WS(';', EMAIL.id, EMAIL.address), PROFILE.last_login, PROFILE.is_active, PROFILE.user_id, PROFILE.organization_id, PROFILE.created_at, PROFILE.updated_at) AS info_data, 'PROFILE' AS type_data
	FROM $schema.USER AS USER
	INNER JOIN $schema.PROFILE AS PROFILE ON PROFILE.user_id = USER.id
	INNER JOIN $schema.PROFILE_EMAIL AS PROFILE_EMAIL ON PROFILE_EMAIL.profile_id = PROFILE.id
	INNER JOIN $schema.EMAIL AS EMAIL ON PROFILE_EMAIL.email_id = EMAIL.id
	WHERE PROFILE_EMAIL.is_main = TRUE
)