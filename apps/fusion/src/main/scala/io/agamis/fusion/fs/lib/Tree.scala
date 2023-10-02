// package io.agamis.fusion.fs.lib

// import io.agamis.fusion.core.db.datastore.document.FileStore
// import io.agamis.fusion.core.db.datastore.document.exceptions.typed.file.FileAlreadyExistsException
// import io.agamis.fusion.core.db.datastore.document.exceptions.typed.file.FileNotFoundException
// import io.agamis.fusion.core.db.datastores.caches.CachedFileStore
// import io.agamis.fusion.core.db.models.documents.file.File
// import io.agamis.fusion.core.db.models.sql.FileSystem
// import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
// import io.agamis.fusion.core.db.wrappers.mongo.ReactiveMongoWrapper
// import io.agamis.fusion.fs.lib.exceptions.tree.NotADirectoryException
// import io.agamis.fusion.fs.lib.exceptions.tree.PathParsingException
// import io.agamis.fusion.fs.lib.exceptions.tree.UnhandledException
// import io.agamis.fusion.fs.lib.exceptions.tree.UnknownException

// import scala.concurrent.ExecutionContext
// import scala.concurrent.Future
// import scala.jdk.CollectionConverters._
// import scala.util.Failure
// import scala.util.Success

// object Tree {

//     /** Instanciate a new Tree based on a specific [[FileSystem FileSystem]]
//       *
//       * @param fs
//       *   the fileSystem no which the tree is based
//       * @return
//       *   a file tree
//       */
//     def of(fs: FileSystem): Tree = {
//         return new Tree(fs)
//     }
// }

// /** Utilities for [[File File]] Tree manipulation */
// class Tree private (fs: FileSystem) {

//     /** A method for creating a new file in the '''File Tree'''
//       *
//       * [[File File]] is created and added to cache for further faster retrieval
//       *
//       * @note
//       *   This method is Async and returns a [[scala.concurrent.Future Future]]
//       *   which resolves a [[scala.Boolean Boolean]]
//       * @example
//       *   {{{
//       *     TreeManager.createFile(
//       *         new File(...)
//       *     ).onComplete({
//       *         case Success(result) => {
//       *             if (result) // File creation is successful
//       *             else // File creation has failed
//       *         }
//       *         case Failure(cause) => // An exception occurred
//       *     })
//       *   }}}
//       * @param file
//       *   the file to create
//       * @param ec
//       *   the '''implicit''' ExecutionContext used to parallelize computing
//       * @param wrapper
//       *   the '''implicit''' MongoDB wrapper used to read data
//       * @param igniteWrapper
//       *   the '''implicit''' Apache Ignite wrapper used to write cache
//       * @return
//       *   a future boolean which attests the actual creation of the file
//       */

//     def createFile(file: File)(implicit
//         ec: ExecutionContext,
//         wrapper: ReactiveMongoWrapper,
//         igniteWrapper: IgniteClientNodeWrapper
//     ): Future[Boolean] = {
//         file.path match {
//             case Some(path: String) =>
//                 getFileFromPath(path).transformWith({
//                     case Success(_) =>
//                         Future.failed(FileAlreadyExistsException())
//                     case Failure(cause) =>
//                         cause match {
//                             case _: FileNotFoundException =>
//                                 getFileFromPath(
//                                   path.replaceAll("/" + file.name, "")
//                                 ).transformWith({
//                                     case Success(dir) =>
//                                         if (!dir.isDirectory)
//                                             Future.failed(
//                                               NotADirectoryException()
//                                             )
//                                         val fileToInsert: File =
//                                             file.copy(parent = Some(dir.id))
//                                         new FileStore(fs.id.toString)
//                                             .insert(fileToInsert)
//                                             .transformWith({
//                                                 case Success(_) =>
//                                                     new CachedFileStore()
//                                                         .put(fileToInsert)
//                                                     Future.successful(true)
//                                                 case Failure(_) =>
//                                                     Future.successful(false)
//                                             })
//                                     case Failure(cause) => Future.failed(cause)
//                                 })
//                             case cause: Throwable =>
//                                 Future.failed(UnhandledException(cause))
//                             case _ => Future.failed(UnknownException())
//                         }
//                 })
//             case None => Future.failed(PathParsingException())
//         }
//     }

