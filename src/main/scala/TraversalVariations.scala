package org.example

import annotation.tailrec
import com.google.caliper.Param

object WordsGenerator {

  val wordLength = 8 
  val big = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
  val small = big.toLowerCase
  val letters = (big+small).map(_.toString)

  val rng = new util.Random

  def randomWord =  {
    var str = ""
    while( str.length < wordLength ) {
      str += letters( rng.nextInt( letters.size ) )
    }
    str
  }
  
  lazy val stream = {
    def drawWord(): Stream[String] =
      Stream.cons( randomWord, drawWord() )
    drawWord()
  }
}


class TraversalVariations extends SimpleScalaBenchmark {
  
  @Param(Array("10" ))
  val length: Int = 0

  var words: List[String] = _
  
  override def setUp() {
    words = WordsGenerator.stream.take(length).toList
  }

  def isCapitalized( s: String ) =
    java.lang.Character.isUpperCase(s.charAt(0))

 
 def timeFunctional(reps: Int) = repeat(reps) {
   val wLength = words.map( _.length )
   val wCaps = words.map( isCapitalized )
    (wLength.head, wCaps.head) 
  }

 

  def timeReassign(reps: Int) = repeat(reps) {
    var wLength = List[Int]()
    var wCaps = List[Boolean]()
    for( word <- words ) {
      wLength ::= word.length
      wCaps ::= isCapitalized(word)
    }
    (wLength.head, wCaps.head)
  }



 def timeBuffer(reps: Int) = repeat(reps) {
   import collection.mutable._
   val wLength = new ArrayBuffer[Int]()
   val wCaps = new ArrayBuffer[Boolean]()
   for( word <- words ) {
     wLength.append(word.length)
     wCaps.append(isCapitalized(word))
   }
   ( wLength.head, wCaps.head )
 }


  def timeOldSchool(reps: Int) = repeat(reps) {
    val n = words.length
    val lengthAry = Array.ofDim[Int](n)
    val capAry = Array.ofDim[Boolean](n)
    var i = 0
    val it = words.iterator
    while( it.hasNext ) {
      val w = it.next
      lengthAry(i) = w.length
      capAry(i) = isCapitalized(w)
      i += 1
    }
    ( lengthAry.head, capAry.head )
  }


 def timeRecurs(reps: Int) = repeat(reps) {

   @tailrec
   def lengthAndCaps( ws: List[String], ls: List[Int], cs: List[Boolean] ): (List[Int],List[Boolean]) = 
     if( ws.isEmpty ) (ls.reverse, cs.reverse)
     else {
       val w = ws.head
       lengthAndCaps( ws.tail, w.length::ls, isCapitalized(w)::cs )
     }

   val (wLength,wCaps) = lengthAndCaps( words, Nil, Nil )
   ( wLength.head, wCaps.head )
 }

 
  
  override def tearDown() {
    // clean up after yourself if required
  }
  
}

