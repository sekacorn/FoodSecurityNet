package com.foodsecuritynet.llm.service;

import com.foodsecuritynet.llm.model.LlmResponse;
import com.foodsecuritynet.llm.model.QueryContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MbtiTailoringService {

    private final LlmIntegrationService llmIntegrationService;

    private static final Map<String, String> MBTI_COMMUNICATION_STYLES = new HashMap<>();

    static {
        // Analysts
        MBTI_COMMUNICATION_STYLES.put("INTJ", "Provide strategic, data-driven insights with long-term implications. Use technical language and focus on efficiency.");
        MBTI_COMMUNICATION_STYLES.put("INTP", "Emphasize logical analysis and theoretical frameworks. Present multiple perspectives and encourage exploration.");
        MBTI_COMMUNICATION_STYLES.put("ENTJ", "Deliver decisive, goal-oriented recommendations. Focus on actionable steps and measurable outcomes.");
        MBTI_COMMUNICATION_STYLES.put("ENTP", "Present innovative solutions and alternative approaches. Encourage brainstorming and creative problem-solving.");

        // Diplomats
        MBTI_COMMUNICATION_STYLES.put("INFJ", "Provide holistic insights with emphasis on long-term impact. Connect data to human and environmental well-being.");
        MBTI_COMMUNICATION_STYLES.put("INFP", "Frame information in terms of values and authenticity. Highlight ethical considerations and sustainability.");
        MBTI_COMMUNICATION_STYLES.put("ENFJ", "Deliver inspiring, people-focused guidance. Emphasize collaboration and community impact.");
        MBTI_COMMUNICATION_STYLES.put("ENFP", "Present enthusiastic, possibility-oriented insights. Encourage exploration and creative applications.");

        // Sentinels
        MBTI_COMMUNICATION_STYLES.put("ISTJ", "Provide detailed, fact-based information. Use structured format with clear procedures and proven methods.");
        MBTI_COMMUNICATION_STYLES.put("ISFJ", "Deliver practical, supportive guidance. Focus on reliability and tested approaches.");
        MBTI_COMMUNICATION_STYLES.put("ESTJ", "Present organized, efficient solutions. Emphasize practical implementation and clear standards.");
        MBTI_COMMUNICATION_STYLES.put("ESFJ", "Offer helpful, community-oriented advice. Focus on cooperation and established best practices.");

        // Explorers
        MBTI_COMMUNICATION_STYLES.put("ISTP", "Provide hands-on, technical solutions. Focus on practical tools and immediate applications.");
        MBTI_COMMUNICATION_STYLES.put("ISFP", "Deliver personalized, experience-based insights. Emphasize aesthetic and practical harmony.");
        MBTI_COMMUNICATION_STYLES.put("ESTP", "Present dynamic, action-oriented recommendations. Focus on immediate results and adaptability.");
        MBTI_COMMUNICATION_STYLES.put("ESFP", "Offer engaging, practical advice. Make information accessible and enjoyable.");
    }

    public Mono<LlmResponse> processWithMbtiTailoring(QueryContext queryContext) {
        log.info("Processing query with MBTI tailoring: type={}", queryContext.getMbtiType());

        String mbtiType = queryContext.getMbtiType().toUpperCase();
        String communicationStyle = MBTI_COMMUNICATION_STYLES.getOrDefault(mbtiType,
                "Provide clear, balanced information suitable for general audience.");

        // Enhance the query with MBTI-specific instructions
        String enhancedQuery = String.format(
                "%s\n\nCommunication preference: %s",
                queryContext.getQuery(),
                communicationStyle
        );

        QueryContext enhancedContext = QueryContext.builder()
                .query(enhancedQuery)
                .userId(queryContext.getUserId())
                .mbtiType(mbtiType)
                .context(queryContext.getContext())
                .build();

        return llmIntegrationService.processQuery(enhancedContext);
    }

    public String getCommunicationStyle(String mbtiType) {
        return MBTI_COMMUNICATION_STYLES.getOrDefault(mbtiType.toUpperCase(),
                "Provide clear, balanced information suitable for general audience.");
    }

    public Map<String, String> getAllCommunicationStyles() {
        return new HashMap<>(MBTI_COMMUNICATION_STYLES);
    }
}
