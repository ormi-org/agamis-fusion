INSERT INTO "FUSION"."USER" (_key, ID, PASSWORD, USERNAME, CREATED_AT, UPDATED_AT)
VALUES
('3ba76d44-e818-440a-86b6-aff41f48f578', '3ba76d44-e818-440a-86b6-aff41f48f578', 'passwordHash', 'user1', '2021-05-06 17:47:52', '2021-05-06 17:47:52'),
('5d2662a3-797f-42de-bb7d-ca9fcac70f12', '5d2662a3-797f-42de-bb7d-ca9fcac70f12', 'passwordHash', 'user2', '2021-05-06 17:47:52', '2021-05-06 17:47:52');
INSERT INTO "FUSION"."PROFILE" (_key, ID, LASTNAME, FIRSTNAME, LAST_LOGIN, IS_ACTIVE, USER_ID, ORGANIZATION_ID, CREATED_AT, UPDATED_AT)
VALUES
('4a91a858-3abc-4666-a851-ab73b4bd867f', '4a91a858-3abc-4666-a851-ab73b4bd867f', 'Michel', 'Dupont', '2021-05-06 17:47:52', 1, '3ba76d44-e818-440a-86b6-aff41f48f578', 'bf508646-1247-497c-8000-9ee8c0e5041b', '2021-05-06 17:47:52', '2021-05-06 17:47:52'),
('c1ce450b-ba5c-4f3d-8051-3109bd6802b2', 'c1ce450b-ba5c-4f3d-8051-3109bd6802b2', 'Michel', 'Dupont', '2021-05-06 17:47:52', 1, '3ba76d44-e818-440a-86b6-aff41f48f578', 'fb77f8d2-7032-4f60-9e97-a053a3bc5453', '2021-05-06 17:47:52', '2021-05-06 17:47:52'),
('dc82f32d-1107-4f62-8321-223c64f9b842', 'dc82f32d-1107-4f62-8321-223c64f9b842', 'Michel', 'Dupont', '2021-05-06 17:47:52', 1, '3ba76d44-e818-440a-86b6-aff41f48f578', 'e3e0f60e-53f9-4611-b5d5-53074f482a05', '2021-05-06 17:47:52', '2021-05-06 17:47:52'),
('7b8aa52e-b5af-4e22-8dae-0a36600430a3', '7b8aa52e-b5af-4e22-8dae-0a36600430a3', 'Jean', 'Dupont', '2021-05-06 17:47:52', 1, '5d2662a3-797f-42de-bb7d-ca9fcac70f12', 'bf508646-1247-497c-8000-9ee8c0e5041b', '2021-05-06 17:47:52', '2021-05-06 17:47:52'),
('6d8ad8ef-7546-49f2-b6a9-e0df9a2871d0', '6d8ad8ef-7546-49f2-b6a9-e0df9a2871d0', 'Jean', 'Dupont', '2021-05-06 17:47:52', 1, '5d2662a3-797f-42de-bb7d-ca9fcac70f12', 'fb77f8d2-7032-4f60-9e97-a053a3bc5453', '2021-05-06 17:47:52', '2021-05-06 17:47:52'),
('49f8b895-f0d6-4213-87d6-f22c6c379df5', '49f8b895-f0d6-4213-87d6-f22c6c379df5', 'Jean', 'Dupont', '2021-05-06 17:47:52', 1, '5d2662a3-797f-42de-bb7d-ca9fcac70f12', 'e3e0f60e-53f9-4611-b5d5-53074f482a05', '2021-05-06 17:47:52', '2021-05-06 17:47:52');
INSERT INTO "FUSION"."GROUP" (_key, ID, NAME, ORGANIZATION_ID, CREATED_AT, UPDATED_AT)
VALUES
('a803c71a-6e6a-428a-9e8d-4e544bdb7dc8', 'a803c71a-6e6a-428a-9e8d-4e544bdb7dc8', 'profiles managers', 'fb77f8d2-7032-4f60-9e97-a053a3bc5453', '2021-05-06 17:47:52', '2021-05-06 17:47:52'),
('a10820b0-071c-4146-b26c-c8eaaa9d6bc2', 'a10820b0-071c-4146-b26c-c8eaaa9d6bc2', 'groups managers', 'e3e0f60e-53f9-4611-b5d5-53074f482a05', '2021-05-06 17:47:52', '2021-05-06 17:47:52');
INSERT INTO "FUSION"."EMAIL" (_key, ID, ADDRESS)
VALUES
('b6e0f569-a1c3-4aa8-830f-88482bdea20b', 'b6e0f569-a1c3-4aa8-830f-88482bdea20b', 'michel.dupont@example.com'),
('c9548ab4-db88-4885-b6e0-fb0ef7ca652d', 'c9548ab4-db88-4885-b6e0-fb0ef7ca652d', 'michel.dupont@example2.com'),
('62de3f27-0b2e-402e-8ada-b1da36d81c5c', '62de3f27-0b2e-402e-8ada-b1da36d81c5c', 'jean.dupont@example.com');
INSERT INTO "FUSION"."LANGUAGE" (_key, ID, CODE, LABEL)
VALUES
('ee33cf08-a17c-4588-89f5-b5e1278ba6a2', 'ee33cf08-a17c-4588-89f5-b5e1278ba6a2', 'en-US', 'English - United States'),
('73e073de-4144-4054-8d9b-f6c65f1ca2ef', '73e073de-4144-4054-8d9b-f6c65f1ca2ef', 'fr-FR', 'French - France');
INSERT INTO "FUSION"."TEXT" (_key, ID, LANGUAGE_ID , CONTENT)
VALUES
/*ORG TYPES*/
('56a436e4-6600-4461-a4be-ea0e700db9cc:ee33cf08-a17c-4588-89f5-b5e1278ba6a2', '56a436e4-6600-4461-a4be-ea0e700db9cc', 'ee33cf08-a17c-4588-89f5-b5e1278ba6a2', 'default'),
('56a436e4-6600-4461-a4be-ea0e700db9cc:73e073de-4144-4054-8d9b-f6c65f1ca2ef', '56a436e4-6600-4461-a4be-ea0e700db9cc', '73e073de-4144-4054-8d9b-f6c65f1ca2ef', 'par défaut'),
('d1dd1415-7c73-4437-9c51-3974823d8c7e:ee33cf08-a17c-4588-89f5-b5e1278ba6a2', 'd1dd1415-7c73-4437-9c51-3974823d8c7e', 'ee33cf08-a17c-4588-89f5-b5e1278ba6a2', 'society'),
('d1dd1415-7c73-4437-9c51-3974823d8c7e:73e073de-4144-4054-8d9b-f6c65f1ca2ef', 'd1dd1415-7c73-4437-9c51-3974823d8c7e', '73e073de-4144-4054-8d9b-f6c65f1ca2ef', 'société'),
('4960c9ea-2e65-4ca7-985a-3d96bddc1bd7:ee33cf08-a17c-4588-89f5-b5e1278ba6a2', '4960c9ea-2e65-4ca7-985a-3d96bddc1bd7', 'ee33cf08-a17c-4588-89f5-b5e1278ba6a2', 'non-profit'),
('4960c9ea-2e65-4ca7-985a-3d96bddc1bd7:73e073de-4144-4054-8d9b-f6c65f1ca2ef', '4960c9ea-2e65-4ca7-985a-3d96bddc1bd7', '73e073de-4144-4054-8d9b-f6c65f1ca2ef', 'association'),

