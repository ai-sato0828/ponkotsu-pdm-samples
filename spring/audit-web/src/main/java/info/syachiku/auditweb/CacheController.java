package info.syachiku.auditweb;

import java.time.Duration;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class CacheController {
    @GetMapping("/account/details")
    ResponseEntity<String> accountDetails() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body("テスト用の個人データ");
    }

    @GetMapping("/help")
    ResponseEntity<String> help() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic())
                .body("公開ヘルプ");
    }
}
