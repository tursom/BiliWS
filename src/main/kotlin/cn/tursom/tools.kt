package cn.tursom

import cn.tursom.core.ThreadLocalSimpleDateFormat
import cn.tursom.core.toUTF8String
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintWriter

val errLog = File("err.log").printWriter()

fun PrintWriter.log(log: String) {
  println("${ThreadLocalSimpleDateFormat.standard.format(System.currentTimeMillis())}: $log")
}

fun PrintWriter.log(log: String, e: Throwable) {
  println("${ThreadLocalSimpleDateFormat.standard.format(System.currentTimeMillis())}: $log")
  e.printStackTrace(this)
}

fun PrintWriter.log(e: Throwable) {
  print(ThreadLocalSimpleDateFormat.standard.format(System.currentTimeMillis()))
  val bos = ByteArrayOutputStream()
  e.printStackTrace(PrintWriter(bos))
  print(
    "${ThreadLocalSimpleDateFormat.standard.format(System.currentTimeMillis())}: ${
    bos.toByteArray().toUTF8String()}"
  )
}