/*PERMISSIONS*/
('10f664ff-2424-43b8-b7d8-379bf1e1aac6:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','10f664ff-2424-43b8-b7d8-379bf1e1aac6','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Mount file system'),
('10f664ff-2424-43b8-b7d8-379bf1e1aac6:73e073de-4144-4054-8d9b-f6c65f1ca2ef','10f664ff-2424-43b8-b7d8-379bf1e1aac6','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Monter système de fichier'),
('2de896d2-54b7-4bab-b5fc-e72bc33ebba1:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','2de896d2-54b7-4bab-b5fc-e72bc33ebba1','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow mounting FS on any organization'),
('2de896d2-54b7-4bab-b5fc-e72bc33ebba1:73e073de-4144-4054-8d9b-f6c65f1ca2ef','2de896d2-54b7-4bab-b5fc-e72bc33ebba1','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise le montage d''un système de fichier sur n''importe quelle organisation'),
('863e31c5-dbd7-4542-ad1f-dab5c587e75b:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','863e31c5-dbd7-4542-ad1f-dab5c587e75b','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Unmount file system'),
('863e31c5-dbd7-4542-ad1f-dab5c587e75b:73e073de-4144-4054-8d9b-f6c65f1ca2ef','863e31c5-dbd7-4542-ad1f-dab5c587e75b','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Démonter système de fichier'),
('bbbdf526-fb16-4dde-8447-ca3507b7221c:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','bbbdf526-fb16-4dde-8447-ca3507b7221c','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow unmounting file system on any organization'),
('bbbdf526-fb16-4dde-8447-ca3507b7221c:73e073de-4144-4054-8d9b-f6c65f1ca2ef','bbbdf526-fb16-4dde-8447-ca3507b7221c','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise lee démontage d''un système de fichier de n''importe quelle organization'),
('f2c3aee4-2745-4597-948a-16ff997edb07:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','f2c3aee4-2745-4597-948a-16ff997edb07','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Set default file system'),
('f2c3aee4-2745-4597-948a-16ff997edb07:73e073de-4144-4054-8d9b-f6c65f1ca2ef','f2c3aee4-2745-4597-948a-16ff997edb07','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Établir système de fichier par défaut'),
('9a67d96c-07e1-45d5-af9d-a3b172d042be:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','9a67d96c-07e1-45d5-af9d-a3b172d042be','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow setting the default file system of any organization'),
('9a67d96c-07e1-45d5-af9d-a3b172d042be:73e073de-4144-4054-8d9b-f6c65f1ca2ef','9a67d96c-07e1-45d5-af9d-a3b172d042be','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise le choix du système de fichier par défaut de n''importe quelle organisation'),

('793883b9-9c09-4bf3-966b-df2b476fbac2:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','793883b9-9c09-4bf3-966b-df2b476fbac2','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Create organization'),
('793883b9-9c09-4bf3-966b-df2b476fbac2:73e073de-4144-4054-8d9b-f6c65f1ca2ef','793883b9-9c09-4bf3-966b-df2b476fbac2','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Créer une organisation'),
('715ad86c-ebf8-410c-9417-2796577ddd11:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','715ad86c-ebf8-410c-9417-2796577ddd11','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow creating organization'),
('715ad86c-ebf8-410c-9417-2796577ddd11:73e073de-4144-4054-8d9b-f6c65f1ca2ef','715ad86c-ebf8-410c-9417-2796577ddd11','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la création d''organization'),
('7ecc2816-eba8-4ddf-8f63-07951447cb1b:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','7ecc2816-eba8-4ddf-8f63-07951447cb1b','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Delete organization'),
('7ecc2816-eba8-4ddf-8f63-07951447cb1b:73e073de-4144-4054-8d9b-f6c65f1ca2ef','7ecc2816-eba8-4ddf-8f63-07951447cb1b','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Supprimer une organization'),
('3f8d9942-c4bc-40cb-af50-7d72d5b2eb56:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','3f8d9942-c4bc-40cb-af50-7d72d5b2eb56','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow deleting any organization'),
('3f8d9942-c4bc-40cb-af50-7d72d5b2eb56:73e073de-4144-4054-8d9b-f6c65f1ca2ef','3f8d9942-c4bc-40cb-af50-7d72d5b2eb56','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la suppression de n''importe quelle organisation'),
('44371626-2d80-4f91-99f7-634e9827e0b9:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','44371626-2d80-4f91-99f7-634e9827e0b9','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Modify organization'),
('44371626-2d80-4f91-99f7-634e9827e0b9:73e073de-4144-4054-8d9b-f6c65f1ca2ef','44371626-2d80-4f91-99f7-634e9827e0b9','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Modifier une organisation'),
('4b6b5698-ee80-4a40-a119-de0942e33767:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','4b6b5698-ee80-4a40-a119-de0942e33767','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow updating organization'),
('4b6b5698-ee80-4a40-a119-de0942e33767:73e073de-4144-4054-8d9b-f6c65f1ca2ef','4b6b5698-ee80-4a40-a119-de0942e33767','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la modification d''une organisation'),

('5695e0e2-5f1a-4fe6-91f6-954bdf0134ec:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','5695e0e2-5f1a-4fe6-91f6-954bdf0134ec','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Create user'),
('5695e0e2-5f1a-4fe6-91f6-954bdf0134ec:73e073de-4144-4054-8d9b-f6c65f1ca2ef','5695e0e2-5f1a-4fe6-91f6-954bdf0134ec','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Créer un utilisateur'),
('74cec919-ec31-4f31-b5d7-6b7a7b409a93:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','74cec919-ec31-4f31-b5d7-6b7a7b409a93','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow creating user'),
('74cec919-ec31-4f31-b5d7-6b7a7b409a93:73e073de-4144-4054-8d9b-f6c65f1ca2ef','74cec919-ec31-4f31-b5d7-6b7a7b409a93','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la création d''utilisateur'),
('b60fd17c-0cce-434c-b67a-7eda03e097fd:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','b60fd17c-0cce-434c-b67a-7eda03e097fd','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Delete user'),
('b60fd17c-0cce-434c-b67a-7eda03e097fd:73e073de-4144-4054-8d9b-f6c65f1ca2ef','b60fd17c-0cce-434c-b67a-7eda03e097fd','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Supprimer un utilisateur'),
('e04eae65-fede-47d7-959e-261049779f93:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','e04eae65-fede-47d7-959e-261049779f93','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow deleting user'),
('e04eae65-fede-47d7-959e-261049779f93:73e073de-4144-4054-8d9b-f6c65f1ca2ef','e04eae65-fede-47d7-959e-261049779f93','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la suppression de n''importe quel utilisateur'),
('b6b63864-8676-4142-b85c-2eaa3ccae561:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','b6b63864-8676-4142-b85c-2eaa3ccae561','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Modify user'),
('b6b63864-8676-4142-b85c-2eaa3ccae561:73e073de-4144-4054-8d9b-f6c65f1ca2ef','b6b63864-8676-4142-b85c-2eaa3ccae561','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Modifier un utilisateur'),
('e712c3ff-4afd-4efe-a0f4-7bd6a405e03f:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','e712c3ff-4afd-4efe-a0f4-7bd6a405e03f','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow updating user'),
('e712c3ff-4afd-4efe-a0f4-7bd6a405e03f:73e073de-4144-4054-8d9b-f6c65f1ca2ef','e712c3ff-4afd-4efe-a0f4-7bd6a405e03f','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la modification d''un utilisateur'),

('a188f450-db0d-4683-98b5-3bc07d55b6be:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','a188f450-db0d-4683-98b5-3bc07d55b6be','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Create profile'),
('a188f450-db0d-4683-98b5-3bc07d55b6be:73e073de-4144-4054-8d9b-f6c65f1ca2ef','a188f450-db0d-4683-98b5-3bc07d55b6be','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Créer un profil'),
('e307c04a-99c1-4cde-a969-ff2469778e5e:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','e307c04a-99c1-4cde-a969-ff2469778e5e','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow creating profile in any organization'),
('e307c04a-99c1-4cde-a969-ff2469778e5e:73e073de-4144-4054-8d9b-f6c65f1ca2ef','e307c04a-99c1-4cde-a969-ff2469778e5e','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la création d''un profil dans n''importe quelle organisation'),
('3a82ff72-ab17-45fd-8161-483c48555978:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','3a82ff72-ab17-45fd-8161-483c48555978','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Delete profile'),
('3a82ff72-ab17-45fd-8161-483c48555978:73e073de-4144-4054-8d9b-f6c65f1ca2ef','3a82ff72-ab17-45fd-8161-483c48555978','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Supprimer un profil'),
('a2d7dca0-bce0-4b9a-a833-64443b41a721:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','a2d7dca0-bce0-4b9a-a833-64443b41a721','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow deleting any profile in any organization'),
('a2d7dca0-bce0-4b9a-a833-64443b41a721:73e073de-4144-4054-8d9b-f6c65f1ca2ef','a2d7dca0-bce0-4b9a-a833-64443b41a721','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la suppression de n''importe quel profil de n''importe quelle organisation'),
('234a6125-f25a-4f69-9468-bef510a76aae:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','234a6125-f25a-4f69-9468-bef510a76aae','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Modify profile'),
('234a6125-f25a-4f69-9468-bef510a76aae:73e073de-4144-4054-8d9b-f6c65f1ca2ef','234a6125-f25a-4f69-9468-bef510a76aae','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Modifier un profil'),
('55231430-0640-48d6-9df3-496c7703beba:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','55231430-0640-48d6-9df3-496c7703beba','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow updating any profile in any organization'),
('55231430-0640-48d6-9df3-496c7703beba:73e073de-4144-4054-8d9b-f6c65f1ca2ef','55231430-0640-48d6-9df3-496c7703beba','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la modification de n''importe quel profil de n''importe quelle organisation'),

