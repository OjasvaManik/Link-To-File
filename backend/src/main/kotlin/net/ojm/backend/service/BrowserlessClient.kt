package net.ojm.backend.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Service
class BrowserlessClient(
    @Value("\${browserless.token}") private val token: String
) {

    private val webClient: WebClient = WebClient.builder()
        .baseUrl("https://chrome.browserless.io")
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .codecs { configurer ->
            configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024) // 10MB limit
        }
        .build()

    private val logger = LoggerFactory.getLogger(BrowserlessClient::class.java)

    fun screenshot(
        url: String,
        fullPage: Boolean = true,
        width: Int? = null,
        height: Int? = null,
        waitForTimeout: Int = 3000,
        waitUntil: String = "networkidle2"
    ): Mono<ByteArray> {
        val gotoOptions = mutableMapOf<String, Any>(
            "waitUntil" to waitUntil,
            "timeout" to waitForTimeout  // Move waitForTimeout inside gotoOptions as "timeout"
        )

        val options = mutableMapOf<String, Any>(
            "fullPage" to fullPage
        )

        width?.let { options["width"] = it }
        height?.let { options["height"] = it }

        val body = mapOf(
            "url" to url,
            "options" to options,
            "gotoOptions" to gotoOptions
            // Remove waitForTimeout from top level
        )

        return webClient.post()
            .uri("/screenshot?token=$token")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(ByteArray::class.java)
            .doOnError { ex ->
                if (ex is WebClientResponseException) {
                    logger.error("Failed to get screenshot from $url: ${ex.responseBodyAsString}", ex)
                } else {
                    logger.error("Unexpected error during screenshot request", ex)
                }
            }
    }

    fun pdf(
        url: String,
        landscape: Boolean = false,
        displayHeaderFooter: Boolean = false,
        printBackground: Boolean = true,
        scale: Double = 1.0,
        waitForTimeout: Int = 3000,
        waitUntil: String = "networkidle2",
        singlePage: Boolean = true
    ): Mono<ByteArray> {
        val gotoOptions = mutableMapOf<String, Any>(
            "waitUntil" to waitUntil,
            "timeout" to waitForTimeout  // Move waitForTimeout inside gotoOptions as "timeout"
        )

        val options = if (singlePage) {
            mapOf(
                "landscape" to landscape,
                "displayHeaderFooter" to displayHeaderFooter,
                "printBackground" to printBackground,
                "scale" to scale,
                "format" to "A4",
                "margin" to mapOf(
                    "top" to "0.1in",
                    "bottom" to "0.1in",
                    "left" to "0.1in",
                    "right" to "0.1in"
                ),
                "preferCSSPageSize" to false,
                "width" to "8.27in",
                "height" to "50in"
            )
        } else {
            mapOf(
                "landscape" to landscape,
                "displayHeaderFooter" to displayHeaderFooter,
                "printBackground" to printBackground,
                "scale" to scale,
                "format" to "A4",
                "margin" to mapOf(
                    "top" to "0.4in",
                    "bottom" to "0.4in",
                    "left" to "0.4in",
                    "right" to "0.4in"
                ),
                "preferCSSPageSize" to true
            )
        }

        val body = mapOf(
            "url" to url,
            "options" to options,
            "gotoOptions" to gotoOptions
            // Remove waitForTimeout from top level
        )

        return webClient.post()
            .uri("/pdf?token=$token")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(ByteArray::class.java)
            .doOnError { ex ->
                if (ex is WebClientResponseException) {
                    logger.error("Failed to get PDF from $url: ${ex.responseBodyAsString}", ex)
                } else {
                    logger.error("Unexpected error during PDF request", ex)
                }
            }
    }
}