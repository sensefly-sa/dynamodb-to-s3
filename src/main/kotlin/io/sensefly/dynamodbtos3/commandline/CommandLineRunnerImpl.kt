package io.sensefly.dynamodbtos3.commandline

import com.beust.jcommander.JCommander
import io.sensefly.dynamodbtos3.BackupTable
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
@Profile("!test")
class CommandLineRunnerImpl @Inject constructor(private val backupTable: BackupTable) : CommandLineRunner {

  private val log = LoggerFactory.getLogger(javaClass)

  override fun run(vararg args: String?) {
    val commandLineArgs = CommandLineArgs()
    JCommander.newBuilder()
        .addObject(commandLineArgs)
        .build()
        .parse(*args)

    log.debug("args: {}", commandLineArgs)

    commandLineArgs.tables.parallelStream().forEach { table ->
      backupTable.backup(table, commandLineArgs.bucket)
    }

  }

}