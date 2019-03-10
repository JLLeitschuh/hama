/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
name := "membership"

libraryDependencies ++= Seq (
  "com.google.guava" % "guava" % "27.0-jre",
  "io.aeron" % "aeron-all" % "1.11.3",
  "org.apache.curator" % "curator-framework" % "4.0.1",
  "org.scalaz" %% "scalaz-zio" % "0.3.1",
  "org.typelevel" %% "cats-core" % "1.4.0",
  "org.apache.curator" % "curator-test" % "4.0.1" % Test
)