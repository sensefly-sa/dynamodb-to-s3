package io.sensefly.dynamodbtos3.commandline

import com.beust.jcommander.JCommander
import io.sensefly.dynamodbtos3.BackupTable
import io.sensefly.dynamodbtos3.RestoreTable
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
@Profile("!test")
class CommandLineParser @Inject constructor(
    private val backupTable: BackupTable,
    private val restoreTable: RestoreTable) : CommandLineRunner {

  internal val mainCommand = MainCommand()
  internal val backupArgs = BackupCommand()
  internal val restoreArgs = RestoreCommand()

  override fun run(vararg args: String?) {
    val jc = JCommander.newBuilder()
        .addObject(mainCommand)
        .addCommand("backup", backupArgs)
        .addCommand("restore", restoreArgs)
        .build()
    jc.parse(*args)

    val command = jc.parsedCommand
    when (command) {
      "backup" -> {
        backupArgs.tables.parallelStream().forEach { table ->
          backupTable.backup(table, backupArgs.bucket, backupArgs.readPercentage, backupArgs.pattern)
        }
      }
      "restore" -> {
        restoreTable.restore(restoreArgs.source!!, restoreArgs.table, restoreArgs.writePercentage)
      }
      null -> {
        jc.usage()
      }
    }
  }

}