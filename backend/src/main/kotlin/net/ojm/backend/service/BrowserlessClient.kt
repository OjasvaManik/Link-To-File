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
        .build()

    private val logger = LoggerFactory.getLogger(BrowserlessClient::class.java)

    fun screenshot(
        url: String,
        fullPage: Boolean = true,
        width: Int? = null,
        height: Int? = null
    ): Mono<ByteArray> {
        val options = mutableMapOf<String, Any>("fullPage" to fullPage)
        width?.let { options["width"] = it }
        height?.let { options["height"] = it }

        val body = mapOf(
            "url" to url,
            "options" to options
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
        scale: Double = 1.0
    ): Mono<ByteArray> {
        val body = mapOf(
            "url" to url,
            "options" to mapOf(
                "landscape" to landscape,
                "displayHeaderFooter" to displayHeaderFooter,
                "printBackground" to printBackground,
                "scale" to scale
            )
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