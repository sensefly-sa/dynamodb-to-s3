package io.sensefly.dynamodbtos3.commandline

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.net.URI


class CommandLineParserTest {

  private val parser = CommandLineParser(mock(), mock(), mock())

  @Test
  fun backupCommand() {
    parser.run("backup", "--table", "table1", "--table", "table2", "--bucket", "my-bucket")
    assertThat(parser.backupCmd.tables).containsExactly("table1", "table2")
    assertThat(parser.backupCmd.bucket).isEqualTo("my-bucket")
  }

  @Test
  fun restoreCommand() {
    parser.run("restore", "--table", "table1", "--source", "s3://bucket/folder/table1.json")
    assertThat(parser.restoreCmd.table).isEqualTo("table1")
    assertThat(parser.restoreCmd.source).isEqualTo(URI("s3://bucket/folder/table1.json"))
  }

}