package v1.api.implicits

import play.api.mvc.RequestHeader
import v1.api.cont.Entities.ArchiveField._
import v1.api.cont.Entities.CategoryField._
import v1.api.cont.Entities.CommonField._
import v1.api.cont.Entities.TagField._
import v1.api.entity._

import java.sql.{Date, PreparedStatement, ResultSet, Timestamp}
import scala.collection.immutable.Range

object ResultSetHelper {

  implicit class ResultSet2LazyList(resultSet: ResultSet) {
    def toLazyList: LazyList[ResultSet] = {
      new Iterator[ResultSet] {
        override def hasNext: Boolean = resultSet.next()

        override def next(): ResultSet = resultSet
      }.to(LazyList)
    }

    def getRowCount: Option[Long] = {
      new Iterator[Long] {
        override def hasNext: Boolean = resultSet.next()

        override def next(): Long = {
          resultSet.getInt(1).toLong
        }
      }.toList
        .headOption
    }

    def asArchive: Archive = ArchiveResultSet.map2Archive(resultSet)

    def asCategory: Category = CategoryResultSet.map2Category(resultSet)

    def asTag: Tag = TagResultSet.map2Tag(resultSet)
  }

  implicit class PreparedStatementHelper(ps: PreparedStatement) {
    def getGeneratedIDs: List[Int] = {
      ps.getGeneratedKeys
        .toLazyList
        .map {
          rs =>
            rs.getInt(1)
        }
        .toList
    }
  }

}

object ArchiveResultSet {
  def map2Archive(implicit resultSet: ResultSet): Archive = {
    val category = resultSet.getString("category")
      .split(",")
      .map {
        c =>
          val split = ArchiveResultSet.split(c)
          Category(split._1, split._2)
      }
      .toSeq
    val tag = resultSet.getString("tag")
      .split(",")
      .map {
        t =>
          val split = ArchiveResultSet.split(t)
          Tag(split._1, split._2)
      }
      .toSeq
    Archive(
      SerialNumber(resultSet.getInt(id)),
      resultSet.getString(title),
      resultSet.getString(author),
      new Date(resultSet.getTimestamp(publishTime).getTime),
      resultSet.getString(content),
      new Date(resultSet.getTimestamp(createTime).getTime),
      category,
      tag,
      resultSet.getString(catalog)
    )
  }

  def split(s: String): (SerialNumber, String) = {
    (SerialNumber(Integer.parseInt(s.split("#")(0))), s.split("#")(1))
  }
}

object CategoryResultSet {
  def map2Category(implicit resultSet: ResultSet): Category = {
    Category(SerialNumber(resultSet.getInt(id)), resultSet.getString(category))
  }

}

object TagResultSet {
  def map2Tag(implicit resultSet: ResultSet): Tag = {
    Tag(SerialNumber(resultSet.getInt(id)), resultSet.getString(tag))
  }
}

object ConnectionHelper {

  implicit class Params(ps: PreparedStatement) {
    /**
      * Set values to placeholder statement for PreparedStatement
      *
      * @param params values
      * @return PreparedStatement
      */
    def setParams(params: Any*): PreparedStatement = {
      for (index <- params.indices) {
        val param = params(index)
        val columnIndex = index + 1
        param match {
          case _: Int => ps.setInt(columnIndex, param.asInstanceOf[Int])
          case _: Long => ps.setLong(columnIndex, param.asInstanceOf[Long])
          case _: String => ps.setString(columnIndex, param.asInstanceOf[String])
          case _: Date => ps.setTimestamp(columnIndex, new Timestamp(param.asInstanceOf[Date].getTime))
          case _: java.util.Date => ps.setTimestamp(columnIndex, new Timestamp(param.asInstanceOf[java.util.Date].getTime))
          case _ =>
        }
      }
      ps
    }

    /**
      * Set values to placeholder statement for PreparedStatement,
      * support int and string type in query
      * like "select * from user where id in(?,?,?)"
      *
      * @param params values
      * @return preparedStatement
      */
    def setInParams(params: Seq[Any]): PreparedStatement = {
      for (index <- Range(0, params.length)) {
        val param = params(index)
        param match {
          case _: Int => ps.setInt(index + 1, param.asInstanceOf[Int])
          case _: String => ps.setString(index + 1, param.asInstanceOf[String])
          case _ =>
        }
      }
      ps
    }
  }

}

object PreparedStatementPlaceHolder {

  implicit class PlaceHolder(private val times: Int) {
    def params: String = {
      val holder = new StringBuilder
      Range(0, times)
        .foreach {
          i =>
            holder.append("?").append(",")
        }
      holder.deleteCharAt(holder.length - 1)
      holder.toString
    }
  }

}

object RequestHandler {

  implicit class BindRequest(request: RequestHeader) {
    def bindRequestQueryString: PostRequestParams = {
      val maxItemSize = if (request.uri.indexOf("/timeline") > 0) 8 else 3
      val limit = Math.min(maxItemSize, Integer.parseInt(request.getQueryString("size").orElse(Some(String.valueOf(maxItemSize))).head))
      val offset = (Math.max(1, Integer.parseInt(request.getQueryString("page").orElse(Some("1")).head)) - 1) * limit
      val order = request.getQueryString("order")
      val page = (offset / limit) + 1
      var category: Option[String] = None
      var tag: Option[String] = None
      val c = "/category/.+".r.findFirstIn(request.uri)
        .iterator.nextOption()
      if (c.nonEmpty) category = Some(request.uri.drop("/category/".length))
      val t = "/tag/.+".r.findFirstIn(request.uri).iterator.nextOption()
      if (t.nonEmpty) tag = Some(request.uri.drop("/tag/".length))
      PostRequestParams(offset, page, limit, category, tag, order)
    }

  }

}






