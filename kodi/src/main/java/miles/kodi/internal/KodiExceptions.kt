package miles.kodi.internal

import miles.kodi.api.Scope

/**
 * Created by peelemil on 10/11/17.
 */
internal class NoMatchingScopeException(scope: Scope) : IllegalArgumentException("No matching scope $scope exists.")

internal class NoMatchingKeyException(key: String) : IllegalArgumentException("No matching key $key exists.")

internal class CyclicalNodeAdditionException(node: Node) : UnsupportedOperationException("Cannot cyclically add $node to itself.")

internal class RemovingNonChildNodeException(child: Node, parent: Node) : UnsupportedOperationException("Node $child is not a child of $parent")

internal class AmbiguousBindingException : IllegalStateException("Module cannot contain build of the same class without specifying a Tag.")

internal class DuplicateBindingException : IllegalStateException("Module cannot contain two build with the same Key.")

internal class InjectNotCalledException : RuntimeException("Call inject(Kodi) before accessing.")