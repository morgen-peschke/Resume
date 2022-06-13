package mpeschke

import cats.syntax.option._

package object resume {
  object Language extends StringWrapper
  type Language = Language.Type

  object LanguageList extends ListWrapper[Language](30.some)
  type LanguageList = LanguageList.Type

  object BulletPoint extends StringWrapper
  type BulletPoint = BulletPoint.Type

  object Title extends StringWrapper
  type Title = Title.Type
}
