package io.ogdt.fusion.db

// import java.util.ArrayList

// import io.ogdt.fusion.db.drivers.MongoDriver
// import io.ogdt.fusion.db.drivers.CacheDriver
// import io.ogdt.fusion.db.drivers.SqlDriver

/*
DbHandler class that will handle all db connections (Ignite, SQL Cache, MongoDB, Key-Value)
*/

class DbHandler() {

    // private val mongoConnections: ArrayList[MongoDriver] = new ArrayList[MongoDriver]()
    // private val cacheConnections: ArrayList[CacheDriver] = new ArrayList[CacheDriver]()
    // private val sqlConnections: ArrayList[SqlDriver] = new ArrayList[SqlDriver]()

    // def makeMongoDriver(): Unit ={
    //     val newMongoDriver: MongoDriver = new MongoDriver()
    //     this.mongoConnections.add(newMongoDriver)
    // }

    // def makeSqlDriver(): Unit ={
    //     val newSqlDriver: SqlDriver = new SqlDriver()
    //     this.sqlConnections.add(newSqlDriver)
    // }

    // def makeCacheDriver(): Unit ={
    //     val newCacheDriver: CacheDriver = new CacheDriver()
    //     this.cacheConnections.add(newCacheDriver)
    // }

    // def getIgniteInstance(): Ignite ={
    //     // TODO: à modifier
    //     return null
    // }
}

object DbHandler extends DbHandler {
    // def apply(): Unit ={
    //     makeSqlDriver
    // }
}