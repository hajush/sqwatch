package com.sshift.sqwatch

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
final class SQWatchController {
    @GetMapping(value = ["/", "/since", "/fixed", "/teams", "/choose-teams", "/browse/**"])
    String index() {
        "index"
    }
}
