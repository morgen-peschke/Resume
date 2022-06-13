package mpeschke.handlebars

import com.github.jknack.handlebars.ValueResolver
import com.github.jknack.handlebars.context.MapValueResolver
import io.circe.Json

import scala.collection.JavaConverters._
import java.util.{Collections => JCollections, Map => JMap, Set => JSet}

/**
 * Shamelessly adapted from https://github.com/softprops/fixie-grips
 */
object CirceResolver extends ValueResolver {
  private[this] val Digit = """(\d+)""".r

  def resolve(ctx: Object, name: String): Object =
    (ctx, name) match {
      case (map: Map[_,_], name) => MapValueResolver.INSTANCE.resolve(map.asJava, name)
      case (xs: Json, key) =>
        xs.arrayOrObject(
          ValueResolver.UNRESOLVED,
          array => {
            key match {
              case Digit(i) =>
                val index = i.toInt
                if (index < array.length) resolveJson(array(index))
                else ValueResolver.UNRESOLVED
              case _ => ValueResolver.UNRESOLVED
            }
          },
          jsonObject => jsonObject(key).fold(ValueResolver.UNRESOLVED)(resolveJson)
        )
      case _ =>
        ValueResolver.UNRESOLVED
    }

  def resolve(ctx: Object): Object =
    ctx match {
      case map: Map[_,_] => MapValueResolver.INSTANCE.resolve(map.asJava)
      case jv: Json => resolveJson(jv)
      case _ => ValueResolver.UNRESOLVED
    }

  private def resolveJson(ctx: Json): Object =
    ctx.fold(
      jsonNull = null,
      jsonBoolean = java.lang.Boolean.valueOf,
      jsonNumber = n => java.lang.Double.valueOf(n.toDouble),
      jsonString = identity,
      jsonArray = vec => vec.map(resolveJson).asJava,
      jsonObject = obj => obj.toMap.map {
        case (key, value) => key -> resolveJson(value)
      }.asJava
    )

  def propertySet(ctx: Object): JSet[JMap.Entry[String, Object]] = {
    val empty = JCollections.emptySet[JMap.Entry[String, Object]]
    ctx match {
      case map: Map[_,_] => MapValueResolver.INSTANCE.propertySet(map.asJava)
      case obj: Json => MapValueResolver.INSTANCE.propertySet {
        obj.asObject
          .fold(Map.empty[String, Object])(_.toMap.map{
            case (key, value) => key -> resolveJson(value)
          })
          .asJava
          .entrySet()
      }
      case _ => empty
    }
  }
}