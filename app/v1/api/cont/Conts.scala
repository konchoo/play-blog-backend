package v1.api.cont

import play.api.libs.json.{Json, OFormat}
import v1.api.entity.{Archive, CategoryCount, TagCount}
import v1.api.json.{PageProductWrites, ProductWrites}

object JsonWrites {

  implicit val defaultJsonWrites: ProductWrites[Archive] = new ProductWrites[Archive]
  implicit val pageDefaultJsonWrites: PageProductWrites[Archive] = new PageProductWrites[Archive]
  implicit val tagCountJsonWrites: OFormat[TagCount] = Json.format[TagCount]
  implicit val categoryCountJsonWrites: OFormat[CategoryCount] = Json.format[CategoryCount]
}

object Page {

  val page = "page"

  val size = "size"

  val total = "total"

  val data = "data"
}

object Entities {
  val categorySplitSymbol = "#"

  val tagSplitSymbol = "#"

  object CommonField {
    val id = "id"
  }


  object ArchiveField {
    val table = "Archive"

    val title = "title"

    val author = "author"

    val publishTime = "publishTime"

    val content = "content"

    val createTime = "createTime"
  }

  object CategoryField {
    val category = "category"
  }

  object TagField {
    val tag = "tag"
  }

}

object ArchiveSql {
  val selectSql = "select ARCHIVE.*,GROUP_CONCAT(DISTINCT CONCAT(C.ID,'#',C.CATEGORY)) as category ,GROUP_CONCAT(DISTINCT CONCAT(T.ID,'#',T.TAG)) as tag from ARCHIVE left join ARCHIVE_CATEGORY AC on ARCHIVE.ID = AC.ARCHIVE_ID left join ARCHIVE_TAG AT on ARCHIVE.ID = AT.ARCHIVE_ID left join CATEGORY C on AC.CATEGORY_ID = C.ID left join TAG T on T.ID = AT.TAG_ID group by AC.ARCHIVE_ID limit ? offset ?"
  val selectByIdSql =
    """
      select ARCHIVE.*,
             GROUP_CONCAT(DISTINCT CONCAT(C.ID, '#', C.CATEGORY)) as category,
             GROUP_CONCAT(DISTINCT CONCAT(T.ID, '#', T.TAG))      as tag
      from ARCHIVE
               left join ARCHIVE_CATEGORY AC on ARCHIVE.ID = AC.ARCHIVE_ID
               left join ARCHIVE_TAG AT on ARCHIVE.ID = AT.ARCHIVE_ID
               left join CATEGORY C on AC.CATEGORY_ID = C.ID
               left join TAG T on T.ID = AT.TAG_ID
      where ARCHIVE.ID=?
      group by ARCHIVE.ID
      """
  val selectByCategoryNameSql =
    """
      select ARCHIVE.*,
             GROUP_CONCAT(DISTINCT CONCAT(C.ID, '#', C.CATEGORY)) as category,
             GROUP_CONCAT(DISTINCT CONCAT(T.ID, '#', T.TAG))      as tag
      from ARCHIVE
               left join ARCHIVE_CATEGORY AC on ARCHIVE.ID = AC.ARCHIVE_ID
               left join ARCHIVE_TAG AT on ARCHIVE.ID = AT.ARCHIVE_ID
               left join CATEGORY C on AC.CATEGORY_ID = C.ID
               left join TAG T on T.ID = AT.TAG_ID
      where ARCHIVE.ID in (select ARCHIVE.ID
                           from ARCHIVE
                                    left join ARCHIVE_CATEGORY AC on AC.ARCHIVE_ID =ARCHIVE.ID
                                    left join CATEGORY C on C.ID= AC.CATEGORY_ID
                           where C.CATEGORY = ?)
      group by ARCHIVE.ID
      limit ? offset ?
    """
  val selectCountByCategoryNameSql =
    """
      select count(*)
      from ARCHIVE
               left join ARCHIVE_CATEGORY AC on ARCHIVE.ID = AC.ARCHIVE_ID
               left join CATEGORY C on AC.CATEGORY_ID = C.ID
      where C.CATEGORY=?
      group by C.ID
      """
  val selectByTagNameSql =
    """
      select ARCHIVE.*,
             GROUP_CONCAT(DISTINCT CONCAT(C.ID, '#', C.CATEGORY)) as category,
             GROUP_CONCAT(DISTINCT CONCAT(T.ID, '#', T.TAG))      as tag
      from ARCHIVE
               left join ARCHIVE_CATEGORY AC on ARCHIVE.ID = AC.ARCHIVE_ID
               left join ARCHIVE_TAG AT on ARCHIVE.ID = AT.ARCHIVE_ID
               left join CATEGORY C on AC.CATEGORY_ID = C.ID
               left join TAG T on T.ID = AT.TAG_ID
      where ARCHIVE.ID in (select ARCHIVE.ID
                           from ARCHIVE
                                    left join ARCHIVE_TAG AT on ARCHIVE.ID = AT.ARCHIVE_ID
                                    left join TAG T on T.ID = AT.TAG_ID
                           where T.TAG = ?)
      group by ARCHIVE.ID
      limit ? offset ?
    """
  val selectCountByTagNameSql =
    """
     select count(*)
     from ARCHIVE
              left join ARCHIVE_TAG AT on ARCHIVE.ID = AT.ARCHIVE_ID
              left join TAG T on T.ID = AT.TAG_ID
     where T.TAG=?
     group by T.ID
     """
}