//     /** A method for getting an existing file in the '''File Tree''' by its
//       * '''id'''
//       *
//       * [[File File]] is retrieved from the provided id and stored to cache for
//       * further faster retrieval
//       *
//       * @note
//       *   This method is Async and returns a [[scala.concurrent.Future Future]]
//       * @note
//       *   id '''must''' be a valid BSONObjectId String
//       * @example
//       *   {{{
//       *     TreeManager.getFileFromId(
//       *         "60884d75e1c3cd6fd6f4bdaa"
//       *     ).onComplete({
//       *         case Success(file) => {
//       *             // file variable contains the retrieved File object
//       *         }
//       *         case Failure(cause) => // An exception occurred
//       *     })
//       *   }}}
//       * @param id
//       *   the id to search for
//       * @param ec
//       *   the '''implicit''' ExecutionContext used to parallelize computing
//       * @param wrapper
//       *   the '''implicit''' MongoDB wrapper used to read data
//       * @param igniteWrapper
//       *   the '''implicit''' Apache Ignite wrapper used to read and write cache
//       * @return
//       *   a future file that reflects actual file state in the datastore
//       */
//     def getFileFromId(id: String)(implicit
//         ec: ExecutionContext,
//         wrapper: ReactiveMongoWrapper,
//         igniteWrapper: IgniteClientNodeWrapper
//     ): Future[File] = {
//         new CachedFileStore()
//             .get(id)
//             .transformWith({
//                 case Success(cachedFile) =>
//                     cachedFile match {
//                         case f: File =>
//                             // log "retrieved from cache"
//                             Future.successful(f)
//                         case null =>
//                             new FileStore(fs.id.toString)
//                                 .findByID(id)
//                                 .transformWith({
//                                     case Success(file) =>
//                                         new CachedFileStore()
//                                             .put(file)
//                                         Future.successful(file)
//                                     case Failure(cause) => Future.failed(cause)
//                                 })
//                     }
//                 case Failure(cause) => Future.failed(cause)
//             })
//     }

//     /** A method for getting an existing file in the '''File Tree''' by its
//       * '''path''' (e.g.: /root/directory/file.extension)
//       *
//       * [[File File]] is retrieved from the provided path
//       *
//       * @note
//       *   This method is Async and returns a [[scala.concurrent.Future Future]]
//       * @example
//       *   {{{
//       *     TreeManager.getFileFromPath(
//       *         "/root/directory/file.extension"
//       *     ).onComplete({
//       *         case Success(file) => {
//       *             // file variable contains the retrieved File object
//       *         }
//       *         case Failure(cause) => // An exception occurred
//       *     })
//       *   }}}
//       * @param path
//       *   the path to search for
//       * @param ec
//       *   the '''implicit''' ExecutionContext used to parallelize computing
//       * @param wrapper
//       *   the '''implicit''' MongoDB wrapper used to read data
//       * @return
//       *   a future file that reflects actual file state in the datastore
//       */
//     def getFileFromPath(path: String)(implicit
//         ec: ExecutionContext,
//         wrapper: ReactiveMongoWrapper
//     ): Future[File] = {
//         new FileStore(fs.id.toString)
//             .findByPath(path)
//             .transformWith({
//                 case Success(file)  => Future.successful(file)
//                 case Failure(cause) => Future.failed(cause)
//             })
//     }

