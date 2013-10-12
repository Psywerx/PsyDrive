package org.psywerx.PsyDrive

import org.lwjgl.opengl.{Display,PixelFormat,DisplayMode,Util}
import org.lwjgl.input.Keyboard
import scala.collection.mutable.ListBuffer
import scala.collection.Traversable
import java.nio._
import scala.concurrent._
import scala.concurrent.util._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import scala.util.Random.nextFloat
import Keyboard._

object PsyDrive {
  import Utils._
  import Global._
  import org.lwjgl.opengl.GL11._
  
  var gameLoopRunning = false // is main loop running
  var (winWidth, winHeight) = (3000, 3000) // window size
  var renderTime = 0f
  
  val timeLock = new TimeLock
  var lastFPS = 0f
  
  /**
   * Initializes display and enters main loop
   */
  def main(args: Array[String]) {
    //withExit(
      initDisplay()//,
    //  println("Can't open display.")
    //)

    gameLoop()
    
    // Cleanup
    Display.destroy()
  }

  def initDisplay() {
    Display.setTitle("PsyDrive")
    Display.setVSyncEnabled(true)
    Display.setFullscreen(true)

    val bestMode = Display.getAvailableDisplayModes.reduce((bestMode,mode) => 
      if(mode.getWidth >= bestMode.getWidth && mode.getHeight >= bestMode.getHeight 
      && mode.getBitsPerPixel >= bestMode.getBitsPerPixel) {
        mode 
      } else {
        bestMode
      }
    )
    
    Display.setDisplayMode(bestMode)
    winWidth = bestMode.getWidth
    winHeight = bestMode.getHeight
    
    println("Display: "+bestMode.getWidth+"x"+bestMode.getHeight+"@"+bestMode.getFrequency+"Hz, "+bestMode.getBitsPerPixel+"bit")
    
    withAlternative(
      Display.create(new PixelFormat(8, 16, 0, 1)),
      Display.create()
    )
  }

  // Frame-independent movement timer
  var frameTime = currentTime

  def decreaseDetail() {
    import Settings._
    graphics -= 1
    if(graphics == 1 && maxDepth > 5) maxDepth = 5
    println("decreased graphic detail to "+graphics)
  }
  def increaseDetail() {
    import Settings._
    graphics += 1
    maxDepth += 1
    println("increased graphic detail to "+graphics)
  }
    
  /**
   * Game loop: renders and processes input events
   */
  def gameLoop() { 
    makeModels() // make generative models
    setupView()  // setup camera and lights
  
    // FPS counter
    var frameCounter = 0
    val second = 1000000000L
    val FPSseconds = 5
    var FPStimer = currentTime
    frameTime = currentTime

    gameLoopRunning = true
    while(gameLoopRunning) {
      processInput() // process keyboard input
      
      resetView()   // clear view and reset transformations
      renderFrame()  // draw stuff
      Display.update() // update window contents and process input messages
      frameCounter += 1

      if(currentTime-FPStimer > second*FPSseconds) {
        val FPS = frameCounter/FPSseconds.toFloat
        println("-------------------")
        println("FPS: "+FPS)
        println("Tasks: "+tasks.length)
        println("Render: "+(renderTimes/fullTimes.toDouble).toFloat)
        println("Physics: "+(physicsTimes/fullTimes.toDouble).toFloat)
        println("Worker: "+(workerTimes/fullTimes.toDouble).toFloat)
        println("-------------------")

        lastFPS = FPS
        frameCounter = 0
        FPStimer = currentTime
      }

      renderTime = (currentTime-frameTime)/frameIndepRatio
      frameTime = currentTime
    } 
  }
  
  //models
  val cam = new Camera
  var terrain: GeneratorModel = null
  var players = ListBuffer[Player]()
  val trees = new ListBuffer[GeneratorModel]
  var futureTree: Future[GeneratorModel] = null
  val dropBranches = new ListBuffer[GeneratorModel]//TODO: HashSet?
  val trails = new ListBuffer[TrailModel]
  case class Particle() extends DisplayModel
  val particles = ListBuffer[Particle]()
  var gameover = -1
  var isGameOver = false
  var gameoverTimeLock = new TimeLock
  
  def models(): Traversable[DisplayModel] = (players.map(_.car) ++ players.flatMap(_.shots).map(_.bullet) ++ List(terrain) ++ particles ++ trees ++ dropBranches ++ trails)
  
