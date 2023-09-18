package io.agamis.fusion.api.rest.model.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.agamis.fusion.api.rest.model.dto.common.LanguageMapping
import io.agamis.fusion.api.rest.model.dto.permission.PermissionDto
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

import java.io.FileNotFoundException
import java.io.InputStream
import java.time.Instant
import java.util.UUID

class PermissionDtoSpec extends AnyFlatSpec with Matchers with MockitoSugar {

    private val permissionDtoJson: String = {
        val permissionDtoFileName = "permissionDto.json"
        val permissionDtoFilePath = getClass.getCanonicalName
            .split("\\.")
            .dropRight(1)
            .mkString("/") + "/" + permissionDtoFileName
        val permissionDtoStreamedFile =
            getClass.getClassLoader.getResourceAsStream(permissionDtoFilePath)
        permissionDtoStreamedFile match {
            case null => throw new FileNotFoundException()
            case input: InputStream =>
                scala.io.Source.fromInputStream(input).getLines().mkString("\n")
        }
    }

    "A PermissionDto" should "serialize to JSON" in {
        val mapper = new ObjectMapper()
        mapper.registerModule(DefaultScalaModule)
        mapper.registerModule(new JavaTimeModule())
        val data = mapper
            .writer()
            .writeValueAsString(
              new PermissionDto(
                Some(UUID.fromString("00c19b37-6be0-4ebf-805b-909bd6e25dd0")),
                "a.sample.permission",
                List[LanguageMapping](
                  new LanguageMapping(
                    UUID.fromString("ce772d05-9f6c-48d8-82ad-139bd67df5e4"),
                    UUID.fromString("b24c0669-8977-46a9-ab81-6881912f912d"),
                    "fr-FR",
                    "a french label"
                  ),
                  new LanguageMapping(
                    UUID.fromString("1a913e70-fc7d-4b0f-8935-94462c9d161a"),
                    UUID.fromString("ae9236c1-b1a9-44d7-b448-a8ec0cec8c0e"),
                    "en-US",
                    "an english label"
                  )
                ),
                List[LanguageMapping](
                  new LanguageMapping(
                    UUID.fromString("65f2db7c-ba3e-4412-885d-db32035b6c9e"),
                    UUID.fromString("cc4c0bbb-67ae-41ac-a776-1e45b5eae444"),
                    "fr-FR",
                    "a french description"
                  ),
                  new LanguageMapping(
                    UUID.fromString("06b49441-f112-47b2-9f32-68291f6d6dbb"),
                    UUID.fromString("ee8e7789-b6e8-46aa-b64d-40022957dab6"),
                    "en-US",
                    "an english description"
                  )
                ),
                None,
                true,
                Some(Instant.ofEpochSecond(1675777966, 118575000)),
                Some(Instant.ofEpochSecond(1675777966, 118575000))
              )
            )
        data should equal(permissionDtoJson)
    }

    it should "deserialize from JSON" in {
        val mapper = new ObjectMapper()
        mapper.registerModule(DefaultScalaModule)
        mapper.registerModule(new JavaTimeModule())
        val fromJson =
            mapper.readValue(permissionDtoJson, classOf[PermissionDto])
        val permission =
            new PermissionDto(
              Some(UUID.fromString("00c19b37-6be0-4ebf-805b-909bd6e25dd0")),
              "a.sample.permission",
              List[LanguageMapping](
                new LanguageMapping(
                  UUID.fromString("ce772d05-9f6c-48d8-82ad-139bd67df5e4"),
                  UUID.fromString("b24c0669-8977-46a9-ab81-6881912f912d"),
                  "fr-FR",
                  "a french label"
                ),
                new LanguageMapping(
                  UUID.fromString("1a913e70-fc7d-4b0f-8935-94462c9d161a"),
                  UUID.fromString("ae9236c1-b1a9-44d7-b448-a8ec0cec8c0e"),
                  "en-US",
                  "an english label"
                )
              ),
              List[LanguageMapping](
                new LanguageMapping(
                  UUID.fromString("65f2db7c-ba3e-4412-885d-db32035b6c9e"),
                  UUID.fromString("cc4c0bbb-67ae-41ac-a776-1e45b5eae444"),
                  "fr-FR",
                  "a french description"
                ),
                new LanguageMapping(
                  UUID.fromString("06b49441-f112-47b2-9f32-68291f6d6dbb"),
                  UUID.fromString("ee8e7789-b6e8-46aa-b64d-40022957dab6"),
                  "en-US",
                  "an english description"
                )
              ),
              None,
              true,
              Some(Instant.ofEpochSecond(1675777966, 118575000)),
              Some(Instant.ofEpochSecond(1675777966, 118575000))
            )
        permission should equal(fromJson)
    }
}
