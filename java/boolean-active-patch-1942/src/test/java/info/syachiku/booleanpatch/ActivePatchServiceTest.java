package info.syachiku.booleanpatch;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static info.syachiku.booleanpatch.ActivePatchService.InputError.*;
import static info.syachiku.booleanpatch.ActivePatchService.Outcome.*;
import static org.junit.jupiter.api.Assertions.*;

class ActivePatchServiceTest {
    record Case(String id, String json, ActivePatchService.Outcome outcome,
                Boolean activeAfterUpdate, ActivePatchService.InputError error) {}

    static List<Case> specifications() {
        return List.of(
                new Case("P01", "{}", UNCHANGED, null, null),
                new Case("P02", "{\"active\":false}", UPDATED, false, null),
                new Case("P03", "{\"active\":true}", UPDATED, true, null),
                new Case("P04", "{\"active\":null}", null, null, ACTIVE_NOT_BOOLEAN),
                new Case("P05", "{\"active\":\"false\"}", null, null, ACTIVE_NOT_BOOLEAN),
                new Case("P06", "{\"active\":0}", null, null, ACTIVE_NOT_BOOLEAN),
                new Case("P07", "{\"active\":[]}", null, null, ACTIVE_NOT_BOOLEAN),
                new Case("P08", "{\"active\":{}}", null, null, ACTIVE_NOT_BOOLEAN),
                new Case("P09", "[]", null, null, ROOT_NOT_OBJECT),
                new Case("P10", "null", null, null, ROOT_NOT_OBJECT),
                new Case("P11", "true", null, null, ROOT_NOT_OBJECT),
                new Case("P12", "1", null, null, ROOT_NOT_OBJECT),
                new Case("P13", "\"false\"", null, null, ROOT_NOT_OBJECT),
                new Case("P14", "{\"active\":", null, null, INVALID_JSON),
                new Case("P15", "", null, null, INVALID_JSON),
                new Case("P16", "{} {}", null, null, INVALID_JSON),
                new Case("P17", "{\"active\":false,\"active\":true}", null, null, INVALID_JSON),
                new Case("P18", null, null, null, INVALID_JSON),
                new Case("P19", " \n\t", null, null, INVALID_JSON));
    }

    static Stream<Arguments> inputsAndBothInitialStates() {
        return specifications().stream().flatMap(spec -> Stream.of(false, true)
                .map(initial -> Arguments.of(spec.id() + (initial ? "-T" : "-F"), spec, initial)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("inputsAndBothInitialStates")
    void inputStateAndSaveCallsMatchTheContract(String id, Case spec, boolean initial) {
        var saved = new ArrayList<Boolean>();
        var service = new ActivePatchService(saved::add);
        var user = new ActivePatchService.User(initial);

        if (spec.error() == null) {
            assertEquals(spec.outcome(), service.apply(spec.json(), user), id);
        } else {
            var error = assertThrows(ActivePatchService.PatchInputException.class,
                    () -> service.apply(spec.json(), user), id);
            assertEquals(spec.error(), error.error(), id);
        }

        boolean expectedActive = spec.outcome() == UPDATED ? spec.activeAfterUpdate() : initial;
        List<Boolean> expectedSaves = spec.outcome() == UPDATED ? List.of(expectedActive) : List.of();
        assertAll(id,
                () -> assertEquals(expectedActive, user.active(), "処理後の状態"),
                () -> assertEquals(expectedSaves, saved, "保存回数と保存された値"));
        System.out.printf("%s initial=%s result=%s active=%s saves=%d saved=%s%n",
                id, initial, spec.error() == null ? spec.outcome() : spec.error(),
                user.active(), saved.size(), saved);
    }
}
