package io.sensefly.dynamodbtos3.commandline

import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters
import io.sensefly.dynamodbtos3.RestoreTable
import java.net.URI

@Parameters(commandDescription = "Restore DynamoDB table from json file hosted on S3.")
class RestoreCommand {

  @Parameter(names = arrayOf("-t", "--table"), description = "Table to restore.", required = true, order = 0)
  var table: String = ""

  @Parameter(names = arrayOf("-s", "--source"), description = "S3 URI to a JSON backup file (s3://my-bucket/folder/my-table.json).", required = true, order = 1)
  var source: URI? = null

  @Parameter(names = arrayOf("-w", "--write-percentage"), description = "Write capacity percentage.", order = 3)
  var writePercentage: Double = RestoreTable.DEFAULT_WRITE_PERCENTAGE

  override fun toString(): String {
    return "RestoreCommand(table='$table', source=$source, writePercentage=$writePercentage)"
  }

}