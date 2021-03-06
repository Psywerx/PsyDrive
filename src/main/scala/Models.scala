package org.psywerx.PsyDrive

import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.GLU
import scala.collection.mutable
import scala.util.Random

abstract class Model {
  var pos = Vec3()
  var rot = Vec3()
  private var scal = Vec3(1f,1f,1f)
  def scale: Vec3 = scal
  def scale_=(v: Vec3): Unit = { scal = v }
  var visible = true

  def setPosition(x: Float, y: Float, z: Float): Unit = { pos = Vec3(x,y,z) }
  def setRotation(x: Float, y: Float, z: Float): Unit = { rot = Vec3(x,y,z) }
  def setScale(x: Float, y: Float, z: Float): Unit = { scale = Vec3(x,y,z) }
  def setPosition(v: Vec3): Unit = { pos = v.clone }
  def setRotation(v: Vec3): Unit = { rot = v.clone }
  def setScale(v: Vec3): Unit = { scale = v.clone }

  def doTranslate(): Unit = {
    GL11.glTranslatef(pos.x, pos.y, pos.z)
  }
  def doRotate(): Unit = {
    if (rot.z != 0) GL11.glRotatef(rot.z, 0,0,1)
    if (rot.y != 0) GL11.glRotatef(rot.y, 0,1,0)
    if (rot.x != 0) GL11.glRotatef(rot.x, 1,0,0)
  }
  def doScale(): Unit = {
    GL11.glScalef(scale.x, scale.y, scale.z)
  }

  def doTransforms(): Unit = {
    doTranslate()
    doRotate()
    doScale()
  }

  def render(): Unit

  override def toString: String = "p:("+pos.toString+"), " + "r:("+rot.toString+"), " + "s:("+scale.toString+")"
}

trait Cache { self: DisplayModel =>
  var compiled = false
  var displayList: Int = -1

  def compile(): Unit = {
    displayList = GL11.glGenLists(1)
    GL11.glNewList(displayList, GL11.GL_COMPILE)
    renderfunc()
    GL11.glEndList()
    compiled = true
  }

  override def render(): Unit = {
    if (visible) {
      GL11.glPushMatrix()
        doTransforms

        if (displayList == -1) {
          compile
        }
        GL11.glCallList(displayList)
      GL11.glPopMatrix()
    }
  }
}

trait Rendrable {
  def renderfunc: () => Unit
}
// doesn't care about points and stuff
class DisplayModel(renderfun: () => Unit = () => ()) extends Model with Properties with Rendrable {
  override def renderfunc: () => Unit = renderfun
  var (vector, vector2) = (Vec3(), Vec3())
/*  def reset(limit: Int = 1, preserveCurrent: Boolean = true) {
    if (compileCache.size > limit) {
      var count = 0
      compileCache.clone.foreach {
        case (id,listid) =>
          if (listid != displayList || !preserveCurrent) {
            count += 1
            Global.tasks = Global.tasks :+ (() => GL11.glDeleteLists(listid, 1))
            compileCache -= id
          }
      }
    }
  }
  def free(): Unit = {
    reset(limit = 0, preserveCurrent = false)
    displayList = -1
  }*/

  override def clone: DisplayModel = {
    val res = new DisplayModel()
    res.pos = this.pos.clone
    res.rot = this.rot.clone
    res.scale = this.scale.clone
    res
  }

  override def render(): Unit = {
    if (visible) {
      GL11.glPushMatrix()
        doTransforms
        renderfunc()
      GL11.glPopMatrix()
    }
  }
}

class GeneratorModel(generator: () => Object, draw: Object => Unit) extends DisplayModel {
  var data: Object = generator()
  var box: BoundingBox = new BoundingBox(Vec3())
  override def renderfunc = () => { draw(data); () }
  //idfunc = _idfunc

  //def regenerate() {
  //  data = generator()
  //  //compile()
  //}

  // make a data constructor, so clone has same data. (eliminate generator in static constructor)
  override def clone: GeneratorModel = {
    val res = new GeneratorModel(generator, draw)
    res.pos = this.pos.clone
    res.rot = this.rot.clone
    res.scale = this.scale.clone
    res
  }
}

class TrailModel(points: List[Vec3])
  extends GeneratorModel(
    () => { points.map(_.clone) },
    (data: Object) => {
      import org.lwjgl.opengl.GL11._
      import Global._

      val points = data.asInstanceOf[List[Vec3]]
      glColor3f(1f, 1f, 1f)
      for (i <- 1 until points.length by 2) {
        val (vecA,vecB) = (points(i-1), points(i))

        val z = Vec3(0,0,1)
        val p = vecA - vecB
        val cross = z X p
        val angle = z angle p

        glPushMatrix()
        glTranslatef(vecB.x,vecB.y,vecB.z)
        glRotatef(angle,cross.x,cross.y,cross.z)
        gluQuadrics.cylinder.draw(0.075f,0.075f, p.length, 4,1)
        glPopMatrix()
      }
    }) with Cache {

  def +=(v: Vec3): Unit = {
    data = data.asInstanceOf[List[Vec3]] :+ v.clone
  }
}

