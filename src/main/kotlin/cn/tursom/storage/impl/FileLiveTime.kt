package cn.tursom.storage.impl

import cn.tursom.core.cast
import cn.tursom.storage.LiveTime
import org.mapdb.DB
import org.mapdb.DBMaker
import org.mapdb.HTreeMap

/**
 * need impl mapdb
 */
class FileLiveTime(
  private val map: HTreeMap<Int, Long> = defaultDB
) : LiveTime by MapLiveTime(map) {
  constructor(
    db: DB,
    map: String = "FileLiveTimeStorage"
  ) : this(db.hashMap(map).createOrOpen().cast<HTreeMap<Int, Long>>())

  constructor(
    dbPath: String,
    map: String = "FileLiveTimeStorage"
  ) : this(DBMaker.fileDB(dbPath).checksumHeaderBypass().make(), map)

  companion object {
    val defaultDB by lazy {
      DBMaker
        .fileDB("live.map")
        .checksumHeaderBypass()
        .make()
        .hashMap("FileLiveTimeStorage")
        .createOrOpen()
        .cast<HTreeMap<Int, Long>>()
    }
    val default by lazy { FileLiveTime() }
  }
}
