package io.sensefly.dynamodbtos3

import com.beust.jcommander.ParameterException
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

  fun run(tables: List<String>, bucket: String, cron: String?, readPercentage: Double?, readCapacity: Int?, pattern: String) {

    if (cron.isNullOrBlank()) {
      tables.parallelStream().forEach { table ->
        backup(table, bucket, readPercentage, readCapacity, pattern)
      }
    } else {
      log.info("Backup scheduled with $cron")
      tables.forEach { table ->
        taskScheduler.schedule(
            Runnable { backup(table, bucket, readPercentage, readCapacity, pattern) },
            CronTrigger("0 $cron"))
      }
    }
  }

  private fun backup(table: String, bucket: String, readPercentage: Double?, readCapacity: Int?, pattern: String) {
    when {
      readPercentage != null -> backupTable.backup(table, bucket, readPercentage, pattern)
      readCapacity != null -> backupTable.backup(table, bucket, readCapacity, pattern)
      else -> throw ParameterException("Missing read configuration. Use '--read-percentage' or '--read-capacity' parameter.")
    }
  }
}