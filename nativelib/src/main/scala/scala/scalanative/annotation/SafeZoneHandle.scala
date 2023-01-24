package scala.scalanative
package annotation

import scala.scalanative.runtime.RawPtr

/** An annotation that is used to indicate the handle of a SafeZone.
 */
final class SafeZoneHandle(handle: RawPtr)
    extends scala.annotation.Annotation {}
