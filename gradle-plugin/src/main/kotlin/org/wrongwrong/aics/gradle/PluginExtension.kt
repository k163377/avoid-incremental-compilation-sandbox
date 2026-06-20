package org.wrongwrong.aics.gradle

/**
 * Configuration for the aics gradle plugin.
 *
 * @property generateTargetFunction when true (default, "improved" approach), the plugin generates
 * the `targetCall` target function as an `internal inline` function with a random marker on every build,
 * forcing all call sites to be recompiled and thus avoiding the incremental compilation skip issue.
 * When false ("legacy" approach), no source is generated and the user is expected to depend on a
 * `targetCall` function provided by `runtime-api`.
 */
open class PluginExtension {
    var generateTargetFunction: Boolean = true
}
