package dev.outfix.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.outfix.clothing.entity.Clothing;
import dev.outfix.clothing.repository.ClothingRepository;
import dev.outfix.schedule.entity.Schedule;
import dev.outfix.schedule.repository.ScheduleRepository;
import dev.outfix.tag.Tag;
import dev.outfix.tag.TagRepository;
import dev.outfix.user.entity.User;
import dev.outfix.user.repository.UserRepository;

@Configuration
public class DataInitializer {

    private static String img(String filename) {
        return "/uploads/clothing/demo/" + filename;
    }

    @Bean
    ApplicationRunner seedDatabase(
            TagRepository tagRepo,
            UserRepository userRepo,
            ClothingRepository clothingRepo,
            ScheduleRepository scheduleRepo) {

        return args -> {
            seedTags(tagRepo);
            if (userRepo.count() == 0) {
                User alice = seedUser(userRepo, "Alice", "christest100@gmail.com", "1234");
                seedClothing(clothingRepo, alice);
                seedSchedules(scheduleRepo, alice);
            }
            // Incremental patches — idempotent, run on every startup
            userRepo.findByEmail("christest100@gmail.com").ifPresent(alice -> {
                patchMenBottoms(clothingRepo, alice);
                patchWomenClothing(clothingRepo, alice);
            });
        };
    }

    private void seedTags(TagRepository tagRepo) {
        if (tagRepo.count() > 0) return;
        List.of("Men", "Women", "Unisex",
                "Top", "Bottom", "Outerwear",
                "Footwear", "Accessories",
                "Formal", "Casual", "Sportswear")
            .forEach(name -> tagRepo.save(Tag.builder().name(name).build()));
    }

    private User seedUser(UserRepository userRepo,
            String username, String email, String password) {
        return userRepo.save(User.builder()
                .username(username)
                .email(email)
                .password(password)
                .role("USER")
                .finishedTutorial(false)
                .createdAt(LocalDateTime.now())
                .build());
    }

