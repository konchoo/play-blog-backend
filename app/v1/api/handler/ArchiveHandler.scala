package v1.api.handler

import com.google.inject.Inject
import play.api.Logger
import v1.api.entity._
import v1.api.page.Page
import v1.api.repository.Repositories

import scala.concurrent.{ExecutionContext, Future}

class ArchiveHandler @Inject()(dao: Repositories)(implicit ec: ExecutionContext) {
  val log: Logger = Logger(getClass)

  def createArchive(form: ArchiveForm): Future[Archive] = {

    dao.archiveRepository.insertOne(form.getArchiveFormData)
      .map(aId => {
        log.debug(s"inserted archive id : $aId")
        dao.categoryRepository.batchInsert(form.category)
          .map {
            cIds =>
              log.debug(s"inserted category ids : $cIds")
              dao.archiveCategoryRepository
                .batchInsert(cIds.map(ArchiveCategoryRel(aId.get, _)))
          }
        dao.tagRepository.batchInsert(form.tag)
          .map {
            tIds =>
              log.debug(s"inserted tag ids : $tIds")
              dao.archiveTagRepository
                .batchInsert(tIds.map(ArchiveTagRel(aId.get, _)))
          }
        form.getArchiveFormData
      })
  }

  def selectById(id: Int): Future[Option[Archive]] = {
    dao.archiveRepository.selectById(id)
      .map { archive =>
        archive
      }
  }

  def selectArchive(archiveQueryParams: ArchiveQueryParams): Future[Page[Archive]] = {
    dao.archiveRepository.list(archiveQueryParams).map {
      result =>
        result
    }
  }

  def selectByCategoryName(categoryName: String): Future[Page[Archive]] = {
    null
  }

  def selectByTagName(tagName: String): Future[Page[Archive]] = {
    null
  }
}