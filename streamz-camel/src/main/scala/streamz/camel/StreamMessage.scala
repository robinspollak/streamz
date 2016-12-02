/*
 * Copyright 2014 - 2017 the original author or authors.
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

package streamz.camel

import org.apache.camel.impl.DefaultMessage
import org.apache.camel.{ TypeConversionException, Message }

import scala.collection.JavaConverters._
import scala.reflect.ClassTag

/**
 * A message received from or sent to a Camel endpoint.
 *
 * @param body Message body.
 * @param headers Message headers.
 */
case class StreamMessage[A](body: A, headers: Map[String, Any] = Map.empty) {
  /**
   * Returns the `body` converted to type `B` using a Camel type converter.
   *
   * @throws TypeConversionException if type conversion fails.
   */
  def bodyAs[B](implicit streamContext: StreamContext, tag: ClassTag[B]): B =
    streamContext.convertObject(body)

  /**
   * Returns the `name` header value converted to type `B` using a Camel type converter.
   *
   * @throws NoSuchElementException if the `name` header does not exist.
   * @throws TypeConversionException if type conversion fails.
   */
  def headerAs[B](name: String)(implicit streamContext: StreamContext, tag: ClassTag[B]): B =
    headerOptionAs[B](name).get

  /**
   * Returns the `name` header value converted to type `B` using a Camel type converter
   * if the `name` header is defined.
   *
   * @throws TypeConversionException if type conversion fails.
   */
  def headerOptionAs[B](name: String)(implicit streamContext: StreamContext, tag: ClassTag[B]): Option[B] =
    headers.get(name).map(streamContext.convertObject[B])

  private[camel] def camelMessage: Message = {
    val result = new DefaultMessage

    headers.foreach {
      case (k, v) => result.setHeader(k, v)
    }

    result.setBody(body)
    result
  }
}

object StreamMessage {
  /**
   * Creates a [[StreamMessage]] from a Camel [[Message]] converting the message body to type `A`
   * using a Camel type converter.
   *
   * @throws TypeConversionException if type conversion fails.
   */
  private[camel] def from[A](camelMessage: Message)(implicit tag: ClassTag[A]): StreamMessage[A] =
    new StreamMessage(camelMessage.getBody(tag.runtimeClass.asInstanceOf[Class[A]]), camelMessage.getHeaders.asScala.toMap)
}