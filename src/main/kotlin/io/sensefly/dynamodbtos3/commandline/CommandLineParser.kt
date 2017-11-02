package io.sensefly.dynamodbtos3.commandline

import com.beust.jcommander.JCommander
import io.sensefly.dynamodbtos3.BackupRunner
import io.sensefly.dynamodbtos3.RestoreTable
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
@Profile("!test")
class CommandLineParser @Inject constructor(
    private val backupRunner: BackupRunner,
    private val restoreTable: RestoreTable) : CommandLineRunner {

  internal val backupCmd = BackupCommand()
  internal val restoreCmd = RestoreCommand()

  override fun run(vararg args: String?) {
    val jc = JCommander.newBuilder()
        .addObject(MainCommand())
        .addCommand("backup", backupCmd)
        .addCommand("restore", restoreCmd)
        .build()
    jc.parse(*args)

    when (jc.parsedCommand) {
      "backup" -> {
        backupRunner.run(backupCmd.tables, backupCmd.bucket, backupCmd.cron, backupCmd.readPercentage, backupCmd.pattern)
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