('3b535c14-6627-4b3b-ad82-3cc9e06a0824:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','3b535c14-6627-4b3b-ad82-3cc9e06a0824','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Create group'),
('3b535c14-6627-4b3b-ad82-3cc9e06a0824:73e073de-4144-4054-8d9b-f6c65f1ca2ef','3b535c14-6627-4b3b-ad82-3cc9e06a0824','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Créer un groupe'),
('71e123e7-bf89-410b-9d1b-90a322eac86b:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','71e123e7-bf89-410b-9d1b-90a322eac86b','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow creating group in any organization'),
('71e123e7-bf89-410b-9d1b-90a322eac86b:73e073de-4144-4054-8d9b-f6c65f1ca2ef','71e123e7-bf89-410b-9d1b-90a322eac86b','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la création d''un profil dans n''importe quelle organisation'),
('717a131f-42d4-436f-83c0-0fbe032b3dc4:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','717a131f-42d4-436f-83c0-0fbe032b3dc4','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Delete group'),
('717a131f-42d4-436f-83c0-0fbe032b3dc4:73e073de-4144-4054-8d9b-f6c65f1ca2ef','717a131f-42d4-436f-83c0-0fbe032b3dc4','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Supprimer un groupe'),
('f01c1ba2-8e3d-4774-b963-1d96e3ad944c:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','f01c1ba2-8e3d-4774-b963-1d96e3ad944c','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow deleting any group in any organization'),
('f01c1ba2-8e3d-4774-b963-1d96e3ad944c:73e073de-4144-4054-8d9b-f6c65f1ca2ef','f01c1ba2-8e3d-4774-b963-1d96e3ad944c','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la suppression de n''importe quel groupe de n''importe quelle organisation'),
('e64bdcd1-0332-4ece-8a77-d8abb05dcf1f:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','e64bdcd1-0332-4ece-8a77-d8abb05dcf1f','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Modify group'),
('e64bdcd1-0332-4ece-8a77-d8abb05dcf1f:73e073de-4144-4054-8d9b-f6c65f1ca2ef','e64bdcd1-0332-4ece-8a77-d8abb05dcf1f','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Modifier un groupe'),
('960cbaea-22c0-466b-8f39-522c8131e2bc:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','960cbaea-22c0-466b-8f39-522c8131e2bc','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow updating any group in any organization'),
('960cbaea-22c0-466b-8f39-522c8131e2bc:73e073de-4144-4054-8d9b-f6c65f1ca2ef','960cbaea-22c0-466b-8f39-522c8131e2bc','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la modification de n''importe quel groupe de n''importe quelle organisation'),

('5a1ba6e6-0a9a-4ae4-a004-178d56df6b64:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','5a1ba6e6-0a9a-4ae4-a004-178d56df6b64','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Add application'),
('5a1ba6e6-0a9a-4ae4-a004-178d56df6b64:73e073de-4144-4054-8d9b-f6c65f1ca2ef','5a1ba6e6-0a9a-4ae4-a004-178d56df6b64','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Ajouter une application'),
('41c3bf93-3dbf-4869-8826-73cdf32bf02d:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','41c3bf93-3dbf-4869-8826-73cdf32bf02d','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow adding application to any organization'),
('41c3bf93-3dbf-4869-8826-73cdf32bf02d:73e073de-4144-4054-8d9b-f6c65f1ca2ef','41c3bf93-3dbf-4869-8826-73cdf32bf02d','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise l''ajout d''une application à n''importe quelle organisation'),
('56eaaf20-7d36-4c50-bcb1-62d77839ae6b:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','56eaaf20-7d36-4c50-bcb1-62d77839ae6b','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Manage application'),
('56eaaf20-7d36-4c50-bcb1-62d77839ae6b:73e073de-4144-4054-8d9b-f6c65f1ca2ef','56eaaf20-7d36-4c50-bcb1-62d77839ae6b','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Gérer une application'),
('144a847f-fd7f-49c7-892b-cd39706decad:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','144a847f-fd7f-49c7-892b-cd39706decad','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow managing application in any organization'),
('144a847f-fd7f-49c7-892b-cd39706decad:73e073de-4144-4054-8d9b-f6c65f1ca2ef','144a847f-fd7f-49c7-892b-cd39706decad','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la gestion d''une application de n''importe quelle organisation'),

('8c3ad2e9-cc88-4c65-a462-bd1ee8995097:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','8c3ad2e9-cc88-4c65-a462-bd1ee8995097','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Install applications'),
('8c3ad2e9-cc88-4c65-a462-bd1ee8995097:73e073de-4144-4054-8d9b-f6c65f1ca2ef','8c3ad2e9-cc88-4c65-a462-bd1ee8995097','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Installer des applications'),
('3d40e2b8-5741-49a0-b74c-3cdc2371bdad:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','3d40e2b8-5741-49a0-b74c-3cdc2371bdad','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow installing applications'),
('3d40e2b8-5741-49a0-b74c-3cdc2371bdad:73e073de-4144-4054-8d9b-f6c65f1ca2ef','3d40e2b8-5741-49a0-b74c-3cdc2371bdad','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise l''installation d''applications'),
('eb6c24e6-7068-40e8-9a2c-93fddd27a9c3:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','eb6c24e6-7068-40e8-9a2c-93fddd27a9c3','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Uninstall applications'),
('eb6c24e6-7068-40e8-9a2c-93fddd27a9c3:73e073de-4144-4054-8d9b-f6c65f1ca2ef','eb6c24e6-7068-40e8-9a2c-93fddd27a9c3','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Désinstaller des applications'),
('9a6dccef-f4e0-4c9f-ae3d-eb8a27a75106:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','9a6dccef-f4e0-4c9f-ae3d-eb8a27a75106','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow uninstalling applications'),
('9a6dccef-f4e0-4c9f-ae3d-eb8a27a75106:73e073de-4144-4054-8d9b-f6c65f1ca2ef','9a6dccef-f4e0-4c9f-ae3d-eb8a27a75106','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la désinstallation d''applications'),
('7ab9a7c9-5640-4d2e-9c58-39bb1f0b3639:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','7ab9a7c9-5640-4d2e-9c58-39bb1f0b3639','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Import application'),
('7ab9a7c9-5640-4d2e-9c58-39bb1f0b3639:73e073de-4144-4054-8d9b-f6c65f1ca2ef','7ab9a7c9-5640-4d2e-9c58-39bb1f0b3639','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Importer une application'),
('4c8cf976-89e6-46b9-833a-c3c3620085c6:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','4c8cf976-89e6-46b9-833a-c3c3620085c6','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow importing an application in ecosystem'),
('4c8cf976-89e6-46b9-833a-c3c3620085c6:73e073de-4144-4054-8d9b-f6c65f1ca2ef','4c8cf976-89e6-46b9-833a-c3c3620085c6','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise l''importation d''une application dans l''ecosystème'),
('9e3de56a-471e-4305-8b19-c14f205f9126:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','9e3de56a-471e-4305-8b19-c14f205f9126','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Delete application'),
('9e3de56a-471e-4305-8b19-c14f205f9126:73e073de-4144-4054-8d9b-f6c65f1ca2ef','9e3de56a-471e-4305-8b19-c14f205f9126','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Supprimer une application'),
('721c1c74-c08f-45a4-8cfe-4b01acd9fe8f:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','721c1c74-c08f-45a4-8cfe-4b01acd9fe8f','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow deleting application in ecosystem'),
('721c1c74-c08f-45a4-8cfe-4b01acd9fe8f:73e073de-4144-4054-8d9b-f6c65f1ca2ef','721c1c74-c08f-45a4-8cfe-4b01acd9fe8f','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la suppression d''une application dans l''ecosystème'),
('a0d6cc8f-12f2-4083-b7c9-f8457bfae51e:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','a0d6cc8f-12f2-4083-b7c9-f8457bfae51e','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Create application related permission'),
('a0d6cc8f-12f2-4083-b7c9-f8457bfae51e:73e073de-4144-4054-8d9b-f6c65f1ca2ef','a0d6cc8f-12f2-4083-b7c9-f8457bfae51e','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Créer une permission d''application'),
('a72fe4a9-8272-4401-899a-2c12e883d0ef:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','a72fe4a9-8272-4401-899a-2c12e883d0ef','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow creating a permission related to a specific application'),
('a72fe4a9-8272-4401-899a-2c12e883d0ef:73e073de-4144-4054-8d9b-f6c65f1ca2ef','a72fe4a9-8272-4401-899a-2c12e883d0ef','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la création d''une permission en relation avec une application spécifique'),
('33bb2848-c0e4-46d8-ae39-d6bcd9a7b720:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','33bb2848-c0e4-46d8-ae39-d6bcd9a7b720','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Delete application related permission'),
('33bb2848-c0e4-46d8-ae39-d6bcd9a7b720:73e073de-4144-4054-8d9b-f6c65f1ca2ef','33bb2848-c0e4-46d8-ae39-d6bcd9a7b720','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Supprimer une permission d''application'),
('9434e9ef-1357-4b1a-9955-c56f1ac5be61:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','9434e9ef-1357-4b1a-9955-c56f1ac5be61','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow deleting a permission related to a specific application'),
('9434e9ef-1357-4b1a-9955-c56f1ac5be61:73e073de-4144-4054-8d9b-f6c65f1ca2ef','9434e9ef-1357-4b1a-9955-c56f1ac5be61','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la suppression d''une permission en relation avec une application spécifique'),
('0b4f30ab-98aa-4d80-bca5-4f7f9e1c0289:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','0b4f30ab-98aa-4d80-bca5-4f7f9e1c0289','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Modify application related permission'),
('0b4f30ab-98aa-4d80-bca5-4f7f9e1c0289:73e073de-4144-4054-8d9b-f6c65f1ca2ef','0b4f30ab-98aa-4d80-bca5-4f7f9e1c0289','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Modifier une permission d''application'),
('044ae53f-75be-4c7a-b499-28bac302aae9:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','044ae53f-75be-4c7a-b499-28bac302aae9','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow updating a permission related to a specific application'),
('044ae53f-75be-4c7a-b499-28bac302aae9:73e073de-4144-4054-8d9b-f6c65f1ca2ef','044ae53f-75be-4c7a-b499-28bac302aae9','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la modification d''une permission en relation avec une application spécifique'),

