package org.psywerx.PsyDrive

import org.lwjgl.opengl.GL11._

object HUD {

  val cam = new Camera
  cam.setViewPort(0,0,PsyDrive.winWidth,PsyDrive.winHeight)
  cam.setOrtho(0,PsyDrive.winHeight,PsyDrive.winWidth,0,-1f,1f)

  def render(): Unit = {
    cam.render

    glDisable(GL_LIGHTING)
    glDisable(GL_DEPTH_TEST)

    glEnable(GL_BLEND)

    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

    def doLine(): Unit = {
      glPushMatrix()
      glColor4f(0f, 0f, 0f, 1f)
      val thickness = 50
      glTranslatef(PsyDrive.winWidth*0.5f - thickness, 0, 0)
      glScalef(thickness, PsyDrive.winHeight, 1)
      glBegin(GL_QUADS)
        glVertex2f(0f, 1f)
        glVertex2f(1f, 1f)
        glVertex2f(1f, 0f)
        glVertex2f(0f, 0f)
      glEnd()
      glPopMatrix()
    }

    def doGameOver(): Unit = {
      glPushMatrix()
      glEnable(GL_TEXTURE_2D)
      glBindTexture(GL_TEXTURE_2D, PsyDrive.gameover)
      val width = PsyDrive.winHeight
      val xpos = PsyDrive.winHeight/2-100
      val ypos = 0
      glTranslatef(xpos,ypos,0)
      glColor4f(1f, 1f, 1f, 0.8f)
      glBegin(GL_QUADS)
        glTexCoord2f(0f, 1f); glVertex2f(   0f, width)
        glTexCoord2f(1f, 1f); glVertex2f(width, width)
        glTexCoord2f(1f, 0f); glVertex2f(width,    0f)
        glTexCoord2f(0f, 0f); glVertex2f(   0f,    0f)
      glEnd()
      glDisable(GL_TEXTURE_2D)
      glPopMatrix()
    }

    def playerHud(p: Player): Unit = {
      glPushMatrix()

      glEnable(GL_TEXTURE_2D)
      glBindTexture(GL_TEXTURE_2D, p.avatar)
      glColor4f(1,1,1,0.8f)
      glBegin(GL_QUADS)
        glTexCoord2f(0f, 1f); glVertex2f(  0f, 128f)
        glTexCoord2f(1f, 1f); glVertex2f(128f, 128f)
        glTexCoord2f(1f, 0f); glVertex2f(128f,   0f)
        glTexCoord2f(0f, 0f); glVertex2f(  0f,   0f)
      glEnd()
      glDisable(GL_TEXTURE_2D)

      glPushMatrix()
      glColor4f(1, 0, 0, 0.8f)
      glTranslatef(200, 0, 0)
      glScalef(500, 128, 1)
      glBegin(GL_QUADS)
        glVertex2f(0f, 1f)
        glVertex2f(1f, 1f)
        glVertex2f(1f, 0f)
        glVertex2f(0f, 0f)
      glEnd()
      glPopMatrix()

      //glPushMatrix()
      glTranslatef(200, 0, 0)
      glScalef(500, 128, 1)
      glColor4f(0, 1, 0, 0.8f)
      glBegin(GL_QUADS)
        glVertex2f(           0f, 1f)
        glVertex2f(p.health/100f, 1f)
        glVertex2f(p.health/100f, 0f)
        glVertex2f(           0f, 0f)
      glEnd()
      //glPopMatrix()

      glPopMatrix()
    }

    if (PsyDrive.players(0).health <= 0 || PsyDrive.players(1).health <= 0) {
      doGameOver()
    } else {
      doLine()
    }

    glTranslatef(50,50,0)
    if (PsyDrive.players(0).health > 0) playerHud(PsyDrive.players(0))
    glTranslatef(PsyDrive.winWidth/2,0,0)
    if (PsyDrive.players(1).health > 0) playerHud(PsyDrive.players(1))

    glDisable(GL_TEXTURE_2D)
    glDisable(GL_BLEND)

    glEnable(GL_LIGHTING)
    glEnable(GL_DEPTH_TEST)
  }

}
