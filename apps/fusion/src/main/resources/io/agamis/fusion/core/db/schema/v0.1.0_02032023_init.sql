-- USER TABLE SECTION

CREATE TABLE `FUSION`.`USER` (
  ID VARCHAR(36) NOT NULL PRIMARY KEY,
  PASSWORD VARCHAR(255) NOT NULL,
  USERNAME VARCHAR(255) NOT NULL,
  CREATED_AT TIMESTAMP NOT NULL,
  UPDATED_AT TIMESTAMP NOT NULL
) WITH "template=FusionSQL,cache_name=SQL_FUSION_USER,key_type=java.util.UUID,value_type=io.agamis.fusion.core.db.models.sql.User";

-- PROFILE TABLES SECTION

CREATE TABLE `FUSION`.`PROFILE` (
  ID VARCHAR(36) NOT NULL PRIMARY KEY,
  ALIAS VARCHAR(255),
  LASTNAME VARCHAR(255) NOT NULL,
  FIRSTNAME VARCHAR(255) NOT NULL,
  LAST_LOGIN TIMESTAMP NOT NULL,
  IS_ACTIVE TINYINT(1) NOT NULL,
  USER_ID VARCHAR(36) NOT NULL,
  ORGANIZATION_ID VARCHAR(36) NOT NULL,
  CREATED_AT TIMESTAMP NOT NULL,
  UPDATED_AT TIMESTAMP NOT NULL
) WITH "template=FusionSQL,cache_name=SQL_FUSION_PROFILE,key_type=java.util.UUID,value_type=io.agamis.fusion.core.db.models.sql.Profile";

CREATE TABLE `FUSION`.`PROFILE_PERMISSION` (
  `KEY` VARCHAR(73) NOT NULL PRIMARY KEY,
  PROFILE_ID VARCHAR(36) NOT NULL,
  PERMISSION_ID VARCHAR(36) NOT NULL,
) WITH "template=FusionSQL,cache_name=SQL_FUSION_PROFILE_PERMISSION,key_type=java.lang.String,value_type=io.agamis.fusion.core.db.models.sql.ProfilePermission";

CREATE TABLE `FUSION`.`PROFILE_EMAIL` (
  `KEY` VARCHAR(73) NOT NULL PRIMARY KEY,
  PROFILE_ID VARCHAR(36) NOT NULL,
  EMAIL_ID VARCHAR(36) NOT NULL,
  IS_MAIN TINYINT(1) NOT NULL
) WITH "template=FusionSQL,cache_name=SQL_FUSION_PROFILE_EMAIL,key_type=java.lang.String,value_type=io.agamis.fusion.core.db.models.sql.ProfileEmail";

CREATE TABLE `FUSION`.`PROFILE_GROUP` (
  `KEY` VARCHAR(73) NOT NULL PRIMARY KEY,
  PROFILE_ID VARCHAR(36) NOT NULL,
  GROUP_ID VARCHAR(36) NOT NULL
) WITH "template=FusionSQL,cache_name=SQL_FUSION_PROFILE_GROUP,key_type=java.lang.String,value_type=io.agamis.fusion.core.db.models.sql.ProfileGroup";

-- GROUP TABLES SECTION

CREATE TABLE `FUSION`.`GROUP` (
  ID VARCHAR(36) NOT NULL PRIMARY KEY,
  NAME VARCHAR(255) NOT NULL,
  ORGANIZATION_ID VARCHAR(36) NOT NULL,
  CREATED_AT TIMESTAMP NOT NULL,
  UPDATED_AT TIMESTAMP NOT NULL
) WITH "template=FusionSQL,cache_name=SQL_FUSION_GROUP,key_type=java.util.UUID,value_type=io.agamis.fusion.core.db.models.sql.Group";

CREATE TABLE `FUSION`.`GROUP_PERMISSION` (
  `KEY` VARCHAR(73) NOT NULL PRIMARY KEY,
  GROUP_ID VARCHAR(36) NOT NULL,
  PERMISSION_ID VARCHAR(36) NOT NULL
) WITH "template=FusionSQL,cache_name=SQL_FUSION_GROUP_PERMISSION,key_type=java.lang.String,value_type=io.agamis.fusion.core.db.models.sql.GroupPermission";

-- EMAIL TABLE SECTION

CREATE TABLE `FUSION`.`EMAIL` (
  ID VARCHAR(36) NOT NULL PRIMARY KEY,
  ADDRESS VARCHAR(255) NOT NULL
) WITH "template=FusionSQL,cache_name=SQL_FUSION_EMAIL,key_type=java.util.UUID,value_type=io.agamis.fusion.core.db.models.sql.Email";

-- LANGUAGE TABLE SECTION

CREATE TABLE `FUSION`.`LANGUAGE` (
  ID VARCHAR(36) NOT NULL PRIMARY KEY,
  CODE VARCHAR(13) NOT NULL,
  LABEL VARCHAR(255) NOT NULL
) WITH "template=FusionSQL,cache_name=SQL_FUSION_LANGUAGE,key_type=java.util.UUID,value_type=io.agamis.fusion.core.db.models.sql.Language";

-- TEXT TABLE SECTION

