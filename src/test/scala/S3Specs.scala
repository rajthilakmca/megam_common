/* 
** Copyright [2012] [Megam Systems]
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
** http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/
/**
 * @author rajthilak
 *
 */

import org.specs2._
import scalaz._
import Scalaz._
import org.specs2.mutable._
import org.specs2.Specification
import org.megam.common.amqp._
import org.specs2.matcher.MatchResult
import org.megam.common.s3._


class S3Specs extends Specification {

  def is =
    "S3Specs".title ^ end ^
      """
  S3 file download
    """ ^ end ^
      "S3 file download spec Should" ^
      "Correctly download the file " ! Download.succeeds ^
      end

  private lazy val s3: S3 = new S3(Tuple2("AKIAIX6YNFLZJDUMS3JA", "VQD76LG8YfPJkgB8kH4dEyisJw2vkzDFwhBeDhv4"), "s3-ap-southeast-1.amazonaws.com")

  case object Download {    

    def succeeds = {     
      val res = s3.download("cloudrecipes", "sandy@megamsandbox.com/chef/chef-repo.zip")
      (new ZipArchive).unZip("cloudrecipes/sandy@megamsandbox.com/chef/chef-repo.zip", "cloudrecipes/sandy@megamsandbox.com/chef/")
      println("-->" + res)
      val expectedRes = 0
      res mustEqual expectedRes

    }
  }

}