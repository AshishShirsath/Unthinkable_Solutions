package com.ashish.rentflatmatefinder.service;

import com.ashish.rentflatmatefinder.config.OpenAiConfig;
import com.ashish.rentflatmatefinder.dto.response.CompatibilityResponse;
import com.ashish.rentflatmatefinder.entity.*;
import com.ashish.rentflatmatefinder.exception.ResourceNotFoundException;
import com.ashish.rentflatmatefinder.repository.CompatibilityScoreRepository;
import com.ashish.rentflatmatefinder.repository.ListingRepository;
import com.ashish.rentflatmatefinder.repository.TenantProfileRepository;
import com.ashish.rentflatmatefinder.repository.UserRepository;
import com.ashish.rentflatmatefinder.util.SecurityUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompatibilityService {

    private final CompatibilityScoreRepository compatibilityScoreRepository;
    private final ListingRepository listingRepository;
    private final TenantProfileRepository tenantProfileRepository;
    private final UserRepository userRepository;
    private final OpenAiConfig openAiConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public CompatibilityResponse getCompatibility(Long listingId) {
        String email = SecurityUtils.getCurrentUserEmail();
        User tenant = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        // Return cached score if already computed
        return compatibilityScoreRepository.findByTenantAndListing(tenant, listing)
                .map(score -> CompatibilityResponse.builder()
                        .listingId(listingId)
                        .score(score.getScore())
                        .explanation(score.getExplanation())
                        .llmUsed(score.getLlmUsed())
                        .build())
                .orElseGet(() -> computeAndStore(tenant, listing));
    }

    @Transactional
    public CompatibilityResponse computeAndStore(User tenant, Listing listing) {
        TenantProfile profile = tenantProfileRepository.findByUser(tenant)
                .orElse(null);

        int score;
        String explanation;
        boolean llmUsed = false;

        if (profile != null && isLlmConfigured()) {
            try {
                Map<String, Object> result = callLlm(profile, listing);
                score = (int) result.get("score");
                explanation = (String) result.get("explanation");
                llmUsed = true;
            } catch (Exception e) {
                log.warn("LLM call failed, falling back to rule-based scoring: {}", e.getMessage());
                Map<String, Object> result = ruleBasedScore(profile, listing);
                score = (int) result.get("score");
                explanation = (String) result.get("explanation");
            }
        } else {
            if (profile == null) {
                score = 50;
                explanation = "Complete your tenant profile to get a personalized compatibility score.";
            } else {
                Map<String, Object> result = ruleBasedScore(profile, listing);
                score = (int) result.get("score");
                explanation = (String) result.get("explanation");
            }
        }

        // Unique constraint exists on (tenant_id, listing_id), so keep this operation idempotent.
        CompatibilityScore entity = compatibilityScoreRepository.findByTenantAndListing(tenant, listing)
                .orElseGet(() -> CompatibilityScore.builder()
                        .tenant(tenant)
                        .listing(listing)
                        .build());

        // CompatibilityScore does not extend BaseEntity, so no deleted flag is available here.
        // Just update score fields and save.
        entity.setDeleted(false);
        entity.setScore(score);
        entity.setExplanation(explanation);
        entity.setLlmUsed(llmUsed);

        compatibilityScoreRepository.save(entity);

        return CompatibilityResponse.builder()
                .listingId(listing.getId())
                .score(score)
                .explanation(explanation)
                .llmUsed(llmUsed)
                .build();
    }

    private boolean isLlmConfigured() {
        String key = openAiConfig.getApiKey();
        return key != null && !key.isBlank() && !key.equals("your-openai-api-key-here");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callLlm(TenantProfile profile, Listing listing) throws Exception {
        String template = openAiConfig.getPrompt();
        if (template == null || template.isBlank()) {
            template = "You are a helpful room compatibility scoring assistant. Respond ONLY with valid JSON and nothing else. Given this room listing: {title: \"{title}\", city: \"{city}\", locality: \"{locality}\", rent: {rent}, roomType: \"{roomType}\", furnishing: \"{furnishing}\"} And this tenant profile: {preferredCity: \"{preferredCity}\", preferredLocality: \"{preferredLocality}\", minBudget: {minBudget}, maxBudget: {maxBudget}, description: \"{description}\"} Compute a compatibility score from 0 to 100 based on budget and location match and return JSON exactly like: {\"score\": <number>, \"explanation\": \"<string>\"}";
        }

        String prompt = template
            .replace("{title}", safe(listing.getTitle()))
            .replace("{city}", safe(listing.getCity()))
            .replace("{locality}", safe(listing.getLocality()))
            .replace("{rent}", listing.getRent() != null ? listing.getRent().toString() : "0")
            .replace("{roomType}", safe(listing.getRoomType()))
            .replace("{furnishing}", safe(listing.getFurnishingStatus()))
            .replace("{preferredCity}", profile.getPreferredCity() != null ? profile.getPreferredCity() : "any")
            .replace("{preferredLocality}", profile.getPreferredLocality() != null ? profile.getPreferredLocality() : "any")
            .replace("{minBudget}", profile.getMinBudget() != null ? profile.getMinBudget().toString() : "0")
            .replace("{maxBudget}", profile.getMaxBudget() != null ? profile.getMaxBudget().toString() : "0")
            .replace("{description}", profile.getDescription() != null ? profile.getDescription() : "no description");

        Map<String, Object> requestBody = Map.of(
                "model", openAiConfig.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful room compatibility scoring assistant. Always respond with valid JSON only."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.3,
                "max_tokens", 200
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiConfig.getApiKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                openAiConfig.getApiUrl(), entity, String.class);

        JsonNode root = objectMapper.readTree(response.getBody());
        String content = root.path("choices").get(0).path("message").path("content").asText();

        // Extract JSON from response (sometimes wrapped in markdown)
        content = content.replaceAll("```json", "").replaceAll("```", "").trim();

        JsonNode result = objectMapper.readTree(content);
        int score = Math.min(100, Math.max(0, result.path("score").asInt()));
        String explanation = result.path("explanation").asText();

        return Map.of("score", score, "explanation", explanation);
    }

    private Map<String, Object> ruleBasedScore(TenantProfile profile, Listing listing) {
        int score = 0;
        StringBuilder explanation = new StringBuilder();

        // Location match (50 points)
        boolean cityMatch = listing.getCity().equalsIgnoreCase(profile.getPreferredCity());
        if (cityMatch) {
            score += 40;
            explanation.append("City matches your preference. ");
            if (profile.getPreferredLocality() != null &&
                    listing.getLocality().toLowerCase().contains(profile.getPreferredLocality().toLowerCase())) {
                score += 10;
                explanation.append("Locality also matches. ");
            }
        } else {
            explanation.append("City does not match your preference. ");
        }

        // Budget match (50 points)
        BigDecimal rent = listing.getRent();
        if (rent.compareTo(profile.getMinBudget()) >= 0 && rent.compareTo(profile.getMaxBudget()) <= 0) {
            score += 50;
            explanation.append("Rent is within your budget range.");
        } else if (rent.compareTo(profile.getMaxBudget()) > 0) {
            BigDecimal overage = rent.subtract(profile.getMaxBudget());
            BigDecimal budgetRange = profile.getMaxBudget().subtract(profile.getMinBudget()).add(BigDecimal.ONE);
            int penalty = Math.min(50, overage.divide(budgetRange, 2, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(50)).intValue());
            score += Math.max(0, 50 - penalty);
            explanation.append("Rent is above your budget range.");
        } else {
            score += 30;
            explanation.append("Rent is below your minimum budget.");
        }

        return Map.of("score", Math.min(100, score), "explanation", explanation.toString().trim());
    }

    private String safe(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString();
        return text.replaceAll("[\n\r]+", " ");
    }
}
