package dev.outfix.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "outfit_items")
@Getter
@Setter
@NoArgsConstructor
public class OutfitItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recommendation_id")
    private OutfitRecommendation recommendation;

    @ManyToOne
    @JoinColumn(name = "clothing_item_id")
    private ClothingItem clothingItem;

}