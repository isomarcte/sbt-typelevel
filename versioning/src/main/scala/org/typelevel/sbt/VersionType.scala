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

package io.isomarcte.sbt

import scala.util.Try

private[sbt] sealed trait VersionType {
  def isPrerelease: Boolean
  def isSameSeries(vt: VersionType): Boolean
  def mustBeBinCompatWith(vt: VersionType): Boolean
}

private[sbt] final case class SemV(
    major: Int,
    minor: Int,
    patch: Option[Int],
    prerelease: Option[String]
) extends Ordered[SemV] with VersionType{

  override def toString: String =
    s"$major.$minor${patch.fold("")(p => s".$p")}${prerelease.fold("")(p => s"-$p")}"

  def isPrerelease: Boolean = prerelease.nonEmpty

  def isSameSeries(that: VersionType): Boolean = that match {
    case a: SemV => this.major == a.major && this.minor == a.minor
    case a: PVPV => sys.error(s"trying to compare pvp to semver")
  }


  def mustBeBinCompatWith(vt: VersionType): Boolean = vt match {
    case that: SemV => this >= that && !that.isPrerelease && this.major == that.major && (major > 0 || this.minor == that.minor)
    case a: PVPV => sys.error(s"trying to compare pvp to semver")
  }

  def compare(that: SemV): Int = {
    val x = this.major.compare(that.major)
    if (x != 0) return x
    val y = this.minor.compare(that.minor)
    if (y != 0) return y
    (this.patch, that.patch) match {
      case (None, None) => 0
      case (None, Some(patch)) => 1
      case (Some(patch), None) => -1
      case (Some(thisPatch), Some(thatPatch)) =>
        val z = thisPatch.compare(thatPatch)
        if (z != 0) return z
        (this.prerelease, that.prerelease) match {
          case (None, None) => 0
          case (Some(_), None) => 1
          case (None, Some(_)) => -1
          case (Some(thisPrerelease), Some(thatPrerelease)) =>
            // TODO not great, but not everyone uses Ms and RCs
            thisPrerelease.compare(thatPrerelease)
        }
    }
  }
}

private[sbt] object SemV {
  val version = """^(0|[1-9]\d*)\.(0|[1-9]\d*)(?:\.(0|[1-9]\d*))?(?:-(.+))?$""".r

  def apply(v: String): Option[SemV] = SemV.unapply(v)

  def unapply(v: String): Option[SemV] = v match {
    case version(major, minor, patch, prerelease) =>
      Try(SemV(major.toInt, minor.toInt, Option(patch).map(_.toInt), Option(prerelease))).toOption
    case _ => None
  }

  object Tag {
    def unapply(v: String): Option[SemV] =
      if (v.startsWith("v")) SemV.unapply(v.substring(1)) else None
  }
}

private[sbt] final case class PVPV(
    majorA: Int,
    majorB: Int,
    minor: Int,
    patch: Option[Int],
    prerelease: Option[String]
) extends Ordered[PVPV] with VersionType{

  override def toString: String =
    s"$majorA.$majorB.$minor${patch.fold("")(p => s".$p")}${prerelease.fold("")(p => s"-$p")}"

  def isPrerelease: Boolean = prerelease.nonEmpty

  def isSameSeries(vt: VersionType): Boolean = vt match {
    case a: SemV => sys.error(s"trying to compare pvp to semver")
    case that: PVPV => this.majorA == that.majorA && this.majorB == that.majorB && this.minor == that.minor
  }


  def mustBeBinCompatWith(vt: VersionType): Boolean = vt match {
    case a: SemV => sys.error(s"trying to compare pvp to semver")
    case that: PVPV => this >= that && !that.isPrerelease && this.majorA == that.majorA && this.majorB == that.majorB && ( majorA > 0 || (majorA == 0 && majorB > 0) || this.minor == that.minor)
  }
  def compare(that: PVPV): Int = {
    val a = this.majorA.compare(that.majorA)
    if (a != 0) return a
    val b = this.majorB.compare(that.majorB)
    if (b != 0) return b
    val y = this.minor.compare(that.minor)
    if (y != 0) return y
    (this.patch, that.patch) match {
      case (None, None) => 0
      case (None, Some(patch)) => 1
      case (Some(patch), None) => -1
      case (Some(thisPatch), Some(thatPatch)) =>
        val z = thisPatch.compare(thatPatch)
        if (z != 0) return z
        (this.prerelease, that.prerelease) match {
          case (None, None) => 0
          case (Some(_), None) => 1
          case (None, Some(_)) => -1
          case (Some(thisPrerelease), Some(thatPrerelease)) =>
            thisPrerelease.compare(thatPrerelease)
        }
    }
  }
}

private[sbt] object PVPV {
  val version = """^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:\.(0|[1-9]\d*))?(?:-(.+))?$""".r

  def apply(v: String): Option[PVPV] = PVPV.unapply(v)

  def unapply(v: String): Option[PVPV] = v match {
    case version(majorA, majorB, minor, patch, prerelease) =>
      Try(PVPV(majorA.toInt, majorB.toInt, minor.toInt, Option(patch).map(_.toInt), Option(prerelease))).toOption
    case _ => None
  }

  object Tag {
    def unapply(v: String): Option[PVPV] =
      if (v.startsWith("v")) PVPV.unapply(v.substring(1)) else None
  }
}