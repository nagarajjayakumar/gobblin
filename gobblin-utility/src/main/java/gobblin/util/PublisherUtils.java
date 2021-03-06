package gobblin.util;

import java.util.Collection;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import gobblin.configuration.WorkUnitState;
import gobblin.configuration.WorkUnitState.WorkingState;
import gobblin.publisher.DataPublisher;
import gobblin.source.workunit.Extract;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Utility class for {@link DataPublisher}.
 */
public class PublisherUtils {

  /**
   * Creates a {@link Multimap} that maps {@link Extract} to their corresponds {@link WorkUnitState}s.
   *
   * @see {@link Multimap}
   */
  public static Multimap<Extract, WorkUnitState> createExtractToWorkUnitStateMap(
      Collection<? extends WorkUnitState> workUnitStates) {
    Multimap<Extract, WorkUnitState> extractToWorkUnitStateMap = ArrayListMultimap.create();

    for (WorkUnitState workUnitState : workUnitStates) {
      extractToWorkUnitStateMap.put(workUnitState.getExtract(), workUnitState);
    }
    return extractToWorkUnitStateMap;
  }

  /**
   * Given a {@link Multimap} of {@link Extract}s to {@link WorkUnitState}s, filter out any {@link Extract}s where all
   * of the corresponding {@link WorkUnitState}s do not meet the given {@link Predicate}.
   */
  public static Multimap<Extract, WorkUnitState> getExtractsForPredicate(
      Multimap<Extract, WorkUnitState> extractToWorkUnitStateMap, Predicate<WorkUnitState> predicate) {
    Multimap<Extract, WorkUnitState> successfulExtracts = ArrayListMultimap.create();
    for (Map.Entry<Extract, Collection<WorkUnitState>> entry : extractToWorkUnitStateMap.asMap().entrySet()) {
      if (Iterables.all(entry.getValue(), predicate)) {
        successfulExtracts.putAll(entry.getKey(), entry.getValue());
      }
    }
    return successfulExtracts;
  }

  /**
   * Given a {@link Multimap} of {@link Extract}s to {@link WorkUnitState}s, filter out any {@link Extract}s where all
   * of the corresponding {@link WorkUnitState}s do not meet the given {@link Predicate}.
   * <ul>
   *  <li> The filtered {@link Extract}s will be available in {@link SplitExtractsResult#getFiltered()}</li>
   *  <li> The {@link Extract}s satisfying the predicated will be available in {@link SplitExtractsResult#getRetained()}</li>
   * </ul>
   *
   */
  public static SplitExtractsResult splitExtractsByPredicate(
      Multimap<Extract, WorkUnitState> extractToWorkUnitStateMap, Predicate<WorkUnitState> predicate) {
    Multimap<Extract, WorkUnitState> retained = ArrayListMultimap.create();
    Multimap<Extract, WorkUnitState> filtered = ArrayListMultimap.create();
    for (Map.Entry<Extract, Collection<WorkUnitState>> entry : extractToWorkUnitStateMap.asMap().entrySet()) {
      if (Iterables.all(entry.getValue(), predicate)) {
        retained.putAll(entry.getKey(), entry.getValue());
      } else {
        filtered.putAll(entry.getKey(), entry.getValue());
      }
    }
    return new SplitExtractsResult(retained, filtered);
  }

  /**
   * Implementation of {@link Predicate} that checks if a given {@link WorkUnitState} has a {@link WorkingState} equal
   * to {@link WorkingState#SUCCESSFUL}.
   */
  public static class WorkUnitStateSuccess implements Predicate<WorkUnitState> {
    @Override
    public boolean apply(WorkUnitState workUnitState) {
      return workUnitState.getWorkingState().equals(WorkingState.SUCCESSFUL);
    }
  }

  @AllArgsConstructor
  @Getter
  public static class SplitExtractsResult {
    private Multimap<Extract, WorkUnitState> retained;
    private Multimap<Extract, WorkUnitState> filtered;
  }
}
