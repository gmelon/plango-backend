package dev.gmelon.plango.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RandomTokenGenerator {
    private static final List<Character> RANDOM_PASSWORD_CANDIDATES = new ArrayList<>();

    static {
        addRandomPasswordCandidates('a', 'z');
        addRandomPasswordCandidates('A', 'Z');
        addRandomPasswordCandidates('0', '9');
    }

    private static void addRandomPasswordCandidates(char startInclusive, char endInclusive) {
        for (char c = startInclusive; c <= endInclusive; c++) {
            RANDOM_PASSWORD_CANDIDATES.add(c);
        }
    }

    public String generate(int length) {
        Collections.shuffle(RANDOM_PASSWORD_CANDIDATES);
        return RANDOM_PASSWORD_CANDIDATES.subList(0, length).stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }
}
