# GT4500 `fireTorpedo` System Test Report

## Test-case table

| ID | Requirement / behavior from header specification | Initial state | Steps | Expected result |
|---|---|---|---|---|
| T1 | First `SINGLE` attempt uses the primary store | Primary: 1, fail rate 0; secondary: 1, fail rate 0 | `TORPEDO,SINGLE` | `SUCCESS` |
| T2 | `SINGLE` alternates stores to allow cooling | Both stores: 2, fail rates 0 | Run `TORPEDO,SINGLE` twice | Both commands return `SUCCESS` |
| T3 | If the next store is empty, try the other store | Primary: 0; secondary: 1, fail rate 0 | `TORPEDO,SINGLE` | `SUCCESS` |
| T4 | If secondary is empty after primary was fired last, use primary again | Primary: 2; secondary: 0; fail rates 0 | Run `TORPEDO,SINGLE` twice | Both commands return `SUCCESS` |
| T5 | If the selected store reports failure, do not try the other store in the same `SINGLE` call | Primary: 1, fail rate 1; secondary: 1, fail rate 0 | Run `TORPEDO,SINGLE` twice | First `FAIL`, second `SUCCESS` (secondary remains available for the second call) |
| T6 | `ALL` tries both stores | Both stores: 1, fail rates 0 | Run `TORPEDO,ALL`, then `TORPEDO,SINGLE` | `SUCCESS`, then `FAIL` (both stores were consumed by `ALL`) |
| T7 | `ALL` succeeds when only one store has ammunition | Primary: 1, secondary: 0; fail rates 0 | `TORPEDO,ALL` | `SUCCESS` |
| T8 | Return failure when both stores are empty | Both stores: 0 | Run `TORPEDO,SINGLE`, then `TORPEDO,ALL` | Both commands return `FAIL` |
| T9 | `ALL` returns success if at least one store fires successfully | Primary: 1, fail rate 0; secondary: 1, fail rate 1 | `TORPEDO,ALL` | `SUCCESS` |

## Why these test cases were selected

The cases are derived from the `fireTorpedo` header comment, not from its implementation. Together they cover the documented first-store choice, alternating behavior, fallback when the next store is empty, the no-fallback-on-failure rule, `ALL` mode, and the return-value rule that at least one successful firing is enough. Empty-store cases check the documented unsuccessful outcome when nothing can be fired. Failure rates of 0 and 1 are used to make the simulated store outcomes deterministic.

## Execution and observations

The test inputs and expected outputs are in `test-data/input-1.txt` through `input-9.txt` and matching `output-*.txt` files. The existing `input-0.txt` / `output-0.txt` example is preserved.

Run from the repository root:

```bash
git switch -c gt4500-testing
./mvnw verify
```

If your operating system does not allow executing the wrapper, try `bash mvnw verify` (on Windows use `mvnw.cmd verify`). Capture a screenshot of the terminal showing the completed `verify` run and test summary, and insert it into the submitted report.

**Environment note:** I could not verify the test run in the provided execution environment because the Maven wrapper could not download Maven from Maven Central. This is an environment/network limitation, not a test assertion result. Run the command in your own configured environment before claiming the tests passed.

## Were changes needed after execution? Could everything be observed and controlled?

The test data was designed to use deterministic firing rates (0 always succeeds; 1 always fails) and expected CLI output. The suite may need adjustments if the actual CLI output differs; compare each failure with the corresponding input/output pair and correct the expected output only when the specification supports that correction.

The CLI exposes only `SUCCESS` or `FAIL` for each torpedo command. It does not reveal which store fired or the remaining ammunition counts. Some internal details therefore cannot be observed directly. The sequence-based checks infer store behavior from later outcomes, but do not directly inspect store state. We can control ammunition counts and failure rates through the `GT4500` initialization command, which makes the important outcomes repeatable. We cannot directly control the random generator or observe the individual store calls through this system-test interface. The specification also does not define behavior for invalid firing modes or invalid initialization parameters, so those are outside this requirement-focused suite.

---

# Part 3 — Pull Request, JaCoCo Coverage, and Unit Tests

## A. Commit, push, and open the pull request

From the repository root, run the following commands after reviewing the changes:

```bash
git switch gt4500-testing
git status
git add test-data src/test/java/hu/bme/mit/spaceship/TorpedoStoreTest.java REPORT.md
git commit -m "Add GT4500 system tests and TorpedoStore unit tests"
git push -u origin gt4500-testing
```

