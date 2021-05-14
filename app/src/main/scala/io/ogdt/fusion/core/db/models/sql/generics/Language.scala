package io.ogdt.fusion.core.db.models.sql.generics

import java.util.UUID

import org.apache.ignite.cache.query.annotations.QuerySqlField

import io.ogdt.fusion.core.db.models.sql.exceptions.MalformedLocalLanguageCodeException
import io.ogdt.fusion.core.db.datastores.sql.generics.LanguageStore

/** A class that reflects the state of the language entity in the database
  *
  * @param store an implicit parameter for accessing store methods
  */
class Language(implicit @transient protected val store: LanguageStore) {

    @transient
    private val locLangCodeReg: String = "[a-z]{2}(-[A-Z]{2,3})?"

    @QuerySqlField(index = true, name = "id", notNull = true)
    private var _id: UUID = null

    /** Language Universal Unique IDentifier property
      *
      * @return [[java.util.UUID UUID]] of this entity
      */
    def id: UUID = _id

    /** A method for setting Universal Unique IDentifier property
      *
      * @param id a [[java.util.UUID UUID]] to assign to language unique id property
      * @return this object
      */
    def setId(id: UUID): Language = {
        _id = id
        this
    }

    @QuerySqlField(index = true, name = "code", notNull = true)
    private var _code: String = null

    /** Language LCID (Local Language IDentifier) property
      * 
      * @return corresponding LCID [[java.lang.String String]]
      */
    def code: String = _code

    /** A method for setting locale language code (LCID) property
      * 
      * The passed [[java.lang.String String]] is compared to regex `[a-z]{2}(-[A-Z]{2,3})?` (e.g. en-US)
      * then assigned if it matches or throw an error if it does not
      *
      * @param code a [[java.lang.String String]] to assign to locale language code property
      * @throws setCode Throws a
      *     [[io.ogdt.fusion.core.db.models.sql.exceptions.MalformedLocalLanguageCodeException MalformedLocalLanguageCodeException]]
      *     exception when provided code is not matching regex test
      * @return this object
      */
    @throws(classOf[MalformedLocalLanguageCodeException])
    def setCode(code: String): Language = {
        if (code.matches(locLangCodeReg)) _code = code
        else throw MalformedLocalLanguageCodeException("locale language code is malformed")
        this
    }

    @QuerySqlField(name = "label", notNull = true)
    private var _label: String = null

    /** Language label property
      *
      * @return language label [[java.lang.String String]]
      */
    def label: String = _label

    /** A method for setting label property
      * 
      * @param label a [[java.lang.String String]] to assign to language label property
      * @return this object
      */
    def setLabel(label: String): Language = {
        _label = label
        this
    }
}
