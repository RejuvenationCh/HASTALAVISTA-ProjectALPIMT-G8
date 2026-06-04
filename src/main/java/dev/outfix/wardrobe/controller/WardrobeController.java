package dev.outfix.wardrobe.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import dev.outfix.user.entity.User;
import dev.outfix.user.repository.UserRepository;
import dev.outfix.wardrobe.dto.CreateWardrobeRequestDto;
import dev.outfix.wardrobe.service.WardrobeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/wardrobes")
public class WardrobeController {

    private final WardrobeService wardrobeService;
    private final UserRepository userRepository;

    @GetMapping
    public String wardrobeList(
            Authentication authentication,
            Model model) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow();

        model.addAttribute(
                "wardrobes",
                wardrobeService.getUserWardrobes(user));

        return "wardrobe/list";
    }

    @GetMapping("/create")
    public String createPage() {
        return "wardrobe/create";
    }

    @PostMapping("/create")
    public String createWardrobe(
            @Valid CreateWardrobeRequestDto request,
            Authentication authentication) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow();

        wardrobeService.createWardrobe(
                user,
                request.getClothingImageUrl(),
                request.getTokenFormalitas(),
                request.getTags());

        return "redirect:/wardrobes";
    }

    @PostMapping("/delete/{id}")
    public String deleteWardrobe(@PathVariable Long id) {
        wardrobeService.deleteWardrobe(id);
        return "redirect:/wardrobes";
    }
}
