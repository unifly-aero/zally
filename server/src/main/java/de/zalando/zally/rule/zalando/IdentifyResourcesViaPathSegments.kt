package de.zalando.zally.rule.zalando

import com.fasterxml.jackson.core.JsonPointer
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.ast.JsonPointers

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "143",
    severity = Severity.MUST,
    title = "Resources must be identified via path segments"
)
class IdentifyResourcesViaPathSegments {
    private val pathStartsWithParameter = "Path must start with a resource"
    private val pathContainsSuccessiveParameters = "Path must not contain successive parameters"
    private val pathParameterContainsPrefixOrSuffix = "Path parameter must not contain prefixes and suffixes"

    private val pathStartingWithAParameter = """(^/\{[^/]+\}|/)""".toRegex()
    @Check(severity = Severity.MUST)
    fun pathStartsWithResource(context: Context): List<Violation> = context.validatePaths(
        pathFilter = { pathStartingWithAParameter.matches(it.key) },
        action = { context.violations(pathStartsWithParameter, JsonPointer.compile("/paths").append(JsonPointers.escape(it.key))) })

    private val pathContainingSuccessiveParameters = """.*\}/\{.*""".toRegex()
    @Check(severity = Severity.MUST)
    fun pathDoesNotContainSuccessiveParameters(context: Context): List<Violation> = context.validatePaths(
        pathFilter = { pathContainingSuccessiveParameters.matches(it.key) },
        action = { context.violations(pathContainsSuccessiveParameters, JsonPointer.compile("/paths").append(JsonPointers.escape(it.key))) })

    private val pathContainingPrefixedOrSuffixedParameter = """.*/([^/]+\{[^/]+\}|\{[^/]+\}[^/]+).*""".toRegex()
    @Check(severity = Severity.MUST)
    fun pathParameterDoesNotContainPrefixAndSuffix(context: Context): List<Violation> = context.validatePaths(
        pathFilter = { pathContainingPrefixedOrSuffixedParameter.matches(it.key) },
        action = { context.violations(pathParameterContainsPrefixOrSuffix, JsonPointer.compile("/paths").append(JsonPointers.escape(it.key))) })
}
