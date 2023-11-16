package io.agamis.fusion.api.rest.model.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.agamis.fusion.api.rest.model.dto.permission.PermissionDto
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.FileNotFoundException
import java.io.InputStream
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class PermissionDtoSpec extends AnyFlatSpec with Matchers {

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
                UUID.fromString("00c19b37-6be0-4ebf-805b-909bd6e25dd0"),
                "a.sample.permission",
                true,
                LocalDateTime
                    .ofEpochSecond(1675777966, 118575000, ZoneOffset.UTC),
                LocalDateTime
                    .ofEpochSecond(1675777966, 118575000, ZoneOffset.UTC)
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
              UUID.fromString("00c19b37-6be0-4ebf-805b-909bd6e25dd0"),
              "a.sample.permission",
              true,
              LocalDateTime
                  .ofEpochSecond(1675777966, 118575000, ZoneOffset.UTC),
              LocalDateTime.ofEpochSecond(1675777966, 118575000, ZoneOffset.UTC)
            )
        permission should equal(fromJson)
    }
}
