package org.psywerx.PsyDrive

case class Player(
    val name: String, 
    val car: Car,
    val keys: Controls, 
    val cam: Camera,
    var health: Int = 100,
    var avatar: Int = -1)
    
case class Controls(
    val up: Int,
    val left: Int,
    val right: Int) 