//     /** A method for getting several existing files in the '''File Tree''' by
//       * their '''id'''
//       *
//       * [[scala.List List]] of [[File File]] is retrieved from the provided ids,
//       * then each file are stored into cache for further retrieval
//       *
//       * @note
//       *   This method is Async and returns a [[scala.concurrent.Future Future]]
//       * @note
//       *   ids '''must''' be valid BSONObjectId String
//       * @example
//       *   {{{
//       *     TreeManager.getManyFiles(
//       *         "60884d75e1c3cd6fd6f4bdaa", "60884d75e1c3cd6fd6f4bdaa", "60884d75e1c3cd6fd6f4bdaa"
//       *     ).onComplete({
//       *         case Success(files) => {
//       *             // files variable contains the retrieved File objects served in a List
//       *         }
//       *         case Failure(cause) => // An exception occurred
//       *     })
//       *   }}}
//       * @param ids
//       *   repeatable parameters consisting of ids to search for
//       * @param ec
//       *   the '''implicit''' ExecutionContext used to parallelize computing
//       * @param wrapper
//       *   the '''implicit''' MongoDB wrapper used to read data
//       * @param igniteWrapper
//       *   the '''implicit''' Apache Ignite wrapper used to read and write cache
//       * @return
//       *   a future list of files that reflects actual files state in the
//       *   datastore
//       */
//     def getManyFiles(ids: String*)(implicit
//         ec: ExecutionContext,
//         wrapper: ReactiveMongoWrapper,
//         igniteWrapper: IgniteClientNodeWrapper
//     ): Future[List[File]] = {
//         new CachedFileStore()
//             .getMany(ids.toList)
//             .transformWith({
//                 case Success(files) =>
//                     val cacheRetrievedFiles: List[File] =
//                         files.asScala.values.toList

//                     // Return if all files are retrieved from the cache (no need to query datastore)
//                     if (cacheRetrievedFiles.length == ids.length)
//                         return Future.successful(cacheRetrievedFiles)

//                     new FileStore(fs.id.toString)
//                         .findMany(ids.toList diff files.asScala.keys.toList)
//                         .transformWith({
//                             case Success(files) =>
//                                 new CachedFileStore()
//                                     .putMany(files)
//                                 Future.successful(files ++ cacheRetrievedFiles)
//                             case Failure(cause) => Future.failed(cause)
//                         })
//                 case Failure(cause) => Future.failed(cause)
//             })
//     }

//     /** A method for getting several existing children files in the '''File
//       * Tree''' by their '''parent''' aka "dir"
//       *
//       * [[scala.List List]] of children [[File File]] is retrieved from the
//       * provided parent
//       *
//       * @note
//       *   This method is Async and returns a [[scala.concurrent.Future Future]]
//       * @example
//       *   {{{
//       *     TreeManager.getChildrenOf(
//       *         new File(...) // or a pre fetched file
//       *     ).onComplete({
//       *         case Success(files) => {
//       *             // files variable contains the retrieved File objects served in a List
//       *         }
//       *         case Failure(cause) => // An exception occurred
//       *     })
//       *   }}}
//       * @param dir
//       *   a file which act as parent of the wanted files
//       * @param ec
//       *   the '''implicit''' ExecutionContext used to parallelize computing
//       * @param wrapper
//       *   the '''implicit''' MongoDB wrapper used to read data
//       * @return
//       *   a future list of files that reflects actual files state in the
//       *   datastore
//       */
//     def getChildrenOf(dir: File)(implicit
//         ec: ExecutionContext,
//         wrapper: ReactiveMongoWrapper
//     ): Future[List[File]] = {
//         new FileStore(fs.id.toString)
//             .getFileChildren(dir)
//             .transformWith({
//                 case Success(files) => Future.successful(files)
//                 case Failure(cause) => Future.failed(cause)
//             })
//     }

