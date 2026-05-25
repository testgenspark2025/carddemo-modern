package com.carddemo.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Forward all SPA routes that are not actual files and not /api/** to index.html
 * so the React Router can resolve them client-side.
 */
@Controller
public class SpaController {

    @GetMapping(value = {"/", "/login", "/accounts/**"})
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