Then open the repository on GitHub. GitHub normally offers a **Compare & pull request** button for the newly pushed branch. Select it, use `main` (or the repository's designated base branch) as the base, and create the pull request.

Wait for the checks to finish. Record the actual status shown for each workflow; do not write that checks passed unless the PR page shows success. Take a screenshot of the PR's checks. If the coverage workflow asks for authorization, open the repository's **Actions** tab and follow the authorization prompt if you have permission.

## B. JaCoCo coverage procedure

Run:

```bash
./mvnw verify
```

Open `target/site/jacoco/index.html` in a browser, select `GT4500`, then select `fireTorpedo`. Green source lines indicate executed instructions, red lines indicate missed instructions, and yellow decision markers indicate partially covered branches.

The lab's gate requires at least **80% instruction coverage and 80% branch coverage for `GT4500`**. The additional system test files `input-10.txt` / `output-10.txt`, `input-11.txt` / `output-11.txt`, and `input-12.txt` / `output-12.txt` exercise the `ALL` path where primary firing fails but secondary succeeds, `ALL` with an empty primary store, and a failed secondary `SINGLE` firing without fallback to the primary store.

**Actual coverage result:** Not yet verified in this environment. The Maven wrapper could not download Maven from Maven Central, so no trustworthy local JaCoCo report was generated here. Run `./mvnw verify` locally and enter the real percentages from the JaCoCo report and PR workflow comment. Capture the colour-coded `fireTorpedo` source and the PR coverage comment for the report.

## C. Unit tests for `TorpedoStore`

The prepared `TorpedoStoreTest.java` tests the public contract of the class using JUnit 5 and its observable methods (`getTorpedoCount()` and `isEmpty()`). No production code or test framework code was changed.

| Unit test | Purpose / expected result |
|---|---|
| `newStoreHasRequestedCountAndIsNotEmpty` | Constructor stores the requested positive count; store is not empty. |
| `emptyStoreReportsEmpty` | A store initialized with zero torpedoes is empty. |
| `fireOneSuccessfullyDecreasesCountByOne` | With failure rate 0, firing one succeeds, decrements count, and leaves the store empty. |
| `firingMultipleTorpedoesDecreasesCountByRequestedAmount` | With failure rate 0, firing three from four leaves one torpedo. |
| `simulatedFiringFailureDoesNotConsumeTorpedoes` | With failure rate 1, firing fails and the count remains unchanged. |
| `firingZeroTorpedoesThrowsIllegalArgumentException` | A request to fire zero torpedoes is rejected. |
| `firingNegativeNumberOfTorpedoesThrowsIllegalArgumentException` | A negative firing quantity is rejected. |
| `firingMoreTorpedoesThanAvailableThrowsIllegalArgumentException` | A request larger than the available count is rejected. |
| `firingFromEmptyStoreThrowsIllegalArgumentException` | Firing from an empty store is rejected and the count remains zero. |

### Why these unit tests were selected

These cases cover normal operation, state changes after success, state preservation after a simulated failure, empty/non-empty observation, and invalid firing quantities. Failure rates of `0.0` and `1.0` make the success and failure outcomes deterministic because `Random.nextDouble()` returns values in the interval from 0 inclusive to 1 exclusive. The tests assert both return values/exceptions and the resulting state, rather than checking only a boolean result.

### Unit test source code

```java
package hu.bme.mit.spaceship;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TorpedoStoreTest {

    @Test
    void newStoreHasRequestedCountAndIsNotEmpty() {
        TorpedoStore store = new TorpedoStore(3);
        assertEquals(3, store.getTorpedoCount());
        assertFalse(store.isEmpty());
    }

    @Test
    void emptyStoreReportsEmpty() {
        TorpedoStore store = new TorpedoStore(0);
        assertTrue(store.isEmpty());
        assertEquals(0, store.getTorpedoCount());
    }

    @Test
    void fireOneSuccessfullyDecreasesCountByOne() {
        TorpedoStore store = new TorpedoStore(1, 0.0);
        assertTrue(store.fire(1));
        assertEquals(0, store.getTorpedoCount());
        assertTrue(store.isEmpty());
    }

    @Test
    void firingMultipleTorpedoesDecreasesCountByRequestedAmount() {
        TorpedoStore store = new TorpedoStore(4, 0.0);
        assertTrue(store.fire(3));
        assertEquals(1, store.getTorpedoCount());
        assertFalse(store.isEmpty());
    }

    @Test
    void simulatedFiringFailureDoesNotConsumeTorpedoes() {
        TorpedoStore store = new TorpedoStore(2, 1.0);
        assertFalse(store.fire(1));
        assertEquals(2, store.getTorpedoCount());
        assertFalse(store.isEmpty());
    }

    @Test
    void firingZeroTorpedoesThrowsIllegalArgumentException() {
        TorpedoStore store = new TorpedoStore(2, 0.0);
        assertThrows(IllegalArgumentException.class, () -> store.fire(0));
        assertEquals(2, store.getTorpedoCount());
    }

    @Test
    void firingNegativeNumberOfTorpedoesThrowsIllegalArgumentException() {
        TorpedoStore store = new TorpedoStore(2, 0.0);
        assertThrows(IllegalArgumentException.class, () -> store.fire(-1));
        assertEquals(2, store.getTorpedoCount());
    }

    @Test
    void firingMoreTorpedoesThanAvailableThrowsIllegalArgumentException() {
        TorpedoStore store = new TorpedoStore(2, 0.0);
        assertThrows(IllegalArgumentException.class, () -> store.fire(3));
        assertEquals(2, store.getTorpedoCount());
    }

    @Test
    void firingFromEmptyStoreThrowsIllegalArgumentException() {
        TorpedoStore store = new TorpedoStore(0, 0.0);
        assertThrows(IllegalArgumentException.class, () -> store.fire(1));
        assertEquals(0, store.getTorpedoCount());
        assertTrue(store.isEmpty());
    }
}
```

## D. Maximum coverage: discussion and evidence

The goal is to add tests for all reachable instructions and decision outcomes without modifying `GT4500`, `TorpedoStore`, or `SystemTests`. The added cases target previously less explicit combinations of store availability and firing success. Whether 100% instruction and branch coverage is achievable must be decided from the actual JaCoCo report and workflow result; do not claim 100% without that evidence. Any remaining uncovered code should be explained using the source highlighting and the constraints of the public command interface.

**PR workflow comment / exit criteria:** Paste the actual `github-actions[bot]` comment here after the workflow runs. If it includes the exit-criteria string, copy that exact string into the required Moodle field.

**Screenshots to attach:**

1. PR page showing completed checks.
2. JaCoCo HTML page with `GT4500.fireTorpedo` colour-coded.
3. PR comment from `github-actions[bot]` summarizing coverage / exit criteria.
4. Local Maven test output showing the unit-test result.
