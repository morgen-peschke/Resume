package mpeschke.handlebars

import cats.syntax.eq._
import com.github.jknack.handlebars.{Context, Options}
import com.github.jknack.handlebars.helper.{EachHelper, IfHelper}
import io.circe.Json

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.mutable

/**
 * Shamelessly adapted from https://github.com/softprops/fixie-grips
 */
trait CirceHelpers {
  def each(obj: Object, options: Options): CharSequence = {
    def punt: CharSequence = EachHelper.INSTANCE(obj, options) match {
      case cs: CharSequence => cs
      case output => throw new RuntimeException(s"Fallback to EachHelper failed, returned: $output")
    }
    obj match {
      case it: Iterable[_] => eachIterable(it, options)
      case json: Json =>
        json.arrayOrObject(
          punt,
          eachIterable(_, options),
          jObj => eachNamed(jObj.toIterable, options)
        )
      case _ => punt
    }
  }

  /** overriding default `if` helper to support scala falsy things */
  def `if`(obj: Object, options: Options): CharSequence = {
    def simple(b: Boolean): CharSequence = IfHelper.INSTANCE(java.lang.Boolean.valueOf(b), options) match {
      case cs: CharSequence => cs
      case output => throw new RuntimeException(s"Fallback to IfHelper failed, returned: $output")
    }
    def punt(o: Object): CharSequence = IfHelper.INSTANCE(o, options) match {
      case cs: CharSequence => cs
      case output => throw new RuntimeException(s"Fallback to IfHelper failed, returned: $output")
    }
    obj match {
      case it: Iterable[_] => if (it.isEmpty) options.inverse() else options.fn()
      case json: Json =>
        json.fold(
          simple(true),
          simple,
          n => simple(n.toInt.exists(_ === 0)),
          s => punt(s),
          vec => punt(vec),
          obj => punt(obj.toMap.asJava)
        )
      case _ => punt(obj)
    }
  }

  def eachNamed(named: Iterable[(String, _)], options: Options): CharSequence = {
    val sb = new mutable.StringBuilder()
    if (named.isEmpty) sb.append(options.inverse()) else {
      val parent = options.context
      for ((key, value) <- named) {
        val ctx = Context.newBuilder(parent, value)
          .combine("@key", key)
          .build()
        sb.append(options(options.fn, ctx))
        ctx.destroy()
      }
    }
    sb
  }

  protected def eachIterable(
                              it: Iterable[_], options: Options): CharSequence = {
    val sb = new mutable.StringBuilder()
    if (it.isEmpty) sb.append(options.inverse()) else {
      val parent = options.context
      @tailrec
      def append(i: Int, iter: Iterator[_]): Unit =
        if (iter.hasNext) {
          val even = i % 2 == 0
          val ctx = Context.newBuilder(parent, iter.next)
            .combine("@index", i)
            .combine("@first", if (i == 0) "first" else "")
            .combine("@last", if (!iter.hasNext) "last" else "")
            .combine("@odd", if (!even) "odd" else "")
            .combine("@even", if (even) "even" else "")
            .build()
          sb.append(options(options.fn, ctx))
          ctx.destroy()
          append(i + 1, iter)
        }
      append(0, it.iterator)
    }
    sb
  }

  /** shim to avoid https://github.com/jknack/handlebars.java/blob/master/handlebars/src/main/java/com/github/jknack/handlebars/helper/DefaultHelperRegistry.java#L211-L212. this helper isn't really helpful but it works */
  def <^> = "<^>"
}

object CirceHelpers extends CirceHelpers