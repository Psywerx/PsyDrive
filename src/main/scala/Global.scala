package org.psywerx.PsyDrive

import org.lwjgl.util.glu.{Sphere,Cylinder,Disk,PartialDisk}
import scala.collection.mutable
import scala.collection.immutable.Queue
// stuff that is used in all the (wrong) places :P
// ... it's made of fail and state
object Global {
  object Settings {
    var graphics = 1 // polygon multiplier
    var maxDepth = 5 // tree depth
    var worldSize = 200
    var gravity = Vec3(0f,-0.5f,0f)
  }
  //def settings: SettingMap[String] = new SettingMap[String]
  var tasks = Queue.empty[() => Unit]
  
  object gluQuadrics {
    val sphere = new Sphere
    val cylinder = new Cylinder
    val disk = new Disk
    val partialdisk = new PartialDisk
  }

  val genTree = new ClojureWrap("org.psywerx.PsyDrive", "gen-tree")
}

object Utils {
  implicit class D(val d: Double) { def prob: Boolean = util.Random.nextDouble < d } //0.5.prob #syntaxabuse
  implicit class F(val f: Float) { def prob: Boolean = util.Random.nextFloat < f }

  def withAlternative[T](func: => T, alternative: => T ): T = try { func } catch { case _: Throwable => alternative}
  def withExit[T](func: => T, exit: => Any = { }): T = try { func } catch { case _: Throwable => exit; sys.exit(-1) }

  def currentTime: Long = System.nanoTime()
  // measures the running time of the provided func
  def time(func: => Unit): Long = {
    val startTime = currentTime
    func
    (currentTime-startTime)
  }

  def loadTex(filename: String, mode: Int = org.lwjgl.opengl.GL11.GL_NEAREST_MIPMAP_LINEAR): Int = {
    import org.lwjgl.opengl.GL12
    import org.lwjgl.opengl.GL11._
    import org.lwjgl.util.glu.GLU._
    import java.io._
    import java.nio._
    import de.matthiasmann.twl.utils.PNGDecoder
    try {
      val file = for(file <- (new File(".")).listFiles ++ (new File("src/main/resources")).listFiles; if(file.isFile && file.getName == filename)) yield file;
      // Open the PNG file as an InputStream
      val in = new FileInputStream(file.head)
      // Link the PNG decoder to this stream
      val decoder = new PNGDecoder(in)
  
      // Get the width and height of the texture
      val tWidth = decoder.getWidth()
      val tHeight = decoder.getHeight()
  
      // Decode the PNG file in a ByteBuffer
      val buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight())
      decoder.decode(buf, decoder.getWidth() * 4, PNGDecoder.Format.RGBA)
      buf.flip()

      in.close()
  
      /*for(y <- 0 until image.getHeight; x <- 0 until image.getWidth) {
        val pixel = pixels((image.getWidth*image.getHeight - 1) -  (y * image.getWidth + x))
        //buffer.put(((pixel >> 24) & 0xFF).toByte)    // Alpha component. Only for RGBA
        buffer.put(((pixel >> 16) & 0xFF).toByte)     // Red component
        buffer.put(((pixel >> 8) & 0xFF).toByte)      // Green component
        buffer.put((pixel & 0xFF).toByte)           // Blue component
      }*/
      
      //buffer.flip //FOR THE LOVE OF GOD DO NOT FORGET THIS

      // You now have a ByteBuffer filled with the color data of each pixel.
      // Now just create a texture ID and bind it. Then you can load it using 
      // whatever OpenGL method you want, for example:

      val textureID = glGenTextures //Generate texture ID
      glBindTexture(GL_TEXTURE_2D, textureID) //Bind texture ID
      
      //Setup wrap mode
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)

      //Setup texture scaling filtering
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, mode)
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, mode)
      
      //Send texel data to OpenGL
      gluBuild2DMipmaps(GL_TEXTURE_2D, 4, tWidth, tHeight, GL_RGBA, GL_UNSIGNED_BYTE, buf);
      //glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, tWidth, tHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf)

      //Return the texture ID so we can bind it later again
      //println(textureID)
      textureID
    } catch {
      case e: Exception => 
        e.printStackTrace
        -1
    }
  }
}

// some small classes

class SettingMap[A] extends mutable.HashMap[A,Any] {
  private val defaultMap = new mutable.AnyRefMap[String, Any]
  def setDefault[B](v: B)(implicit m: Manifest[B]): Unit = defaultMap += m.toString -> v
  def getDefault[B](implicit m: Manifest[B]): B = defaultMap.getOrElse(m.toString, null).asInstanceOf[B]
  
  def get[B: Manifest](key: A): B = getOrElse(key, getDefault[B]).asInstanceOf[B]
  // add trigger hooks for when some value updates :P
}
trait Properties {
  val properties = new SettingMap[String]
}


class TimeLock {
  private var locked = false
  def isLocked: Boolean = {
    if(locked && milliTime-lockTime > lockDuration) locked = false
    
    locked
  }
  
  private def milliTime: Long = System.nanoTime()/1000000L
  
  private var lockTime = milliTime
  private var lockDuration = 0L
  def lockIt(ms: Int) {
    lockTime = milliTime
    lockDuration = ms
    locked = true
  }
}