  def makeModels() {
    terrain = TerrainFactory()
    terrain.setPosition(-Settings.worldSize,0,-Settings.worldSize)
    terrain.setScale(Settings.worldSize*2, 5, Settings.worldSize*2)
    
    gameover = Utils.loadTex("gameover.png", GL_NEAREST)
    
    val controls1 = Controls(up = KEY_W, left = KEY_A, right = KEY_D, shoot = KEY_S)
    val controls2 = Controls(up = KEY_I, left = KEY_J, right = KEY_L, shoot = KEY_K)
    val controls3 = Controls(up = KEY_UP, left = KEY_LEFT, right = KEY_RIGHT, shoot = KEY_DOWN)

    /*players += Player(
      "hypernurb", 
      Car(color = Vec3(0.9f,0.0f,0.0f)),
      Controls(up = KEY_W, left = KEY_A, right = KEY_D),
      new Camera,
      avatar = Utils.loadTex("hypernurb.png", GL_NEAREST))*/
    players += Player(
      "rainbowsocks", 
      Car(color = Vec3(-1f,-1f,-1f)),
      controls1,
      new Camera,
      avatar = Utils.loadTex("rainbowsocks.png", GL_NEAREST))
    players += Player(
      "lord_ddoom", 
      Car(color = Vec3(0.1f,0.25f,0.1f)),
      controls2,
      new Camera,
      avatar = Utils.loadTex("lord_ddoom.png", GL_NEAREST))
    players += Player(
      "smotko",
      Car(color = Vec3(0.35f,0.75f,0.95f)),
      controls3,
      new Camera,
      avatar = Utils.loadTex("smotko.png", GL_NEAREST))
    
    players = scala.util.Random.shuffle(players.toBuffer).take(2).to[ListBuffer]

    for((player, i) <- players.zipWithIndex) {
      player.car.setPosition(i*20,player.car.scaling.y+1,0)
      player.car.vector = Vec3(0,0,0)
      if(i == 0)
        player.cam.setViewPort(0,0,winWidth/2,winHeight)
      else
        player.cam.setViewPort(winWidth/2,0,winWidth/2,winHeight)
      
      player.cam.setPerspective(50, (winWidth/2)/winHeight.toFloat, 1f, 600f)
      player.cam.setPosition(0,Settings.worldSize-5,-Settings.worldSize+5)
      player.cam.pos = player.car.pos
      player.cam.setRotation(0,0,0)
    }
    
    futureTree = future { TreeFactory() }
  }
  
  /**
  * Initial setup of projection of the scene onto screen, lights etc.
  */
  def setupView() {
    glClearColor(0.3f, 0.6f, 0.8f, 1f)

    glEnable(GL_DEPTH_TEST) // enable depth buffer (off by default)
    //glEnable(GL_CULL_FACE)  // enable culling of back sides of polygons
    //glCullFace(GL_BACK)
    
    // smooth shading - Gouraud
    glShadeModel(GL_SMOOTH)
    //glShadeModel(GL_FLAT)

    // lights
    glEnable(GL_LIGHTING)
    glEnable(GL_LIGHT0)

    // LWJGL makes float buffers a bit difficult
    def floatBuffer(a: Float*): FloatBuffer = {
      ByteBuffer
        .allocateDirect(a.length*4)
        .order(ByteOrder.nativeOrder)
        .asFloatBuffer.put(a.toArray)
        .flip
        .asInstanceOf[FloatBuffer]
    }
    //def floatBuffer(a: Float*): FloatBuffer = org.lwjgl.BufferUtils.createFloatBuffer(a.length).put(a.toArray) // fails for some reason

    glLight(GL_LIGHT0, GL_AMBIENT, floatBuffer(0.3f, 0.3f, 0.3f, 0.0f))
    glLight(GL_LIGHT0, GL_DIFFUSE, floatBuffer(0.7f, 0.7f, 0.7f, 0.0f))
    glLightf(GL_LIGHT0, GL_LINEAR_ATTENUATION, 20f)
    glLight(GL_LIGHT0, GL_POSITION, floatBuffer(0f, 0f, 10f, 0f))
    glEnable(GL_COLOR_MATERIAL)
    glMaterial(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, floatBuffer(0.9f, 0.9f, 0.9f, 0f))
    glColorMaterial(GL_FRONT, GL_AMBIENT_AND_DIFFUSE)
  }
  
  /**
  * Resets the view of current frame
  */
  def resetView() {
    // clear color and depth buffer
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
  }
  
  val frameIndepRatio = 48000000f
  var pause = false

  var fullTimes = 0L
  var renderTimes = 0L
  var physicsTimes = 0L
  var workerTimes = 0L
  
