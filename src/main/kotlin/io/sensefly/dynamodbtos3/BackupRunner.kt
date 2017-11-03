package io.sensefly.dynamodbtos3

import org.slf4j.LoggerFactory
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.support.CronTrigger
import org.springframework.stereotype.Component
import javax.inject.Inject


@Component
class BackupRunner @Inject constructor(
    private val backupTable: BackupTable,
    private val taskScheduler: ThreadPoolTaskScheduler) {

  private val log = LoggerFactory.getLogger(javaClass)

  fun run(tables: List<String>, bucket: String, cron: String?, readPercentage: Double, pattern: String) {

    if (cron.isNullOrBlank()) {
      tables.parallelStream().forEach { table ->
        backupTable.backup(table, bucket, readPercentage, pattern)
      }
    } else {
      log.info("Backup scheduled with $cron")
      tables.forEach { table ->
        taskScheduler.schedule(
            Runnable { backupTable.backup(table, bucket, readPercentage, pattern) },
            CronTrigger("0 $cron"))
      }
    }
  }
}