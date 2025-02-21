package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.ast.JsonPointers
import io.swagger.v3.oas.models.responses.ApiResponse

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "153",
    severity = Severity.MUST,
    title = "Use 429 With Header For Rate Limits"
)
class Use429HeaderForRateLimitRule {

    private val description = "Response has to contain rate limit information via headers"
    private val xRateLimitHeaders = listOf("X-RateLimit-Limit", "X-RateLimit-Remaining", "X-RateLimit-Reset")

    @Check(severity = Severity.MUST)
    fun checkHeadersForRateLimiting(context: Context): List<Violation> =
        context.validateOperations { (_, operation) ->
            operation?.responses?.let { responses ->
                responses
                    .filter {
                        violatingResponse(it)
                    }
                    .flatMap { (status, _) ->
                        context.violations(description,
                            context.getJsonPointer(responses).append(JsonPointers.escape(status)))
                    }
            }.orEmpty()
        }

    private fun violatingResponse(entry: Map.Entry<String, ApiResponse?>): Boolean {
        val headers = entry.value?.headers.orEmpty().keys
        return "429" == entry.key &&
            "Retry-After" !in headers &&
            !headers.containsAll(xRateLimitHeaders)
    }
}
