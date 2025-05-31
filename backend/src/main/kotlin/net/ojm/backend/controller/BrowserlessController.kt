package net.ojm.backend.controller

import net.ojm.backend.service.BrowserlessClient
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/browserless")
class BrowserlessController(
    private val browserlessClient: BrowserlessClient
) {

    @GetMapping("/screenshot")
    fun screenshot(@RequestParam url: String): Mono<ResponseEntity<ByteArray>> {
        return browserlessClient.screenshot(url)
            .map { imageBytes ->
                ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
                    .body(imageBytes)
            }
    }

    @GetMapping("/pdf")
    fun pdf(@RequestParam url: String): Mono<ResponseEntity<ByteArray>> {
        return browserlessClient.pdf(url)
            .map { pdfBytes ->
                ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .body(pdfBytes)
            }
    }
}