('b9dc5653-be6e-460f-99d9-5067536d5349:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','b9dc5653-be6e-460f-99d9-5067536d5349','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Create organization type'),
('b9dc5653-be6e-460f-99d9-5067536d5349:73e073de-4144-4054-8d9b-f6c65f1ca2ef','b9dc5653-be6e-460f-99d9-5067536d5349','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Créer un type d''organization'),
('b0a8743e-64d3-4fff-bf2a-151b21398a72:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','b0a8743e-64d3-4fff-bf2a-151b21398a72','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow creating an organization type'),
('b0a8743e-64d3-4fff-bf2a-151b21398a72:73e073de-4144-4054-8d9b-f6c65f1ca2ef','b0a8743e-64d3-4fff-bf2a-151b21398a72','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la création d''un type d''organisation'),
('8675a617-57ec-42f2-8189-661a04038569:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','8675a617-57ec-42f2-8189-661a04038569','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Delete organization type'),
('8675a617-57ec-42f2-8189-661a04038569:73e073de-4144-4054-8d9b-f6c65f1ca2ef','8675a617-57ec-42f2-8189-661a04038569','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Supprimer un type d''organization'),
('d7872a45-cb44-4af8-a533-efe0260f02c7:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','d7872a45-cb44-4af8-a533-efe0260f02c7','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow deleting an organization type'),
('d7872a45-cb44-4af8-a533-efe0260f02c7:73e073de-4144-4054-8d9b-f6c65f1ca2ef','d7872a45-cb44-4af8-a533-efe0260f02c7','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la création d''un type d''organisation'),
('dd0cecc6-d8ba-413c-9a81-778bd7f9ab09:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','dd0cecc6-d8ba-413c-9a81-778bd7f9ab09','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Modify organization type'),
('dd0cecc6-d8ba-413c-9a81-778bd7f9ab09:73e073de-4144-4054-8d9b-f6c65f1ca2ef','dd0cecc6-d8ba-413c-9a81-778bd7f9ab09','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Modifier un type d''organization'),
('5afb1d87-0e86-44be-b2f3-850de7fc3e3c:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','5afb1d87-0e86-44be-b2f3-850de7fc3e3c','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow updating an organization type'),
('5afb1d87-0e86-44be-b2f3-850de7fc3e3c:73e073de-4144-4054-8d9b-f6c65f1ca2ef','5afb1d87-0e86-44be-b2f3-850de7fc3e3c','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la modification d''un type d''organisation'),

('b52c1559-6a20-4d9f-b44f-d0bfe19c7e00:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','b52c1559-6a20-4d9f-b44f-d0bfe19c7e00','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Add language'),
('b52c1559-6a20-4d9f-b44f-d0bfe19c7e00:73e073de-4144-4054-8d9b-f6c65f1ca2ef','b52c1559-6a20-4d9f-b44f-d0bfe19c7e00','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Ajouter une langue'),
('ea8e759c-0324-4c89-9927-231c2ead3cba:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','ea8e759c-0324-4c89-9927-231c2ead3cba','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow adding a language'),
('ea8e759c-0324-4c89-9927-231c2ead3cba:73e073de-4144-4054-8d9b-f6c65f1ca2ef','ea8e759c-0324-4c89-9927-231c2ead3cba','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise l''ajout d''une langue'),
('defd2795-2ecb-429b-b7e0-67de8bb1a374:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','defd2795-2ecb-429b-b7e0-67de8bb1a374','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Delete language'),
('defd2795-2ecb-429b-b7e0-67de8bb1a374:73e073de-4144-4054-8d9b-f6c65f1ca2ef','defd2795-2ecb-429b-b7e0-67de8bb1a374','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Supprimer une langue'),
('96795717-f710-48a1-9b04-a12d184a14f2:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','96795717-f710-48a1-9b04-a12d184a14f2','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow deleting a language'),
('96795717-f710-48a1-9b04-a12d184a14f2:73e073de-4144-4054-8d9b-f6c65f1ca2ef','96795717-f710-48a1-9b04-a12d184a14f2','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la suppression d''une langue'),
('f6f3c89b-17c2-4387-a9a3-4b9455ecde4e:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','f6f3c89b-17c2-4387-a9a3-4b9455ecde4e','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Modify language'),
('f6f3c89b-17c2-4387-a9a3-4b9455ecde4e:73e073de-4144-4054-8d9b-f6c65f1ca2ef','f6f3c89b-17c2-4387-a9a3-4b9455ecde4e','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Modifier une langue'),
('18ba0221-ec8e-4fe1-a36d-3a1e68640e89:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','18ba0221-ec8e-4fe1-a36d-3a1e68640e89','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow updating a language'),
('18ba0221-ec8e-4fe1-a36d-3a1e68640e89:73e073de-4144-4054-8d9b-f6c65f1ca2ef','18ba0221-ec8e-4fe1-a36d-3a1e68640e89','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la modification d''une langue'),

