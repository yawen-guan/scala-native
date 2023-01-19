package scala.scalanative.nscplugin

import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools._
import dotc._
import dotc.ast.tpd._
import dotc.cc.{EventuallyCapturingType, CaptureSet}
import dotc.transform.SymUtils.setter
import dotc.util.Property
import core.Contexts._
import core.Definitions
import core.Names._
import core.Symbols._
import core.Types._
import core.StdNames._
import core.Constants.Constant

/** This phase runs before erasure phase and preserves annotation infos for
 *  GenNIR phase: let C T' be the result type of `new T(args)`, the result type
 *  of `new {sz} T(args)` is `C ∪ {sz} (T' @SafeZoneHandle(sz.handle))`. This
 *  phase will preserve the `sz.handle` expression to the Apply tree.
 */
object Preserve {
  val name = "scalanative-preserve"

  val SafeZoneHandleKey: Property.StickyKey[Tree] = Property.StickyKey[Tree]
}

class Preserve extends PluginPhase {
  override val runsAfter = Set(transform.ArrayConstructors.name)
  override val runsBefore = Set(transform.Erasure.name)
  val phaseName = Preserve.name
  override def description: String = "preserve info for GenNIR phase"

  def defn(using Context): Definitions = ctx.definitions

  override def transformApply(tree: Apply)(using Context): Tree = {
    val tpe = tree match {
      case Apply(Select(New(tpt), name), _) if name == nme.CONSTRUCTOR =>
        tpt.tpe
      case Apply(fun, _) if fun.symbol == defn.newArrayMethod =>
        tree.tpe
      case _ => NoType
    }
    tpe match {
      case ty @ EventuallyCapturingType(_, _) =>
        extractSafeZoneHandle(ty) match {
          case Some(handle) =>
            tree.putAttachment(Preserve.SafeZoneHandleKey, handle)
          case None =>
        }
      case _ =>
    }
    tree
  }

  def extractSafeZoneHandle(tpe: AnnotatedType)(using Context): Option[Tree] = {
    val AnnotatedType(parent, annot) = tpe
    annot.tree match {
      case Apply(_, List(handle))
          if annot.symbol == defn.NativeSafeZoneHandleAnnot.clssym =>
        Some(handle)
      case _ =>
        parent match {
          case ty: AnnotatedType => extractSafeZoneHandle(ty)
          case _                 => None
        }
    }
  }
}
