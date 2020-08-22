package com.github.adamantcheese.model.mapper

import com.github.adamantcheese.model.data.board.ChanBoard
import com.github.adamantcheese.model.data.descriptor.BoardDescriptor
import com.github.adamantcheese.model.data.descriptor.SiteDescriptor
import com.github.adamantcheese.model.entity.chan.ChanBoardEntity
import com.github.adamantcheese.model.entity.chan.ChanBoardFull

object ChanBoardMapper {

  fun fromChanBoardEntity(chanBoardFull: ChanBoardFull?): ChanBoard? {
    if (chanBoardFull == null) {
      return null
    }

    val boardDescriptor = BoardDescriptor(
      SiteDescriptor(chanBoardFull.chanBoardIdEntity.ownerSiteName),
      chanBoardFull.chanBoardIdEntity.boardCode
    )

    return ChanBoard(
      boardDescriptor = boardDescriptor,
      order = chanBoardFull.chanBoardEntity.order,
      name = chanBoardFull.chanBoardEntity.name,
      perPage = chanBoardFull.chanBoardEntity.perPage,
      pages = chanBoardFull.chanBoardEntity.pages,
      maxFileSize = chanBoardFull.chanBoardEntity.maxFileSize,
      maxWebmSize = chanBoardFull.chanBoardEntity.maxWebmSize,
      maxCommentChars = chanBoardFull.chanBoardEntity.maxCommentChars,
      bumpLimit = chanBoardFull.chanBoardEntity.bumpLimit,
      imageLimit = chanBoardFull.chanBoardEntity.imageLimit,
      cooldownThreads = chanBoardFull.chanBoardEntity.cooldownThreads,
      cooldownReplies = chanBoardFull.chanBoardEntity.cooldownReplies,
      cooldownImages = chanBoardFull.chanBoardEntity.cooldownImages,
      customSpoilers = chanBoardFull.chanBoardEntity.customSpoilers,
      description = chanBoardFull.chanBoardEntity.description,
      saved = chanBoardFull.chanBoardEntity.saved,
      workSafe = chanBoardFull.chanBoardEntity.workSafe,
      spoilers = chanBoardFull.chanBoardEntity.spoilers,
      userIds = chanBoardFull.chanBoardEntity.userIds,
      codeTags = chanBoardFull.chanBoardEntity.codeTags,
      preuploadCaptcha = chanBoardFull.chanBoardEntity.preuploadCaptcha,
      countryFlags = chanBoardFull.chanBoardEntity.countryFlags,
      mathTags = chanBoardFull.chanBoardEntity.mathTags,
      archive = chanBoardFull.chanBoardEntity.archive,
    )
  }

  fun toChanBoardEntity(boardDatabaseId: Long, order: Int?, board: ChanBoard): ChanBoardEntity {
    return ChanBoardEntity(
      ownerChanBoardId = boardDatabaseId,
      active = board.active,
      order = order ?: board.order,
      name = board.name,
      perPage = board.perPage,
      pages = board.pages,
      maxFileSize = board.maxFileSize,
      maxWebmSize = board.maxWebmSize,
      maxCommentChars = board.maxCommentChars,
      bumpLimit = board.bumpLimit,
      imageLimit = board.imageLimit,
      cooldownThreads = board.cooldownThreads,
      cooldownReplies = board.cooldownReplies,
      cooldownImages = board.cooldownImages,
      customSpoilers = board.customSpoilers,
      description = board.description,
      saved = board.saved,
      workSafe = board.workSafe,
      spoilers = board.spoilers,
      userIds = board.userIds,
      codeTags = board.codeTags,
      preuploadCaptcha = board.preuploadCaptcha,
      countryFlags = board.countryFlags,
      mathTags = board.mathTags,
      archive = board.archive,
    )
  }

  fun merge(prevBoard: ChanBoardEntity, board: ChanBoard): ChanBoardEntity {
    return ChanBoardEntity(
      ownerChanBoardId = prevBoard.ownerChanBoardId,
      // Be careful here, we don't want to overwrite the "active" flag here because it will most
      // likely always be false here. We want to use one from the DB when updating a board.
      active = prevBoard.active,
      // Same with orders, prefer the database order.
      order = prevBoard.order,
      name = board.name,
      perPage = board.perPage,
      pages = board.pages,
      maxFileSize = board.maxFileSize,
      maxWebmSize = board.maxWebmSize,
      maxCommentChars = board.maxCommentChars,
      bumpLimit = board.bumpLimit,
      imageLimit = board.imageLimit,
      cooldownThreads = board.cooldownThreads,
      cooldownReplies = board.cooldownReplies,
      cooldownImages = board.cooldownImages,
      customSpoilers = board.customSpoilers,
      description = board.description,
      saved = board.saved,
      workSafe = board.workSafe,
      spoilers = board.spoilers,
      userIds = board.userIds,
      codeTags = board.codeTags,
      preuploadCaptcha = board.preuploadCaptcha,
      countryFlags = board.countryFlags,
      mathTags = board.mathTags,
      archive = board.archive,
    )
  }

}