package laughedelic.atom.ide.scala

import scala.scalajs.js, js.annotation._, js.Dynamic.global
import io.scalajs.nodejs.child_process.ChildProcess
import io.scalajs.nodejs.path.Path
import io.scalajs.nodejs.os.OS
import facade.atom_languageclient._

class ScalaLanguageClient extends AutoLanguageClient { client =>

  override def getGrammarScopes(): js.Array[String] = js.Array("source.scala")
  override def getLanguageName(): String = "Scala"
  override def getServerName(): String = "Scalameta"

  override def startServerProcess(projectPath: String): ChildProcess = {
    global.console.log(s"startServerProcess(${projectPath})")

    // FIXME: use more civilized way of detecting JAVA_HOME
    val javaHome = ChildProcess.asInstanceOf[js.Dynamic].execSync("/usr/libexec/java_home").toString.trim
    val toolsJar = Path.join(javaHome, "lib", "tools.jar")

    // TODO: try to use coursier directly
    val coursierJar = Path.join(OS.homedir, "bin", "coursier")
    val coursierArgs = js.Array(
      "launch", "--quiet",
      "--repository", "bintray:dhpcs/maven",
      "--repository", "sonatype:releases",
      "--extra-jars", toolsJar,
      "org.scalameta:metaserver_2.12:0.1-SNAPSHOT",
      "--main", "scala.meta.languageserver.Main"
    )

    val javaBin = Path.join(javaHome, "bin", "java")
    val javaArgs = js.Array(
      s"-Dvscode.workspace=${projectPath}",
      "-jar", coursierJar
    ).concat(coursierArgs)

    global.console.log(javaArgs.mkString(javaBin, "\n", ""))

    val serverProcess = ChildProcess.spawn(javaBin, javaArgs)
    client.captureServerErrors(serverProcess)
    serverProcess.on("exit", { err: js.Any =>
      client.handleSpawnFailure(client.processStdErr)
    })
    serverProcess
  }

  override def filterChangeWatchedFiles(filePath: String): Boolean = {
    filePath.endsWith(".semanticdb") ||
    filePath.endsWith(".compilerconfig")
  }

  override def preInitialization(connection: js.Any): Unit = {
    client.updateBusyMessage("initializing server", true, true)
  }

  override def postInitialization(server: js.Any): Unit = {
    client.updateBusyMessage()
  }

  private var busySignal: js.Any = js.undefined
  private var busyMessage: js.Any = js.undefined

  def consumeBusySignal(service: js.Any): Unit = {
    client.busySignal = service
  }

  private def busyMessageFormat(text: String) = s"${client.getServerName()}: ${text}"

  def updateBusyMessage(
    text: String = "",
    init: Boolean = false,
    reveal: Boolean = false
  ): Unit = {
    if (client.busyMessage != js.undefined) {
      if (text.isEmpty) {
        client.busyMessage.asInstanceOf[js.Dynamic].dispose()
        client.busyMessage = js.undefined
      } else {
        client.busyMessage.asInstanceOf[js.Dynamic].setTitle(
          client.busyMessageFormat(text)
        )
      }
    } else if (init) {
      client.busyMessage = client.busySignal.asInstanceOf[js.Dynamic].reportBusy(
        client.busyMessageFormat(text)
      )
    }
  }

}
