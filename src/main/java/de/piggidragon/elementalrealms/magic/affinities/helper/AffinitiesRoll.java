package de.piggidragon.elementalrealms.magic.affinities.helper;

import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.magic.affinities.AffinityType;
import de.piggidragon.elementalrealms.magic.affinities.ModAffinities;
import de.piggidragon.elementalrealms.registries.attachments.ModAttachments;
import de.piggidragon.elementalrealms.registries.configs.AffinityConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Three-stage affinity assignment for new players (issue #23).
 *
 * <p>Flow:</p>
 * <ol>
 *   <li><b>Stage 1 (guaranteed):</b> 1\u00d7 ELEMENTAL at 100% (hardcoded; random pick from the 4
 *       elementals). This is the only affinity the player receives at full strength at login.</li>
 *   <li><b>Stage 2 (rare):</b> Roll the matching DEVIANT of the guaranteed Elemental.
 *       If the roll succeeds (default 5%), the player gets the Deviant at a partial
 *       completion, capped at {@code deviantMaxCompletionPercent()} (default 80%).
 *       Continuous left-skew distribution via {@code (int)(max * U^slope)}.</li>
 *   <li><b>Stage 3 (decaying loop):</b> Additional ELEMENTALs, plus DEVIANTs of the guaranteed
 *       Elemental (only one candidate exists \u2014 the Deviant matching the Stage-1 Elemental, if
 *       Stage 2 didn't already claim it). Each iteration: roll "continue?" (chance decays per
 *       iter), pick a candidate (Deviant weighted by {@code partialDeviantWeightPercent()}), then
 *       roll a partial completion capped at {@code elementalMaxCompletionPercent()} (default
 *       80%). Same continuous left-skew distribution as stage 2.</li>
 * </ol>
 *
 * <p>Stage 1's 100% is hardcoded by spec \u2014 the player's anchor element is always full strength.
 * The config-level partial caps ({@code deviantMaxCompletionPercent}, {@code
 * elementalMaxCompletionPercent}) only apply to stages 2 and 3, keeping partials bounded at
 * the configured ceiling (default 80%) regardless of any other config setting.</p>
 *
 * <p>Stages 2 and 3 read "what the player already holds" from this method's local
 * {@code result} map, NOT from {@link ModAffinities#getAffinities(ServerPlayer)}. Reason:
 * Stage 1 writes to the local map but does not commit to the player's attachment, so a later
 * stage that read from the attachment would not see the Stage-1 element as already-assigned
 * and could re-pick it as a Stage-3 candidate \u2014 silently overwriting the 100% with a partial
 * completion. This was a real bug (symptom: reroll could yield a single partial instead of a
 * 100% anchor + partials).</p>
 *
 * <p>ETERNAL affinities are never assigned at login: stage 1 only picks ELEMENTAL,
 * stage 2 only emits the matching DEVIANT of the chosen ELEMENTAL (which is, by definition,
 * DEVIANT-typed), and stage 3 only emits ELEMENTALs or DEVIANTs of the Stage-1 ELEMENTAL
 * (filtered by AffinityType.DEVIANT).</p>
 */
public final class AffinitiesRoll {

    private AffinitiesRoll() {
    }

    /**
     * Generates a map of affinities to add to a player at login.
     * Returns an empty map if no eligible elemental exists (caller's job to gate on first-login).
     */
    public static Map<Affinity, Integer> rollAffinities(ServerPlayer player) {
        RandomSource random = player.getRandom();
        Map<Affinity, Integer> result = new HashMap<>();

        // Stage 1: guaranteed ELEMENTAL @ 100%. Hardcoded anchor by spec.
        Affinity guaranteed = rollFreshElemental(player, random);
        if (guaranteed == Affinity.VOID) {
            return result;
        }
        result.put(guaranteed, 100);

        // Stage 2: rare DEVIANT of the guaranteed Elemental, partial completion.
        Affinity deviant = guaranteed.getDeviant();
        if (deviant != Affinity.VOID
                && !result.containsKey(deviant)
                && chance(random, AffinityConfig.deviantPartialChancePercent())) {
            int completion = rollCompletion(random, AffinityConfig.deviantMaxCompletionPercent());
            if (completion > 0) {
                result.put(deviant, completion);
            }
        }

        // Stage 3: decaying loop for additional affinities. Reads pool from local result,
        // NOT from the player attachment, so Stage 1's pick is seen as already-assigned.
        int continueChance = AffinityConfig.elementalContinueChanceStartPercent();
        for (int i = 0; i < AffinityConfig.elementalMaxIterations(); i++) {
            if (!chance(random, continueChance)) {
                break;
            }
            Affinity candidate = pickPartialCandidate(result, random);
            if (candidate == Affinity.VOID) {
                break; // pool exhausted
            }
            int completion = rollCompletion(random, AffinityConfig.elementalMaxCompletionPercent());
            if (completion > 0) {
                result.put(candidate, completion);
            }
            continueChance = Math.max(1, continueChance * AffinityConfig.elementalContinueChanceDecayPercent() / 100);
        }
        return result;
    }

    /**
     * Picks a random ELEMENTAL the player does not yet hold. Returns VOID if all 4 are taken.
     * Reads from the local {@code result} map for consistency with stages 2 and 3.
     */
    private static Affinity rollFreshElemental(ServerPlayer player, RandomSource random) {
        // Stage 1 reads the player attachment as source of truth for what's already held.
        // On a fresh login this is empty; on a reroll, clearAffinities() was called first by
        // the caller so this contains only {VOID: 0}. Either way, every ELEMENTAL is available.
        Map<Affinity, Integer> attachment = player.getData(ModAttachments.AFFINITIES.get());
        List<Affinity> available = Affinity.getAllElemental().stream()
                .filter(a -> !attachment.containsKey(a))
                .toList();
        if (available.isEmpty()) {
            return Affinity.VOID;
        }
        return available.get(random.nextInt(available.size()));
    }

    /**
     * Picks a candidate for stage 3 (partial roll). Reads "what's already assigned" from
     * the local {@code result} map so it sees Stage 1's pick and never re-selects it.
     * <p>Rules:</p>
     * <ul>
     *   <li>DEVIANT candidates: their matching Elemental must be held at 100% in
     *       {@code result} (i.e. the guaranteed one). The candidate itself must not
     *       already be in {@code result} (Stage 2 may have claimed it).</li>
     *   <li>ELEMENTAL candidates: any elemental not already in {@code result}.</li>
     *   <li>Weight {@code partialDeviantWeightPercent()}% chance to pick a Deviant over an
     *       Elemental. If the chosen pool is empty, fall back to the other pool before
     *       returning VOID.</li>
     * </ul>
     */
    private static Affinity pickPartialCandidate(Map<Affinity, Integer> result, RandomSource random) {
        // Elemental pool: any elemental not already in result.
        List<Affinity> availableElementals = Affinity.getAllElemental().stream()
                .filter(a -> !result.containsKey(a))
                .toList();

        // Deviant pool: deviants of held Elementals @ 100% in result. Stage 1 hardcodes the
        // guaranteed Elemental to 100%, so the only Deviant that ever appears here is the one
        // matching the guaranteed Elemental — unless Stage 2 already claimed it.
        List<Affinity> eligibleDeviants = Affinity.getAllElemental().stream()
                .filter(a -> result.getOrDefault(a, 0) >= 100)
                .map(Affinity::getDeviant)
                .filter(d -> d != Affinity.VOID)
                .filter(d -> !result.containsKey(d))
                .filter(d -> d.getType() == AffinityType.DEVIANT) // sanity: eternal never eligible
                .toList();

        if (availableElementals.isEmpty() && eligibleDeviants.isEmpty()) {
            return Affinity.VOID;
        }
        if (availableElementals.isEmpty()) {
            return eligibleDeviants.get(random.nextInt(eligibleDeviants.size()));
        }
        if (eligibleDeviants.isEmpty()) {
            return availableElementals.get(random.nextInt(availableElementals.size()));
        }
        boolean pickDeviant = chance(random, AffinityConfig.partialDeviantWeightPercent());
        if (pickDeviant) {
            return eligibleDeviants.get(random.nextInt(eligibleDeviants.size()));
        }
        return availableElementals.get(random.nextInt(availableElementals.size()));
    }

    /**
     * Rolls a partial completion % using an exponential left-skew distribution.
     * The formula is {@code completion = (int)(maxCompletion * U^slope)}, where
     * {@code U ~ Uniform[0,1)} and {@code slope >= 1} controls the skew.
     *
     * <p>The int-cast truncation is the implicit "rounding" \u2014 partials are integer percentages.
     * For {@code slope = 1} the distribution is uniform across the partial range.
     * For {@code slope > 1} (default 3) the distribution is left-skewed: low values are
     * common, high values are rare. Concretely with {@code slope = 3, maxCompletion = 80}:
     * ~79% of partials are &lt;= 40%, ~9% are &gt; 60%, and the @ max bucket itself is &lt; 1%.</p>
     */
    private static int rollCompletion(RandomSource random, int maxCompletion) {
        double u = random.nextDouble(); // [0, 1)
        double slope = AffinityConfig.partialCompletionSlope();
        double skewed = Math.pow(u, slope); // left-skew: u^slope for slope > 1
        int completion = (int) (skewed * maxCompletion);
        if (completion <= 0) {
            completion = 1; // minimum partial = 1%
        }
        return Math.min(completion, maxCompletion);
    }

    private static boolean chance(RandomSource random, int probabilityPercent) {
        return random.nextInt(100) < probabilityPercent;
    }
}