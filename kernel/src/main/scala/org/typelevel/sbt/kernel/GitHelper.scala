/*
 * Copyright 2022 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.typelevel.sbt.kernel

import scala.util.Try

import scala.sys.process._

private[sbt] object GitHelper {

  /**
   * Returns a list of strictly previous releases (i.e. ignores tags on HEAD).
   * @param fromHead
   *   if `true`, only tags reachable from HEAD's history. If `false`, all tags in the repo.
   */
  def previousReleases(
      versionScheme: Option[String],
      fromHead: Boolean = false): List[VersionType] = {
    Try {
      val merged = if (fromHead) " --merged HEAD" else ""
      // --no-contains omits tags on HEAD
      val l = s"git tag --no-contains HEAD$merged".!!.split("\n").toList.map(_.trim)
      versionScheme match {
        case Some("early-semver") =>
          l.collect { case SemV.Tag(version) => version }.sorted.reverse
        case Some("pvp") => l.collect { case PVPV.Tag(version) => version }.sorted.reverse
        case Some(s) => sys.error(s"unsupported version scheme $s")
        case None => Nil
      }
    }.getOrElse(List.empty)
  }

  def getTagOrHash(
      versionScheme: Option[String],
      tags: Seq[String],
      hash: Option[String]): Option[String] =
    versionScheme.flatMap {
      _ match {
        case "early-semver" =>
          tags.collect { case v @ SemV.Tag(_) => v }.headOption.orElse(hash)
        case "pvp" => tags.collect { case v @ SemV.Tag(_) => v }.headOption.orElse(hash)
        case s => sys.error(s"unsupported version scheme $s")
      }

    }
}
