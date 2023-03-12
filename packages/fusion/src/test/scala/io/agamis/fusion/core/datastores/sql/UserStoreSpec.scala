package io.agamis.fusion.core.datastores.sql

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll
import io.agamis.fusion.core.db.datastores.sql.UserStore
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import io.agamis.fusion.core.db.datastores.typed.sql.EntityQueryParams
import org.scalatest.matchers.should.Matchers
import org.mockito.ArgumentMatchers._

class UserStoreSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar {

    implicit val wrapper: IgniteClientNodeWrapper = mock[IgniteClientNodeWrapper]
    doReturn(true).when(wrapper).cacheExists(anyString())
    val userStore: UserStore = new UserStore

    "makeUsersQuery" should {
        "return a query on USER cache joining on PROFILE cache When including profiles relation" in {
            val params: UserStore.UserQueryParams = UserStore.UserQueryParams(
                filters = List(),
                orderBy = List(),
                pagination = Some(EntityQueryParams.Pagination(10, 0)),
                include = Some(
                    List(UserStore.Includables.PROFILES.name)
                )
            )

            val sqlQuery = userStore.makeUsersQuery(params)
            sqlQuery.query should include regex "LEFT JOIN.+W_p ON TRUE"
            sqlQuery.query should include regex "LEFT JOIN.+p ON TRUE"
            sqlQuery.query shouldNot include regex ".+$INCLUDE_PROFILES.+"
        }
    }
}