class Branch(var parent: Branch) extends Properties {
  var (diffVec,rootVec) = (Vec3(), Vec3())
  def destVec: Vec3 = rootVec+diffVec

  var depth = 1
  var marked = false
  val children = new mutable.ListBuffer[Branch]

  setParent(parent)

  def setParent(p: Branch): Unit = {
    if (p != null) {
      p.children += this
      depth = p.depth+1
    }
    parent = p
  }
  def addChild(c: Branch): Unit = if (!(this eq c)) c.setParent(this)

  def detach(): Unit = {
    if (parent != null) {
      parent.children -= this
      this.setParent(null)
    }
  }

  def doAll(f: Branch => Unit): Unit = {
    f(this)
    children.foreach(_.doAll(f))
  }
  def doWhile(w: Branch => Boolean, f: Branch => Unit): Unit = {
    f(this)
    if (w(this)) children.foreach(_.doWhile(w, f))
  }

  def print(): Unit = {
    println(depth+" "*(depth*2) + rootVec +" -- " + diffVec)
    children.foreach(_.print())
  }

  def render(): Unit = {
    import org.lwjgl.opengl.GL11._
    import Global._
    if (depth <= Settings.maxDepth) {
      val (vecA,vecB) = (rootVec, destVec)

      val z = Vec3(0,0,1)
      val p = vecA - vecB
      val cross = z X p
      val angle = z angle p

      glPushMatrix()
      glTranslatef(vecB.x,vecB.y,vecB.z)
      glRotatef(angle,cross.x,cross.y,cross.z)
      glColor3f(0.7f,0.2f,0f)
      val fatness = properties.get[Float]("fatness")
      gluQuadrics.cylinder.draw(fatness/depth,(fatness*2)/depth, diffVec.length, Settings.graphics*5,1)
      if (properties.get[Boolean]("hasLeaf")) {
        glDisable(GL_CULL_FACE)
        glScalef(1,1.6f,1)
        glColor3f(0.2f,0.8f,0.1f)
        glTranslatef(0,-0.17f,0)
        glRotatef(Random.nextFloat*12-Random.nextFloat*12, 0,0,1)
        gluQuadrics.disk.draw(0,0.175f, Settings.graphics*6,1)
        glEnable(GL_CULL_FACE)
      }
      glPopMatrix()
    }
  }
}

class Camera extends Model {
  // default projection
  var perspective = false
  var (near,far) = (1f,30f) // near, far clipping plane
  var (fov,aspectRatio) = (45f,4/3f) // perspective stuff
  var (minX,minY,maxX,maxY) = (-1f,-1f, 1f, 1f) // ortho stuff
  var projectionChanged = true // do we need to remake projection matrix
  var vector = Vec3()
  var angle = Vec3()
  var viewPort = (0,0,0,0)

  def setViewPort(x: Int, y: Int, xx: Int, yy: Int): Unit = {
    GL11.glViewport(x,y,xx,yy)
    viewPort = (x,y,xx,yy)
  }

  // set a perspective projection
  def setPerspective(fv: Float, ar: Float, n: Float, f: Float): Unit = {
    perspective = true
    fov = fv
    aspectRatio = ar
    near = n
    far = f
    projectionChanged = true
  }

  // set an ortographic projection
  def setOrtho(mx: Float, my: Float, Mx: Float, My: Float, n: Float, f: Float): Unit = {
    perspective = false
    minX = mx
    minY = my
    maxX = Mx
    maxY = My
    near = n
    far = f
    projectionChanged = true
  }

  private var lookAtV = Vec3()
  def lookAt(v: Vec3): Unit = lookAtV = v.clone
  def lookAt(m: Model): Unit = lookAtV = m.pos.clone

  override def render(): Unit = {
    // setup projection matrix stack
    //if (projectionChanged) {
      projectionChanged = false
      GL11.glFlush()
      GL11.glViewport(viewPort._1,viewPort._2,viewPort._3,viewPort._4)
      GL11.glMatrixMode(GL11.GL_PROJECTION)
      GL11.glLoadIdentity()
      if (perspective) {
        // perspective projection
        GLU.gluPerspective(fov,aspectRatio, near,far)
      } else {
        // orthographic projection
        GL11.glOrtho(minX,maxX, minY,maxY, near,far)
      }
    //}

    // model view stack
    GL11.glMatrixMode(GL11.GL_MODELVIEW)
    GL11.glLoadIdentity()
    if (perspective) {
      GLU.gluLookAt(pos.x,pos.y,pos.z,             // camera position
                    lookAtV.x,lookAtV.y,lookAtV.z, // look-at vector
                    0,1,0)                         // up vector
    }
  }
}

class ModelLink(m1: Model, m2: Model, var vector: Vec3=Vec3(), var vector2: Vec3=Vec3()) {
  private var linked = false
  def isLinked: Boolean = linked
  def breakLink(): Unit = { linked = false }
  def forgeLink(): Unit = { linked = true }

  def applyLink(): Unit = {
    if (linked) {
      m1.setPosition(m2.pos+vector)
      m1.setRotation(m2.rot+vector2)
    }
  }
}