('a82bef92-e27e-4ec3-8564-b1a785a42794:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','a82bef92-e27e-4ec3-8564-b1a785a42794','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Create permission'),
('a82bef92-e27e-4ec3-8564-b1a785a42794:73e073de-4144-4054-8d9b-f6c65f1ca2ef','a82bef92-e27e-4ec3-8564-b1a785a42794','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Créer une permission'),
('19ffc818-9a11-4428-9ed4-7c5904eb099d:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','19ffc818-9a11-4428-9ed4-7c5904eb099d','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow creating a permission'),
('19ffc818-9a11-4428-9ed4-7c5904eb099d:73e073de-4144-4054-8d9b-f6c65f1ca2ef','19ffc818-9a11-4428-9ed4-7c5904eb099d','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la création d''une permission'),
('36c4850a-c77b-47b5-a026-1ead49e8a1df:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','36c4850a-c77b-47b5-a026-1ead49e8a1df','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Delete permission'),
('36c4850a-c77b-47b5-a026-1ead49e8a1df:73e073de-4144-4054-8d9b-f6c65f1ca2ef','36c4850a-c77b-47b5-a026-1ead49e8a1df','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Supprimer une permission'),
('c5af9225-3389-4988-a750-072ad83ab84a:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','c5af9225-3389-4988-a750-072ad83ab84a','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow deleting a permission'),
('c5af9225-3389-4988-a750-072ad83ab84a:73e073de-4144-4054-8d9b-f6c65f1ca2ef','c5af9225-3389-4988-a750-072ad83ab84a','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la suppression d''une permission'),
('7ea4127b-a29d-4659-8fe4-323b9d986373:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','7ea4127b-a29d-4659-8fe4-323b9d986373','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Modify permission'),
('7ea4127b-a29d-4659-8fe4-323b9d986373:73e073de-4144-4054-8d9b-f6c65f1ca2ef','7ea4127b-a29d-4659-8fe4-323b9d986373','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Modifier une permission'),
('a0965851-5dd1-4730-b6b5-a1c3f6de6ab8:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','a0965851-5dd1-4730-b6b5-a1c3f6de6ab8','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow updating a permission'),
('a0965851-5dd1-4730-b6b5-a1c3f6de6ab8:73e073de-4144-4054-8d9b-f6c65f1ca2ef','a0965851-5dd1-4730-b6b5-a1c3f6de6ab8','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la modification d''une permission'),

('71e242ca-365f-4b1f-802f-4c3c32f4c6ee:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','71e242ca-365f-4b1f-802f-4c3c32f4c6ee','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Create file system'),
('71e242ca-365f-4b1f-802f-4c3c32f4c6ee:73e073de-4144-4054-8d9b-f6c65f1ca2ef','71e242ca-365f-4b1f-802f-4c3c32f4c6ee','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Créer un système de fichier'),
('ff54a6d6-5bba-4a4f-8016-0e6b6b6d7e9a:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','ff54a6d6-5bba-4a4f-8016-0e6b6b6d7e9a','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow creating a file system'),
('ff54a6d6-5bba-4a4f-8016-0e6b6b6d7e9a:73e073de-4144-4054-8d9b-f6c65f1ca2ef','ff54a6d6-5bba-4a4f-8016-0e6b6b6d7e9a','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la création d''un système de fichier'),
('1064a2ff-5648-4191-8378-9b9c37d61b65:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','1064a2ff-5648-4191-8378-9b9c37d61b65','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Manage file system'),
('1064a2ff-5648-4191-8378-9b9c37d61b65:73e073de-4144-4054-8d9b-f6c65f1ca2ef','1064a2ff-5648-4191-8378-9b9c37d61b65','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Gérer un système de fichier'),
('0d6d9391-4c60-46ca-a7f6-b545a7cd8e8c:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','0d6d9391-4c60-46ca-a7f6-b545a7cd8e8c','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow managing a file system'),
('0d6d9391-4c60-46ca-a7f6-b545a7cd8e8c:73e073de-4144-4054-8d9b-f6c65f1ca2ef','0d6d9391-4c60-46ca-a7f6-b545a7cd8e8c','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la gestion d''un système de fichier'),
('832c4cc1-76ec-412f-b47d-24038218ed1e:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','832c4cc1-76ec-412f-b47d-24038218ed1e','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Delete file system'),
('832c4cc1-76ec-412f-b47d-24038218ed1e:73e073de-4144-4054-8d9b-f6c65f1ca2ef','832c4cc1-76ec-412f-b47d-24038218ed1e','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Supprimer un système de fichier'),
('b2f089c3-b123-4cc1-9a3e-cd60828c146a:ee33cf08-a17c-4588-89f5-b5e1278ba6a2','b2f089c3-b123-4cc1-9a3e-cd60828c146a','ee33cf08-a17c-4588-89f5-b5e1278ba6a2','Allow deleting a file system'),
('b2f089c3-b123-4cc1-9a3e-cd60828c146a:73e073de-4144-4054-8d9b-f6c65f1ca2ef','b2f089c3-b123-4cc1-9a3e-cd60828c146a','73e073de-4144-4054-8d9b-f6c65f1ca2ef','Autorise la suppression d''un système de fichier');
INSERT INTO "FUSION"."ORGANIZATIONTYPE" (_key, ID, LABEL_TEXT_ID, CREATED_AT, UPDATED_AT)
VALUES
('3ee11bd4-6083-4f9d-9064-f8024e626a3c', '3ee11bd4-6083-4f9d-9064-f8024e626a3c', '56a436e4-6600-4461-a4be-ea0e700db9cc', '2021-05-06 17:47:52', '2021-05-06 17:47:52'),
('3a81266a-5875-425c-a276-01d1aff1e187', '3a81266a-5875-425c-a276-01d1aff1e187', 'd1dd1415-7c73-4437-9c51-3974823d8c7e', '2021-05-06 17:47:52', '2021-05-06 17:47:52'),
('b38bd4db-3b5d-4dac-9af2-078c5e2972ad', 'b38bd4db-3b5d-4dac-9af2-078c5e2972ad', '4960c9ea-2e65-4ca7-985a-3d96bddc1bd7', '2021-05-06 17:47:52', '2021-05-06 17:47:52');
INSERT INTO "FUSION"."ORGANIZATION" (_key, ID, LABEL, ORGANIZATIONTYPE_ID, QUERYABLE, CREATED_AT, UPDATED_AT)
VALUES
('bf508646-1247-497c-8000-9ee8c0e5041b', 'bf508646-1247-497c-8000-9ee8c0e5041b', 'default', '3ee11bd4-6083-4f9d-9064-f8024e626a3c', 0, '2021-05-06 17:47:52', '2021-05-06 17:47:52'),
('fb77f8d2-7032-4f60-9e97-a053a3bc5453', 'fb77f8d2-7032-4f60-9e97-a053a3bc5453', 'org1', 'b38bd4db-3b5d-4dac-9af2-078c5e2972ad', 1, '2021-05-06 17:47:52', '2021-05-06 17:47:52'),
('e3e0f60e-53f9-4611-b5d5-53074f482a05', 'e3e0f60e-53f9-4611-b5d5-53074f482a05', 'org2', '3a81266a-5875-425c-a276-01d1aff1e187', 1, '2021-05-06 17:47:52', '2021-05-06 17:47:52');
INSERT INTO "FUSION"."FILESYSTEM" (_key, ID, ROOTDIR_ID, LABEL, SHARED, CREATED_AT, UPDATED_AT)
VALUES
('2bbdca80-7097-4a7c-aa4d-a0808992c7c5', '2bbdca80-7097-4a7c-aa4d-a0808992c7c5', '5fb5b887-6f85-4bc3-bd2f-500145513ee4', 'RADICAL PLANET', 0, '2021-05-06 17:47:52', '2021-05-06 17:47:52'),
('3bf5c748-fecc-476a-8759-06cac217f556', '3bf5c748-fecc-476a-8759-06cac217f556', 'b7f673fe-e13d-45ed-8d97-bd3f862a78e6', 'TINY UNIFORM', 1, '2021-05-06 17:47:52', '2021-05-06 17:47:52'),
('69da5cf0-e63c-4c3f-9762-28e004992f78', '69da5cf0-e63c-4c3f-9762-28e004992f78', 'aa559b06-934d-40e4-b16f-4a3626aef07f', 'INEVITABLE MOOR', 0, '2021-05-06 17:47:52', '2021-05-06 17:47:52'),
('e9c0d799-458f-40d2-ad87-76c61d746191', 'e9c0d799-458f-40d2-ad87-76c61d746191', '083239b2-3e6e-4a5f-a1e2-85ee46e512de', 'EXCESS FEEDBACK', 0, '2021-05-06 17:47:52', '2021-05-06 17:47:52');
INSERT INTO "FUSION"."APPLICATION" (_key, ID, APP_UNIVERSAL_ID, VERSION, STATUS, MANIFEST_URL, STORE_URL, CREATED_AT, UPDATED_AT)
VALUES
('7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced', '7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced', 'c9189963-c698-3c8e-b1d8-f93f7b42b262', '1.0.0', 1, 'https://repo.ogdt.io/applications/fusion/1.0.0/manifest.xml', 'https://launch.ogdt.io/store/apps/details?id=io.ogdt.apps.fusion&version=1.0.0', '2021-05-06 17:47:52', '2021-05-06 17:47:52'),
('f4ba4e3c-a343-4adb-a088-93cc42db2434', 'f4ba4e3c-a343-4adb-a088-93cc42db2434', 'b3fe1e38-00b5-39d3-82f2-53693de1e388', '1.0.0', 1, 'https://repo.ogdt.io/applications/workspace/1.0.0/manifest.xml', 'https://launch.ogdt.io/store/apps/details?id=io.ogdt.apps.workspace&version=1.0.0', '2021-05-06 17:47:52', '2021-05-06 17:47:52');
INSERT INTO "FUSION"."PERMISSION" (_key, ID, "KEY", LABEL_TEXT_ID, DESCRIPTION_TEXT_ID, EDITABLE, APP_ID, CREATED_AT, UPDATED_AT)
VALUES
('9af8b5bb-65b9-4fd3-8b4d-b3dcf09a0590','9af8b5bb-65b9-4fd3-8b4d-b3dcf09a0590','io.ogdt.fusion.organization.*.fs.mount','10f664ff-2424-43b8-b7d8-379bf1e1aac6','2de896d2-54b7-4bab-b5fc-e72bc33ebba1',0,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),
('43addfd2-c2c9-4bf1-ab6b-cd52e07cb166','43addfd2-c2c9-4bf1-ab6b-cd52e07cb166','io.ogdt.fusion.organization.*.fs.unmount','863e31c5-dbd7-4542-ad1f-dab5c587e75b','bbbdf526-fb16-4dde-8447-ca3507b7221c',1,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),
('74515934-a8f1-4428-9bbc-3d66ba583e16','74515934-a8f1-4428-9bbc-3d66ba583e16','io.ogdt.fusion.organization.*.fs.set_default','f2c3aee4-2745-4597-948a-16ff997edb07','9a67d96c-07e1-45d5-af9d-a3b172d042be',1,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),