  /**
  * Renders current frame
  */
  def renderFrame() {
    fullTimes += time {
      workerTimes += time {///write tasks object
        def doTask() {
          tasks.head()
          tasks = tasks.tail
          if(tasks.isEmpty) println("all tasks done")
        }
        
        // execute non-time-critical tasks... spread them out
        if(!tasks.isEmpty) {
          val cutoff = if(pause) 10 else 50
          for(i <- 0 to tasks.length/cutoff; if(0.05f+(tasks.length-cutoff*i)/(cutoff.toFloat) > nextFloat)) doTask()
        }
      }
    
      if(!pause) {
        physicsTimes += time {
          def moveVector(obj: DisplayModel): Vec3 = Vec3(
            math.sin(obj.rot.y/(180f/math.Pi)).toFloat*obj.vector.z,
            obj.vector.y,
            math.cos(obj.rot.y/(180f/math.Pi)).toFloat*obj.vector.z
          )
          for(p <- players) {
            //shots
            for(s <- p.shots) {
              s.bullet.pos += moveVector(s.bullet)*renderTime
              val tmp = s.bullet.pos.clone
              s.bullet.pos.clamp(Settings.worldSize+5)
              if(tmp != s.bullet.pos) s.dispose()
            }

            if(math.abs(p.car.vector.z) < 0.1) p.car.vector.z = 0.1f
            
            p.car.vector.z += 0.05f*renderTime
            p.car.vector.clamp(0,0,5)

            p.car.pos += moveVector(p.car)*renderTime
            //collision detection
            for(p2 <- players filterNot { _ == p}; s <- p2.shots) {
              val pBox = p.car.box.offsetBy(p.car.pos)
              val sBox = s.bullet.box.offsetBy(s.bullet.pos)
              val pBox2 = p.car.box.offsetBy(p.car.pos + moveVector(p.car)*renderTime)
              if((pBox boxCollide sBox) || (pBox2 boxCollide sBox)) {
                  p.health -= 50
                  p.car.vector.z = -p.car.vector.z/300f
                  s.dispose()
              }
            }
            for(r <- players; if !(r eq p)) {
              val pBox = p.car.box.offsetBy(p.car.pos)
              val rBox = r.car.box.offsetBy(r.car.pos)
              val pBox2 = p.car.box.offsetBy(p.car.pos + moveVector(p.car)*renderTime)
              val rBox2 = r.car.box.offsetBy(r.car.pos + moveVector(r.car)*renderTime)
              if(pBox boxCollide rBox) {
                if(p.car.vector.z < r.car.vector.z) {
                  p.health -= r.car.vector.z.toInt*3
                } else {
                  r.health -= p.car.vector.z.toInt*3
                }

                p.health -= r.car.vector.z.toInt*2
                r.health -= p.car.vector.z.toInt*2

                p.car.pos -= moveVector(p.car)*renderTime
                while(!(pBox boxCollide rBox)) {
                  p.car.pos += moveVector(p.car) * 0.01f
                }
                if(pBox boxCollide rBox) p.car.pos -= moveVector(p.car) * 0.01f
                p.car.vector.z = -p.car.vector.z/1.5f
                r.car.vector.z = -r.car.vector.z/1.5f
              }
            }
            for(r <- trees) {
              val pBox = p.car.box.offsetBy(p.car.pos)
              val rBox = r.box.offsetBy(r.pos)
              val pBox2 = p.car.box.offsetBy(p.car.pos + moveVector(p.car)*renderTime)
              if((pBox boxCollide rBox) || (pBox2 boxCollide rBox)) {
                if(false && p.car.vector.z > 3) {
                  val branch = r.data.asInstanceOf[Branch]
                  def dropBranch(b: Branch): GeneratorModel = {
                    b.detach()
                    b.children.foreach { child =>
                      if(child.depth < Settings.maxDepth) dropBranch(child)
                      child.marked = true
                    }
                    
                    if(0.6.prob) b.properties += "hasLeaf" -> false
                    
                    val drop = new GeneratorModel(() => b, (data: Object) => data.asInstanceOf[Branch].doAll(_.render))
                    drop.pos = r.pos.clone
                    drop.vector = Vec3(
                      (math.sin(p.car.rot.y/(180f/math.Pi)).toFloat*p.car.vector.z/2)*(1+nextFloat/6-nextFloat/12) +nextFloat/17-nextFloat/17,
                      p.car.vector.y/(5 + nextFloat/4 - nextFloat/4), 
                      (math.cos(p.car.rot.y/(180f/math.Pi)).toFloat*p.car.vector.z/2)*(1+nextFloat/6-nextFloat/12) +nextFloat/17-nextFloat/17
                    )
                    dropBranches += drop
                    drop
                  }
                  
                  branch.doWhile(b => true, b => dropBranches += dropBranch(b))
                  r.visible = false
                  trees -= r
                } else {
                  p.health -= p.car.vector.z.toInt
                  p.car.pos -= moveVector(p.car)*renderTime
                  p.car.vector.z = -p.car.vector.z/3f
                }
              }
            }
            p.car.pos.clamp(Settings.worldSize - p.car.scaling.z)
            if(p.car.pos.x >= Settings.worldSize - p.car.scaling.z) p.car.vector.z = p.car.vector.z/2f

            val moveObj = p.car
            p.cam.lookAt(moveObj)
            val camVec = Vec3(
              math.sin(moveObj.rot.y/(180f/math.Pi)).toFloat*moveObj.vector.z,
              0,
              math.cos(moveObj.rot.y/(180f/math.Pi)).toFloat*moveObj.vector.z
            )
            //cam.angle = Vec3(0,-1,50)
            p.cam.angle = Vec3(0,-5,100)
              
            val camMulti = 7f
            //cam.pos = ((cam.pos*camMulti) + ((moveObj.pos - Vec3(0f,0f,-50f))*renderTime))/(camMulti+renderTime)
            p.cam.pos = ((p.cam.pos*camMulti) + ((moveObj.pos - p.cam.angle)*renderTime))/(camMulti+renderTime)
            p.cam.vector -= p.cam.vector*renderTime*0.05f
          }

        if(players(0).health <= 0 || players(1).health <= 0) {
          players.map(_.car.vector = Vec3())
          pause = true
          isGameOver = true
          gameoverTimeLock.lockIt(5000)
        }
        // drop branches
        for(branch <- dropBranches) {
          branch.vector += (Settings.gravity)*renderTime
          branch.pos += branch.vector*renderTime
          if(branch.pos.y < -Settings.worldSize-50) {
            dropBranches -= branch
          }
        }
          
/*        // collision detection with trees
        for(tree <- trees; if(tree.visible)) {
          var done = false
          var collision = false
          val branch = tree.data.asInstanceOf[Branch]
          def dropBranch(b: Branch): GeneratorModel = {          
            b.detach()
            b.children.foreach { child =>
              if(child.depth < Settings.maxDepth) dropBranch(child)
              child.marked = true
            }
            
            if(0.6.prob) b.properties += "hasLeaf" -> false
            
            val drop = new GeneratorModel(() => b, (data: Object) => data.asInstanceOf[Branch].doAll(_.render))
            drop.pos = tree.pos.clone
            drop.vector = Vec3(
              (math.sin(pig.rot.y/(180f/math.Pi)).toFloat*pig.vector.z/2)*(1+nextFloat/6-nextFloat/12) +nextFloat/17-nextFloat/17,
              pig.vector.y/(5 + nextFloat/4 - nextFloat/4), 
              (math.cos(pig.rot.y/(180f/math.Pi)).toFloat*pig.vector.z/2)*(1+nextFloat/6-nextFloat/12) +nextFloat/17-nextFloat/17
            )
            //drop.vector = Vec3(0,0,0)
            //drop.compile()
            dropBranches += drop
            drop
          }
          
          def getBox(m: DisplayModel): BoundingBox = { //TODO default value :)
            val out = m.properties.get[BoundingBox]("box")
            if(out == null) new BoundingBox(Vec3()) else out
          }
          val moveBoxy = getBox(moveObj)
          moveObj.properties += "box" -> moveBoxy
          def moveBox: BoundingBox = moveBoxy offsetBy moveObj.pos

          branch.doWhile(b => (!done && !b.marked && b.depth <= Settings.maxDepth),
            b => {
              val box = b.properties.get[BoundingBox]("box")
              val canCollide = box.pointCollide(pig.pos, tree.pos)
              if(b.depth == 1) {
                if(!canCollide) {
                  done = true 
                } else {
                  val basebox = (new BoundingBox(b.rootVec, b.destVec)).offsetBy(tree.pos)
                  basebox.min -= 2f
                  basebox.max.x += 2f
                  basebox.max.y -= 2f
                  basebox.max.z += 2f
                  if(basebox.boxCollide(moveBox)) {
                    moveObj.vector.z = -moveObj.vector.z
                    if(math.abs(moveObj.vector.z) < 0.01f) moveObj.vector.z = 0.01f*math.abs(moveObj.vector.z)/moveObj.vector.z
                    var limit = 500
                    while(basebox.boxCollide(moveBox) && limit > 0) {
                      val moveVec = Vec3(
                        math.sin(moveObj.rot.y/(180f/math.Pi)).toFloat*moveObj.vector.z,
                        0,
                        math.cos(moveObj.rot.y/(180f/math.Pi)).toFloat*moveObj.vector.z
                      )
                      moveObj.pos += moveVec*renderTime
                      limit -= 1
                    }
                    moveObj.vector.z = moveObj.vector.z/2
                  }
                }
              } else if(Settings.pigAir && ((pig.pos-(box.min+tree.pos)).length < 4.25f || (pig.pos-(box.max+tree.pos)).length < 4.25f)) { // should be if(box.pointCollide(pig.pos, tree.pos) && 
                collision = true
                var dropped = false
                
                pig.vector.y /= 2
                
                if(0.6.prob) {
                  for(child <- b.children) if(0.75.prob) {
                    dropped = true
                    dropBranch(child)
                  }
                } else {
                  dropped = true
                  dropBranch(b)
                }
                
                if(dropped) {
                  //println("collision")
                  done = true
                } else {
                  collision = false
                }
              }
            }
          )
          
          if(collision) {
            //tree.compile()
            //tree.reset()

            var depthSum = 0
            val sumLim = 4
            branch.doWhile(b => depthSum <= sumLim, b => depthSum += 1)
            if(depthSum <= sumLim) { // tree is dead
              val drop = dropBranch(branch)
              drop.vector.y = 2
              trees -= tree
              //tree.free()
            }
          }
        }

        // drop branches
        for(branch <- dropBranches) {
          branch.vector += (Settings.gravity)*renderTime
          //branch.vector -= branch.vector*renderTime*0.05f
          branch.pos += branch.vector*renderTime
          if(branch.pos.y < -Settings.worldSize-50) {
            dropBranches -= branch
            //branch.free()
            //if(dropBranches.isEmpty) println("all broken branches removed")
          }
        }*/
        }
      }
      
      
      if(futureTree == null) {
        if(trees.length < 7 && tasks.size < 500) futureTree = future { TreeFactory() }
      } else if(futureTree.isCompleted) {
        val presentTree = Await.result(futureTree, Duration.Inf)
        trees += presentTree
        
        def growTree(lvl: Int, tree: GeneratorModel) {
          if(lvl <= Settings.maxDepth) {///@ move setting to tree!
            //val ex = Settings.maxDepth
            //Settings.maxDepth = lvl //TODO: oh my gawd, thread unsafe
            //tree.compile()
            //Settings.maxDepth = ex
          }
        }
        
        growTree(2, presentTree)
        for(i <- 3 to Settings.maxDepth) tasks = tasks :+ (() => growTree(i, presentTree))
        
        futureTree = null
        println("new tree added")
      }

      renderTimes += time { 
        for(p <- players) {
          glPushMatrix()
          p.cam.render
          models().foreach(_.render)
          glPopMatrix()
        }
        HUD.render()
      }
    }
  }
  
