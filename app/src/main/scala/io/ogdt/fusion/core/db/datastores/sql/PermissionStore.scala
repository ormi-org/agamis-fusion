package io.ogdt.fusion.core.db.datastores.sql

import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.ogdt.fusion.core.db.datastores.typed.sql.SqlStoreQuery
import io.ogdt.fusion.core.db.datastores.typed.SqlMutableStore
import io.ogdt.fusion.core.db.datastores.typed.sql.GetEntityFilters
import java.sql.Timestamp
import org.apache.ignite.IgniteCache
import java.util.UUID
import io.ogdt.fusion.core.db.models.sql.Permission
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.cache.CacheAtomicityMode

class PermissionStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlMutableStore[UUID, Permission] {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_PERMISSION"
    override protected var igniteCache: IgniteCache[UUID, Permission] = wrapper.cacheExists(cache) match {
        case true => wrapper.getCache[UUID, Permission](cache)
        case false => {
            wrapper.createCache[UUID, Permission](
                wrapper.makeCacheConfig[UUID, Permission]
                .setCacheMode(CacheMode.REPLICATED)
                .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
                .setDataRegionName("Fusion")
                .setName(cache)
                .setSqlSchema(schema)
                .setIndexedTypes(classOf[UUID], classOf[Permission])
            )
        }
    }

    // Create Permission object 
    def makePermission: Permission = {
        implicit val permissionStore: PermissionStore = this
        new Permission
    }

    // def makePermissionQuery(queryFilters: PermissionStore.GetPermissionsFilter): SqlStoreQuery = {
    //     var queryString: String = 
    //         "Select "
    // }

}

object PermissionStore {
    case class GetPermissionsFilter(
        id: List[String],
        editable: List[Boolean], 
        created_at: Option[(String, Timestamp)],
        updated_at: Option[(String, Timestamp)]
    )
    case class GetPermissionsFilters(
        filters: List[GetPermissionsFilter],
        orderBy: List[(String,Int)]
    ) extends GetEntityFilters
}