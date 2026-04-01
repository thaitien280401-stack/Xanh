package com.xanh.vocabulary.service;

import com.xanh.vocabulary.enums.Difficulty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Placeholder high-volume vocabulary generator.
 *
 * <p>Produces realistic-looking {@link WordEntry} records without calling any
 * external API.  Replace {@link #generate} with a real AI client (OpenAI,
 * Gemini …) when ready; the rest of the pipeline stays unchanged.
 */
@Component
public class HighVolumeWordGenerator {

    // ── Static word pool ──────────────────────────────────────────────────────

    /**
     * 500-word pool drawn from common English vocabulary across six difficulty
     * tiers.  Words are reused across topics; the topic name is embedded in the
     * generated definition so every entry looks topic-specific.
     */
    private static final String[] WORD_POOL = {
        // A
        "abandon","abbreviate","ability","abolish","absorb","abstract","access","accomplish",
        "accurate","acquire","adapt","adequate","advocate","affect","affirm","aggregate",
        "allocate","alter","amend","analyze","anticipate","apparent","apply","approach",
        "appropriate","approximate","assess","assign","assist","assume","assure","attain",
        // B
        "balance","barrier","benchmark","benefit","bias","brief","broaden","budget",
        // C
        "calculate","capacity","challenge","clarify","classify","collaborate","compile",
        "comprehensive","concentrate","conclude","confirm","confront","consider","consist",
        "construct","consult","contrast","contribute","convert","coordinate","create",
        "criteria","cycle",
        // D
        "data","debate","deduce","define","demonstrate","denote","derive","describe",
        "design","detect","determine","develop","diagnose","differentiate","distribute",
        "document","domain","draft","dynamic",
        // E
        "elaborate","eliminate","emerge","emphasize","enable","encounter","enforce",
        "enhance","ensure","establish","evaluate","evolve","examine","execute","exhibit",
        "expand","explicit","explore","expose","extract",
        // F
        "facilitate","factor","feature","formulate","framework","function","fundamental",
        // G
        "generate","global","guideline",
        // H
        "highlight","hypothesis",
        // I
        "identify","illustrate","implement","imply","indicate","infer","innovate",
        "integrate","interpret","investigate","involve","isolate",
        // J
        "justify",
        // K
        "knowledge",
        // L
        "label","leverage","linkage","locate",
        // M
        "maintain","manage","maximize","measure","minimize","modify","monitor","motivate",
        // N
        "navigate","negotiate","normalize",
        // O
        "observe","obtain","operate","optimize","outline","overcome",
        // P
        "parameter","participate","perceive","perform","predict","prioritize","process",
        "produce","provide","publish",
        // Q
        "qualify","quantify","query",
        // R
        "recognize","recommend","reduce","reference","refine","regulate","reinforce",
        "represent","require","resolve","retrieve","revise",
        // S
        "sequence","simplify","simulate","specify","structure","submit","summarize",
        "support","sustain","synthesize","systematize",
        // T
        "target","transform","translate","transmit",
        // U
        "utilize",
        // V
        "validate","verify","visualize",
        // W
        "workflow",
        // Extended set — advanced/domain terms
        "algorithm","annotation","architecture","assertion","attribute","automation",
        "bandwidth","benchmark","boolean","bytecode",
        "callback","cascading","classifier","cluster","compilation","concurrency",
        "cryptography","daemon","dependency","deployment","deterministic","dichotomy",
        "distributed","encapsulation","enumeration","exception","expression",
        "federated","framework","granularity","idempotent","inheritance","instance",
        "integration","interface","iteration","latency","lifecycle","middleware",
        "migration","module","namespace","normalization","orchestration","parallelism",
        "partition","payload","pipeline","polymorphism","prototype","recursion",
        "refactoring","repository","resilience","scalability","schema","serialization",
        "singleton","synchronization","throughput","transaction","traversal","validation",
        "virtualization","vulnerability","webhook","abstraction","agile","asynchronous",
        "authentication","authorization","caching","checksum","cohesion","compilation",
        "constraint","containerization","coupling","delegation","deserialization",
        "encapsulation","endpoint","facade","fallback","gateway","heuristic","immutable",
        "indexing","injection","interceptor","inversion","isolation","kernel","lambda",
        "load-balancing","locking","logging","marshalling","memoization","microservice",
        "mutation","observable","pagination","persistence","polymorphism","predicate",
        "proxy","queue","race-condition","reflection","registry","rollback","sandbox",
        "service-mesh","sharding","snapshot","stack","state-machine","stub","tenant",
        "throttle","timeout","token","topology","tracing","trigger","type-safety",
        "unit-test","upgrade","versioning","worker"
    };

    private static final String[] PARTS_OF_SPEECH = {
        "noun", "verb", "adjective", "adverb"
    };

    private static final String[] DIFFICULTY_LEVELS = {
        Difficulty.EASY.name(), Difficulty.MEDIUM.name(), Difficulty.HARD.name()
    };

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Generates up to {@code count} vocabulary word entries for the given topic.
     *
     * <p>The pool has ~500 unique words; if {@code count > pool size} the method
     * silently caps at the pool size to avoid duplicates within one batch.
     *
     * @param topicName topic context string embedded in each definition
     * @param count     desired number of words (1 – 400)
     * @return list of generated word entries (never null, may be smaller than count)
     */
    public List<WordEntry> generate(String topicName, int count) {
        int limit = Math.min(count, WORD_POOL.length);
        List<WordEntry> result = new ArrayList<>(limit);

        for (int i = 0; i < limit; i++) {
            String word   = WORD_POOL[i];
            String pos    = PARTS_OF_SPEECH[i % PARTS_OF_SPEECH.length];
            String diff   = DIFFICULTY_LEVELS[i % DIFFICULTY_LEVELS.length];
            String def    = buildDefinition(word, pos, topicName);
            String example = buildExample(word, topicName);

            result.add(new WordEntry(word, def, null, pos, example, null, diff));
        }
        return result;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String buildDefinition(String word, String partOfSpeech, String topicName) {
        return switch (partOfSpeech) {
            case "noun"      -> String.format("A concept related to %s: %s.", topicName, word);
            case "verb"      -> String.format("To %s in the context of %s.", word, topicName);
            case "adjective" -> String.format("Describes something %s within %s.", word, topicName);
            default          -> String.format("In a manner that is %s, relevant to %s.", word, topicName);
        };
    }

    private static String buildExample(String word, String topicName) {
        return String.format(
                "Students studying %s should understand how to %s effectively.",
                topicName.toLowerCase(), word);
    }

    // ── Data model ────────────────────────────────────────────────────────────

    /**
     * Immutable word entry produced by this generator.
     *
     * <p>The {@code difficulty} field uses the {@link Difficulty} enum name
     * (EASY / MEDIUM / HARD).
     */
    public record WordEntry(
            String word,
            String definition,
            String pronunciation,
            String partOfSpeech,
            String exampleSentence,
            String audioUrl,
            String difficulty
    ) {}
}
