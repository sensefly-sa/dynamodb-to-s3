package io.sensefly.dynamodbtos3.commandline

import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException
import com.beust.jcommander.Parameters
import io.sensefly.dynamodbtos3.BackupTable

@Parameters(commandDescription = "Backup DynamoDB tables to S3 bucket.")
class BackupCommand {

  @Parameter(names = ["-t", "--table"], description = "Table to backup to S3. Comma-separated list to backup multiple tables or repeat this param.", required = true, order = 0)
  var tables: List<String> = arrayListOf()

  @Parameter(names = ["-b", "--bucket"], description = "Destination S3 bucket.", required = true, order = 1)
  var bucket: String = ""

  @Parameter(names = ["-c", "--cron"], description = "Cron pattern. (http://www.manpagez.com/man/5/crontab/)", order = 2)
  var cron: String? = null

  @Parameter(names = ["--read-percentage"], description = "Read percentage based on current table capacity. Cannot be used with '--read-capacity'.", order = 3)
  var readPercentage: Double? = null

  @Parameter(names = ["--read-capacity"], description = "Read capacity (useful if auto scaling enabled). Cannot be used with '--read-percentage'.", order = 4)
  var readCapacity: Int? = null

  @Parameter(names = ["-p", "--pattern"], description = "Destination file path pattern.", order = 5)
  var pattern: String = BackupTable.DEFAULT_PATTERN

  @Parameter(names = ["-n", "--namespace"], description = "Cloudwatch namespace to send metrics.", order = 6)
  var cloudwatchNamespace: String? = null

  @Parameter(names = ["--jvmMetrics"], description = "Collect JVM metrics")
  var jvmMetrics = false

  fun parseTables(): List<String> {
    return tables.map { it.split(",") }
        .flatten()
        .filter { it.isNotBlank() }
        .map { it.trim() }
  }

  fun validate() {
    if (readPercentage == null && readCapacity == null) {
      throw ParameterException("Missing read configuration. Use '--read-percentage' or '--read-capacity' parameter.")
    }
    if (readPercentage != null && readCapacity != null) {
      throw ParameterException("Cannot use '--read-percentage' and '--read-capacity' together.")
    }
  }

}