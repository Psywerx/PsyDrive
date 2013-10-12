package org.psywerx.PsyDrive

import org.lwjgl.opengl.GL11._

object HUD {

  val cam = new Camera
  cam.setViewPort(0,0,PsyDrive.winWidth,PsyDrive.winHeight)
  cam.setOrtho(0,PsyDrive.winHeight,PsyDrive.winWidth,0,-1f,1f)
  
  def render() {
    cam.render
    
    glDisable(GL_LIGHTING)
    glDisable(GL_DEPTH_TEST)
    
    glEnable(GL_BLEND)
    
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    
    def doLine() {
      glPushMatrix()
      glColor4f(0,0,0,1f)
      val thickness = 50
      glTranslatef(PsyDrive.winWidth/2f-thickness,0,0)
      glScalef(50,PsyDrive.winHeight,1)
      glBegin(GL_QUADS)
        glVertex3f(0,1, 0)
        glVertex3f(1,1, 0)
        glVertex3f(1,0, 0)
        glVertex3f(0,0, 0)
      glEnd()
      glPopMatrix()
    }

    def doGameOver() {
      glPushMatrix()
      glEnable(GL_TEXTURE_2D)
      glBindTexture(GL_TEXTURE_2D, PsyDrive.gameover)
      val width = PsyDrive.winHeight
      val xpos = PsyDrive.winHeight/2-100
      val ypos = 0
      glTranslatef(xpos,ypos,0)
      glColor4f(1,1,1,0.8f)
      glBegin(GL_QUADS)
        glTexCoord2f(0,1); glVertex3f(0,width, 0)
        glTexCoord2f(1,1); glVertex3f(width,width, 0)
        glTexCoord2f(1,0); glVertex3f(width,0, 0)
        glTexCoord2f(0,0); glVertex3f(0,0, 0)
      glEnd()      
      glDisable(GL_TEXTURE_2D)
      glPopMatrix()
    }

    def playerHud(p: Player) {
      glPushMatrix()

      glEnable(GL_TEXTURE_2D)
      glBindTexture(GL_TEXTURE_2D, p.avatar)
      glColor4f(1,1,1,0.8f)
      glBegin(GL_QUADS)
        glTexCoord2f(0,1); glVertex3f(0,128, 0)
        glTexCoord2f(1,1); glVertex3f(128,128, 0)
        glTexCoord2f(1,0); glVertex3f(128,0, 0)
        glTexCoord2f(0,0); glVertex3f(0,0, 0)
      glEnd()      
      glDisable(GL_TEXTURE_2D)
      
      glPushMatrix()
      glColor4f(1,0,0,0.8f)
      glTranslatef(200,0,0)
      glScalef(500,128,1)
      glBegin(GL_QUADS)
        glVertex3f(0,1, 0)
        glVertex3f(1,1, 0)
        glVertex3f(1,0, 0)
        glVertex3f(0,0, 0)
      glEnd()
      glPopMatrix()

      glPushMatrix()
      glTranslatef(200,0,0)
      glScalef(500,128,1)
      glColor4f(0,1,0,0.8f)
      glBegin(GL_QUADS)
        glVertex3f(0,1, 0)
        glVertex3f(p.health/100f,1, 0)
        glVertex3f(p.health/100f,0, 0)
        glVertex3f(0,0, 0)
      glEnd()
      glPopMatrix()

      glPopMatrix()
    }
    
    if(PsyDrive.players(0).health <= 0 || PsyDrive.players(1).health <= 0) {
      doGameOver()
    } else {
      doLine()
    }

    glTranslatef(50,50,0)
    if(PsyDrive.players(0).health > 0) playerHud(PsyDrive.players(0))
    glTranslatef(PsyDrive.winWidth/2,0,0)
    if(PsyDrive.players(1).health > 0) playerHud(PsyDrive.players(1))

    glDisable(GL_TEXTURE_2D)
    glDisable(GL_BLEND)

    glEnable(GL_LIGHTING)
    glEnable(GL_DEPTH_TEST)
  }

}
