package io.sensefly.dynamodbtos3.commandline

import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters
import io.sensefly.dynamodbtos3.BackupTable

@Parameters(commandDescription = "Backup DynamoDB tables to S3 bucket.")
class BackupCommand {

  @Parameter(names = arrayOf("-t", "--tables"), description = "Tables to backup to S3.", required = true, order = 0)
  var tables: List<String> = arrayListOf()

  @Parameter(names = arrayOf("-b", "--bucket"), description = "Destination S3 bucket.", required = true, order = 1)
  var bucket: String = ""

  @Parameter(names = arrayOf("-r", "--read-percentage"), description = "Read capacity percentage.", order = 2)
  var readPercentage: Double = BackupTable.DEFAULT_READ_PERCENTAGE

  @Parameter(names = arrayOf("-p", "--pattern"), description = "Destination file path pattern.", order = 3)
  var pattern: String = BackupTable.DEFAULT_PATTERN

  override fun toString(): String {
    return "BackupCommand(tables=$tables, bucket='$bucket', readPercentage=$readPercentage, pattern='$pattern')"
  }

}