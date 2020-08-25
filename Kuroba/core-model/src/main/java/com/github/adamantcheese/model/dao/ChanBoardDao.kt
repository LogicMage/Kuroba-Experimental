package com.github.adamantcheese.model.dao

import androidx.room.*
import com.github.adamantcheese.common.mutableListWithCap
import com.github.adamantcheese.common.mutableMapWithCap
import com.github.adamantcheese.model.KurobaDatabase
import com.github.adamantcheese.model.data.descriptor.BoardDescriptor
import com.github.adamantcheese.model.entity.chan.board.ChanBoardEntity
import com.github.adamantcheese.model.entity.chan.board.ChanBoardFull
import com.github.adamantcheese.model.entity.chan.board.ChanBoardIdEntity

@Dao
abstract class ChanBoardDao {

  @Insert(onConflict = OnConflictStrategy.ABORT)
  abstract suspend fun insertBoardId(chanBoardIdEntity: ChanBoardIdEntity): Long

  @Insert
  abstract suspend fun insertManyBoardIds(chanBoardIdEntities: List<ChanBoardIdEntity>): List<Long>

  @Insert(onConflict = OnConflictStrategy.ABORT)
  abstract suspend fun insertBoard(chanBoardEntity: ChanBoardEntity)

  @Update(onConflict = OnConflictStrategy.IGNORE)
  abstract suspend fun updateBoard(chanBoardEntity: ChanBoardEntity)

  @Update(onConflict = OnConflictStrategy.IGNORE)
  abstract suspend fun updateBoards(chanBoardEntities: List<ChanBoardEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun createOrUpdateBoards(chanBoardEntities: List<ChanBoardEntity>)

  @Query("""
    UPDATE ${ChanBoardEntity.TABLE_NAME}
    SET ${ChanBoardEntity.BOARD_ACTIVE_COLUMN_NAME} = :activate
    WHERE ${ChanBoardEntity.OWNER_CHAN_BOARD_ID_COLUMN_NAME} IN (:boardIds)
  """)
  abstract suspend fun activateDeactivateBoards(boardIds: Collection<Long>, activate: Boolean)

  @Query("""
        SELECT * 
        FROM ${ChanBoardIdEntity.TABLE_NAME}
        WHERE 
            ${ChanBoardIdEntity.OWNER_SITE_NAME_COLUMN_NAME} = :siteName
        AND
            ${ChanBoardIdEntity.BOARD_CODE_COLUMN_NAME} = :boardCode
    """)
  abstract suspend fun selectBoardId(siteName: String, boardCode: String): ChanBoardIdEntity?

  @Query("""
    SELECT * 
    FROM ${ChanBoardEntity.TABLE_NAME}
    WHERE ${ChanBoardEntity.OWNER_CHAN_BOARD_ID_COLUMN_NAME} = :boardId
  """)
  abstract suspend fun selectBoard(boardId: Long): ChanBoardEntity?

  @Query("""
        SELECT ${ChanBoardIdEntity.BOARD_ID_COLUMN_NAME} 
        FROM ${ChanBoardIdEntity.TABLE_NAME}
        WHERE 
            ${ChanBoardIdEntity.OWNER_SITE_NAME_COLUMN_NAME} = :siteName
        AND
            ${ChanBoardIdEntity.BOARD_CODE_COLUMN_NAME} = :boardCode
    """)
  abstract suspend fun selectBoardDatabaseId(siteName: String, boardCode: String): Long?

  @Query("""
    SELECT *
    FROM ${ChanBoardIdEntity.TABLE_NAME} cbie
    INNER JOIN ${ChanBoardEntity.TABLE_NAME} cbe 
        ON cbie.${ChanBoardIdEntity.BOARD_ID_COLUMN_NAME} = cbe.${ChanBoardEntity.OWNER_CHAN_BOARD_ID_COLUMN_NAME}
    WHERE cbe.${ChanBoardEntity.BOARD_ACTIVE_COLUMN_NAME} = ${KurobaDatabase.SQLITE_TRUE}
  """)
  abstract suspend fun selectAllActiveBoards(): List<ChanBoardFull>

  @Query("""
    SELECT * 
    FROM ${ChanBoardIdEntity.TABLE_NAME}
    WHERE 
        ${ChanBoardIdEntity.OWNER_SITE_NAME_COLUMN_NAME} = :ownerSiteName
    AND
        ${ChanBoardIdEntity.BOARD_CODE_COLUMN_NAME} IN (:boardCodes)
  """)
  abstract suspend fun selectManyBoardIdEntities(
    ownerSiteName: String,
    boardCodes: List<String>
  ): List<ChanBoardIdEntity>

  @Query("""
        SELECT *
        FROM ${ChanBoardIdEntity.TABLE_NAME}
        WHERE ${ChanBoardIdEntity.BOARD_ID_COLUMN_NAME} = :boardId
    """)
  abstract suspend fun selectBoardId(boardId: Long): ChanBoardIdEntity?

  @Query("""
        SELECT *
        FROM ${ChanBoardIdEntity.TABLE_NAME}
        WHERE ${ChanBoardIdEntity.BOARD_ID_COLUMN_NAME} IN (:boardIdList)
    """)
  abstract suspend fun selectMany(boardIdList: List<Long>): List<ChanBoardIdEntity>

  suspend fun createNewBoardIdEntities(
    ownerSiteName: String,
    boardCodes: List<String>
  ): Map<BoardDescriptor, Long> {
    val alreadyExistingBoardIdEntities = selectManyBoardIdEntities(ownerSiteName, boardCodes)
    val totalBoardIdEntities = mutableListWithCap<ChanBoardIdEntity>(alreadyExistingBoardIdEntities)
    totalBoardIdEntities.addAll(alreadyExistingBoardIdEntities)

    if (alreadyExistingBoardIdEntities.size != boardCodes.size) {
      val alreadyExistingBoardCodes = alreadyExistingBoardIdEntities
        .map { chanBoardIdEntity -> chanBoardIdEntity.boardCode }
        .toSet()

      val newBoardIdEntities = mutableListOf<ChanBoardIdEntity>()

      boardCodes.forEach { boardCode ->
        if (boardCode in alreadyExistingBoardCodes) {
          return@forEach
        }

        newBoardIdEntities += ChanBoardIdEntity(
          ownerSiteName = ownerSiteName,
          boardCode = boardCode
        )
      }

      val databaseIds = insertManyBoardIds(newBoardIdEntities)
      newBoardIdEntities.forEachIndexed { index, board -> board.boardId = databaseIds[index] }

      totalBoardIdEntities.addAll(newBoardIdEntities)
    }

    val resultMap = mutableMapWithCap<BoardDescriptor, Long>(totalBoardIdEntities)

    totalBoardIdEntities.forEach { chanBoardIdEntity ->
      require(chanBoardIdEntity.boardId > 0L) { "Bad boardId: ${chanBoardIdEntity.boardId}" }
      resultMap[chanBoardIdEntity.boardDescriptor()] = chanBoardIdEntity.boardId
    }

    return resultMap
  }

  suspend fun insertBoardId(siteName: String, boardCode: String): ChanBoardIdEntity {
    val prev = selectBoardId(siteName, boardCode)
    if (prev != null) {
      return prev
    }

    val chanBoardEntity = ChanBoardIdEntity(
      boardId = 0L,
      ownerSiteName = siteName,
      boardCode = boardCode
    )

    val insertedId = insertBoardId(chanBoardEntity)
    check(insertedId >= 0L) { "Couldn't insert entity, insert() returned ${insertedId}" }

    chanBoardEntity.boardId = insertedId
    return chanBoardEntity
  }

}