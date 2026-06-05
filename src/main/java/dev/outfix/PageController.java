package dev.outfix;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() { return "index"; }

    @GetMapping("/clothing")
    public String clothing() { return "clothing"; }

    @GetMapping("/wardrobe")
    public String wardrobe() { return "wardrobe"; }

    @GetMapping("/generate")
    public String generate() { return "generate"; }

    @GetMapping("/generate/new")
    public String generateNew() { return "generate-new"; }

    @GetMapping("/schedule")
    public String schedule() { return "schedule"; }

    @GetMapping("/collection")
    public String collection() { return "collection"; }

    @GetMapping("/account")
    public String account() { return "account"; }
}
