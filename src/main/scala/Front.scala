package front

import scalafx.Includes._
import scalafx.application.JFXApp3
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.layout.{VBox, HBox, Priority}
import scalafx.stage.{FileChooser, StageStyle}
import scalafx.scene.text.Font
import scalafx.scene.paint.Color
import java.io.File

import scala.io.Source
import scala.util.{Try, Success, Failure, Random}
import java.nio.file.{Files, Paths, Path, StandardOpenOption}
import java.nio.charset.StandardCharsets
import java.io.IOException

def saveFile(absolutePath: String, filename: String, content: String): Unit = {
    val fullPath = Paths.get(absolutePath, filename)

    if (!Files.exists(fullPath.getParent)) {
        Files.createDirectories(fullPath.getParent)
    }

    Files.write(fullPath, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
}


object SimpleEncryptorScala extends JFXApp3 {

  var selectedFile: File = _
  var xOffset = 0.0
  var yOffset = 0.0

  override def start(): Unit = {

    val fileChooser = new FileChooser {
      title = "Open File"
      initialDirectory = new java.io.File(System.getProperty("user.dir"))
    }

    val fileLabel = new Label("No file selected") {
      font = Font("Arial", 16)
      style = "-fx-text-fill: white; -fx-font-weight: bold;"
    }

    val fileButton = new Button("Select File") {
      font = Font("Arial", 14)
      style = "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 20 10 20; -fx-border-radius: 5; -fx-background-radius: 5;"
      onAction = _ => {
        selectedFile = fileChooser.showOpenDialog(stage)
        if (selectedFile != null) {
          fileLabel.text = "Selected: " + selectedFile.getName
        } else {
          fileLabel.text = "File selection cancelled."
        }
      }
      onMouseEntered = _ => style = "-fx-background-color: #45a049; -fx-text-fill: white; -fx-padding: 10 20 10 20; -fx-border-radius: 5; -fx-background-radius: 5;"
      onMouseExited = _ => style = "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 20 10 20; -fx-border-radius: 5; -fx-background-radius: 5;"
    }

    val returnLabel = new Label("") {
      alignment = Pos.Center
      minWidth = 400
      maxWidth = 400
      font = Font("Arial", 16)
      style = "-fx-text-fill: white; -fx-font-weight: bold; "
    }

    val keyField = new TextField {
      promptText = "Key"
    }

    val encryptButton = new Button("Encrypt") {
      font = Font("Arial", 14)
      style = "-fx-background-color: #008CBA; -fx-text-fill: white; -fx-padding: 10 20 10 20; -fx-border-radius: 5; -fx-background-radius: 5;"
      onAction = _ => {
        if (selectedFile == null) {
          returnLabel.text = "Select file first!"
        } else {
          val key = if (keyField.text.value.isEmpty) "0" else keyField.text.value
          if ((encryption.run(selectedFile.getAbsolutePath, key, true))) {
            returnLabel.text = selectedFile.getAbsolutePath + " encrypted successfully"
          } else {
            returnLabel.text = selectedFile.getName + " encrypt failure"
          }
        }
      }
      onMouseEntered = _ => style = "-fx-background-color: #005f73; -fx-text-fill: white; -fx-padding: 10 20 10 20; -fx-border-radius: 5; -fx-background-radius: 5;"
      onMouseExited = _ => style = "-fx-background-color: #008CBA; -fx-text-fill: white; -fx-padding: 10 20 10 20; -fx-border-radius: 5; -fx-background-radius: 5;"
    }

    val decryptButton = new Button("Decrypt") {
      font = Font("Arial", 14)
      style = "-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 10 20 10 20; -fx-border-radius: 5; -fx-background-radius: 5;"
      onAction = _ => {
        if (selectedFile == null) {
          returnLabel.text = "Select file first!"
        } else {
          val key = if (keyField.text.value.isEmpty) "0" else keyField.text.value
          if (encryption.run(selectedFile.getAbsolutePath, key, false)) {
            returnLabel.text = selectedFile.getName + " decrypted successfully"
          } else {
            returnLabel.text = selectedFile.getName + " decrypt failure"
          }
        }
      }
      onMouseEntered = _ => style = "-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-padding: 10 20 10 20; -fx-border-radius: 5; -fx-background-radius: 5;"
      onMouseExited = _ => style = "-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 10 20 10 20; -fx-border-radius: 5; -fx-background-radius: 5;"
    }

    val buttonBox = new HBox {
      spacing = 10
      alignment = Pos.Center
      children = Seq(encryptButton, decryptButton)
    }

    val vbox = new VBox {
      padding = Insets(20)
      spacing = 20
      alignment = Pos.Center
      children = Seq(fileButton, fileLabel, keyField, buttonBox, returnLabel)
      vgrow = Priority.Always
      hgrow = Priority.Always
      style = "-fx-background-color: #333333;"
    }

    val closeButton = new Button("X") {
      style = "-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5; -fx-background-radius: 5;"
      minWidth = 30
      maxWidth = 30
      minHeight = 30
      maxHeight = 30
      onAction = _ => stage.close()

      onMouseEntered = _ => style = "-fx-background-color: #a13030; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5; -fx-background-radius: 5;"
      onMouseExited = _ => style = "-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5; -fx-background-radius: 5;"
    }

    val minimiseButton = new Button("—"){
      style = "-fx-background-color: #4d4dff; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5; -fx-background-radius: 5;"
      minWidth = 30
      maxWidth = 30
      minHeight = 30
      maxHeight = 30
      onAction = _ => stage.setIconified(true)

      onMouseEntered = _ => style = "-fx-background-color: #3030a1; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5; -fx-background-radius: 5;"
      onMouseExited = _ => style = "-fx-background-color: #4d4dff; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5; -fx-background-radius: 5;"
    }

    val controlBox = new HBox {
      padding = Insets(10)
      spacing = 10
      alignment = Pos.CenterRight
      children = Seq(minimiseButton, closeButton)
    }

    val header = new HBox {
      padding = Insets(10)
      spacing = 10
      alignment = Pos.CenterRight
      minHeight = 35
      maxHeight = 35
      children = Seq(
        new Label("Simple Encryptor Scala"){
          font = Font("Arial", 14)
        },
        controlBox
      )
      onMousePressed = event => {
        xOffset = event.getSceneX
        yOffset = event.getSceneY
      }
      onMouseDragged = event => {
        stage.x = event.getScreenX - xOffset
        stage.y = event.getScreenY - yOffset
      }
    }


    val myRoot = new VBox {
      children = Seq(header, vbox)
      styleClass += "root"
    }

    stage = new JFXApp3.PrimaryStage {
      initStyle(StageStyle.Undecorated)
      resizable = true
      minHeight = 280
      minWidth = 500
      title = "Simple Encryptor Scala"

      scene = new Scene {
        root = myRoot
      }
    }
    WindowResizer.makeResizable(stage)
  }
}
