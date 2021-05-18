package io.ogdt.fusion.external.http.entities.nested.file

import java.time.Instant
import java.text.SimpleDateFormat

import spray.json._
import spray.json.DefaultJsonProtocol._

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

import io.ogdt.fusion.external.http.entities.nested.file.metadata.FusionXmlMeta
import io.ogdt.fusion.external.http.entities.nested.file.metadata.FusionXmlMetaJsonProtocol._

import io.ogdt.fusion.external.http.entities.common.JsonFormatters.InstantFormat

import io.ogdt.fusion.core.db.models.documents.nested.file.{Metadata => MetadataDocument}
import io.ogdt.fusion.core.db.models.documents.nested.file.metadata.{FusionXmlMeta => FusionXmlMetaDocument}

import java.time.{ZoneId,ZoneOffset}
import akka.http.javadsl.model.headers.Date
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.text.SimpleDateFormat
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime
import org.xbill.DNS.Zone


final case class Metadata(
    size: Option[Int],
    creationDate: Instant, // to change
    lastVersionDate: Option[Instant],
    lastModificationDate: Instant,
    chainsCount: Option[Int],
    versionsCount: Option[Int],
    fusionXML: Option[FusionXmlMeta],
    hidden: Boolean,
    readonly: Boolean
)

object Metadata {

    implicit def metadataToMetadataDocument(m: Metadata): MetadataDocument = {
        MetadataDocument(
            m.size,
            m.creationDate,
            m.lastVersionDate,
            m.lastModificationDate,
            m.chainsCount,
            m.versionsCount,
            m.fusionXML.getOrElse(null) match {
                case meta: FusionXmlMeta => Some(meta.copy())
                case null => None
            },
            m.hidden,
            m.readonly
        )
    }

    implicit def documentToMetadata(doc: MetadataDocument): Metadata = {
        Metadata(
            doc.size,
            doc.creationDate,
            doc.lastVersionDate,
            doc.lastModificationDate,
            doc.chainsCount,
            doc.versionsCount,
            doc.fusionXML.getOrElse(null) match {
                case meta: FusionXmlMetaDocument => Some(meta.copy())
                case null => None
            },
            doc.hidden,
            doc.readonly
        )
    }

}

object MetadataJsonProtocol extends DefaultJsonProtocol {

    implicit val metadataFormat = jsonFormat9(Metadata.apply)
 
}

// trait MetadataJsonProtocol extends SprayJsonSupport {
 
//     private val parserDatetime: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
 
//     implicit object MetadataJsonReader extends JsonFormat[Metadata] {
//         def read(value: JsValue) = {
//             value.asJsObject.getFields("size", "createdDate", "lastVersionDate", "lastModificationDate", "chainsCount", "versionCount", "fusionXml", "hidden", "readonly") match {
//                 case Seq(
//                     size,
//                     createdDate,
//                     lastVersionDate,
//                     lastModificationDate,
//                     chainsCount,
//                     versionCount,
//                     fusionXML,
//                     hidden,
//                     readonly 
//                 ) => { 
//                     new Metadata(
//                         Some(size.convertTo[Int]),
//                         createdDate.convertTo[Instant], 
//                         Some(lastVersionDate.convertTo[Instant]), 
//                         lastModificationDate.convertTo[Instant], 
//                         Some(chainsCount.convertTo[Int]), 
//                         Some(versionCount.convertTo[Int]), 
//                         Some(fusionXML.convertTo[FusionXmlMeta]),
//                         hidden.convertTo[Boolean], 
//                         readonly.convertTo[Boolean])
//                     }           
//                 case _ => throw new DeserializationException("Metadata expected")
//             }   
//         }
        
//         def write(m: Metadata) = {
//             var values: List[JsField] = List()
//             m.size match {
//                 case Some(value) => JsNumber(value)
//                 case None => JsNumber(0)
//             }
//             m.creationDate match {
//                 case date: Instant => JsString(date.toString())
//             }

//         }
//    }
// }