  def processInput() {
    if(Display.isCloseRequested || isKeyDown(KEY_ESCAPE)) {
      gameLoopRunning = false
      return
    }    
    
    if(isGameOver && !gameoverTimeLock.isLocked) sys.exit(0)
    
    val keymove = 1.5f*renderTime
    
    if(isKeyDown(KEY_Z) && !timeLock.isLocked){
      terrain.visible = !terrain.visible
      timeLock.lockIt(100)     
    }

    if(isKeyDown(KEY_G)) players.head.health = 0

    if(isKeyDown(KEY_P)) {
      pause = true; println("paused")
    }
    if(isKeyDown(KEY_RSHIFT)) {
      pause = false; println("unpaused")
    }
    if(pause) return;

    for(p <- players) {
      if(isKeyDown(p.keys.up))  { p.car.vector.z += 0.2f; }
      if(isKeyDown(p.keys.left))  { p.car.rot.y += keymove*7f; p.car.vector.z -= 0.05f*p.car.vector.z*renderTime }
      if(isKeyDown(p.keys.right)) { p.car.rot.y -= keymove*7f; p.car.vector.z -= 0.05f*p.car.vector.z*renderTime }
      if(isKeyDown(p.keys.shoot)) { if(!p.isShooting) p.shoot() }
    }
    
    if(isKeyDown(KEY_O)) {
      println("Cam: "+cam.toString)
    }
  }
}