CREATE TABLE `FUSION`.`TEXT` (
  ID VARCHAR(36) NOT NULL PRIMARY KEY,
  LANGUAGE_ID VARCHAR(36) NOT NULL,
  CONTENT VARCHAR NOT NULL
) WITH "template=FusionSQL,cache_name=SQL_FUSION_TEXT,key_type=java.lang.String,value_type=io.agamis.fusion.core.db.models.sql.Text";

-- ORGANIZATIONTYPE TABLE SECTION

CREATE TABLE `FUSION`.`ORGANIZATIONTYPE` (
  ID VARCHAR(36) NOT NULL PRIMARY KEY,
  LABEL_TEXT_ID VARCHAR(36) NOT NULL,
  CREATED_AT TIMESTAMP NOT NULL,
  UPDATED_AT TIMESTAMP NOT NULL
) WITH "template=FusionSQL,cache_name=SQL_FUSION_ORGANIZATIONTYPE,key_type=java.util.UUID,value_type=io.agamis.fusion.core.db.models.sql.OrganizationType";

-- ORGANIZATION TABLES SECTION

CREATE TABLE `FUSION`.`ORGANIZATION` (
  ID VARCHAR(36) NOT NULL PRIMARY KEY,
  LABEL VARCHAR(255) NOT NULL,
  ORGANIZATIONTYPE_ID VARCHAR(36) NOT NULL,
  QUERYABLE TINYINT(1) NOT NULL,
  CREATED_AT TIMESTAMP NOT NULL,
  UPDATED_AT TIMESTAMP NOT NULL
) WITH "template=FusionSQL,cache_name=SQL_FUSION_ORGANIZATION,key_type=java.util.UUID,value_type=io.agamis.fusion.core.db.models.sql.Organization";

CREATE TABLE `FUSION`.`ORGANIZATION_APPLICATION` (
  `KEY` VARCHAR(73) NOT NULL PRIMARY KEY,
  ORGANIZATION_ID VARCHAR(36) NOT NULL,
  APPLICATION_ID VARCHAR(36) NOT NULL,
  `STATUS` TINYINT(1) NOT NULL,
  LICENSE_FILE_FS_ID VARCHAR(36) NOT NULL,
  LICENSE_FILE_ID VARCHAR(36) NOT NULL
) WITH "template=FusionSQL,cache_name=SQL_FUSION_ORGANIZATION_APPLICATION,key_type=java.lang.String,value_type=io.agamis.fusion.core.db.models.sql.OrganizationApplication";

-- FILESYSTEM TABLES SECTION

CREATE TABLE `FUSION`.`FILESYSTEM` (
  ID VARCHAR(36) NOT NULL PRIMARY KEY,
  ROOTDIR_ID VARCHAR(36) NOT NULL,
  LABEL VARCHAR(255) NOT NULL,
  SHARED TINYINT(1) NOT NULL,
  CREATED_AT TIMESTAMP NOT NULL,
  UPDATED_AT TIMESTAMP NOT NULL
) WITH "template=FusionSQL,cache_name=SQL_FUSION_FILESYSTEM,key_type=java.util.UUID,value_type=io.agamis.fusion.core.db.models.sql.FileSystem";

CREATE TABLE `FUSION`.`FILESYSTEM_ORGANIZATION` (
  `KEY` VARCHAR(73) NOT NULL PRIMARY KEY,
  FILESYSTEM_ID VARCHAR(36) NOT NULL,
  ORGANIZATION_ID VARCHAR(36) NOT NULL,
  IS_DEFAULT TINYINT(1) NOT NULL
) WITH "template=FusionSQL,cache_name=SQL_FUSION_FILESYSTEM_ORGANIZATION,key_type=java.lang.String,value_type=io.agamis.fusion.core.db.models.sql.FilesystemOrganization";

-- APPLICATION TABLE SECTION

CREATE TABLE `FUSION`.`APPLICATION` (
  ID VARCHAR(36) NOT NULL PRIMARY KEY,
  APP_UNIVERSAL_ID VARCHAR(36) NOT NULL,
  LABEL VARCHAR(255) NOT NULL,
  VERSION VARCHAR(255) NOT NULL,
  STATUS TINYINT(1) NOT NULL,
  MANIFEST_URL VARCHAR(511) NOT NULL,
  STORE_URL VARCHAR(511) NOT NULL,
  CREATED_AT TIMESTAMP NOT NULL,
  UPDATED_AT TIMESTAMP NOT NULL
) WITH "template=FusionSQL,cache_name=SQL_FUSION_APPLICATION,key_type=java.util.UUID,value_type=io.agamis.fusion.core.db.models.sql.Application";

-- PERMISSION TABLE SECTION

CREATE TABLE `FUSION`.`PERMISSION` (
  ID VARCHAR(36) NOT NULL PRIMARY KEY,
  `KEY` VARCHAR(255) NOT NULL,
  LABEL_TEXT_ID VARCHAR(36) NOT NULL,
  DESCRIPTION_TEXT_ID VARCHAR(36) NOT NULL,
  EDITABLE TINYINT(1) NOT NULL,
  APPLICATION_ID VARCHAR(36) NOT NULL,
  CREATED_AT TIMESTAMP NOT NULL,
  UPDATED_AT TIMESTAMP NOT NULL
) WITH "template=FusionSQL,cache_name=SQL_FUSION_PERMISSION,key_type=java.util.UUID,value_type=io.agamis.fusion.core.db.models.sql.Permission";

