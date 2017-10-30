package io.sensefly.dynamodbtos3.commandline

import com.beust.jcommander.Parameter
import io.sensefly.dynamodbtos3.BackupTable

class CommandLineArgs {

  @Parameter(names = arrayOf("-t", "--table"), description = "Tables to backup to S3", required = true)
  var tables: List<String> = arrayListOf()

  @Parameter(names = arrayOf("-b", "--bucket"), description = "Destination S3 bucket", required = true)
  var bucket: String = ""

  @Parameter(names = arrayOf("-p", "--pattern"),
      description = "Destination file path pattern. Default is ${BackupTable.DEFAULT_PATTERN}")
  var pattern: String? = BackupTable.DEFAULT_PATTERN

  @Parameter(names = arrayOf("-r", "--read-percentage"),
      description = "Read capacity percentage. Default is ${BackupTable.DEFAULT_READ_PERCENTAGE}")
  var readPercentage: Double? = BackupTable.DEFAULT_READ_PERCENTAGE

  override fun toString(): String {
    return "CommandLineArgs(tables=$tables, bucket=$bucket, pattern=$pattern, readPercentage=$readPercentage)"
  }

}