//     /** A method for getting parent file in the '''File Tree''' by one of its
//       * '''child''' aka "file"
//       *
//       * parent [[File File]] is retrieved from the provided child
//       *
//       * @note
//       *   This method is Async and returns a [[scala.concurrent.Future Future]]
//       * @example
//       *   {{{
//       *     TreeManager.getParentOf(
//       *         // a pre fetched file
//       *     ).onComplete({
//       *         case Success(file) => {
//       *             // file variable contains the retrieved File object
//       *         }
//       *         case Failure(cause) => // An exception occurred
//       *     })
//       *   }}}
//       * @param ec
//       *   the '''implicit''' ExecutionContext used to parallelize computing
//       * @param file
//       *   a file which act as child of the wanted file
//       * @param wrapper
//       *   the '''implicit''' MongoDB wrapper used to read data
//       * @return
//       *   a future file that reflects actual file state in the datastore
//       */
//     def getParentOf(file: File)(implicit
//         ec: ExecutionContext,
//         wrapper: ReactiveMongoWrapper
//     ): Future[File] = {
//         file.path match {
//             case Some(path: String) =>
//                 getFileFromPath(path.replaceAll("/" + file.name, ""))
//             case None =>
//                 Future.failed(
//                   new Exception("Couldn't parse value 'path' from File object")
//                 )
//         }
//     }

//     /** A method for updating a file in the '''File Tree'''
//       *
//       * [[File File]] is updated using the internal Id, then file is
//       * stored/updated into cache for further retrieval
//       *
//       * @note
//       *   This method is Async and returns a [[scala.concurrent.Future Future]]
//       * @example
//       *   {{{
//       *     TreeManager.updateFile(
//       *         // a pre fetched modified file
//       *     ).onComplete({
//       *         case Success(file) => {
//       *             // file variable contains the retrieved File object
//       *         }
//       *         case Failure(cause) => // An exception occurred
//       *     })
//       *   }}}
//       * @param file
//       *   a file which act as updated state of the file
//       * @param ec
//       *   the '''implicit''' ExecutionContext used to parallelize computing
//       * @param wrapper
//       *   the '''implicit''' MongoDB wrapper used to read data
//       * @param igniteWrapper
//       *   the '''implicit''' Apache Ignite wrapper used to write cache
//       * @return
//       *   a future file which reflects new file state in the datastore
//       */
//     def updateFile(file: File)(implicit
//         ec: ExecutionContext,
//         wrapper: ReactiveMongoWrapper,
//         igniteWrapper: IgniteClientNodeWrapper
//     ): Future[File] = {
//         new FileStore(fs.id.toString)
//             .update(file)
//             .transformWith({
//                 case Success(file) =>
//                     file match {
//                         case Some(matchingFile: File) =>
//                             // Update cache too
//                             new CachedFileStore()
//                                 .put(matchingFile)

//                             Future.successful(matchingFile)
//                         case None => Future.failed(FileNotFoundException())
//                     }
//                 case Failure(cause) => Future.failed(cause)
//             })
//     }

//     /** A method for deleting a file in the '''File Tree'''
//       *
//       * [[File File]] is deleted using the internal Id, then file is deleted
//       * from cache to invalidate it
//       *
//       * @note
//       *   This method is Async and returns a [[scala.concurrent.Future Future]]
//       * @example
//       *   {{{
//       *     TreeManager.deleteFile(
//       *         <a pre fetched file>
//       *     ).onComplete({
//       *         case Success(file) => {
//       *             // file variable contains the deleted File object
//       *         }
//       *         case Failure(cause) => // An exception occurred
//       *     })
//       *   }}}
//       * @param file
//       *   a file which will be deleted
//       * @param ec
//       *   the '''implicit''' ExecutionContext used to parallelize computing
//       * @param wrapper
//       *   the '''implicit''' MongoDB wrapper used to read data
//       * @param igniteWrapper
//       *   the '''implicit''' Apache Ignite wrapper used to write cache
//       * @return
//       *   a future file that reflects old file state in the datastore
//       */
//     def deleteFile(file: File)(implicit
//         ec: ExecutionContext,
//         wrapper: ReactiveMongoWrapper,
//         igniteWrapper: IgniteClientNodeWrapper
//     ): Future[File] = {
//         getChildrenOf(file).transformWith({
//             case Success(children) =>
//                 // Handle case where file to delete has child
//                 if (children.nonEmpty)
//                     return Future.failed(
//                       new Exception("Directory can't be deleted if not empty")
//                     )

