/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eed3si9n.expecty

import collection.mutable.ListBuffer
import collection.immutable.TreeMap

class ExpressionRenderer(showTypes: Boolean, shortString: Boolean) {
  def render(recordedExpr: RecordedExpression[_]): String = {
    val offset = recordedExpr.text.segmentLength(_.isWhitespace, 0)
    val intro = new StringBuilder().append(recordedExpr.text.trim())
    val lines = ListBuffer(new StringBuilder)

    val rightToLeft = filterAndSortByAnchor(recordedExpr.recordedValues)
    for (recordedValue <- rightToLeft)
      placeValue(lines, recordedValue.value, math.max(recordedValue.anchor - offset, 0))

    lines.prepend(intro)
    lines.append(new StringBuilder)

    // debug
    // recordedExpr.recordedValues foreach { v =>
    //   val line = new StringBuilder()
    //   line.append(v.toString)
    //   lines.append(line)
    // }
    lines.mkString("\n")
  }

  private[this] def filterAndSortByAnchor(recordedValues: List[RecordedValue]): Iterable[RecordedValue] = {
    var map = TreeMap[Int, RecordedValue]()(Ordering.by(-_))
    // values stemming from compiler generated code often have the same anchor as regular values
    // and get recorded before them; let's filter them out
    for { value <- recordedValues } {
      if (!map.contains(value.anchor)) map += (value.anchor -> value)
    }
    map.values
  }

  private[this] def placeValue(lines: ListBuffer[StringBuilder], value: Any, col: Int): Unit = {
    val str = renderValue(value)

    placeString(lines(0), "|", col)

    import util.control.Breaks._
    breakable {
      for (line <- lines.drop(1)) {
        if (fits(line, str, col)) {
          placeString(line, str, col)
          break()
        }
        placeString(line, "|", col)
      }
      val newLine = new StringBuilder()
      placeString(newLine, str, col)
      lines.append(newLine)
    }
  }

  private[this] def renderValue(value: Any): String = {
    val str0 = if (value == null) "null" else value.toString
    val str =
      if (!shortString) str0
      else if (str0.contains("\n")) str0.linesIterator.toList.headOption.getOrElse("") + "..."
      else str0
    if (showTypes) str + " (" + value.getClass.getName + ")" // TODO: get type name the Scala way
    else str
  }

  private[this] def placeString(line: StringBuilder, str: String, anchor: Int): Unit = {
    val diff = anchor - line.length
    for (i <- 1 to diff) line.append(' ')
    line.replace(anchor, anchor + str.length(), str)
  }

  private[this] def fits(line: StringBuilder, str: String, anchor: Int): Boolean = {
    line.slice(anchor, anchor + str.length() + 1).forall(_.isWhitespace)
  }
}
