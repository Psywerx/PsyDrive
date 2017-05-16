package org.psywerx.PsyDrive

/*
import org.lwjgl.opengl.GL11._


case class Particles(var life: Int, var color: Vec3 = Vec3(1,1,1), val size: Float) extends DisplayModel(() => {
    glPushMatrix()
    glColor3f(color.x,color.y,color.z)
    glBegin(GL_QUADS)
      // top
      glNormal3f( 0f, 1f, 0f)
      glVertex3f( 1f, 1f,-1f)
      glVertex3f(-1f, 1f,-1f)
      glVertex3f(-1f, 1f, 1f)
      glVertex3f( 1f, 1f, 1f)
      // bottom
      glNormal3f( 0f,-1f, 1f)
      glVertex3f( 1f,-1f, 1f)
      glVertex3f(-1f,-1f, 1f)
      glVertex3f(-1f,-1f,-1f)
      glVertex3f( 1f,-1f,-1f)
      // Front
      glNormal3f( 0f, 0f, 1f)
      glVertex3f( 1f, 1f, 1f)
      glVertex3f(-1f, 1f, 1f)
      glVertex3f(-1f,-1f, 1f)
      glVertex3f( 1f,-1f, 1f)
      // back
      glNormal3f( 0f, 0f,-1f)
      glVertex3f( 1f,-1f,-1f)
      glVertex3f(-1f,-1f,-1f)
      glVertex3f(-1f, 1f,-1f)
      glVertex3f( 1f, 1f,-1f)
      // left
      glNormal3f(-1f, 0f, 0f)
      glVertex3f(-1f, 1f, 1f)
      glVertex3f(-1f, 1f,-1f)
      glVertex3f(-1f,-1f,-1f)
      glVertex3f(-1f,-1f, 1f)
      // right
      glNormal3f( 1f, 0f, 0f)
      glVertex3f( 1f, 1f,-1f)
      glVertex3f( 1f, 1f, 1f)
      glVertex3f( 1f,-1f, 1f)
      glVertex3f( 1f,-1f,-1f)
    glEnd()
    glPopMatrix()

    def drawWheel(): Unit = {
      glRotatef(90, 0,1,0)
      glColor3f(0.1f,0.1f,0.1f)
      gluQuadrics.cylinder.draw(1f,1f, scaling.x*2+2, Settings.graphics*9,1)
      glColor3f(0.6f,0.6f,0.6f)
      gluQuadrics.disk.draw(0, 1, 20, 1)
      glTranslatef(0,0,scaling.x*2+2)
      gluQuadrics.disk.draw(0, 1, 20, 1)
    }
    // Front wheel
    glPushMatrix()
    glTranslatef(-scaling.x-1,-scaling.y,scaling.z-2f)
    drawWheel()
    glPopMatrix()
    // Back wheel
    glPushMatrix()
    glTranslatef(-scaling.x-1,-scaling.y,-scaling.z+2f)
    drawWheel()
    glPopMatrix()
  })
{
  val box = new BoundingBox(Vec3())
  box.min -= scaling
  box.max += scaling
}
*/
