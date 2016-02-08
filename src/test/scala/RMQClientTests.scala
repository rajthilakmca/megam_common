/*
** Copyright [2012-2013] [Megam Systems]
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
 * @author ram
 *
 */

import io.megam.common.amqp._
import io.megam.common.concurrent._
import org.specs2.Specification
import io.megam.common.amqp.{ AMQPClient, RabbitMQClient }
import io.megam.common.amqp.response.{ AMQPResponse, AMQPResponseCode }
import java.net.URL
import scalaz._
import Scalaz._
import scalaz.Validation._
import scala.concurrent.{ Future }
import scala.concurrent.duration.Duration

trait RMQClientTests { this: Specification =>

  class RMQClientTests {

    private val exchange_name = "megam_bannister1.megam.co_exchange"
    private val queue_name = "megam_bannister1.megam.co_queue"
    private val routingKey = "megam_key"
    private val message1 = Messages("message" -> "{\"Id\":\"APR416511659171905536\"},{\"Action\":\"nstop\"},{\"Args\":\"Nah\"}")
    private val messageJson = MessagePayLoad(message1).toJson(false)
    private def executeP(client: AMQPClient, expectedCode: AMQPResponseCode = AMQPResponseCode.Ok,
      duration: Duration = duration) = {
      import io.megam.common.concurrent.SequentialExecutionContext
      val responseFuture: Future[ValidationNel[Throwable, AMQPResponse]] =
        client.publish(messageJson, routingKey).apply
      responseFuture.block(duration).toEither must beRight.like {
        case ampq_res => ampq_res.code must beEqualTo(expectedCode)
      }
    }

    private def executeS(client: AMQPClient, expectedCode: AMQPResponseCode = AMQPResponseCode.Ok,
      duration: Duration = duration) = {
      import io.megam.common.concurrent.SequentialExecutionContext
      val responseFuture: Future[ValidationNel[Throwable, AMQPResponse]] =
        client.subscribe(qThirst, routingKey).apply
      responseFuture.block(duration).toEither must beRight.like {
        case ampq_res => ampq_res.code must beEqualTo(expectedCode)
      }
    }

    /**
     * This is a callback function invoked when an consumer thirsty for a response wants it to be quenched.
     * The response is a either a success or  a failure delivered as scalaz (ValidationNel).
     */
    private def qThirst(h: AMQPResponse) = {
      val result = h.toJson(true) // the response is parsed back
      val res: ValidationNel[Throwable, Option[String]] = result match {
        case respJSON => respJSON.some.successNel
        case _ => new java.lang.Error("Error occurred in the subscribed response. Unsupported response type").failureNel
      }
      res
    }

    def pubBADURL = {
      lazy val bad_uris = "localhost:5672/vhost,amqp://rabbitmq1.megam.co.in,amqp://ec2-54-251-68-164.ap-southeast-1.compute.amazonaws.com:5672/vhost"
      lazy val badClient = new RabbitMQClient(bad_uris, exchange_name, queue_name)
      executeP(badClient)
    }

    def pubCONNDOWN = {
      lazy val badconn_uris = "amqp://localhost:5673/vhost,amqp://rabbitmq1.megam.co.in:5673/vhost,amqp://ec2-54-251-68-164.ap-southeast-1.compute.amazonaws.com:5672/vhost"
      lazy val conndownClient = new RabbitMQClient(badconn_uris, exchange_name, queue_name)
      executeP(conndownClient)
    }

    def pub = {
      lazy val good_uris = "amqp://localhost:5672/vhost,amqp://rabbitmq1.megam.co.in:5672/vhost"
      lazy val goodClient = new RabbitMQClient(good_uris, exchange_name, queue_name)
      executeP(goodClient)
    }

    def subBADURL = {
      lazy val bad_uris = "localhost:5672/vhost,amqp://rabbitmq1.megam.co.in,amqp://ec2-54-251-68-164.ap-southeast-1.compute.amazonaws.com:5672/vhost"
      lazy val badClient = new RabbitMQClient(bad_uris, exchange_name, queue_name)
      executeS(badClient)
    }
    def subCONNDOWN = {
      lazy val badconn_uris = "amqp://localhost:5673/vhost,amqp://rabbitmq1.megam.co.in:5673/vhost,amqp://ec2-54-251-68-164.ap-southeast-1.compute.amazonaws.com:5672/vhost"
      lazy val conndownClient = new RabbitMQClient(badconn_uris, exchange_name, queue_name)
      executeS(conndownClient)
    }
    def sub = {
      lazy val good_uris = "amqp://localhost:5672/vhost,amqp://rabbitmq1.megam.co.in:5672/vhost,amqp://ec2-54-251-68-164.ap-southeast-1.compute.amazonaws.com:5672/vhost"
      lazy val goodClient = new RabbitMQClient(good_uris, exchange_name, queue_name)
      executeS(goodClient)
    }

  }

  object RMQClientTests {
    def apply = new RMQClientTests()
  }

}