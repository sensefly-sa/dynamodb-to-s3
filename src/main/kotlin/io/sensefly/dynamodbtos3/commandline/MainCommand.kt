package io.sensefly.dynamodbtos3.commandline

import com.beust.jcommander.Parameter


class MainCommand {

  @Parameter(names = ["--help"], description = "Print usage", help = true)
  var help = false

}