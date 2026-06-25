package edu.ruc.platform.party.service;

import edu.ruc.platform.party.domain.LatestPartyStudentProgress;
import edu.ruc.platform.party.dto.PartyProgressResponse;
import edu.ruc.platform.party.dto.ReminderResponse;
import edu.ruc.platform.party.repository.LatestPartyFlowNodeRepository;
import edu.ruc.platform.party.repository.LatestPartyReminderTaskRepository;
import edu.ruc.platform.party.repository.LatestPartyStudentProgressRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KingbasePartyProgressServiceUnitTest {

    @Test
    void getProgressSelectsOneProgressWhenStudentHasMultipleActiveRecords() {
        LatestPartyStudentProgressRepository progressRepository = mock(LatestPartyStudentProgressRepository.class);
        LatestPartyFlowNodeRepository nodeRepository = mock(LatestPartyFlowNodeRepository.class);
        LatestPartyReminderTaskRepository reminderTaskRepository = mock(LatestPartyReminderTaskRepository.class);
        KingbasePartyProgressService service = new KingbasePartyProgressService(
                progressRepository,
                nodeRepository,
                reminderTaskRepository
        );

        LatestPartyStudentProgress completed = progress(1L, "completed", LocalDate.now().minusDays(20), LocalDate.now().plusDays(1));
        LatestPartyStudentProgress inProgress = progress(2L, "in_progress", LocalDate.now().minusDays(10), LocalDate.now().plusDays(7));
        when(progressRepository.findAllByStudentUserIdAndIsDeleted(10001L, 0)).thenReturn(List.of(completed, inProgress));

        PartyProgressResponse response = service.getProgress(10001L);

        assertEquals("积极分子", response.currentStage());
        assertEquals(inProgress.getNextDeadlineAt().toLocalDate(), response.nextDeadline());
        verify(progressRepository, never()).findByStudentUserIdAndIsDeleted(10001L, 0);
    }

    @Test
    void listRemindersReturnsGeneratedRemindersForAllStudentProgresses() {
        LatestPartyStudentProgressRepository progressRepository = mock(LatestPartyStudentProgressRepository.class);
        LatestPartyFlowNodeRepository nodeRepository = mock(LatestPartyFlowNodeRepository.class);
        LatestPartyReminderTaskRepository reminderTaskRepository = mock(LatestPartyReminderTaskRepository.class);
        KingbasePartyProgressService service = new KingbasePartyProgressService(
                progressRepository,
                nodeRepository,
                reminderTaskRepository
        );

        LatestPartyStudentProgress first = progress(1L, "in_progress", LocalDate.now().minusDays(10), LocalDate.now().plusDays(5));
        LatestPartyStudentProgress second = progress(2L, "paused", LocalDate.now().minusDays(20), LocalDate.now().plusDays(2));
        when(progressRepository.findAllByStudentUserIdAndIsDeleted(10001L, 0)).thenReturn(List.of(first, second));
        when(reminderTaskRepository.findByProgressIdOrderByDueAtAsc(1L)).thenReturn(List.of());
        when(reminderTaskRepository.findByProgressIdOrderByDueAtAsc(2L)).thenReturn(List.of());

        List<ReminderResponse> reminders = service.listReminders(10001L);

        assertEquals(2, reminders.size());
        assertEquals(second.getNextDeadlineAt().toLocalDate(), reminders.get(0).remindDate());
        assertEquals(first.getNextDeadlineAt().toLocalDate(), reminders.get(1).remindDate());
        verify(progressRepository, never()).findByStudentUserIdAndIsDeleted(10001L, 0);
    }

    private LatestPartyStudentProgress progress(Long id, String status, LocalDate startedAt, LocalDate nextDeadline) {
        LatestPartyStudentProgress progress = new LatestPartyStudentProgress();
        progress.setId(id);
        progress.setStudentUserId(10001L);
        progress.setFlowId(id);
        progress.setStatus(status);
        progress.setStartedAt(startedAt.atStartOfDay());
        progress.setUpdatedNodeAt(startedAt.plusDays(1).atStartOfDay());
        progress.setNextDeadlineAt(nextDeadline.atStartOfDay());
        progress.setCreatedAt(LocalDateTime.now().minusDays(30));
        progress.setUpdatedAt(LocalDateTime.now().minusDays(1));
        progress.setIsDeleted(0);
        return progress;
    }
}
