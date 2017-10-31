package io.sensefly.dynamodbtos3.commandline

import com.nhaarman.mockito_kotlin.mock
import io.sensefly.dynamodbtos3.BackupTable
import io.sensefly.dynamodbtos3.RestoreTable
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.net.URI

object CommandLineParserSpek : Spek({

  given("an CommandLineParser") {

    val backupTable = mock<BackupTable>()
    val restoreTable = mock<RestoreTable>()
    val parser = CommandLineParser(backupTable, restoreTable)

    on("backup command") {

      it("should parse args") {
        parser.run("backup", "--table", "table1", "--table", "table2", "--bucket", "my-bucket")
        parser.backupArgs.tables shouldEqual listOf("table1", "table2")
        parser.backupArgs.bucket shouldEqual "my-bucket"
      }

    }

    on("restore command") {

      it("should parse args") {
        parser.run("restore", "--table", "table1", "--source", "s3://bucket/folder/table1.json")
        parser.restoreArgs.table shouldEqual "table1"
        parser.restoreArgs.source shouldEqual URI("s3://bucket/folder/table1.json")
      }

    }

  }
})