('1deb5637-d5eb-437a-ac6e-6b8327da13e1','1deb5637-d5eb-437a-ac6e-6b8327da13e1','io.ogdt.fusion.organization.create','793883b9-9c09-4bf3-966b-df2b476fbac2','715ad86c-ebf8-410c-9417-2796577ddd11',0,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),
('fba48cd5-ea38-4972-9fa6-b52aba36862a','fba48cd5-ea38-4972-9fa6-b52aba36862a','io.ogdt.fusion.organization.*.delete','7ecc2816-eba8-4ddf-8f63-07951447cb1b','3f8d9942-c4bc-40cb-af50-7d72d5b2eb56',1,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),
('1c22938e-2d03-4300-b2f5-6c0708e6eace','1c22938e-2d03-4300-b2f5-6c0708e6eace','io.ogdt.fusion.organization.*.update','44371626-2d80-4f91-99f7-634e9827e0b9','4b6b5698-ee80-4a40-a119-de0942e33767',0,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),

('d38f60c4-cca6-47fa-a506-9ebc1509563e','d38f60c4-cca6-47fa-a506-9ebc1509563e','io.ogdt.fusion.user.create','5695e0e2-5f1a-4fe6-91f6-954bdf0134ec','74cec919-ec31-4f31-b5d7-6b7a7b409a93',0,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),
('62fd3609-c0a9-426b-a553-d119b16d8e99','62fd3609-c0a9-426b-a553-d119b16d8e99','io.ogdt.fusion.user.*.delete','b60fd17c-0cce-434c-b67a-7eda03e097fd','e04eae65-fede-47d7-959e-261049779f93',0,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),
('792652e4-369d-46c3-b929-2a46217462fc','792652e4-369d-46c3-b929-2a46217462fc','io.ogdt.fusion.user.*.update','b6b63864-8676-4142-b85c-2eaa3ccae561','e712c3ff-4afd-4efe-a0f4-7bd6a405e03f',1,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),

('a54671d8-d25b-4da8-99cf-1b76bd3462fc','a54671d8-d25b-4da8-99cf-1b76bd3462fc','io.ogdt.fusion.organization.*.profile.create','a188f450-db0d-4683-98b5-3bc07d55b6be','e307c04a-99c1-4cde-a969-ff2469778e5e',1,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),
('e19236fe-771d-482e-bc77-b4699e7fd4b9','e19236fe-771d-482e-bc77-b4699e7fd4b9','io.ogdt.fusion.organization.*.profile.*.delete','3a82ff72-ab17-45fd-8161-483c48555978','a2d7dca0-bce0-4b9a-a833-64443b41a721',1,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),
('6d8dfc30-4b02-424c-a88d-18d605473798','6d8dfc30-4b02-424c-a88d-18d605473798','io.ogdt.fusion.organization.*.profile.*.update','234a6125-f25a-4f69-9468-bef510a76aae','55231430-0640-48d6-9df3-496c7703beba',1,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),

('dd4e1992-9267-4810-b6c4-94a24b3b8320','dd4e1992-9267-4810-b6c4-94a24b3b8320','io.ogdt.fusion.organization.*.group.create','3b535c14-6627-4b3b-ad82-3cc9e06a0824','71e123e7-bf89-410b-9d1b-90a322eac86b',0,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),
('dbfd05cd-f0f2-499e-8f6e-abd3e8673366','dbfd05cd-f0f2-499e-8f6e-abd3e8673366','io.ogdt.fusion.organization.*.group.*.delete','717a131f-42d4-436f-83c0-0fbe032b3dc4','f01c1ba2-8e3d-4774-b963-1d96e3ad944c',0,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),
('f49a95e9-0a91-4f8f-9dde-60846f507ff5','f49a95e9-0a91-4f8f-9dde-60846f507ff5','io.ogdt.fusion.organization.*.group.*.update','e64bdcd1-0332-4ece-8a77-d8abb05dcf1f','960cbaea-22c0-466b-8f39-522c8131e2bc',0,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),

('086161c5-25f2-4fbc-9cc3-2c84258d906e','086161c5-25f2-4fbc-9cc3-2c84258d906e','io.ogdt.fusion.organization.*.application.add','5a1ba6e6-0a9a-4ae4-a004-178d56df6b64','41c3bf93-3dbf-4869-8826-73cdf32bf02d',1,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),
('25f7823a-00d9-4ee5-8153-5e891bb59d53','25f7823a-00d9-4ee5-8153-5e891bb59d53','io.ogdt.fusion.organization.*.application.*.manage','56eaaf20-7d36-4c50-bcb1-62d77839ae6b','144a847f-fd7f-49c7-892b-cd39706decad',0,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),

('493bb36b-621b-49c7-a3af-6a22bc3224cd','493bb36b-621b-49c7-a3af-6a22bc3224cd','io.ogdt.fusion.application.install','8c3ad2e9-cc88-4c65-a462-bd1ee8995097','3d40e2b8-5741-49a0-b74c-3cdc2371bdad',1,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),
('30d6dd5b-c899-432e-aeb9-aee9de6a6af5','30d6dd5b-c899-432e-aeb9-aee9de6a6af5','io.ogdt.fusion.application.uninstall','eb6c24e6-7068-40e8-9a2c-93fddd27a9c3','9a6dccef-f4e0-4c9f-ae3d-eb8a27a75106',1,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),
('9ca6029c-44ed-49a1-8cba-ca7dd117cf5c','9ca6029c-44ed-49a1-8cba-ca7dd117cf5c','io.ogdt.fusion.application.add','7ab9a7c9-5640-4d2e-9c58-39bb1f0b3639','4c8cf976-89e6-46b9-833a-c3c3620085c6',1,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),
('8b234dbf-222d-4f7a-b7fd-62b7d7194017','8b234dbf-222d-4f7a-b7fd-62b7d7194017','io.ogdt.fusion.application.delete','9e3de56a-471e-4305-8b19-c14f205f9126','721c1c74-c08f-45a4-8cfe-4b01acd9fe8f',0,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),
('0539de4b-3258-46b3-a3c6-70c0236decb5','0539de4b-3258-46b3-a3c6-70c0236decb5','io.ogdt.fusion.application.*.permission.create','a0d6cc8f-12f2-4083-b7c9-f8457bfae51e','a72fe4a9-8272-4401-899a-2c12e883d0ef',1,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),
('f58fdc9b-c305-473f-a159-cee2ee5d9cfb','f58fdc9b-c305-473f-a159-cee2ee5d9cfb','io.ogdt.fusion.application.*.permission.*.delete','33bb2848-c0e4-46d8-ae39-d6bcd9a7b720','9434e9ef-1357-4b1a-9955-c56f1ac5be61',0,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),
('f0278ac9-daa6-4f5a-b273-c0c2a7dfb3bc','f0278ac9-daa6-4f5a-b273-c0c2a7dfb3bc','io.ogdt.fusion.application.*.permission.*.update','0b4f30ab-98aa-4d80-bca5-4f7f9e1c0289','044ae53f-75be-4c7a-b499-28bac302aae9',0,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),

