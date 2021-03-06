package io.sensefly.dynamodbtos3.commandline

import com.beust.jcommander.JCommander
import io.sensefly.dynamodbtos3.BackupRunner
import io.sensefly.dynamodbtos3.RestoreTable
import io.sensefly.dynamodbtos3.config.MetricsConfig
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
@Profile("!test")
class CommandLineParser @Inject constructor(
    private val backupRunner: BackupRunner,
    private val restoreTable: RestoreTable,
    private val metricsConfig: MetricsConfig) : CommandLineRunner {

  private val log = LoggerFactory.getLogger(javaClass)

  internal val backupCmd = BackupCommand()
  internal val restoreCmd = RestoreCommand()

  override fun run(vararg args: String?) {
    log.info("Run with {}", args.toList())

    val jc = JCommander.newBuilder()
        .addObject(MainCommand())
        .addCommand("backup", backupCmd)
        .addCommand("restore", restoreCmd)
        .build()
    jc.parse(*args)

    when (jc.parsedCommand) {
      "backup" -> {
        backupCmd.validate()
        metricsConfig.setupJvmMetrics(backupCmd.jvmMetrics)
        metricsConfig.setupCloudwatchMetrics(backupCmd.cloudwatchNamespace)
        backupRunner.run(backupCmd.parseTables(), backupCmd.bucket, backupCmd.cron, backupCmd.readPercentage, backupCmd.readCapacity, backupCmd.pattern)
      }
      "restore" -> {
        restoreTable.restore(restoreCmd.source!!, restoreCmd.table, restoreCmd.writePercentage)
      }
      null -> {
        jc.usage()
      }
    }
  }

}