    private void seedClothing(ClothingRepository clothingRepo, User alice) {
        LocalDateTime now = LocalDateTime.now();
        clothingRepo.saveAll(List.of(

            // ── Men's tops ────────────────────────────────────────────────────

            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-formal-shirt-1.jpg"))
                    .tokenFormalitas(3).tags("Men,Top,Formal").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-formal-shirt-2.jpg"))
                    .tokenFormalitas(3).tags("Men,Top,Formal").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-formal-blazer.jpg"))
                    .tokenFormalitas(3).tags("Men,Outerwear,Formal").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-casual-top.jpg"))
                    .tokenFormalitas(1).tags("Men,Top,Casual").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("unisex-casual-outerwear.jpg"))
                    .tokenFormalitas(1).tags("Unisex,Outerwear,Casual").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-sportswear-top.jpg"))
                    .tokenFormalitas(1).tags("Men,Top,Sportswear").createdAt(now).build(),

            // ── Men's bottoms ─────────────────────────────────────────────────

            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-formal-pants.jpg"))
                    .tokenFormalitas(3).tags("Men,Bottom,Formal").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-casual-pants.jpg"))
                    .tokenFormalitas(1).tags("Men,Bottom,Casual").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-jeans.jpg"))
                    .tokenFormalitas(1).tags("Men,Bottom,Casual").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-chinos.jpg"))
                    .tokenFormalitas(2).tags("Men,Bottom,Casual").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-casual-shorts.jpg"))
                    .tokenFormalitas(1).tags("Men,Bottom,Casual").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-sport-shorts.jpg"))
                    .tokenFormalitas(1).tags("Men,Bottom,Sportswear").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-sport-pants.jpg"))
                    .tokenFormalitas(1).tags("Men,Bottom,Sportswear").createdAt(now).build(),

            // ── Men's footwear ────────────────────────────────────────────────

            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-formal-shoes-1.jpg"))
                    .tokenFormalitas(3).tags("Men,Footwear,Formal").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-formal-shoes-2.jpg"))
                    .tokenFormalitas(3).tags("Men,Footwear,Formal").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("unisex-casual-sneakers.jpg"))
                    .tokenFormalitas(1).tags("Unisex,Footwear,Casual").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-casual-shoes.jpg"))
                    .tokenFormalitas(1).tags("Men,Footwear,Casual").createdAt(now).build(),

            // ── Women's tops ──────────────────────────────────────────────────

            Clothing.builder().user(alice)
                    .clothingImageUrl(img("women-formal-blouse.jpg"))
                    .tokenFormalitas(3).tags("Women,Top,Formal").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("women-casual-top-1.jpg"))
                    .tokenFormalitas(2).tags("Women,Top,Casual").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("women-casual-top-2.jpg"))
                    .tokenFormalitas(1).tags("Women,Top,Casual").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("women-sport-top.jpg"))
                    .tokenFormalitas(1).tags("Women,Top,Sportswear").createdAt(now).build(),

            // ── Women's bottoms ───────────────────────────────────────────────

            Clothing.builder().user(alice)
                    .clothingImageUrl(img("women-formal-pants.jpg"))
                    .tokenFormalitas(3).tags("Women,Bottom,Formal").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("women-casual-pants.jpg"))
                    .tokenFormalitas(1).tags("Women,Bottom,Casual").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("women-jeans.jpg"))
                    .tokenFormalitas(1).tags("Women,Bottom,Casual").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("women-chinos.jpg"))
                    .tokenFormalitas(2).tags("Women,Bottom,Casual").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("women-casual-shorts.jpg"))
                    .tokenFormalitas(1).tags("Women,Bottom,Casual").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("women-sport-shorts.jpg"))
                    .tokenFormalitas(1).tags("Women,Bottom,Sportswear").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("women-leggings.jpg"))
                    .tokenFormalitas(1).tags("Women,Bottom,Sportswear").createdAt(now).build(),

            // ── Women's footwear ──────────────────────────────────────────────

            Clothing.builder().user(alice)
                    .clothingImageUrl(img("women-sport-shoes.jpg"))
                    .tokenFormalitas(1).tags("Women,Footwear,Sportswear").createdAt(now).build()
        ));
    }

    /**
     * Adds new men's bottom items for Alice if they aren't already in the database.
     * Safe to run on every startup — skips any item whose image URL already exists.
     */
    private void patchMenBottoms(ClothingRepository clothingRepo, User alice) {
        LocalDateTime now = LocalDateTime.now();
        List.of(
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-formal-pants.jpg"))
                    .tokenFormalitas(3).tags("Men,Bottom,Formal").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-casual-pants.jpg"))
                    .tokenFormalitas(1).tags("Men,Bottom,Casual").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-jeans.jpg"))
                    .tokenFormalitas(1).tags("Men,Bottom,Casual").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-chinos.jpg"))
                    .tokenFormalitas(2).tags("Men,Bottom,Casual").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-casual-shorts.jpg"))
                    .tokenFormalitas(1).tags("Men,Bottom,Casual").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-sport-shorts.jpg"))
                    .tokenFormalitas(1).tags("Men,Bottom,Sportswear").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-sport-pants.jpg"))
                    .tokenFormalitas(1).tags("Men,Bottom,Sportswear").createdAt(now).build()
        ).forEach(item -> saveIfAbsent(clothingRepo, alice, item));
    }

    /**
     * Adds all women's clothing for Alice if not already present.
     * Covers items formerly seeded for Bob plus new expanded bottoms.
     */
    private void patchWomenClothing(ClothingRepository clothingRepo, User alice) {
        LocalDateTime now = LocalDateTime.now();
        List.of(
            // Tops
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("women-formal-blouse.jpg"))
                    .tokenFormalitas(3).tags("Women,Top,Formal").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("women-casual-top-1.jpg"))
                    .tokenFormalitas(2).tags("Women,Top,Casual").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("women-casual-top-2.jpg"))
                    .tokenFormalitas(1).tags("Women,Top,Casual").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("women-sport-top.jpg"))
                    .tokenFormalitas(1).tags("Women,Top,Sportswear").createdAt(now).build(),
            // Bottoms
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("women-formal-pants.jpg"))
                    .tokenFormalitas(3).tags("Women,Bottom,Formal").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("women-casual-pants.jpg"))
                    .tokenFormalitas(1).tags("Women,Bottom,Casual").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("women-jeans.jpg"))
                    .tokenFormalitas(1).tags("Women,Bottom,Casual").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("women-chinos.jpg"))
                    .tokenFormalitas(2).tags("Women,Bottom,Casual").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("women-casual-shorts.jpg"))
                    .tokenFormalitas(1).tags("Women,Bottom,Casual").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("women-sport-shorts.jpg"))
                    .tokenFormalitas(1).tags("Women,Bottom,Sportswear").createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("women-leggings.jpg"))
                    .tokenFormalitas(1).tags("Women,Bottom,Sportswear").createdAt(now).build(),
            // Footwear
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("women-sport-shoes.jpg"))
                    .tokenFormalitas(1).tags("Women,Footwear,Sportswear").createdAt(now).build()
        ).forEach(item -> saveIfAbsent(clothingRepo, alice, item));
    }

    private void saveIfAbsent(ClothingRepository clothingRepo, User alice, Clothing item) {
        boolean exists = clothingRepo.findByUser(alice).stream()
                .anyMatch(c -> c.getClothingImageUrl().equals(item.getClothingImageUrl()));
        if (!exists) clothingRepo.save(item);
    }

    private void seedSchedules(ScheduleRepository scheduleRepo, User alice) {
        LocalDate today = LocalDate.now();
        scheduleRepo.saveAll(List.of(
            Schedule.builder().user(alice).activityName("Job Interview")
                    .eventDate(today.plusDays(2))
                    .targetToken(3).targetTag("Formal").build(),
            Schedule.builder().user(alice).activityName("Gym Session")
                    .eventDate(today.plusDays(1))
                    .targetToken(1).targetTag("Sportswear").build(),
            Schedule.builder().user(alice).activityName("Casual Friday")
                    .eventDate(today.plusDays(4))
                    .targetToken(1).targetTag("Casual").build(),
            Schedule.builder().user(alice).activityName("Client Dinner")
                    .eventDate(today.plusDays(6))
                    .targetToken(3).targetTag("Formal").dresscode("Batik").build()
        ));
    }
}
