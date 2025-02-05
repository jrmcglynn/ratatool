/*
 * Copyright 2016 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.ratatool.io

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File}
import java.nio.file.Files

import com.spotify.ratatool.Schemas
import com.spotify.ratatool.avro.specific.TestRecord
import com.spotify.ratatool.scalacheck._
import org.apache.commons.io.FileUtils
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ParquetIOTest extends AnyFlatSpec with Matchers {

  private val genericSchema = Schemas.avroSchema
  private val genericGen = genericRecordOf(genericSchema)
  private val genericData = (1 to 100).flatMap(_ => genericGen.sample)

  private val specificSchema = TestRecord.getClassSchema
  private val specificGen = specificRecordOf[TestRecord]
  private val specificData = (1 to 100).flatMap(_ => specificGen.sample)

  "ParquetIO" should "work with generic record and stream" in {
    val out = new ByteArrayOutputStream()
    ParquetIO.writeToOutputStream(genericData, genericSchema, out)
    val in = new ByteArrayInputStream(out.toByteArray)
    val result = ParquetIO.readFromInputStream(in).toList
    result should equal(genericData)
  }

  it should "work with generic record and file" in {
    val dir = Files.createTempDirectory("ratatool-")
    val file = new File(dir.toString, "temp.parquet")
    ParquetIO.writeToFile(genericData, genericSchema, file)
    val result = ParquetIO.readFromFile(file).toList
    result should equal(genericData)
    FileUtils.deleteDirectory(dir.toFile)
  }

  it should "work with specific record and stream" in {
    val out = new ByteArrayOutputStream()
    ParquetIO.writeToOutputStream(specificData, specificSchema, out)
    val in = new ByteArrayInputStream(out.toByteArray)
    val result = ParquetIO.readFromInputStream[TestRecord](in).toList
    result.map(FixRandomData(_)) should equal(specificData.map(FixRandomData(_)))
  }

  it should "work with specific record and file" in {
    val dir = Files.createTempDirectory("ratatool-")
    val file = new File(dir.toString, "temp.parquet")
    ParquetIO.writeToFile(specificData, specificSchema, file)
    val result = ParquetIO.readFromFile[TestRecord](file).toList
    result.map(FixRandomData(_)) should equal(specificData.map(FixRandomData(_)))
    FileUtils.deleteDirectory(dir.toFile)
  }

}
