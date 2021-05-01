package io.ogdt.fusion.core.db.models.sql.typed.annotations

import scala.annotation.StaticAnnotation
import io.ogdt.fusion.core.db.models.sql.typed.Model
import scala.collection.View
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

class PostRemove extends StaticAnnotation

object PostRemove {

    def executeAnnotatedMethods(model: Model)(implicit ec: ExecutionContext): Future[List[Boolean]] = {
        import reflect.runtime.universe._
        
        val classOfModel = model.getClass()
        Future.sequence(
            runtimeMirror(classOfModel.getClassLoader())
            .classSymbol(classOfModel)
            .toType
            .members.view.filter(_.isMethod).filter { method =>
                method.annotations.filter { a =>
                    a.tree.tpe =:= typeOf[PostRemove]
                }.length > 0
            }.toList map { method => 
                classOfModel.getMethod(method.fullName).invoke(model).asInstanceOf[Future[Boolean]]
            }
        )
    }
}