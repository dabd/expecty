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

import com.eed3si9n.expecty.DiffUtil.splitTokens

object DiffUtilTest extends verify.BasicTestSuite {
  val assert1 = Expecty.assert

  test("handle empty string") {
    val result = splitTokens("", Nil)
    assert1 {
      result.isEmpty
    }
  }

  test("handle standard SGR sequence") {
    val result = splitTokens("\u001b[31mRed\u001b[0m", Nil)
    assert1 {
      result == List("\u001b[31m", "Red", "\u001b[0m")
    }
  }

  test("handle incomplete SGR sequence") {
    val result = splitTokens("\u001bRed", Nil)
    assert1 {
      result == List("\u001b", "Red")
    }
  }

  test("handle multiple ANSI sequences") {
    val result = splitTokens("\u001b[31mRed\u001b[0m \u001b[32mGreen", Nil)
    assert1 { result == List("\u001b[31m", "Red", "\u001b[0m", " ", "\u001b[32m", "Green") }
  }

  test("handle mixed content") {
    val result = splitTokens("Normal \u001b[31mRed\u001b[0m Text\u001b", Nil)
    assert1 { result == List("Normal", " ", "\u001b[31m", "Red", "\u001b[0m", " ", "Text", "\u001b") }
  }

  test("handle alphanumeric string") {
    val result = splitTokens("abc123", Nil)
    assert1 {
      result == List("abc123")
    }
  }

  test("handle string with whitespace and mirrored characters") {
    val result = splitTokens(" \u202E ", Nil)
    assert1 {
      result == List(" ", "\u202E", " ")
    }
  }

  test("handle string with special characters") {
    val result = splitTokens("!@#$%^&*()_+|", Nil)
    assert1 {
      result == List("!@#$%^&*", "(", ")", "_+|")
    }
  }

  test("handle long string") {
    val longString = "a" * 10000
    val result = splitTokens(longString, Nil)
    assert1 {
      result == List(longString)
    }
  }

  test("handle escaped escape character") {
    val result = splitTokens("\u001b\u001bDouble Escape", Nil)
    assert1 {
      result == List("\u001b", "\u001b", "Double", " ", "Escape")
    }
  }

}
