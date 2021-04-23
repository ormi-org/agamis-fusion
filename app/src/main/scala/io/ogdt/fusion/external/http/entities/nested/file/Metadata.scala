package io.ogdt.fusion.external.http.entities.nested.file

import java.time.Instant
import io.ogdt.fusion.external.http.entities.nested.file.metadata.FusionXmlMeta

import spray.json.{DefaultJsonProtocol, JsonFormat, JsObject, JsValue, JsNumber, JsString, JsBoolean, DeserializationException}
import java.time.format.DateTimeFormatter

final case class Metadata(
    size: Option[Int],
    creationDate: Instant,
    lastVersionDate: Option[Instant],
    lastModificationDate: Instant,
    chainsCount: Option[Int],
    versionsCount: Option[Int],
    fusionXML: Option[FusionXmlMeta],
    hidden: Boolean,
    readonly: Boolean
)

object MetadataJsonProtocol extends DefaultJsonProtocol {

    private val parserISO : DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");


    implicit object MetadataJsonReader extends JsonFormat[Metadata] {
        def read(value: JsValue) = {
            value.asJsObject.getFields("size", "createdDate", "lastVersionDate", "lastModificationDate", "chainsCount", "versionCount", "fusionXml", "hidden", "readonly") match {
                case Seq(JsNumber(size),JsString(parserISO(createdDate)),JsString(parserISO.parseDateTime(lastVersionDate)), JsString(parserISO.parseDateTime(lastModificationDate)),  JsNumber(chainsCount), JsNumber(versionCount), fusionXML, JsBoolean(hidden), JsBoolean(readonly)) => 
                    new Metadata(size.toInt, create, lastVersionDate, lastModificationDate, chainsCount.toInt, versionCount.toInt)
                case _ => throw new DeserializationException("Metadata expected")
            }   
        }
        
        def write(m: Metadata) = JsObject(
            "size"                  -> JsNumber(m.size),
            "createdDate"           -> JsString(m.creationDate),
            "lastVersionDate"       -> JsString(m.lastVersionDate),
            "lastModificationDate"  -> JsString(m.lastModificationDate),
            "chainsCount"           -> JsNumber(m.chainsCount),
            "versionsCount"         -> JsNumber(m.versionCount),
            "fusionXML"             -> fusionXML,
            "hidden"                -> JsBoolean(hidden),
            "readonly"              -> JsBoolean(readonly)
        )
   }
}