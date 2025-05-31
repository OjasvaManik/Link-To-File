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
    fun screenshot(
        @RequestParam url: String,
        @RequestParam(defaultValue = "true") fullPage: Boolean,
        @RequestParam(required = false) width: Int?,
        @RequestParam(required = false) height: Int?,
        @RequestParam(defaultValue = "3000") waitForTimeout: Int,
        @RequestParam(defaultValue = "networkidle2") waitUntil: String
    ): Mono<ResponseEntity<ByteArray>> {
        return browserlessClient.screenshot(
            url = url,
            fullPage = fullPage,
            width = width,
            height = height,
            waitForTimeout = waitForTimeout,
            waitUntil = waitUntil
        ).map { imageBytes ->
            ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
                .body(imageBytes)
        }
    }

    @GetMapping("/pdf")
    fun pdf(
        @RequestParam url: String,
        @RequestParam(defaultValue = "false") landscape: Boolean,
        @RequestParam(defaultValue = "false") displayHeaderFooter: Boolean,
        @RequestParam(defaultValue = "true") printBackground: Boolean,
        @RequestParam(defaultValue = "1.0") scale: Double,
        @RequestParam(defaultValue = "3000") waitForTimeout: Int,
        @RequestParam(defaultValue = "networkidle2") waitUntil: String,
        @RequestParam(defaultValue = "true") singlePage: Boolean
    ): Mono<ResponseEntity<ByteArray>> {
        return browserlessClient.pdf(
            url = url,
            landscape = landscape,
            displayHeaderFooter = displayHeaderFooter,
            printBackground = printBackground,
            scale = scale,
            waitForTimeout = waitForTimeout,
            waitUntil = waitUntil,
            singlePage = singlePage
        ).map { pdfBytes ->
            ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .body(pdfBytes)
        }
    }
}