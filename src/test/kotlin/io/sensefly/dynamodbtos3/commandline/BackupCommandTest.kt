package io.sensefly.dynamodbtos3.commandline

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class BackupCommandTest {

  @Test
  fun test_multiple_tables_with_comma() {
    val cmd = BackupCommand()
    cmd.tables = listOf("tables1", " tables2, tables3 , ")
    assertThat(cmd.parseTables()).containsExactly("tables1", "tables2", "tables3")
  }

  @Test
  fun test_multiple_tables() {
    val cmd = BackupCommand()
    cmd.tables = listOf("tables1", " tables2 ", "tables3 ")
    assertThat(cmd.parseTables()).containsExactly("tables1", "tables2", "tables3")
  }

}