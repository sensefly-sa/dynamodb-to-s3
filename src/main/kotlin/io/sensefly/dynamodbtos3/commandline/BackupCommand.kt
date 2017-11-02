package io.sensefly.dynamodbtos3.commandline

import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters
import io.sensefly.dynamodbtos3.BackupTable

@Parameters(commandDescription = "Backup DynamoDB tables to S3 bucket.")
class BackupCommand {

  @Parameter(names = arrayOf("-t", "--table"), description = "Table to backup to S3. Repeat this param to backup multiple tables.", required = true, order = 0)
  var tables: List<String> = arrayListOf()

  @Parameter(names = arrayOf("-b", "--bucket"), description = "Destination S3 bucket.", required = true, order = 1)
  var bucket: String = ""

  @Parameter(names = arrayOf("-c", "--cron"), description = "Cron pattern. (http://www.manpagez.com/man/5/crontab/)", order = 2)
  var cron: String? = null

  @Parameter(names = arrayOf("-r", "--read-percentage"), description = "Read capacity percentage.", order = 3)
  var readPercentage: Double = BackupTable.DEFAULT_READ_PERCENTAGE

  @Parameter(names = arrayOf("-p", "--pattern"), description = "Destination file path pattern.", order = 4)
  var pattern: String = BackupTable.DEFAULT_PATTERN

}