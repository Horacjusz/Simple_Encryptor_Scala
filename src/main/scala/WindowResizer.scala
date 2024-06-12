package hello

import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.stage.Stage

object WindowResizer {

  var resizing = false
  var resizeStartX = 0.0
  var resizeStartY = 0.0
  var startWidth = 0.0
  var startHeight = 0.0

  def makeResizable(stage: Stage): Unit = {
    val borderThickness = 10
    stage.scene().addEventHandler(javafx.scene.input.MouseEvent.MOUSE_MOVED, event => {
      val sceneWidth = stage.getWidth
      val sceneHeight = stage.getHeight
      val mouseX = event.getSceneX
      val mouseY = event.getSceneY

      if (mouseX < borderThickness && mouseY < borderThickness) {
        stage.scene().setCursor(javafx.scene.Cursor.NW_RESIZE)
      } else if (mouseX < borderThickness && mouseY > sceneHeight - borderThickness) {
        stage.scene().setCursor(javafx.scene.Cursor.SW_RESIZE)
      } else if (mouseX > sceneWidth - borderThickness && mouseY < borderThickness) {
        stage.scene().setCursor(javafx.scene.Cursor.NE_RESIZE)
      } else if (mouseX > sceneWidth - borderThickness && mouseY > sceneHeight - borderThickness) {
        stage.scene().setCursor(javafx.scene.Cursor.SE_RESIZE)
      } else if (mouseX < borderThickness) {
        stage.scene().setCursor(javafx.scene.Cursor.W_RESIZE)
      } else if (mouseX > sceneWidth - borderThickness) {
        stage.scene().setCursor(javafx.scene.Cursor.E_RESIZE)
      } else if (mouseY < borderThickness) {
        stage.scene().setCursor(javafx.scene.Cursor.N_RESIZE)
      } else if (mouseY > sceneHeight - borderThickness) {
        stage.scene().setCursor(javafx.scene.Cursor.S_RESIZE)
      } else {
        stage.scene().setCursor(javafx.scene.Cursor.DEFAULT)
      }
    })

    stage.scene().addEventHandler(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event => {
      if (stage.scene().getCursor != javafx.scene.Cursor.DEFAULT) {
        resizing = true
        resizeStartX = event.getScreenX
        resizeStartY = event.getScreenY
        startWidth = stage.getWidth
        startHeight = stage.getHeight
      }
    })

    stage.scene().addEventHandler(javafx.scene.input.MouseEvent.MOUSE_DRAGGED, event => {
      if (resizing) {
        val newWidth = startWidth + (event.getScreenX - resizeStartX)
        val newHeight = startHeight + (event.getScreenY - resizeStartY)
        if (newWidth > stage.getMinWidth) {
          stage.setWidth(newWidth)
        }
        if (newHeight > stage.getMinHeight) {
          stage.setHeight(newHeight)
        }
      }
    })

    stage.scene().addEventHandler(javafx.scene.input.MouseEvent.MOUSE_RELEASED, _ => {
      resizing = false
    })
  }
}