('731e4711-8d9e-42be-8ea7-123fd8bea33d','731e4711-8d9e-42be-8ea7-123fd8bea33d','io.ogdt.fusion.organizationtype.create','b9dc5653-be6e-460f-99d9-5067536d5349','b0a8743e-64d3-4fff-bf2a-151b21398a72',0,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),
('66b892b5-f100-4e3b-a1c7-df36837cfd29','66b892b5-f100-4e3b-a1c7-df36837cfd29','io.ogdt.fusion.organizationtype.*.delete','8675a617-57ec-42f2-8189-661a04038569','d7872a45-cb44-4af8-a533-efe0260f02c7',1,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),
('3cc0aa75-d971-4b72-a6b9-abe2fe4eac19','3cc0aa75-d971-4b72-a6b9-abe2fe4eac19','io.ogdt.fusion.organizationtype.*.update','dd0cecc6-d8ba-413c-9a81-778bd7f9ab09','5afb1d87-0e86-44be-b2f3-850de7fc3e3c',1,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),

('33d95a77-0f01-416d-9ff9-db7ee4361b0f','33d95a77-0f01-416d-9ff9-db7ee4361b0f','io.ogdt.fusion.language.create','b52c1559-6a20-4d9f-b44f-d0bfe19c7e00','ea8e759c-0324-4c89-9927-231c2ead3cba',0,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),
('df3cee86-d58c-470b-bad9-d15f16eb6999','df3cee86-d58c-470b-bad9-d15f16eb6999','io.ogdt.fusion.language.*.delete','defd2795-2ecb-429b-b7e0-67de8bb1a374','96795717-f710-48a1-9b04-a12d184a14f2',0,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),
('456244d6-d206-48fd-98b2-f7aa99e60d58','456244d6-d206-48fd-98b2-f7aa99e60d58','io.ogdt.fusion.language.*.update','f6f3c89b-17c2-4387-a9a3-4b9455ecde4e','18ba0221-ec8e-4fe1-a36d-3a1e68640e89',0,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),

('136e20f2-5abd-44e8-afd3-548fff183e16','136e20f2-5abd-44e8-afd3-548fff183e16','io.ogdt.fusion.permission.create','a82bef92-e27e-4ec3-8564-b1a785a42794','19ffc818-9a11-4428-9ed4-7c5904eb099d',0,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),
('87219148-b0bd-462d-be49-4647daaca86f','87219148-b0bd-462d-be49-4647daaca86f','io.ogdt.fusion.permission.*.delete','36c4850a-c77b-47b5-a026-1ead49e8a1df','c5af9225-3389-4988-a750-072ad83ab84a',0,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),
('05fedc63-7900-4136-8056-c5d5f3f84012','05fedc63-7900-4136-8056-c5d5f3f84012','io.ogdt.fusion.permission.*.update','7ea4127b-a29d-4659-8fe4-323b9d986373','a0965851-5dd1-4730-b6b5-a1c3f6de6ab8',1,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),

