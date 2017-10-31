package io.sensefly.dynamodbtos3.commandline

import com.beust.jcommander.JCommander
import io.sensefly.dynamodbtos3.BackupTable
import io.sensefly.dynamodbtos3.RestoreTable
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
//@Profile("!test")
class CommandLineParser @Inject constructor(
    private val backupTable: BackupTable,
    private val restoreTable: RestoreTable) : CommandLineRunner {

  private val log = LoggerFactory.getLogger(javaClass)

  override fun run(vararg args: String?) {
    val mainCommand = MainCommand()
    val backupArgs = BackupCommand()
    val restoreArgs = RestoreCommand()
    val jc = JCommander.newBuilder()
        .addObject(mainCommand)
        .addCommand("backup", backupArgs)
        .addCommand("restore", restoreArgs)
        .build()
    jc.parse(*args)

    val command = jc.parsedCommand
    when (command) {
      "backup" -> {
        log.debug("backupArgs: {}", backupArgs)
        backupArgs.tables.parallelStream().forEach { table ->
          backupTable.backup(table, backupArgs.bucket, backupArgs.readPercentage, backupArgs.pattern)
        }
      }
      "restore" -> {
        log.debug("restoreArgs: {}", restoreArgs)
        restoreTable.restore(restoreArgs.source, restoreArgs.table, restoreArgs.writePercentage)
      }
      null -> {
        jc.usage()
      }
    }
  }

}