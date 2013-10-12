package org.psywerx.PsyDrive

case class Player(
    val name: String, 
    val car: Car,
    val keys: Controls, 
    val cam: Camera,
    var health: Int = 100,
    var avatar: Int = -1,
    var shots: Seq[Shot] = Seq[Shot]()) {

  def shoot() {
    val bullet = Bullet(this.car.color)
    bullet.pos = this.car.pos.clone
    bullet.vector = Vec3(0,0,7)
    bullet.rot = this.car.rot.clone

    shots = shots :+ Shot(bullet, this)
  }
  def isShooting(): Boolean = shots.nonEmpty
}

case class Shot(
    val bullet: Bullet,
    val owner: Player) {

  def dispose() {
    owner.shots = owner.shots.filterNot(_ == this)
  }
}
    
case class Controls(
    val up: Int,
    val left: Int,
    val right: Int,
    val shoot: Int) 
