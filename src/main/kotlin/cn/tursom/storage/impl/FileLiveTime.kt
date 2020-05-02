package cn.tursom.storage.impl

import cn.tursom.core.cast
import cn.tursom.core.datastruct.DefaultValueMap
import cn.tursom.storage.LiveTime
import org.mapdb.DB
import org.mapdb.DBMaker
import org.mapdb.HTreeMap
import java.util.concurrent.ConcurrentHashMap

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
    val map = DefaultValueMap<String, FileLiveTime>(ConcurrentHashMap()) { FileLiveTime(it) }
    val defaultDB by lazy { map["live.map"].map }
    val default by lazy { map["live.map"] }
    fun forDB(db: String) = map[db]
  }
}
