package io.agamis.fusion.api.rest.routes.apps

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.http.scaladsl.server.Directives._

/** Class User Routes
  *
  * @param system
  * @param data
  */
class AppRoutes()(implicit system: ActorSystem[_]) {

    system.executionContext

    val routes =
        concat(
          path("routing")(
            concat(
              path(Segment) { _: String =>
                  concat(
                    path(Remaining) { _: String =>
                        extractRequest { _ =>
                            implicit val logger = system.log
                            // TODO: implement proxying through container registry
                            failWith(new NotImplementedError)
                        }
                    }
                  )
              }
            )
          ),
          path(Segment) { _: String =>
              concat(
                pathSuffix("bootstrap")(
                  failWith(new NotImplementedError)
                  // Redirect to client
                ),
                path("client")(
                  concat(
                    get {
                        failWith(new NotImplementedError)
                    },
                    pathSuffix("assets/endpoints.json")(
                      headerValueByName("agamis-io-fusion-app-sessionId") {
                          _: String =>
                              // TODO : implement endpoints.json generation
                              failWith(new NotImplementedError)
                      }
                    )
                  )
                )
              )
          }
        )
}
