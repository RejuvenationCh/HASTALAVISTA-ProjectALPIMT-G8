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
                User bob   = seedUser(userRepo, "Bob",   "bob@demo.com",           "1234");
                seedClothing(clothingRepo, alice, bob);
                seedSchedules(scheduleRepo, alice, bob);
            }
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

    private void seedClothing(ClothingRepository clothingRepo, User alice, User bob) {
        LocalDateTime now = LocalDateTime.now();
        clothingRepo.saveAll(List.of(

            // ── Alice (Men's) ──────────────────────────────────────────────────

            // Formal tops
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-formal-shirt-1.jpg"))
                    .tokenFormalitas(3).tags("Men,Top,Formal")
                    .createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-formal-shirt-2.jpg"))
                    .tokenFormalitas(3).tags("Men,Top,Formal")
                    .createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-formal-blazer.jpg"))
                    .tokenFormalitas(3).tags("Men,Outerwear,Formal")
                    .createdAt(now).build(),

            // Formal footwear
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-formal-shoes-1.jpg"))
                    .tokenFormalitas(3).tags("Men,Footwear,Formal")
                    .createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-formal-shoes-2.jpg"))
                    .tokenFormalitas(3).tags("Men,Footwear,Formal")
                    .createdAt(now).build(),

            // Casual tops & outerwear
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-casual-top.jpg"))
                    .tokenFormalitas(1).tags("Men,Top,Casual")
                    .createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("unisex-casual-outerwear.jpg"))
                    .tokenFormalitas(1).tags("Unisex,Outerwear,Casual")
                    .createdAt(now).build(),

            // Casual footwear
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("unisex-casual-sneakers.jpg"))
                    .tokenFormalitas(1).tags("Unisex,Footwear,Casual")
                    .createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-casual-shoes.jpg"))
                    .tokenFormalitas(1).tags("Men,Footwear,Casual")
                    .createdAt(now).build(),

            // Sportswear
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-sportswear-top.jpg"))
                    .tokenFormalitas(1).tags("Men,Top,Sportswear")
                    .createdAt(now).build(),

            // Bottoms (pants)
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-formal-pants.jpg"))
                    .tokenFormalitas(3).tags("Men,Bottom,Formal")
                    .createdAt(now).build(),
            Clothing.builder().user(alice)
                    .clothingImageUrl(img("men-casual-pants.jpg"))
                    .tokenFormalitas(1).tags("Men,Bottom,Casual")
                    .createdAt(now).build(),

            // ── Bob (Women's) ──────────────────────────────────────────────────

            // Formal
            Clothing.builder().user(bob)
                    .clothingImageUrl(img("women-formal-blouse.jpg"))
                    .tokenFormalitas(3).tags("Women,Top,Formal")
                    .createdAt(now).build(),

            // Smart casual
            Clothing.builder().user(bob)
                    .clothingImageUrl(img("women-casual-top-1.jpg"))
                    .tokenFormalitas(2).tags("Women,Top,Casual")
                    .createdAt(now).build(),

            // Casual
            Clothing.builder().user(bob)
                    .clothingImageUrl(img("women-casual-top-2.jpg"))
                    .tokenFormalitas(1).tags("Women,Top,Casual")
                    .createdAt(now).build(),

            // Sportswear
            Clothing.builder().user(bob)
                    .clothingImageUrl(img("women-sport-top.jpg"))
                    .tokenFormalitas(1).tags("Women,Top,Sportswear")
                    .createdAt(now).build(),
            Clothing.builder().user(bob)
                    .clothingImageUrl(img("women-sport-shoes.jpg"))
                    .tokenFormalitas(1).tags("Women,Footwear,Sportswear")
                    .createdAt(now).build(),

            // Bottoms (pants)
            Clothing.builder().user(bob)
                    .clothingImageUrl(img("women-formal-pants.jpg"))
                    .tokenFormalitas(3).tags("Women,Bottom,Formal")
                    .createdAt(now).build(),
            Clothing.builder().user(bob)
                    .clothingImageUrl(img("women-casual-pants.jpg"))
                    .tokenFormalitas(1).tags("Women,Bottom,Casual")
                    .createdAt(now).build()
        ));
    }

    private void seedSchedules(ScheduleRepository scheduleRepo, User alice, User bob) {
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
                    .targetToken(3).targetTag("Formal").dresscode("Batik").build(),
            Schedule.builder().user(bob).activityName("Business Meeting")
                    .eventDate(today.plusDays(3))
                    .targetToken(3).targetTag("Formal").build(),
            Schedule.builder().user(bob).activityName("Weekend Brunch")
                    .eventDate(today.plusDays(5))
                    .targetToken(1).targetTag("Casual").build(),
            Schedule.builder().user(bob).activityName("Morning Run")
                    .eventDate(today.plusDays(1))
                    .targetToken(1).targetTag("Sportswear").build()
        ));
    }
}