('582f13ee-b2e6-4407-a7ae-6a4bd847112d','582f13ee-b2e6-4407-a7ae-6a4bd847112d','io.ogdt.fusion.fs.create','71e242ca-365f-4b1f-802f-4c3c32f4c6ee','ff54a6d6-5bba-4a4f-8016-0e6b6b6d7e9a',0,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),
('6290da41-3f06-49ea-b6de-efd867b3b6c5','6290da41-3f06-49ea-b6de-efd867b3b6c5','io.ogdt.fusion.fs.*.manage','1064a2ff-5648-4191-8378-9b9c37d61b65','0d6d9391-4c60-46ca-a7f6-b545a7cd8e8c',0,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52'),
('38cfa511-a58d-452d-9393-f7716997fe28','38cfa511-a58d-452d-9393-f7716997fe28','io.ogdt.fusion.fs.*.delete','832c4cc1-76ec-412f-b47d-24038218ed1e','b2f089c3-b123-4cc1-9a3e-cd60828c146a',0,'7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced','2021-05-06 17:47:52','2021-05-06 17:47:52');
INSERT INTO "FUSION"."PROFILE_PERMISSION" (_key, PROFILE_ID, PERMISSION_ID)
VALUES
('4a91a858-3abc-4666-a851-ab73b4bd867f:1deb5637-d5eb-437a-ac6e-6b8327da13e1','4a91a858-3abc-4666-a851-ab73b4bd867f','1deb5637-d5eb-437a-ac6e-6b8327da13e1'),
('4a91a858-3abc-4666-a851-ab73b4bd867f:fba48cd5-ea38-4972-9fa6-b52aba36862a','4a91a858-3abc-4666-a851-ab73b4bd867f','fba48cd5-ea38-4972-9fa6-b52aba36862a'),
('4a91a858-3abc-4666-a851-ab73b4bd867f:1c22938e-2d03-4300-b2f5-6c0708e6eace','4a91a858-3abc-4666-a851-ab73b4bd867f','1c22938e-2d03-4300-b2f5-6c0708e6eace'),
('4a91a858-3abc-4666-a851-ab73b4bd867f:d38f60c4-cca6-47fa-a506-9ebc1509563e','4a91a858-3abc-4666-a851-ab73b4bd867f','d38f60c4-cca6-47fa-a506-9ebc1509563e'),
('7b8aa52e-b5af-4e22-8dae-0a36600430a3:1deb5637-d5eb-437a-ac6e-6b8327da13e1','7b8aa52e-b5af-4e22-8dae-0a36600430a3','1deb5637-d5eb-437a-ac6e-6b8327da13e1'),
('7b8aa52e-b5af-4e22-8dae-0a36600430a3:fba48cd5-ea38-4972-9fa6-b52aba36862a','7b8aa52e-b5af-4e22-8dae-0a36600430a3','fba48cd5-ea38-4972-9fa6-b52aba36862a'),
('7b8aa52e-b5af-4e22-8dae-0a36600430a3:1c22938e-2d03-4300-b2f5-6c0708e6eace','7b8aa52e-b5af-4e22-8dae-0a36600430a3','1c22938e-2d03-4300-b2f5-6c0708e6eace'),
('7b8aa52e-b5af-4e22-8dae-0a36600430a3:d38f60c4-cca6-47fa-a506-9ebc1509563e','7b8aa52e-b5af-4e22-8dae-0a36600430a3','d38f60c4-cca6-47fa-a506-9ebc1509563e');
INSERT INTO "FUSION"."GROUP_PERMISSION" (_key, GROUP_ID, PERMISSION_ID)
VALUES
('a803c71a-6e6a-428a-9e8d-4e544bdb7dc8:a54671d8-d25b-4da8-99cf-1b76bd3462fc','a803c71a-6e6a-428a-9e8d-4e544bdb7dc8','a54671d8-d25b-4da8-99cf-1b76bd3462fc'),
('a803c71a-6e6a-428a-9e8d-4e544bdb7dc8:e19236fe-771d-482e-bc77-b4699e7fd4b9','a803c71a-6e6a-428a-9e8d-4e544bdb7dc8','e19236fe-771d-482e-bc77-b4699e7fd4b9'),
('a803c71a-6e6a-428a-9e8d-4e544bdb7dc8:6d8dfc30-4b02-424c-a88d-18d605473798','a803c71a-6e6a-428a-9e8d-4e544bdb7dc8','6d8dfc30-4b02-424c-a88d-18d605473798'),
('a10820b0-071c-4146-b26c-c8eaaa9d6bc2:dd4e1992-9267-4810-b6c4-94a24b3b8320','a10820b0-071c-4146-b26c-c8eaaa9d6bc2','dd4e1992-9267-4810-b6c4-94a24b3b8320'),
('a10820b0-071c-4146-b26c-c8eaaa9d6bc2:dbfd05cd-f0f2-499e-8f6e-abd3e8673366','a10820b0-071c-4146-b26c-c8eaaa9d6bc2','dbfd05cd-f0f2-499e-8f6e-abd3e8673366'),
('a10820b0-071c-4146-b26c-c8eaaa9d6bc2:f49a95e9-0a91-4f8f-9dde-60846f507ff5','a10820b0-071c-4146-b26c-c8eaaa9d6bc2','f49a95e9-0a91-4f8f-9dde-60846f507ff5');
INSERT INTO "FUSION"."PROFILE_EMAIL" (_key, PROFILE_ID, EMAIL_ID, IS_MAIN)
VALUES
('4a91a858-3abc-4666-a851-ab73b4bd867f:b6e0f569-a1c3-4aa8-830f-88482bdea20b', '4a91a858-3abc-4666-a851-ab73b4bd867f', 'b6e0f569-a1c3-4aa8-830f-88482bdea20b', 1),
('4a91a858-3abc-4666-a851-ab73b4bd867f:c9548ab4-db88-4885-b6e0-fb0ef7ca652d', '4a91a858-3abc-4666-a851-ab73b4bd867f', 'c9548ab4-db88-4885-b6e0-fb0ef7ca652d', 0),
('c1ce450b-ba5c-4f3d-8051-3109bd6802b2:b6e0f569-a1c3-4aa8-830f-88482bdea20b', 'c1ce450b-ba5c-4f3d-8051-3109bd6802b2', 'b6e0f569-a1c3-4aa8-830f-88482bdea20b', 1),
('dc82f32d-1107-4f62-8321-223c64f9b842:b6e0f569-a1c3-4aa8-830f-88482bdea20b', 'dc82f32d-1107-4f62-8321-223c64f9b842', 'b6e0f569-a1c3-4aa8-830f-88482bdea20b', 1),
('dc82f32d-1107-4f62-8321-223c64f9b842:c9548ab4-db88-4885-b6e0-fb0ef7ca652d', 'dc82f32d-1107-4f62-8321-223c64f9b842', 'c9548ab4-db88-4885-b6e0-fb0ef7ca652d', 0),
('7b8aa52e-b5af-4e22-8dae-0a36600430a3:62de3f27-0b2e-402e-8ada-b1da36d81c5c', '7b8aa52e-b5af-4e22-8dae-0a36600430a3', '62de3f27-0b2e-402e-8ada-b1da36d81c5c', 1),
('6d8ad8ef-7546-49f2-b6a9-e0df9a2871d0:62de3f27-0b2e-402e-8ada-b1da36d81c5c', '6d8ad8ef-7546-49f2-b6a9-e0df9a2871d0', '62de3f27-0b2e-402e-8ada-b1da36d81c5c', 1),
('49f8b895-f0d6-4213-87d6-f22c6c379df5:62de3f27-0b2e-402e-8ada-b1da36d81c5c', '49f8b895-f0d6-4213-87d6-f22c6c379df5', '62de3f27-0b2e-402e-8ada-b1da36d81c5c', 1);
INSERT INTO "FUSION"."FILESYSTEM_ORGANIZATION" (_key, FILESYSTEM_ID, ORGANIZATION_ID, IS_DEFAULT)
VALUES
('2bbdca80-7097-4a7c-aa4d-a0808992c7c5:bf508646-1247-497c-8000-9ee8c0e5041b', '2bbdca80-7097-4a7c-aa4d-a0808992c7c5', 'bf508646-1247-497c-8000-9ee8c0e5041b', 1),
('3bf5c748-fecc-476a-8759-06cac217f556:fb77f8d2-7032-4f60-9e97-a053a3bc5453', '3bf5c748-fecc-476a-8759-06cac217f556', 'fb77f8d2-7032-4f60-9e97-a053a3bc5453', 0),
('3bf5c748-fecc-476a-8759-06cac217f556:e3e0f60e-53f9-4611-b5d5-53074f482a05', '3bf5c748-fecc-476a-8759-06cac217f556', 'e3e0f60e-53f9-4611-b5d5-53074f482a05', 0),
('69da5cf0-e63c-4c3f-9762-28e004992f78:fb77f8d2-7032-4f60-9e97-a053a3bc5453', '69da5cf0-e63c-4c3f-9762-28e004992f78', 'fb77f8d2-7032-4f60-9e97-a053a3bc5453', 1),
('e9c0d799-458f-40d2-ad87-76c61d746191:e3e0f60e-53f9-4611-b5d5-53074f482a05', 'e9c0d799-458f-40d2-ad87-76c61d746191', 'e3e0f60e-53f9-4611-b5d5-53074f482a05', 1);
INSERT INTO "FUSION"."ORGANIZATION_APPLICATION" (_key, ORGANIZATION_ID, APPLICATION_ID, STATUS, LICENSE_FILE_FS_ID, LICENSE_FILE_ID)
VALUES
('fb77f8d2-7032-4f60-9e97-a053a3bc5453:7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced', 'fb77f8d2-7032-4f60-9e97-a053a3bc5453', '7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced', 0, '69da5cf0-e63c-4c3f-9762-28e004992f78', '84794dc5-707d-4d1a-a06b-318e5d347c3e'),
('fb77f8d2-7032-4f60-9e97-a053a3bc5453:f4ba4e3c-a343-4adb-a088-93cc42db2434', 'fb77f8d2-7032-4f60-9e97-a053a3bc5453', 'f4ba4e3c-a343-4adb-a088-93cc42db2434', 0, '69da5cf0-e63c-4c3f-9762-28e004992f78', '46a39a82-d7ce-476a-b76e-9f31fb1b4cbe'),
('e3e0f60e-53f9-4611-b5d5-53074f482a05:7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced', 'e3e0f60e-53f9-4611-b5d5-53074f482a05', '7c3e3fe5-ec7f-46da-b3bf-4bc4ed36eced', 0, 'e9c0d799-458f-40d2-ad87-76c61d746191', '78cae382-3c21-422b-8c80-ba17cb04a2d5'),
('e3e0f60e-53f9-4611-b5d5-53074f482a05:f4ba4e3c-a343-4adb-a088-93cc42db2434', 'e3e0f60e-53f9-4611-b5d5-53074f482a05', 'f4ba4e3c-a343-4adb-a088-93cc42db2434', 0, 'e9c0d799-458f-40d2-ad87-76c61d746191', '09d3991c-41e3-4bb8-b630-47870cde6429');
INSERT INTO "FUSION"."PROFILE_GROUP" (_key, PROFILE_ID, GROUP_ID)
VALUES
('c1ce450b-ba5c-4f3d-8051-3109bd6802b2:a803c71a-6e6a-428a-9e8d-4e544bdb7dc8', 'c1ce450b-ba5c-4f3d-8051-3109bd6802b2', 'a803c71a-6e6a-428a-9e8d-4e544bdb7dc8'),
('6d8ad8ef-7546-49f2-b6a9-e0df9a2871d0:a803c71a-6e6a-428a-9e8d-4e544bdb7dc8', '6d8ad8ef-7546-49f2-b6a9-e0df9a2871d0', 'a803c71a-6e6a-428a-9e8d-4e544bdb7dc8'),
('dc82f32d-1107-4f62-8321-223c64f9b842:a10820b0-071c-4146-b26c-c8eaaa9d6bc2', 'dc82f32d-1107-4f62-8321-223c64f9b842', 'a10820b0-071c-4146-b26c-c8eaaa9d6bc2'),
('49f8b895-f0d6-4213-87d6-f22c6c379df5:a10820b0-071c-4146-b26c-c8eaaa9d6bc2', '49f8b895-f0d6-4213-87d6-f22c6c379df5', 'a10820b0-071c-4146-b26c-c8eaaa9d6bc2');