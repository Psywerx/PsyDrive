package org.psywerx.PsyDrive

class ClojureWrap(ns: String, objName: String) {
  import clojure.lang.{RT,Var}
  import scala.collection.mutable

  RT.loadResourceScript(objName+".clj")
  val obj = ns+"."+objName
  val funcs = new mutable.AnyRefMap[String, Var]
  
  def /(func: String, a: Any): Object =
    funcs.getOrElseUpdate(func, RT.`var`(obj, func)).invoke(a.asInstanceOf[Object])
  def /(func: String, a: Any, b: Any): Object =
    funcs.getOrElseUpdate(func, RT.`var`(obj, func)).invoke(a.asInstanceOf[Object], b.asInstanceOf[Object])
  def /(func: String, a: Any, b: Any, c: Any): Object =
    funcs.getOrElseUpdate(func, RT.`var`(obj, func)).invoke(a.asInstanceOf[Object], b.asInstanceOf[Object], c.asInstanceOf[Object])
  def /(func: String, a: Any, b: Any, c: Any, d: Any): Object =
    funcs.getOrElseUpdate(func, RT.`var`(obj, func)).invoke(a.asInstanceOf[Object], b.asInstanceOf[Object], c.asInstanceOf[Object], d.asInstanceOf[Object])
}