//                 // Delete from cache too
//                 new CachedFileStore()
//                     .delete(file)

//                 // Delete from datastore
//                 new FileStore(fs.id.toString)
//                     .delete(file)
//                     .transformWith({
//                         case Success(deletedFile) =>
//                             deletedFile match {
//                                 case Some(matchingFile: File) =>
//                                     Future.successful(matchingFile)
//                                 case None =>
//                                     Future
//                                         .failed(new Exception("file not found"))
//                             }
//                         case Failure(cause) => Future.failed(cause)
//                     })
//             case Failure(cause) => Future.failed(cause)
//         })
//     }

//     /** A result of deleteManyFiles method
//       *
//       * @constructor
//       *   create a new deleteManyFilesResult with a count of deleted files and a
//       *   list of errors
//       * @param deleted
//       *   a count of the effectively deleted file
//       * @param errors
//       *   a list of errors caught from a file deletion
//       */
//     case class DeleteManyFilesResult(deleted: Int, errors: List[String])

//     /** A method for deleting many files in the '''File Tree'''
//       *
//       * @note
//       *   [[File File]] is deleted using the internal Id, then file is deleted
//       *   from cache to invalidate it
//       * @note
//       *   This method is Async and returns a [[scala.concurrent.Future Future]]
//       * @example
//       *   {{{
//       *     TreeManager.deleteManyFile(
//       *         // a pre fetched file
//       *     ).onComplete({
//       *         case Success(file) => {
//       *             // file variable is contains the deleted File object
//       *         }
//       *         case Failure(cause) => // An exception occurred
//       *     })
//       *   }}}
//       * @param files
//       *   repeatable parameters which consist of files to delete
//       * @param ec
//       *   the '''implicit''' ExecutionContext used to parallelize computing
//       * @param wrapper
//       *   the '''implicit''' MongoDB wrapper used to read data
//       * @param igniteWrapper
//       *   the '''implicit''' Apache Ignite wrapper used to write cache
//       * @return
//       *   a future result which contains count of deleted files and caught
//       *   errors
//       */
//     def deleteManyFiles(files: File*)(implicit
//         ec: ExecutionContext,
//         wrapper: ReactiveMongoWrapper,
//         igniteWrapper: IgniteClientNodeWrapper
//     ): Future[DeleteManyFilesResult] = {

//         Future
//             .sequence(
//               files.map(file => {
//                   getChildrenOf(file).map(_.nonEmpty)
//               })
//             )
//             .map(lookup => files.toList zip lookup)
//             .transformWith(lookup => {
//                 // Handle case where file to delete has child
//                 val validDelete: List[File] =
//                     lookup.get.filter(_._2 == false).map(_._1)
//                 val childLookupError: List[String] = lookup.get
//                     .filter(_._2 == true)
//                     .map(
//                       "Couldn't delete directory " + _._1.path.get + " as it has still children"
//                     )

//                 // Delete from cache too
//                 new CachedFileStore()
//                     .deleteMany(validDelete)

//                 // Delete from datastore
//                 new FileStore(fs.id.toString)
//                     .deleteMany(validDelete)
//                     .transformWith({
//                         case Success(deletedFilesCount) =>
//                             Future.successful(
//                               DeleteManyFilesResult(
//                                 deletedFilesCount,
//                                 childLookupError
//                               )
//                             )
//                         case Failure(cause) => Future.failed(cause)
//                     })
//             })
//     }
// }
