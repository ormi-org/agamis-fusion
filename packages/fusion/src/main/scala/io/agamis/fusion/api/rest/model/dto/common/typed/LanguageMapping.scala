package io.agamis.fusion.api.rest.model.dto.common.typed

import java.util.UUID
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.json.RootJsonFormat

//[(text_id, language_id), (language_code, content)]
final case class LanguageMapping(
    textId: UUID,
    languageId: UUID,
    languageCode: String,
    content: String
)

trait LanguageMappingJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
    import io.agamis.fusion.api.rest.model.dto.common.JsonFormatters.UUIDFormat
    implicit val languageMappingFormat: RootJsonFormat[LanguageMapping] = jsonFormat4(LanguageMapping.apply)
}

object LanguageMappingJsonProtocol extends LanguageMappingJsonSupport
